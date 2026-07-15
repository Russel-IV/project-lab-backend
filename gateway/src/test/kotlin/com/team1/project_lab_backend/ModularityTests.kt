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
     * Full boundary verification is intentionally disabled, not deleted. As of Phase 5
     * (docs/adr/0002, docs/adr/0010 — Inventory extracted into its own service), the
     * remaining known structural issue is:
     *
     * - booking -> inventory  (BookingService/BookingBatchResolver reference
     *   inventory.services.RoomFeignClient to reach the now-extracted
     *   inventory-service — no longer a live JPA relation, and no longer a cycle:
     *   inventory's own code stopped referencing booking.models.Booking/BookingStatus
     *   entirely once StayService/RoomService's availability checks became Feign calls
     *   to booking-service instead of a local JPQL join. What's left is a
     *   one-directional in-JVM package reference, same shape identity/media/review
     *   already had post-extraction, until Booking is itself extracted (Phase 6) and
     *   it becomes a real network call.)
     * - review -> inventory, review -> booking (ReviewService/ReviewBatchResolver
     *   reference StayFeignClient and BookingService respectively — same
     *   one-directional shape, resolves fully once Booking is extracted, Phase 6.)
     *
     * identity -> media and media -> inventory (Phase 3/4's remaining items) are
     * resolved: Identity's own extraction (Phase 4) made the former a real network
     * call, and StayPictureService/RoomPictureService now reach inventory-service via
     * StayFeignClient/RoomFeignClient (Phase 5) instead of local repositories.
     *
     * Re-enable this test (delete the @Disabled) once Phase 6 extracts Booking and the
     * remaining one-directional references become real network calls rather than
     * in-JVM package references Modulith still needs an explicit allowance for.
     */
    @Test
    @Disabled("known structural issues pending later migration phases — see kdoc above")
    fun verifiesModularStructure() {
        modules.verify()
    }
}
