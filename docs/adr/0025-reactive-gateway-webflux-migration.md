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
unchanged. Resilience4j is *not* added to these WebClient calls as part of this
migration — the Gateway has declared the Resilience4j dependency since ADR-0008
but has never actually applied it (zero `@CircuitBreaker` usage anywhere in the
module, confirmed by grep), so this migration preserves that pre-existing gap
rather than silently fixing it under an unrelated ADR. Wiring `resilience4j-
kotlin`'s suspend-function decorators onto the new WebClient calls is a
legitimate, separate follow-up, not a consequence of going reactive.

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
- Batch resolvers gain real concurrency, not just thread efficiency.
  `StayBatchResolver`'s 10 `@BatchMapping` methods each make one downstream
  call; under the servlet stack, graphql-java's execution strategy runs
  sibling fields' plain synchronous `DataFetcher`s to completion one after
  another. Once each method is `suspend fun`, Spring for GraphQL bridges it to
  a `Mono`, letting the same execution strategy schedule all 10 sibling
  fields concurrently instead of sequentially — a real tail-latency reduction
  on the Gateway's most expensive query shape, achieved without any manual
  `coroutineScope`/`awaitAll` composition inside the resolver methods
  themselves.
- Every other service in the system is unaffected. If a future service
  develops a similar I/O-bound-fan-out profile, this ADR does not automatically
  apply to it — that would be its own decision, evaluated on its own blocking
  dependencies (as this evaluation found for chatbot-service and
  inventory-service, both rejected as WebFlux candidates for reasons specific to
  each).
- See ADR-0008's status note for the scoped Feign→WebClient exception this
  introduces.
- `spring-cloud-starter-gateway-server-webmvc` (ADR-0004) turned out to be
  dead weight, not something this migration needed to port: a full source
  grep found zero `org.springframework.cloud.gateway.*` usage anywhere in the
  Gateway module. Every route ADR-0004 describes as declaratively proxied
  (`/api/v1/auth/**`, the picture-upload `RewritePath`) is actually a
  hand-written `@RestController` calling a Feign/WebClient client directly
  (e.g. `identity/controllers/AuthController.kt`,
  `inventory/controllers/StayPictureController.kt`) — ADR-0004's
  implementation description was stale versus the real code. This migration
  removes the unused dependency outright rather than swapping it for
  `spring-cloud-starter-gateway-server-webflux`; there is no separate routing
  layer to migrate, only ordinary controllers, which convert to `suspend fun`
  the same way the GraphQL resolvers do.
- **Two more "one more artifact" gaps found via live `docker compose` +
  Zipkin verification (2026-07-21), same pattern as the four-instance list in
  [[project_microservices_migration]] and Phase 7's tracing findings there**:
  a bare `WebClient.builder()` (what `WebClientConfig.kt` originally used)
  silently produces zero cross-service trace propagation — gateway's own
  spans reached Zipkin, but no span from any outbound call ever continued
  into inventory-service/media-service/identity-service, with no error
  anywhere. Root cause was two independent missing pieces, confirmed by
  fixing one at a time and re-checking Zipkin after each: (1)
  `spring-boot-starter-webflux` only autoconfigures the *server* side
  (`WebFluxAutoConfiguration`) — it does not transitively pull in
  `spring-boot-http-client`'s `ReactiveHttpClientAutoConfiguration`, which is
  what provides the `ClientHttpConnector` bean carrying Reactor Netty's
  Micrometer instrumentation; added the dependency explicitly and wired
  `.clientConnector(...)` into the `@LoadBalanced` builder. (2) Even with a
  real connector, `WebClient.Builder` still never creates a CLIENT-kind
  Observation span per outbound call — and therefore never adds trace-context
  headers to the outgoing request — without `.observationRegistry(...)`
  explicitly set; the connector and the observation registry are two
  independent `WebClient.Builder` settings, not one producing the other.
  Confirmed fixed via a live GraphQL query hitting `StayBatchResolver`: a
  single Zipkin trace now correctly spans gateway → inventory-service →
  media-service → identity-service. This is also why the plan's "verify
  Feign's `feign-micrometer` equivalent isn't a silent no-op" caution
  (Phase 7) turned out to be exactly right, twice over.
