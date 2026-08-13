# 17. Memory governance for the local dev stack

Date: 2026-07-16

## Status

Accepted

## Context

Bringing up the full local stack (`scripts/lift-stack.sh`) took 7–10 minutes, and
during investigation the dev box's WSL2 VM OOM-killed `systemd` itself, cascading
into killing all 14 containers — while otherwise idle, after the stack had already
passed its health checks.

Root cause: `docker-compose.yml` set **no `mem_limit` and no
`-XX:MaxRAMPercentage`** on any of its 7 JVM containers, unlike
`docker-compose.prod.yml` ([ADR-0014](0014-deployment-topology-oracle-cloud.md)).
Without a cgroup limit, each JVM sizes its heap against the *full host memory*,
not a per-container budget — `dmesg` showed several JVMs at hundreds of MB to
~1GB each, growing independently with no ceiling stopping their combined
footprint from exceeding 7.4GB + 2GB swap. [ADR-0016](0016-jvm-gc-and-jit-tuning.md)'s
GC/JIT tuning makes each JVM lighter but doesn't cap it. This likely also
explains part of the slow startup — swap usage was climbing toward its ceiling
repeatedly during testing.

## Decision

Add the same `mem_limit` + `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=70.0`
governance to `docker-compose.yml` that prod already has, sized for this box's
smaller 7.4GB:

```
6x Postgres containers        @ 200m = 1200m
1x Zipkin                     @ 300m =  300m
1x Gateway JVM                @ 900m (heaviest: GraphQL + JPA + Feign + Resilience4j)
6x domain-service/Eureka JVMs @ 600m = 3600m
-----------------------------------------------
total                                = 6000m (~5.86GB of 7.4GB)
```

~1.5GB headroom for WSL2/Docker/host OS — tighter than prod's ~2.25GB,
deliberately, since nothing else should compete for memory during local dev.

## Consequences

- A JVM that outgrows its budget gets OOM-killed inside its own cgroup — one
  container restarts, instead of an unbounded race that can take down the whole
  VM.
- 600m/900m per JVM is tighter than prod's 1024m/1536m; raise a specific
  service's limit deliberately if it genuinely needs more during debugging,
  rather than unbounding the whole file again.
- Doesn't fix build-time slowness on its own (sequential builds are untouched),
  but removes a confound that made the box's real capacity hard to reason about.
- Unblocked shrinking `lift-stack.sh`'s startup stagger: a live memory sample
  through a full bring-up at `LIFT_STAGGER_SECONDS=5` (down from 15) showed a
  smooth, bounded climb (never below ~3.2GB free) and cut startup from ~3m47s to
  ~2m16s — what justified 5s as the new default.
