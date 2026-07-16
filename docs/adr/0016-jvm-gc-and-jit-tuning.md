# 16. JVM garbage collector and JIT tuning for small containerized heaps

Date: 2026-07-16

## Status

Accepted

## Context

Every service JVM (7 total: Gateway, Eureka, and 5 domain services) ran with no
GC or compilation tuning at all — `docker-compose.prod.yml` only set
`-XX:MaxRAMPercentage=70.0` (ADR-0014), and `docker-compose.yml` (local dev) set
no `JAVA_TOOL_OPTIONS` whatsoever. Both defaults follow from that:

- **G1GC**, the default collector since Java 9, is designed around multi-GB
  heaps — it partitions the heap into regions and maintains remembered sets to
  make partial collections cheap at that scale. Below roughly 1GB (every service
  here caps out around 700MB–1GB per ADR-0014's `mem_limit` budget), that
  bookkeeping is pure overhead: there isn't enough heap for region partitioning
  to pay for itself, and G1's background/parallel GC threads compete for CPU
  that's already scarce — the Ampere A1 prod box is 2 OCPUs shared across all 7
  JVMs, and the local dev box has the same 7 JVMs sharing whatever cores that
  machine has.
- **Full tiered compilation** (C1 then C2), also default, spends CPU profiling
  and JIT-compiling hot methods up to C2's aggressively optimized tier. That
  investment pays off for long-running, high-throughput services — it does not
  pay off here: this is a lab-scale app, and the C2 compilation work itself
  competes with application threads for CPU during every service's startup
  window, which matters directly to `scripts/lift-stack.sh`'s health-check-gated
  staggered startup sequence and to how long a prod redeploy takes to go green.

A related idea considered and **rejected**: a custom AppCDS (Application Class
Data Sharing) archive, which can further cut startup time by pre-linking
application-specific classes (not just JDK platform classes, which already get
a default CDS archive for free via `-Xshare:auto`). Spring Boot's documented
approach requires a training run — start the app with
`-Dspring.context.exit=onRefresh -XX:ArchiveClassesAtExit=app-cds.jsa`, let the
Spring context fully refresh, then exit and capture the archive. Baking that
training run into the Dockerfile build doesn't work for these services: Flyway
migration and JPA `EntityManagerFactory` initialization both require a live
Postgres connection during context startup, and no database is reachable at
`docker build` time. Making it work would mean standing up a throwaway
in-memory or containerized datasource just for the training run — real added
build-pipeline complexity for a benefit that's mostly redundant with the
already-default JDK-classes CDS archive, and one that only pays off if these
containers restart often (they don't, once deployed — see ADR-0014's `restart:
unless-stopped`).

## Decision

Add to every service's `JAVA_TOOL_OPTIONS` (`docker-compose.prod.yml`) or
introduce fresh (`docker-compose.yml`, which had none):

```
-XX:+UseSerialGC -XX:TieredStopAtLevel=1
```

- `-XX:+UseSerialGC` — single-threaded stop-the-world collector, no region
  partitioning or remembered-set overhead. The standard recommendation for
  containerized heaps under ~1GB.
- `-XX:TieredStopAtLevel=1` — stop at C1, skip C2 entirely. Faster warm-up,
  less CPU spent compiling, at the cost of peak steady-state throughput that
  this app's traffic scale doesn't need.

Custom AppCDS is left out of scope per the Context section above; the JDK's own
default CDS archive (already active, no configuration needed) is kept as-is.

## Consequences

- Faster, less CPU-hungry startup for all 7 JVMs — most visible in
  `scripts/lift-stack.sh`'s sequential build + staggered-startup flow locally,
  and in how quickly a prod redeploy's health check goes green.
- Trades away peak sustained request throughput (no C2-optimized hot paths,
  more time in stop-the-world pauses under sustained load) for that startup
  win. Acceptable at this project's traffic scale; revisit both flags together
  if load ever grows enough that steady-state throughput starts to matter more
  than boot time.
- If a future need (e.g. much more frequent container restarts, or a real
  latency-sensitive production workload) justifies AppCDS's added complexity,
  the blocker to solve first is the training-run's live-datasource dependency,
  not the CDS mechanism itself.
