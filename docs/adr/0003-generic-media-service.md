# 3. Generic (polymorphic) media service

Date: 2026-07-02

## Status

Accepted

## Context

`StayPicture` today is Stay-specific: a flat `stayId` FK, its own REST controller
(`StayPictureController`, mounted at `/api/v1/stays/{stayId}/pictures`), and a
`StorageService` abstraction already decoupled from the persistence model
(`StayPictureService.resolveUrl()` → `storageService.toUrl()`).

Normally, a component used by exactly one owner type with no evidence of reuse
elsewhere would be folded into that owner's service rather than made a standalone,
generic microservice — a single-caller, single-table service is a common
"nanoservice" anti-pattern. That is the default recommendation.

This decision explicitly overrides that default: the product direction is to keep
Media as its own service and generalize it now, in anticipation of future owner
types (e.g. user avatars, review photos) even though only `Stay` uses it today.
This is a deliberate trade-off, not a correctness or coupling argument.

## Decision

Media service owns a polymorphic entity, replacing `StayPicture`:

```
Media(
  id: Int,
  ownerType: String,   // e.g. "STAY"; extensible, not a hardcoded FK target
  ownerId: Int,
  url: String,
  caption: String?,
  isPrimary: Boolean,
  displayOrder: Int
)
```

Internal API is generic, not Stay-specific:

- `POST /api/v1/media/{ownerType}/{ownerId}` (multipart upload)
- `GET /api/v1/media/{ownerType}/{ownerId}`
- `GET /api/v1/media?ownerType=STAY&ownerIds=1,2,3` (bulk, for the GraphQL batch
  resolver — mirrors the existing `findByStayIdIn` repository pattern so
  `StayBatchResolver.pictures()` keeps its `Map<Stay, List<Media>>` shape, just
  backed by a Feign call instead of a local repository)
- `DELETE /api/v1/media/{id}`

The **externally-facing** path stays exactly as today —
`/api/v1/stays/{stayId}/pictures` — the Gateway rewrites this to the generic
internal path (`/api/v1/media/STAY/{stayId}`) via a path-rewrite filter (see
[ADR-0004](0004-gateway-technology-and-endpoint-preservation.md)), so the frontend
contract does not change even though the service underneath is generic.

`StorageService`/`LocalStorageService` moves into this service unchanged; it is
already storage-key-portable (`stays/{stayId}/{uuid}.jpg` → generalizes to
`{ownerType}/{ownerId}/{uuid}.jpg`).

## Consequences

- One extra field (`ownerType`) and one extra path segment even though only one
  owner type exists today — accepted cost of the explicit reuse bet.
- Validation of `ownerType` values happens in the Media service (reject unknown
  types) since it no longer has a DB-level FK to enforce that an owner exists;
  existence of the owner is not checked by Media at all (it trusts the caller,
  same trust boundary as the current Stay-ownership check happening in
  `StayPictureService.addPicture()` today, which will move to the Inventory
  service or the Gateway's auth layer).
- Pays off the moment a second owner type is introduced; until then it is a known,
  intentional instance of building ahead of demonstrated need.
