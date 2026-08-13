# 6. Service discovery via Eureka

Date: 2026-07-02

## Status

Accepted

## Context

Services need to find each other (Gateway → domain services, Inventory →
Booking). Considered: static Gateway/Feign routes pointed at Compose service names
(fewest containers, but hardcodes topology, not the canonical pattern) vs. Eureka
(self-registration, `lb://` resolution via Spring Cloud LoadBalancer). RAM was the
deciding factor on the original 1GB EC2 target; resolved by
[ADR-0014](0014-deployment-topology-oracle-cloud.md)'s move to a 24GB+ box, making
one more small JVM a non-issue.

## Decision

Adopt Eureka. Each service registers with a logical name (`identity-service`,
`inventory-service`, etc.); Feign clients and Gateway routes reference services by
name (`lb://identity-service`), not fixed hostnames.

## Consequences

- One more container, with a healthcheck gating dependent services' startup order.
- Decouples routing from fixed hostnames.
- The textbook Spring Cloud discovery pattern — useful to demonstrate end-to-end
  beyond its operational necessity at this scale.
