# 13. Observability and tracing

Date: 2026-07-02

## Status

Accepted

## Context

`RequestLoggingFilter` currently reads or generates an `X-Correlation-Id` per
request, puts it in MDC, and logs `METHOD /path -> status (Xms) [correlationId]`.
This works within one process, but a single frontend request will now fan out
across the Gateway and one or more domain services (via Feign) — a manual
correlation ID has no mechanism to propagate across that boundary without extra
hand-written code at every Feign call site.

## Decision

Adopt **Micrometer Tracing** (`micrometer-tracing-bridge-otel`) across all
services, with traces exported to a self-hosted **Zipkin** container (single
Docker Compose service, free, gives a visual trace view — a meaningful win for a
course context demonstrating distributed tracing, and the Oracle Cloud host has
RAM headroom for it per [ADR-0014](0014-deployment-topology-oracle-cloud.md)).

Micrometer Tracing auto-instruments Spring MVC, OpenFeign, and JDBC, and
auto-propagates trace/span IDs across Feign calls with no per-call-site code.
The existing console log pattern
(`logging.pattern.console=... [%X{correlationId}] ...`) is updated to use
`%X{traceId}` — Micrometer Tracing populates MDC automatically, so `RequestLoggingFilter`'s
manual correlation-ID generation/propagation logic is deleted once this lands.

## Consequences

- Trace IDs now propagate *across* services, not just within one process — a
  genuine improvement over the current correlation ID, not just a like-for-like
  replacement.
- Removes hand-written correlation ID code (`RequestLoggingFilter`'s ID
  generation/attachment logic); the per-request log line at the end of the filter
  chain can stay, just keyed on `traceId` instead of a manually generated UUID.
- One more container (Zipkin) — optional in the sense that Micrometer Tracing
  works with logging-only export if that container turns out not to be worth
  running; the tracing library adoption is the important part, Zipkin is a nice-to-have
  visualization on top.
