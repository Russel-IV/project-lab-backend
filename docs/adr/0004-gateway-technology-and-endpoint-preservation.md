# 4. Gateway technology and endpoint preservation

Date: 2026-07-02

## Status

Accepted

## Context

The frontend depends on a fixed set of paths that must not change or become
obsolete:

- `POST /graphql` (and `/graphiql` in dev)
- `POST /api/v1/auth/login`, `POST /api/v1/auth/signup`
- `POST /api/v1/stays/{stayId}/pictures`
- `GET /actuator/health`, `/actuator/info`, `/actuator/metrics`

The existing app is built on `spring-boot-starter-webmvc` (servlet stack), not
WebFlux.

## Decision

Use **Spring Cloud Gateway Server MVC**
(`spring-cloud-starter-gateway-server-webmvc`) rather than the classic reactive
Spring Cloud Gateway, since it matches the existing servlet-based stack and avoids
introducing a second (reactive) programming model anywhere in the system.

The Gateway exposes the **exact same external paths** as today. Internally:

| External path | Routing |
|---|---|
| `/graphql`, `/graphiql` | Handled directly by the Gateway — it hosts the actual GraphQL server itself (see [ADR-0005](0005-graphql-composition-strategy.md)), not proxied elsewhere |
| `/api/v1/auth/**` | Proxied to Identity service, path unchanged |
| `/api/v1/stays/{stayId}/pictures/**` | Rewritten to Media service's generic path `/api/v1/media/STAY/{stayId}/**` via a `RewritePath` filter |
| `/actuator/**` | The Gateway's own actuator only. Backend services keep internal actuators (used by Docker/Eureka health checks) that are not exposed externally |

Authentication is validated once at the Gateway edge (see
[ADR-0009](0009-authentication-strategy.md)); downstream services trust that an
authenticated request has already been checked, though they can independently
re-validate the JWT locally at no extra network cost since JWT is self-verifying.

## Consequences

- Zero frontend changes — the same base URL, the same paths, the same request/
  response shapes.
- One `RewritePath` filter to maintain for the Media route; everything else is a
  straight proxy.
- The Gateway is a bigger, less "thin" component than a typical edge proxy since it
  hosts the actual GraphQL resolution layer — accepted trade-off in exchange for
  contract stability (see ADR-0005 for the alternative considered and rejected).
- Internal service actuator endpoints are not part of the public contract; if
  external health monitoring per-service is needed later, that is a new decision,
  not a reopening of this one.
