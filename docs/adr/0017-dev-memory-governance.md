# 17. Memory governance for the local dev stack

Date: 2026-07-16

## Status

Accepted

## Context

While investigating why bringing up the full local stack (via
`scripts/lift-stack.sh`) was taking 7-10 minutes, the dev box's WSL2 VM ran out
of memory badly enough to OOM-kill `systemd` itself (`dmesg`: `Out of memory:
Killed process 1433 (systemd)`), which cascaded into killing all 14 containers.
This happened during otherwise idle operation — the stack had already finished
starting and passed all its health checks; no build was running at the time.

Root cause: `docker-compose.yml` (local dev) set **no `mem_limit` and no
`-XX:MaxRAMPercentage`** on any of its 7 JVM containers — unlike
`docker-compose.prod.yml`, which has carried a full per-container memory budget
since [ADR-0014](0014-deployment-topology-oracle-cloud.md). Without a cgroup
memory limit, each JVM's default heap sizing is computed against the *full host
memory* it can see, not a per-container budget. `dmesg` confirmed multiple
`java` processes present at the time of the crash with resident memory well
into the hundreds of MB to ~1GB range each — normal, unconstrained JVM
ergonomics — with 7 of them growing independently and no ceiling stopping their
combined footprint from exceeding this box's 7.4GB + 2GB swap. [ADR-0016](0016-jvm-gc-and-jit-tuning.md)'s
GC/JIT tuning was necessary but not sufficient on its own — it makes each JVM
lighter, but doesn't cap it.

This also plausibly explains part of the slow build/startup times being
investigated in the first place: memory pressure and swapping (`free -h`
showed swap usage climbing toward its 2GB ceiling repeatedly during testing)
slow down everything running on the box, not just the specific process that
eventually gets killed.

## Decision

Add the same `mem_limit` + `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=70.0`
governance to `docker-compose.yml` that `docker-compose.prod.yml` already has,
sized for this box's smaller 7.4GB (vs. prod's 12GB):

```
6x Postgres containers  @ 200m = 1200m
1x Zipkin                @ 300m =  300m
1x Gateway JVM            @ 900m (heaviest: GraphQL + JPA + Feign + Resilience4j)
6x domain-service/Eureka JVMs @ 600m each = 3600m
------------------------------------------------
total                            = 6000m (~5.86GB of 7.4GB)
```

~1.5GB is left as headroom for the WSL2 VM, Docker daemon, and host OS —
tighter than ADR-0014's ~2.25GB prod headroom, deliberately, since this box has
less total memory to begin with and nothing else should be competing for it
during local dev.

## Consequences

- A JVM that outgrows its budget now gets OOM-killed *inside its own cgroup* —
  that one container restarts or needs investigation, rather than the failure
  mode being an unbounded race to exhaust the whole VM's memory and taking
  every container down with it (and, as observed, potentially the VM's own
  init process).
- 600m/900m per JVM is noticeably tighter than prod's 1024m/1536m budget for
  the same services. If a service genuinely needs more headroom during local
  development (e.g. debugging a memory-heavy query), raise that specific
  service's `mem_limit` deliberately rather than leaving the whole file
  unbounded again.
- Doesn't fix build-time slowness on its own — that's a separate, still-open
  question (sequential builds and staggered startup are deliberate trade-offs
  documented in `scripts/lift-stack.sh`, not touched here) — but removes a
  confound that was making the box's actual capacity hard to reason about
  while investigating it.
