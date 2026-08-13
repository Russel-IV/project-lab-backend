# 3. Generic (polymorphic) media service

Date: 2026-07-02

## Status

Accepted

## Context

`StayPicture` today is Stay-specific (flat `stayId` FK, `StayPictureController` at
`/api/v1/stays/{stayId}/pictures`), already decoupled from storage via
`StorageService`. Normally a single-owner, single-table component would fold into
its owner's service rather than become a standalone "nanoservice." This decision
deliberately overrides that default: keep Media as its own service and generalize it
now, ahead of demonstrated need (future owner types like avatars/review photos) — a
product-direction trade-off, not a coupling argument.

## Decision

Media owns a polymorphic entity replacing `StayPicture`:

```
Media(id, ownerType: String, ownerId: Int, url, caption?, isPrimary, displayOrder)
```

Generic internal API:
- `POST /api/v1/media/{ownerType}/{ownerId}` (multipart upload)
- `GET /api/v1/media/{ownerType}/{ownerId}`
- `GET /api/v1/media?ownerType=STAY&ownerIds=1,2,3` (bulk, for
  `StayBatchResolver.pictures()`'s `Map<Stay, List<Media>>` shape)
- `DELETE /api/v1/media/{id}`

External path stays `/api/v1/stays/{stayId}/pictures`; the Gateway rewrites it to
`/api/v1/media/STAY/{stayId}` ([ADR-0004](0004-gateway-technology-and-endpoint-preservation.md)),
so the frontend contract is unchanged. `StorageService`/`LocalStorageService` move in
unchanged — keys already generalize (`stays/{stayId}/{uuid}.jpg` →
`{ownerType}/{ownerId}/{uuid}.jpg`).

## Consequences

- One extra field/path segment for a currently-single owner type — accepted cost.
- Media validates `ownerType` (rejects unknown values) but doesn't verify the owner
  exists — trusts the caller, same trust boundary as today.
- Pays off once a second owner type appears; until then, intentional building-ahead.
