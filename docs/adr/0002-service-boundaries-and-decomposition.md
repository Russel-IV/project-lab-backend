# 2. Service boundaries and decomposition

Date: 2026-07-02

## Status

Accepted

## Context

Entity coupling in the monolith:

| Domain | Entities | Coupling |
|---|---|---|
| Identity | `User`, `Host`, `Language`, JWT/Auth | `Host.id` *is* `User.id` (shared PK) |
| Inventory | `Stay`, `Room`, `Address`, `PropertyBrand`, lookup tables | `Stay.host` is a live `@ManyToOne` |
| Booking | `Booking` | Live `@ManyToOne User`, `@ManyToMany Room` |
| Review | `Review` | Flat `userId`/`stayId`, no JPA relation — already decoupled |
| Media | `StayPicture` → generic `Media` ([ADR-0003](0003-generic-media-service.md)) | Flat ids, already isolated behind `StorageService` |

## Decision

Five deployable services, plus the Gateway (not a domain service):

- **Identity** — `User`, `Host`, `Language`, `AuthService`, `JwtService`,
  `AuthController`. Host stays here (shared PK with User, profile data), referenced
  from Inventory by `hostId: Int` via Feign ([ADR-0008](0008-inter-service-communication.md)).
- **Inventory** — `Stay`, `Room`, `Address`, `PropertyBrand`, six lookup tables. Kept
  together — heavily self-joined internally (`StayBatchResolver`).
- **Booking** — `Booking`, referencing `User`/`Room` by ID only, validated against
  the caller's JWT rather than a DB join.
- **Review** — unchanged, already decoupled.
- **Media** — see [ADR-0003](0003-generic-media-service.md).

## Consequences

- Any Stay operation needing host details requires an Identity↔Inventory call, not
  a DB join.
- Booking is the last/hardest extraction — the one cross-domain query
  ([ADR-0010](0010-booking-inventory-consistency.md)).
- Review and Media are extracted first as the cheapest walking skeleton for
  Gateway/Feign/Eureka wiring.
