# 25. Reactive gateway: WebFlux + non-blocking outbound calls

Date: 2026-07-21

## Status

Accepted

## Context

ADR-0004 chose Spring Cloud Gateway Server **MVC** specifically "to avoid
introducing a second (reactive) programming model anywhere in the system." That
reasoning held while the Gateway was a straightforward proxy. It no longer
reflects what the Gateway has become: it owns zero data of its own (no
`@Entity`/`@Repository` remain — `V18`–`V22` dropped every table it used to own
during the microservices split; its `spring-boot-starter-data-jpa`/`postgresql`/
`hibernate-spatial`/Flyway dependencies are dead weight), and it is now the sole
GraphQL host (ADR-0005), fanning out to up to 21 Feign clients across 6
downstream services per request. `StayBatchResolver` alone calls 9 of them in a
single GraphQL query batch — synchronously, one after another, each blocking its
resolver thread in turn. The Gateway is also, by construction, the single
highest-concurrency component in the system: every external request passes
through it.

This is the textbook profile for a non-blocking web layer: a thin, I/O-bound
aggregator with high fan-out and high connection count, holding no business data
of its own. It is a different profile from every domain service (Identity,
Inventory, Booking, Review, Media, chatbot-service), each of which is
JPA/Postgres-backed CRUD or has a hard blocking dependency with no reactive
driver (chatbot-service's Oracle Vector Store; Inventory's PostGIS geography
columns via `hibernate-spatial`) — none of those are reactive-migration
candidates, and this decision does not touch them.

Feign (ADR-0008) has no non-blocking client. A reactive Gateway that still
funnels every outbound call through blocking Feign would just be a non-blocking
accept loop wrapping blocking calls — no real benefit, and a real footgun if the
blocking pool under it isn't sized deliberately. Getting an actual benefit (both
thread efficiency under load, and, concretely, running `StayBatchResolver`'s 9
independent downstream calls concurrently instead of sequentially) requires the
Gateway's outbound calls to become non-blocking too.

## Decision

Migrate the Gateway module to Spring WebFlux
(`spring-boot-starter-webflux`, `spring-cloud-starter-gateway-server-webflux`),
scoped to the Gateway only — every downstream service stays exactly as it is
today (Spring MVC, JPA, Feign). The external contract (paths, request/response
shapes, ADR-0004's endpoint-preservation guarantee) is unchanged; this is an
internal boundary conversion, not a system-wide reactive rewrite, and ADR-0004's
routing/endpoint decisions otherwise stand.

Use **Kotlin coroutines** (`suspend fun`, `kotlinx-coroutines-reactor`) as the
idiom, not raw `Mono`/`Flux` chains — Spring for GraphQL, Spring WebFlux, Spring
Security, and Spring Cloud LoadBalancer's `WebClient` all have first-class
coroutine support in this stack, and it keeps resolver/service code shaped like
it is today (direct return values, linear control flow) rather than forcing a
reactive-chaining rewrite throughout.

The Gateway's 21 Feign clients are replaced with a `@LoadBalanced` coroutine
`WebClient`, resolved via Eureka the same way Feign clients are today. This is a
scoped exception to ADR-0008, which otherwise stands: Inventory↔Booking Feign
calls (ADR-0010) and every other inter-service call in the system remain Feign,
unchanged. Resilience4j (`resilience4j-kotlin`'s suspend-function decorators)
is applied to these WebClient calls — the Gateway declares the Resilience4j
dependency today but has never actually used it; this migration is also the
first time it does.

## Consequences

- The Gateway becomes the one reactive component in an otherwise fully blocking
  system. This is the "second programming model" ADR-0004 explicitly avoided —
  accepted here as a deliberate, scoped trade-off given the Gateway's distinct
  I/O-bound-fan-out profile, not a reversal of the concern in general.
- `requireAuthenticated()` (`util/AuthenticatedPrincipal.kt`) and every resolver
  that calls it (17 files) become `suspend fun`, reading
  `ReactiveSecurityContextHolder` instead of the ThreadLocal
  `SecurityContextHolder`. This also breaks the testing convention documented in
  `CLAUDE.md` (direct `SecurityContextHolder` manipulation with no Spring
  context) — that convention is updated alongside this migration, for the
  Gateway module only.
- `FeignConfig.kt`'s OkHttp/LoadBalancer wrapper (needed to work around Feign's
  dropped OkHttp auto-config and get PATCH support) retires — `WebClient`
  supports PATCH natively.
- Batch resolvers gain real concurrency, not just thread efficiency:
  independent downstream calls in the same batch (e.g. `StayBatchResolver`'s 9
  clients) can run via `coroutineScope { awaitAll(...) }` instead of
  sequentially, reducing tail latency on the Gateway's most expensive query
  shape.
- Every other service in the system is unaffected. If a future service
  develops a similar I/O-bound-fan-out profile, this ADR does not automatically
  apply to it — that would be its own decision, evaluated on its own blocking
  dependencies (as this evaluation found for chatbot-service and
  inventory-service, both rejected as WebFlux candidates for reasons specific to
  each).
- See ADR-0008's status note for the scoped Feign→WebClient exception this
  introduces.
