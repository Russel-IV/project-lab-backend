# 9. Authentication strategy

Date: 2026-07-02

## Status

Accepted

## Context

Today, `JwtAuthFilter` reads the `Authorization` header, validates the JWT
(`JwtService`, jjwt), and populates `SecurityContextHolder` — all in one process.
Once split, every service potentially needs to know who's calling, but a network
round-trip to Identity on every request would make every request depend on
Identity's uptime.

## Decision

- **Identity** keeps issuing tokens exactly as today — this code moves, unchanged,
  into the Identity service.
- **Every other service, including the Gateway**, validates JWTs **locally** via
  Spring Security's OAuth2 Resource Server (`NimbusJwtDecoder`, shared HMAC secret)
  instead of the hand-written `JwtAuthFilter`. This isn't OAuth2 flows/scopes/an
  external IdP — "Resource Server" is just Spring Security's name for the stock
  bearer-JWT-verifying filter; the app still issues its own tokens via its own
  login endpoint.

The Gateway validates once at the edge (rejecting bad tokens before they reach any
backend) and forwards the `Authorization` header downstream unchanged — no
gateway-specific token translation.

## Consequences

- No per-request network call to Identity for authentication.
- Replaces hand-rolled filter code with stock, configuration-only Spring Security
  in every service.
- The shared signing secret must reach every validating service — via the existing
  `JWT_SECRET` convention ([ADR-0007](0007-configuration-management.md)), not a new
  mechanism.
- `requireAuthenticated()` keeps its current shape regardless of which filter
  populated the context.
- Downstream services can independently re-validate the JWT at zero extra cost.
