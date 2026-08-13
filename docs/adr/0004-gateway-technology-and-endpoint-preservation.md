# 4. Gateway technology and endpoint preservation

Date: 2026-07-02

## Status

Accepted

## Context

The frontend depends on fixed paths that can't change: `POST /graphql`
(+`/graphiql` in dev), `POST /api/v1/auth/{login,signup}`,
`POST /api/v1/stays/{stayId}/pictures`, `GET /actuator/{health,info,metrics}`. The
existing app is servlet-stack (`spring-boot-starter-webmvc`), not WebFlux.

## Decision

Use **Spring Cloud Gateway Server MVC** (not the reactive variant) — matches the
existing servlet stack, avoids a second programming model.

Same external paths, routed internally:

| External path | Routing |
|---|---|
| `/graphql`, `/graphiql` | Handled directly — hosts the GraphQL server itself ([ADR-0005](0005-graphql-composition-strategy.md)) |
| `/api/v1/auth/**` | Proxied to Identity, path unchanged |
| `/api/v1/stays/{stayId}/pictures/**` | Rewritten to `/api/v1/media/STAY/{stayId}/**` |
| `/actuator/**` | Gateway's own only; backend services keep internal-only actuators |

Auth is validated once at the Gateway edge ([ADR-0009](0009-authentication-strategy.md));
downstream services trust that, though they can re-validate locally at no extra cost
since JWTs self-verify.

## Consequences

- Zero frontend changes.
- One `RewritePath` filter to maintain (Media); everything else is a straight proxy.
- The Gateway is less "thin" than a typical edge proxy since it hosts GraphQL
  resolution — accepted for contract stability (see ADR-0005 for the alternative).
- Per-service actuator endpoints aren't public; exposing them later is a new
  decision.
