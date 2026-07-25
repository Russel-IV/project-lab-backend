# Database Schema

One section per service (docs/adr/0011 — database-per-service). Generated from each service's own `src/main/resources/db/migration/V1__*.sql`. No FK crosses a service boundary — cross-service id references (`hostId`, `userId`, `roomIds`, `stayId`, `ownerId`) are noted per section but not drawn as ERD relationships, since nothing at the database level enforces them.

## gateway (`project-lab-database`)

No domain tables — everything was dropped by `V18`–`V22__drop_*.sql` during the migration (docs/adr/0001 and on). The Postgres connection is retained only because Flyway/JDBC config hasn't been removed from the module.

## identity-service (`identity-database`)

```mermaid
erDiagram
    USER {
        int id PK
        uuid public_id UK
        string name
        string email UK
        string password_hash
        string phone
        string profile_picture_url
        timestamp deleted_at
    }
    HOST {
        int id PK "FK -> USER.id, ON DELETE CASCADE"
        numeric communication_rating "0-100"
        numeric checkin_process_rating "0-100"
        numeric cancellation_rate "0-100"
    }
    LANGUAGE {
        int id PK
        string language_name UK
    }
    HOST_LANGUAGE {
        int host_id PK "FK -> HOST.id"
        int language_id PK "FK -> LANGUAGE.id"
    }
    PAYMENT_METHOD {
        int id PK
        int user_id FK
        string stripe_payment_method_id
        string brand
        string last_four
        string type
        int expiry_month
        int expiry_year
        boolean is_default
    }

    USER ||--o| HOST : "is (subtype)"
    USER ||--o{ PAYMENT_METHOD : "has"
    HOST }o--o{ LANGUAGE : "speaks (via HOST_LANGUAGE)"
```

`email` is unique only among non-soft-deleted rows (`deleted_at IS NULL`). No raw card number/CVV is ever persisted (docs/adr — payment methods are mocked-Stripe, see CLAUDE.md).

## inventory-service (`inventory-database`, PostGIS)

```mermaid
erDiagram
    ADDRESS {
        int id PK
        string street_address
        string extended_address
        string city
        string state_province
        string postal_code
        string country_code
        int region_id FK
    }
    REGION {
        int id PK
        string city
        string country_code
        string state_province "nullable"
        int curated_rank "nullable"
    }
    PROPERTY_BRAND {
        int id PK
        string brand_name UK
    }
    STAY {
        int id PK
        uuid public_id UK
        string name
        string about
        enum property_type "HOME | HOTEL"
        boolean is_refundable
        numeric star_rating "0.0-5.0"
        int days_from_booking_cancellation_deadline
        string policies_text
        string important_information
        int host_id "no FK — identity-service"
        int address_id FK "UNIQUE — one stay per address"
        int property_brand_id FK "nullable"
        geography location "GEOGRAPHY(POINT,4326)"
    }
    ROOM {
        int id PK
        int stay_id FK
        string name
        numeric price
        int sleeps
        int bedroom_amount
        numeric bathrooms
        numeric size
    }
    VIEW {
        int id PK
        string view_type UK
    }
    AMENITY {
        int id PK
        string name UK
        string type "ROOM_AMENITY | PROPERTY_AMENITY"
    }
    ACCESSIBILITY {
        int id PK
        string accessibility_type UK
    }
    MEAL_PLAN {
        int id PK
        string meal_plan_type UK
    }
    PAYMENT_TYPE {
        int id PK
        string payment_type UK
    }
    TRAVELER_EXPERIENCE {
        int id PK
        string traveler_experience_type UK
    }

    STAY ||--|| ADDRESS : "has"
    ADDRESS }o--|| REGION : "region_id"
    STAY }o--o| PROPERTY_BRAND : "branded as"
    STAY ||--o{ ROOM : "has"
    STAY }o--o{ VIEW : "stay_view"
    STAY }o--o{ AMENITY : "stay_amenity"
    STAY }o--o{ ACCESSIBILITY : "stay_accessibility"
    STAY }o--o{ MEAL_PLAN : "stay_meal_plan"
    STAY }o--o{ PAYMENT_TYPE : "stay_payment_type"
    STAY }o--o{ TRAVELER_EXPERIENCE : "stay_traveler_experience"
```

`STAY.host_id` has no FK — resolved via Feign to identity-service's `Host`/`User`. `amenity.type` is a plain `VARCHAR`, not the `amenity_type` enum (kept as free-form string for `ROOM_AMENITY`/`PROPERTY_AMENITY` values). `location` is GIST-indexed for geo search. `ADDRESS.city` is GIN-indexed on `lower(city)` with `pg_trgm` (`idx_address_city_trgm`, docs/adr/0019) to accelerate substring/fuzzy destination search; the `pg_trgm` and `unaccent` extensions are enabled for this purpose.

`REGION` is the stable dedup key for a (city, country_code) pair (docs/adr/0018) — one row per distinct pair, unique-indexed on `(lower(city), country_code)`. `Address.region_id` is resolved/created by `StayService.findOrCreateRegion()` from the free-text city/country on write, not supplied directly by API callers. `destinations`/`StayFilterInput.regionId` read/filter against `region` directly rather than re-deriving distinct pairs from `address` on every call. `curated_rank` (docs/adr/0022) is a manually-assigned editorial ranking for `popularDestinations`'s empty-query state — null means "not curated"; there's no admin/host-facing way to set it yet, by design.

## booking-service (`booking-database`)

```mermaid
erDiagram
    BOOKING {
        int id PK
        int user_id "no FK — identity-service"
        date check_in_date
        date check_out_date
        enum status "PENDING | CONFIRMED | CANCELLED | COMPLETED"
        int guests_count
        timestamp created_at
        numeric total_price
    }
    BOOKING_ROOM {
        int booking_id PK "FK -> BOOKING.id"
        int room_id PK "no FK — inventory-service"
    }

    BOOKING ||--o{ BOOKING_ROOM : "includes"
```

`check_out_date > check_in_date` and `guests_count > 0` are DB-level `CHECK` constraints. `BOOKING_ROOM.room_id` has no FK to inventory-service's `Room` — validated via Feign at `createBooking` time instead.

## review-service (`review-database`)

```mermaid
erDiagram
    REVIEW {
        int id PK
        string text
        int user_id "no FK — identity-service"
        int stay_id "no FK — inventory-service"
        smallint rating "1-5"
    }
```

`UNIQUE(user_id, stay_id)` — one review per user per stay.

## media-service (`media-database`)

```mermaid
erDiagram
    MEDIA {
        int id PK
        enum owner_type "STAY | ROOM | USER"
        int owner_id "no FK — polymorphic, inventory-service or identity-service"
        string url "portable storage key, e.g. stays/1/uuid.jpg"
        string caption
        boolean is_primary
        int display_order
    }
```

One generic table replaces the old monolith's separate `stay_picture`/`room_picture` tables (docs/adr/0003). `UNIQUE(owner_type, owner_id) WHERE is_primary` enforces one primary picture per owner.
