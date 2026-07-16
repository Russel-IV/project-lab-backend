# 9. Authentication strategy

Date: 2026-07-02

## Status

Accepted

## Context

Today, `JwtAuthFilter` (custom code) reads the `Authorization` header, validates
the JWT via `JwtService` (jjwt), and populates `SecurityContextHolder` — all within
one process. Once split, every service potentially needs to know who the caller
is, but a network round-trip to Identity on every request would reintroduce a hard
dependency on Identity's uptime for every single request in the system.

## Decision

- **Identity service** keeps issuing tokens exactly as today (`AuthService`,
  `JwtService`, jjwt) — this code moves, unchanged in behavior, into the Identity
  service.
- **Every other service, including the Gateway**, validates JWTs **locally**,
  using Spring Security's OAuth2 Resource Server support
  (`spring-boot-starter-oauth2-resource-server`, `NimbusJwtDecoder` configured with
  the shared HMAC secret via `spring.security.oauth2.resourceserver.jwt.secret-key`)
  instead of the current hand-written `JwtAuthFilter`.

This is *not* an adoption of OAuth2 authorization flows, scopes, or an external
identity provider — "Resource Server" here is only Spring Security's name for the
stock filter that verifies a bearer JWT's signature/expiry and populates a
`JwtAuthenticationToken`. The app keeps issuing its own tokens via its own login
endpoint exactly as now.

The Gateway validates the JWT once at the edge (rejecting invalid/expired tokens
before they reach any backend service) and forwards the original `Authorization`
header downstream unchanged, since the JWT is self-verifying — no gateway-specific
token translation step is introduced.

## Consequences

- No per-request network call to Identity for authentication — the biggest
  practical win of already having stateless JWTs.
- Replaces custom filter code (`JwtAuthFilter`) with stock, configuration-only
  Spring Security setup in every service — less code to maintain, well-tested
  library behavior instead of hand-rolled parsing.
- The shared signing secret must be distributed to every service that validates
  tokens — via the existing `JWT_SECRET` env var convention
  ([ADR-0007](0007-configuration-management.md)), not a new mechanism.
- `requireAuthenticated()` (`util/AuthenticatedPrincipal.kt`) keeps its current shape —
  reading `SecurityContextHolder` — regardless of which filter populated it.
- Downstream services *can* independently re-validate the JWT (defense in depth)
  at zero extra network cost, since validation is local.
