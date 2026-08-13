# 16. JVM garbage collector and JIT tuning for small containerized heaps

Date: 2026-07-16

## Status

Accepted

## Context

All 7 service JVMs ran with no GC/compilation tuning — only
`-XX:MaxRAMPercentage=70.0` ([ADR-0014](0014-deployment-topology-oracle-cloud.md))
in prod, nothing in local dev. Both defaults are mismatched to this deployment:

- **G1GC** (default since Java 9) is built for multi-GB heaps — its region
  partitioning and remembered-set bookkeeping is pure overhead below ~1GB (every
  service here caps at 700MB–1GB), and its background GC threads compete for CPU
  that's already scarce (2 OCPUs shared across 7 JVMs in prod).
- **Full tiered compilation** (C1→C2) spends CPU profiling/JIT-compiling for
  sustained high throughput this lab-scale app doesn't need — that cost lands
  directly on startup time, which matters to `lift-stack.sh`'s health-gated
  staggered startup and to prod redeploy time.

Considered and **rejected**: a custom AppCDS archive. Spring Boot's training-run
approach (`-XX:ArchiveClassesAtExit`) needs a live Postgres connection during
context startup (Flyway, JPA), which isn't available at `docker build` time —
standing up a throwaway datasource just for this isn't worth it, especially since
these containers rarely restart once deployed.

## Decision

Add to every service's `JAVA_TOOL_OPTIONS`:

```
-XX:+UseSerialGC -XX:TieredStopAtLevel=1
```

- `-XX:+UseSerialGC` — single-threaded stop-the-world collector, no
  region/remembered-set overhead; the standard recommendation under ~1GB.
- `-XX:TieredStopAtLevel=1` — stop at C1, skip C2. Faster warm-up at the cost of
  peak throughput this app's traffic doesn't need.

AppCDS stays out of scope; the JDK's default CDS archive (already active) is kept.

## Consequences

- Faster, less CPU-hungry startup across all 7 JVMs — most visible in
  `lift-stack.sh`'s build/startup flow and prod redeploy time.
- Trades away peak sustained throughput for that win. Revisit both flags if load
  ever grows enough that steady-state throughput matters more than boot time.
- If AppCDS becomes worth it later, the blocker to solve is the training run's
  live-datasource dependency, not the CDS mechanism itself.
