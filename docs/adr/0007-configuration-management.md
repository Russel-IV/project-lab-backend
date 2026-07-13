# 7. Configuration management

Date: 2026-07-02

## Status

Accepted

## Context

Configuration (JWT secret, DB credentials, CORS origins, upload dir) is currently
handled via environment variables and per-profile `application-*.properties`
files, documented per-variable in `CLAUDE.md`. Spring Cloud Config Server was
considered as the "batteries-included" alternative: a dedicated service that
serves shared configuration over HTTP to all instances.

## Decision

Keep the current env-var/`application-*.properties` convention. Do not add a
Config Server.

## Consequences

- No new container/JVM.
- Continues an already-documented, already-working convention — no migration cost.
- Config Server's actual value (dynamic refresh without restart, single source of
  truth across many services/teams, config versioning) doesn't offset its cost at
  this project's scale: a fixed, small number of services, known at deploy time,
  operated by one team.
- Secrets (JWT signing key, DB password) stay exactly where they are today — env
  vars — rather than introducing a second place they could be stored or leaked
  from.
- Revisit if the number of services or the team grows enough that keeping
  per-service env files in sync becomes its own maintenance burden.
