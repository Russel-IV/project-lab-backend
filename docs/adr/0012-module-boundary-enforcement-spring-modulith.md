# 12. Module boundary enforcement with Spring Modulith

Date: 2026-07-02

## Status

Accepted

## Context

Before any service is physically extracted, the monolith's code already crosses
what would become service boundaries in ways that are easy to miss by inspection
alone (e.g. a batch resolver reaching into another domain's repository directly).
Extracting a service and discovering a missed coupling only when a network call
fails at runtime is expensive to debug.

## Decision

As Phase 0 of the migration (before [ADR-0001](0001-adopt-microservices-with-gateway.md)'s
later phases), reorganize the current single-package-per-layer structure
(`models/`, `services/`, `resolvers/`) into per-domain packages
(`identity/`, `inventory/`, `booking/`, `review/`, `media/`), and add
**Spring Modulith** with an `ApplicationModules.of(...).verify()` test that fails
the build on any illegal cross-module reference.

This is applied to the monolith itself, with no deployment or infrastructure
change — pure package/dependency reorganization, verified by a test.

## Consequences

- Every illegal cross-domain reference (e.g. `BookingService` touching a `Room`
  JPA entity directly instead of an ID) is surfaced as a failing test, before any
  network boundary is introduced — cheapest possible point to fix it.
- Zero deployment risk: this phase changes package structure and adds a test, not
  runtime behavior.
- This is an interim step. Once a module is physically extracted into its own
  deployable, Modulith's compile-time boundary checking for that module is
  superseded by the actual process/network boundary — Modulith remains useful only
  for modules still living in a shared deployable during the transition.
- Spring Modulith's event publication registry (`@ApplicationModuleListener`) is
  available as a mechanism for decoupling side effects between modules during this
  phase, ahead of any physical message broker — not required by any other ADR here,
  but available if a coupling surfaces that's cleaner to solve with an in-process
  event than a direct call.
