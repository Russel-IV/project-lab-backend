# 2. Service boundaries and decomposition

Date: 2026-07-02

## Status

Accepted

## Context

Entity coupling analysis of the current monolith:

| Domain | Entities | Coupling to other domains |
|---|---|---|
| Identity | `User`, `Host`, `Language`, JWT/Auth | `Host.id` *is* `User.id` (shared PK, not a separate FK) |
| Inventory | `Stay`, `Room`, `Address`, `PropertyBrand`, lookup tables (Amenity, Accessibility, MealPlan, PaymentType, TravelerExperience, View) | `Stay.host` is a live `@ManyToOne` to `Host` |
| Booking | `Booking` | Live `@ManyToOne User`, `@ManyToMany Room` — JPA object refs, not IDs |
| Review | `Review` | Flat `userId`/`stayId` columns, no JPA relation — already decoupled |
| Media | `StayPicture` (to become generic `Media`, see ADR-0003) | Flat `stayId`/`ownerId`, already isolated behind `StorageService` and its own REST controller |

## Decision

Decompose into five deployable services, plus the Gateway (not itself a domain
service):

- **Identity service** — `User`, `Host`, `Language`, `AuthService`, `JwtService`,
  `AuthController`. Host stays with Identity (not Inventory) because it shares a
  primary key with `User` and is fundamentally profile/reputation data about a
  user, not stay-inventory data. Inventory references hosts by `hostId: Int` only,
  resolved via Feign when needed ([ADR-0008](0008-inter-service-communication.md)).
- **Inventory service** — `Stay`, `Room`, `Address`, `PropertyBrand`, and the six
  lookup tables. Kept together because it is heavily self-joined internally (see
  `StayBatchResolver`); splitting it further has no payoff.
- **Booking service** — `Booking`. References `User` and `Room` by ID only (same
  pattern `Review` already uses), validated against the caller's JWT claims rather
  than a DB join.
- **Review service** — `Review`, unchanged in shape since it was already decoupled.
- **Media service** — generic, see [ADR-0003](0003-generic-media-service.md).

## Consequences

- Host's shared-PK relationship to User means Identity and Inventory must
  communicate for any Stay operation that needs host details (e.g. rating
  display) — a Feign call or a denormalized/cached summary, not a DB join.
- Booking becomes the last and hardest service to extract, since it is the one
  place a cross-domain query (availability search) currently exists — addressed in
  [ADR-0010](0010-booking-inventory-consistency.md).
- Review and Media are the cheapest, lowest-risk extractions and are done first as
  a walking skeleton to prove the Gateway/Feign/Eureka wiring end-to-end.
