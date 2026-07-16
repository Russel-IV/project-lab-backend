# Container Diagram (C4 Level 2)

Reflects the current microservices topology (docs/adr/0001, 0004, 0005, 0006, 0008, 0011, 0013). One box per running container in `docker-compose.yml`.

```mermaid
graph TB
    Client["Client<br/>(browser/frontend)"]

    subgraph Backend["project-lab-backend"]
        Gateway["gateway :8080<br/>GraphQL schema + resolvers<br/>REST: auth, profile, payment methods, uploads<br/>JWT issuance/validation"]
        Identity["identity-service :8081<br/>User, Host, Language,<br/>PaymentMethod, auth"]
        Inventory["inventory-service :8082<br/>Stay, Room, 7 lookup tables,<br/>PostGIS geo search"]
        Booking["booking-service :8083<br/>Booking"]
        Review["review-service :8084<br/>Review"]
        Media["media-service :8085<br/>Media (owner_type/owner_id)"]
        Eureka["eureka-server :8761<br/>Service registry"]
        Zipkin["zipkin :9411<br/>Trace collector/UI"]
    end

    IDDB[("identity-database")]
    INVDB[("inventory-database<br/>(PostGIS)")]
    BOOKDB[("booking-database")]
    REVDB[("review-database")]
    MEDDB[("media-database")]

    Client -->|GraphQL POST /graphql<br/>REST /api/v1/**| Gateway

    Gateway -->|Feign| Identity
    Gateway -->|Feign| Inventory
    Gateway -->|Feign| Booking
    Gateway -->|Feign| Review
    Gateway -->|Feign| Media

    Inventory -->|Feign: host existence| Identity
    Inventory -->|Feign: availability/conflict check<br/>Resilience4j-wrapped| Booking
    Booking -->|Feign: room validation| Inventory

    Gateway --> Eureka
    Identity --> Eureka
    Inventory --> Eureka
    Booking --> Eureka
    Review --> Eureka
    Media --> Eureka

    Gateway -.->|spans, fire-and-forget| Zipkin
    Identity -.->|spans| Zipkin
    Inventory -.->|spans| Zipkin
    Booking -.->|spans| Zipkin
    Review -.->|spans| Zipkin
    Media -.->|spans| Zipkin

    Identity --> IDDB
    Inventory --> INVDB
    Booking --> BOOKDB
    Review --> REVDB
    Media --> MEDDB
```

## Notes

- No inter-service call has a database-level FK across the boundary (docs/adr/0011) — every cross-service reference (`hostId`, `userId`, `roomIds`, `stayId`, `ownerId`) is either Feign-validated at write time or trusted from an authenticated JWT, never enforced by Postgres.
- `eureka-server` and `zipkin` are infrastructure, not domain services — no Feign traffic to/from them, only registration heartbeats and span export respectively.
- `zipkin` export is explicitly not in any service's `depends_on` — a slow/absent Zipkin must never block app startup or requests.
- `inventory-service → booking-service` is the only Resilience4j circuit-breaker-wrapped call (docs/adr/0010); on failure it falls back to a permissive "show all rooms" result rather than erroring.
