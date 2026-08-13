# 7. Configuration management

Date: 2026-07-02

## Status

Accepted

## Context

Config (JWT secret, DB creds, CORS origins, upload dir) is handled via env vars and
per-profile `application-*.properties`, documented in `CLAUDE.md`. Spring Cloud
Config Server was considered as the alternative — a dedicated service serving
shared config over HTTP.

## Decision

Keep the current env-var/`application-*.properties` convention. No Config Server.

## Consequences

- No new container/JVM.
- Continues an already-working convention, no migration cost.
- Config Server's value (dynamic refresh, single source of truth across many
  services/teams, versioning) doesn't offset its cost at this scale.
- Secrets stay in env vars, not a second store.
- Revisit if service/team count grows enough that per-service env files become a
  real sync burden.
