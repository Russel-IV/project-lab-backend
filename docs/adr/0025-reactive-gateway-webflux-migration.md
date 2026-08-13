# 25. Reactive gateway: WebFlux + non-blocking outbound calls

Date: 2026-07-21

## Status

Accepted

## Context

ADR-0004 chose Spring Cloud Gateway Server **MVC** specifically to avoid a second
(reactive) programming model. That no longer fits what the Gateway has become: it
owns no data of its own (`V18`–`V22` dropped every table it used to own; its
JPA/Postgres/Flyway deps are dead weight) and is the sole GraphQL host
([ADR-0005](0005-graphql-composition-strategy.md)), fanning out to up to 21 Feign
clients across 6 services per request — `StayBatchResolver` alone calls 9 of them
synchronously, one after another, each blocking its resolver thread. The Gateway
is also the single highest-concurrency component in the system, since every
external request passes through it.

That's the textbook profile for a non-blocking web layer: thin, I/O-bound, high
fan-out, no data of its own — unlike every domain service (JPA/Postgres-backed
CRUD, or a hard blocking dependency with no reactive driver, e.g.
chatbot-service's Oracle Vector Store, Inventory's PostGIS columns). None of those
are reactive-migration candidates; this decision doesn't touch them.

Feign has no non-blocking client ([ADR-0008](0008-inter-service-communication.md)),
so a reactive Gateway still funneling outbound calls through blocking Feign would
gain nothing — and risk a footgun if the underlying blocking pool isn't sized
deliberately. A real benefit (thread efficiency, and running `StayBatchResolver`'s
9 calls concurrently instead of sequentially) requires the Gateway's outbound
calls to go non-blocking too.

## Decision

Migrate the Gateway module to Spring WebFlux, scoped to the Gateway only — every
downstream service stays Spring MVC/JPA/Feign as-is. The external contract
(paths, shapes, ADR-0004's guarantee) is unchanged; this is an internal boundary
conversion.

Use **Kotlin coroutines** (`suspend fun`, `kotlinx-coroutines-reactor`), not raw
`Mono`/`Flux` — this stack (GraphQL, WebFlux, Security, LoadBalancer's
`WebClient`) has first-class coroutine support, and it keeps resolver/service code
shaped like today (direct return values, linear control flow).

The 21 Feign clients become a `@LoadBalanced` coroutine `WebClient`, resolved via
Eureka the same way — a scoped exception to ADR-0008, which otherwise stands
(Inventory↔Booking and every other inter-service call remain Feign). Resilience4j
is *not* added to these WebClient calls here — the Gateway has declared the
dependency since ADR-0008 but never applied it (zero `@CircuitBreaker` usage), so
this migration preserves that pre-existing gap rather than silently fixing it
under an unrelated ADR.

## Consequences

- The Gateway becomes the one reactive component in an otherwise blocking
  system — the "second programming model" ADR-0004 avoided, accepted here as a
  scoped trade-off for the Gateway's distinct profile, not a reversal of that
  concern.
- `requireAuthenticated()` and all 17 calling resolvers become `suspend fun`,
  reading `ReactiveSecurityContextHolder` instead of the ThreadLocal version —
  also updates `CLAUDE.md`'s testing convention for the Gateway module only.
- `FeignConfig.kt`'s OkHttp/LoadBalancer wrapper (needed for Feign PATCH support)
  retires — `WebClient` supports PATCH natively.
- Batch resolvers gain real concurrency: once each `@BatchMapping` method is
  `suspend fun`, graphql-java's execution strategy schedules sibling fields
  concurrently instead of sequentially — a real tail-latency win on
  `StayBatchResolver`'s 10 methods, with no manual `coroutineScope`/`awaitAll`
  needed.
- Every other service is unaffected; a future service with a similar profile
  would need its own separate decision.
- `spring-cloud-starter-gateway-server-webmvc` (ADR-0004) turns out to be dead
  weight — a full grep found zero actual usage; every route ADR-0004 describes as
  declaratively proxied is really a hand-written `@RestController`. Removed
  outright rather than swapped for the WebFlux equivalent; there's no routing
  layer to migrate, just controllers converting to `suspend fun` like the
  resolvers.
- **Two "one more artifact" tracing gaps**, found via live `docker compose` +
  Zipkin verification: a bare `WebClient.builder()` silently produced zero
  cross-service trace propagation. Root cause was two independent missing
  pieces: (1) `spring-boot-starter-webflux` only autoconfigures the server side —
  it doesn't pull in `spring-boot-http-client`'s
  `ReactiveHttpClientAutoConfiguration`, needed for the Micrometer-instrumented
  `ClientHttpConnector`; added explicitly and wired into the `@LoadBalanced`
  builder. (2) Even with a real connector, `WebClient.Builder` needs
  `.observationRegistry(...)` explicitly set to create CLIENT-kind spans and
  attach trace headers — the connector and the registry are independent
  settings. Confirmed fixed live: a single Zipkin trace now spans gateway →
  inventory-service → media-service → identity-service.
