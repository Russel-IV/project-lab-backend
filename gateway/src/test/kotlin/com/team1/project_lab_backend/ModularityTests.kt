package com.team1.project_lab_backend

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules

class ModularityTests {
    private val modules = ApplicationModules.of(ProjectLabBackendApplication::class.java)

    /**
     * Module detection itself works today: identity/inventory/booking/review/media are
     * correctly resolved as separate application modules, with config/util as shared
     * modules per @Modulithic(sharedModules = ...) on the application class.
     */
    @Test
    fun modulesAreDetected() {
        listOf("identity", "inventory", "booking", "review", "media").forEach { name ->
            assertTrue(modules.getModuleByName(name).isPresent, "expected module '$name' to be detected")
        }
    }

    /**
     * Full boundary verification is intentionally disabled, not deleted. Phase 6
     * (docs/adr/0002, docs/adr/0010, docs/adr/0011 — Booking extracted into its own
     * service, the last of the five domains) actually fixed booking's own
     * involvement in this check: run with `mvn clean test` (the `clean` matters — a
     * stale `target/classes` left over from before Phase 5's extraction kept a
     * deleted `inventory.repositories.RoomRepository` class on the scanned
     * classpath, which falsely made it look like `inventory -> booking` was still a
     * live cycle via `BookingStatus`; it isn't once actually rebuilt from clean) and
     * `booking` is no longer part of ANY cycle — it's now a purely one-directional
     * dependent of `identity`/`inventory` (BookingBatchResolver's Feign lookups) and
     * dependency of `review` (ReviewService.hasCompletedBookingForStay), exactly as
     * ADR-0010/0011 intend.
     *
     * That is real, if partial, progress — but it corrects a mistaken assumption in
     * this kdoc's own prior revisions (Phase 3/4/5), not a green light to re-enable
     * the test. Those revisions claimed "identity -> media" and "media -> inventory"
     * were "resolved" once each side became a Feign call. Empirically, `.verify()`
     * still fails today with two pre-existing structural cycles, both entirely
     * unrelated to Booking and untouched by Phase 6:
     *
     * - `identity -> media -> inventory -> identity` (ProfileService's
     *   MediaFeignClient dependency, media's Stay/RoomFeignClient dependencies, and
     *   StayBatchResolver's HostFeignClient dependency, chained into a 3-cycle)
     * - `inventory -> media -> inventory` (StayResponse/RoomBatchResolver's
     *   StayPictureResponse/RoomPictureService dependencies vs. media's own
     *   Stay/RoomFeignClient dependencies)
     *
     * Spring Modulith's `verify()` doesn't distinguish "a live JPA relation" from "a
     * Feign client call" — it flags any cross-module package reference that isn't
     * routed through an explicitly declared Named Interface (`@NamedInterface` /
     * `package-info.java`), cycle or not. Every extraction phase so far, including
     * this one, left each domain's `services`/`models` packages fully open rather
     * than declaring Named Interfaces, so plain one-directional references (e.g.
     * `booking -> identity` via `UserFeignClient`) also still fail `.verify()` as
     * "depends on non-exposed type" even where no cycle exists. Fully re-enabling
     * this test needs its own follow-up: either declare Named Interfaces for each
     * FeignClient/DTO surface every other module is allowed to reach, or restructure
     * the two remaining cycles above (e.g. move picture-ownership host/stay checks
     * so `media` stops depending on `inventory` while `inventory` depends on
     * `media`). Neither is in scope for a single-domain extraction phase.
     */
    @Test
    @Disabled("two pre-existing cycles (identity/media/inventory) plus non-exposed-type violations across every module — see kdoc above")
    fun verifiesModularStructure() {
        modules.verify()
    }
}
