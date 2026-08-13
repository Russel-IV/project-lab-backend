# 13. Observability and tracing

Date: 2026-07-02

## Status

Accepted

## Context

`RequestLoggingFilter` reads/generates an `X-Correlation-Id` per request and logs
it via MDC. That works within one process, but a request now fans out across the
Gateway and domain services via Feign — a manual correlation ID has no way to
propagate across that boundary without hand-written code at every call site.

## Decision

Adopt **Micrometer Tracing** (`micrometer-tracing-bridge-otel`) across all
services, exported to a self-hosted **Zipkin** container (single Compose service,
free, RAM headroom available per [ADR-0014](0014-deployment-topology-oracle-cloud.md)).
It auto-instruments Spring MVC, OpenFeign, and JDBC, and auto-propagates
trace/span IDs across Feign calls with no per-call-site code. The console log
pattern switches from `%X{correlationId}` to `%X{traceId}` — Micrometer Tracing
populates MDC automatically, so `RequestLoggingFilter`'s manual ID logic is
deleted.

## Consequences

- Trace IDs now propagate *across* services, a real improvement over the old
  correlation ID.
- Removes hand-written correlation-ID code; the per-request log line stays, keyed
  on `traceId`.
- One more container (Zipkin) — optional in the sense that tracing itself still
  works without it; Zipkin is the visualization layer on top.
