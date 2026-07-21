# project-lab-backend

GraphQL/REST backend for a lodging platform (home rentals and hotels), built as a set of Kotlin/Spring Boot microservices behind a single GraphQL gateway.

## Stack and architecture

- Kotlin, Spring Boot 4.0.6, Spring Cloud 2025.1.2
- Spring AI + OpenAI (GPT-4o) for RAG-based travel inquiries
- Maven multi-module reactor build
- GraphQL (Spring GraphQL) via the gateway; REST for auth, file upload, profile, and payment-method endpoints
- Netflix Eureka — service discovery
- OpenFeign — inter-service HTTP calls; Resilience4j circuit breakers on select calls
- Micrometer Tracing + OpenTelemetry bridge → Zipkin
- PostgreSQL (PostGIS on `gateway`/`inventory-service` for geo search, PGVector on `chatbot-service` for vector search), one database per service, Flyway migrations
- JWT (jjwt) + Spring Security OAuth2 Resource Server
- Docker / Docker Compose

| Module              | Port | Role                                                                         |
| ------------------- | ---- | ---------------------------------------------------------------------------- |
| `eureka-server`     | 8761 | Service registry                                                             |
| `gateway`           | 8080 | GraphQL schema + resolvers, REST auth/profile/upload endpoints, JWT issuance |
| `identity-service`  | 8081 | Users, hosts, auth, profiles, payment methods                                |
| `inventory-service` | 8082 | Stays, rooms, lookup tables, geo search                                      |
| `booking-service`   | 8083 | Bookings                                                                     |
| `review-service`    | 8084 | Reviews                                                                      |
| `media-service`     | 8085 | Picture storage (`Media` entity: `ownerType`/`ownerId`)                      |
| `chatbot-service`   | 8086 | Travel advisor chatbot utilizing Spring AI and PGVector                      |
| `zipkin`            | 9411 | Trace UI (docker-compose only)                                               |

Each service module owns its own Postgres database and Flyway migration history (`src/main/resources/db/migration/`).

## How to run locally

Requirements: Java 21, the included `./mvnw` wrapper, Docker + Docker Compose.

1. Copy `.env.example` to `.env` and fill in the values (`POSTGRES_*`, `SPRING_PORT`, `SPRING_PROFILES_ACTIVE`, `SPRING_AI_OPENAI_API_KEY`).
2. Bring up the full stack:
   ```bash
   ./scripts/lift-stack.sh
   ```
   Builds each service image sequentially and starts containers in dependency order (databases → Eureka → app services), to avoid saturating CPU on a constrained dev machine. Pass `--no-build` to skip rebuilding when images are already current.
3. Seed dev data (users, stays, rooms, bookings, reviews, pictures):
   ```bash
   ./scripts/populate-db.sh
   ```
4. Access points:
   - GraphQL: `http://localhost:8080/graphql`
   - GraphiQL: `http://localhost:8080/graphiql`
   - Eureka dashboard: `http://localhost:8761`
   - Zipkin: `http://localhost:9411`

Tear down with `docker compose down` (add `-v` to also wipe database volumes).
