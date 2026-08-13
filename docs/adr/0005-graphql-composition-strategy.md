# 5. GraphQL composition strategy

Date: 2026-07-02

## Status

Accepted

## Context

One schema, one endpoint today. Splitting into services threatens that unless
recomposed. Considered:

1. **Apollo Federation** — each service implements the subgraph spec, composed by a
   federation router. Spring for GraphQL doesn't support this; would require
   Netflix DGS in every domain service.
2. **Gateway-hosted single schema** — Gateway stays the one GraphQL app; resolvers
   call domain services via Feign instead of local JPA. Domain services need no
   GraphQL library, just REST.

## Decision

Gateway-hosted single schema — no federation, no second GraphQL framework anywhere.

Domain services expose plain REST, consumed by the Gateway via OpenFeign
([ADR-0008](0008-inter-service-communication.md)). The existing `resolvers/` →
`services/` → `repositories/` layering stays; only the `services/` layer's data
source changes from `Repository` to `FeignClient`. `@BatchMapping` resolvers keep
their `Map<Parent, Child>` shape — each domain service just needs a bulk "by ids"
endpoint (mirroring existing methods like `findByStayIdIn`).

## Consequences

- `/graphql` is byte-for-byte identical in contract — no frontend changes
  ([ADR-0004](0004-gateway-technology-and-endpoint-preservation.md)).
- Avoids adopting Netflix DGS as a second framework.
- The Gateway becomes the biggest, most complex service by design (real GraphQL
  resolution, not just proxying).
- Revisit in favor of federation only if independent teams need to own separate
  schemas without a shared Gateway codebase — not a concern at this scale.
