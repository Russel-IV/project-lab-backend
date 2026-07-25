# 26. Dual-ID pilot: opaque UUIDv7 `publicId` alongside internal `Int` PK

## Status

Accepted

## Context

Every entity's id is a sequential `Int` `SERIAL` PK, and that same int is what's
exposed everywhere external: every GraphQL type/argument uses `id: Int!` (no
`ID` scalar is wired up anywhere in this schema today), every REST path
variable is `Int`, and the JWT `sub` claim is the raw user id. A client can
enumerate stays, users, bookings, etc. by incrementing an integer, and a
decoded JWT discloses a user's raw internal id (and thus roughly their signup
order).

## Decision

Add a second, opaque, time-sortable UUIDv7 column (`publicId`) as a pilot on
two entities — `User` (identity-service) and `Stay` (inventory-service) —
before considering a rollout to the rest. The internal `Int` PK is untouched
and keeps doing everything it already did (joins, FKs, Feign calls between
services, ownership checks) — `publicId` only exists at the client-facing
boundary.

- **Schema**: additive `publicId: ID!` field next to the existing `id: Int!`
  on `User`/`Stay` (matches this project's endpoint-preservation practice —
  ADR-0004, ADR-0018). New `userByPublicId(publicId: ID!)` /
  `stayByPublicId(publicId: ID!)` queries are the actual edge-resolution
  proof-of-concept: a client supplies only the UUID, the gateway resolves it
  to the internal `Int` via a new internal REST lookup
  (`GET /internal/users/by-public-id/{publicId}`, same shape for stays), and
  everything downstream of that point is unchanged `Int`-based plumbing.
- **Backfill**: existing rows get `gen_random_uuid()` (v4) — insertion-order
  locality (the only reason to prefer v7) doesn't apply retroactively to rows
  that already exist. New rows get an app-generated UUIDv7 (`Uuid7`, an
  object already used by media-service for storage-object keys, duplicated
  into identity-service and inventory-service rather than pulled into a
  shared module — this codebase has no shared-code module across services,
  and a two-entity pilot doesn't justify introducing one).
- **JWT**: `sub` switches to the user's `publicId`, so a decoded token no
  longer discloses the raw internal id. A separate `uid` claim carries the
  internal `Int` id, so the gateway's per-request auth conversion
  (`AuthenticatedPrincipal`) doesn't need a network round trip to resolve one
  from the other — `uid` is only ever readable by the token's own holder, not
  a new leak to anyone else.

## Consequences

- Enumeration is **not** actually closed by this change — `id: Int!` and
  `user(id)`/`stay(id)` remain fully functional. That only happens once a
  client migrates to the `publicId`-based paths and the `Int` paths are
  deprecated, which is future work, not part of this pilot.
- All previously issued JWTs are invalidated on deploy (old tokens carry an
  int `sub` with no `uid` claim). Acceptable for a dev-stage rollout.
- Every write path that reconstructs a `User`/`Stay` row (profile updates,
  stay updates) must now explicitly carry the existing `publicId` forward —
  otherwise an update would silently mint a new one and break that user's/
  stay's already-issued links and JWTs. Handled at each such call site as
  part of this change.
- Rollout to the remaining ~13 entities across the other services, and
  eventually deprecating the raw `Int` id in client-facing paths, is future
  work — this ADR covers the pilot only.
