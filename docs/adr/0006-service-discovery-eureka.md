# 6. Service discovery via Eureka

Date: 2026-07-02

## Status

Accepted

## Context

Services need to find each other (Gateway → domain services, Inventory → Booking,
etc.). Options considered:

- **Static Gateway/Feign routes** — no discovery server, config points directly at
  Docker Compose service names. Fewest containers/JVMs, but not the canonical
  Spring Cloud pattern, and hardcodes topology.
- **Eureka** (`spring-cloud-starter-netflix-eureka-server` +
  `spring-cloud-starter-netflix-eureka-client`) — services self-register; Gateway
  and Feign clients resolve by logical name via `lb://`, load-balanced through
  Spring Cloud LoadBalancer.

The deciding factor is RAM: on the previous hosting target (1GB EC2 box) an extra
JVM for Eureka would have been a real cost. That constraint is resolved by
[ADR-0014](0014-deployment-topology-oracle-cloud.md) (migration to a 24GB Oracle
Cloud instance), making one more small JVM a non-issue.

## Decision

Adopt Eureka. Each service (Identity, Inventory, Booking, Review, Media, Gateway)
registers with a logical name (`identity-service`, `inventory-service`, etc.).
Feign clients and the Gateway's routes reference services by name
(`lb://identity-service`) rather than fixed hostnames.

## Consequences

- One additional container (Eureka server) in Docker Compose, with a healthcheck
  gating dependent services' startup order.
- Decouples routing configuration from fixed hostnames — instance count/location
  can change without touching Gateway config.
- This is the textbook Spring Cloud discovery pattern, useful for demonstrating it
  end-to-end in a course context, beyond its operational necessity at this scale.
