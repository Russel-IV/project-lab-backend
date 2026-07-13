# 5. GraphQL composition strategy

Date: 2026-07-02

## Status

Accepted

## Context

Today there is one GraphQL schema and one endpoint across all domains. Splitting
into services threatens this unless the schema is composed back together
somewhere. Two options were considered:

1. **Apollo Federation** — each service implements the Apollo Federation subgraph
   spec (`@key`, `_entities`), composed by a federation-aware router. Spring for
   GraphQL does not support this natively; it would require adopting Netflix DGS
   (a different framework) in every domain service.
2. **Gateway-hosted single schema** — the Gateway remains the one Spring for
   GraphQL application. Resolvers, batch resolvers, and services keep their current
   shape, but services call out to the domain services via OpenFeign instead of
   local JPA repositories. Domain services need no GraphQL library at all — plain
   REST is enough.

## Decision

Gateway-hosted single schema. No federation, no second GraphQL framework anywhere
in the system.

Domain services expose plain internal REST APIs, consumed by the Gateway's
services layer via OpenFeign
([ADR-0008](0008-inter-service-communication.md)). The existing
`resolvers/` → `services/` → `repositories/` layering is preserved in the Gateway;
only the `services/` layer's data source changes, from a `Repository` to a
`FeignClient`. `@BatchMapping` resolvers keep their `Map<Parent, Child>` return
shape — the N+1-safe pattern is unchanged, it just needs each domain service to
expose a bulk "by ids" endpoint (mirroring existing methods like
`findByStayIdIn`).

## Consequences

- `/graphql` is byte-for-byte identical in contract and introspection to what
  exists today — no frontend changes, satisfying the endpoint-preservation goal in
  [ADR-0004](0004-gateway-technology-and-endpoint-preservation.md).
- Avoids introducing Netflix DGS as a second framework across five services.
- The Gateway is now doing real work (GraphQL schema resolution), not just
  proxying — it is the biggest, most complex service in the system by design.
- If two independent teams later need to own their own GraphQL schema without
  coordinating through a shared Gateway codebase, this decision should be revisited
  in favor of federation — not a concern at this project's scale.
