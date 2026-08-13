# 12. Module boundary enforcement with Spring Modulith

Date: 2026-07-02

## Status

Accepted

## Context

Before physical extraction, the monolith already crosses future service
boundaries in ways easy to miss by inspection (e.g. a batch resolver reaching into
another domain's repository directly). Discovering a missed coupling only when a
network call fails at runtime is expensive to debug.

## Decision

As Phase 0 (before [ADR-0001](0001-adopt-microservices-with-gateway.md)'s later
phases), reorganize `models/`/`services/`/`resolvers/` into per-domain packages
(`identity/`, `inventory/`, `booking/`, `review/`, `media/`), and add **Spring
Modulith** with an `ApplicationModules.of(...).verify()` test that fails the build
on any illegal cross-module reference. Applied to the monolith itself — pure
package/dependency reorganization, no infra change.

## Consequences

- Every illegal cross-domain reference is now a failing test, caught before any
  network boundary exists — cheapest point to fix it.
- Zero deployment risk.
- An interim step: once a module is physically extracted, the real process/network
  boundary supersedes Modulith's compile-time check for it.
- Modulith's event publication registry (`@ApplicationModuleListener`) is
  available for decoupling side effects during this phase, ahead of any physical
  broker — not required, just available if useful.
