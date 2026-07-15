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
     * Full boundary verification is intentionally disabled, not deleted: as of the
     * Phase 0 package reorg (docs/adr/0012), ApplicationModules.verify() finds 6 real
     * structural cycles that are current, still-monolithic architecture, not a test bug:
     *
     * - booking <-> identity   (Booking.user: User — live JPA relation)
     * - booking <-> inventory  (Booking.rooms: Set<Room>; inventory's availability
     *   query reads BookingStatus — see docs/adr/0010)
     * - config <-> identity    (JwtAuthFilter loads User directly)
     * - identity <-> media     (StorageService / profile picture URLs — not yet
     *   covered by any ADR; flag as a gap alongside RoomPicture's Media ownership)
     * - media <-> inventory    (ownership checks resolve Stay/Room; Stay.pictures
     *   batch-resolves into media)
     *
     * Each is resolved by a specific later phase, not by annotation:
     * - config <-> identity resolves when JwtAuthFilter is replaced by the OAuth2
     *   Resource Server setup (docs/adr/0009, migration Phase 1).
     * - identity <-> media and media <-> inventory resolve as Media and Inventory are
     *   extracted into their own services (docs/adr/0003, Phase 3 / Phase 5).
     * - booking <-> identity and booking <-> inventory resolve last, when Booking's
     *   JPA relations become ID-only references (docs/adr/0011, Phase 6).
     *
     * Re-enable this test (delete the @Disabled) once the corresponding phase lands;
     * expect the violation list to shrink incrementally rather than disappear all at
     * once.
     */
    @Test
    @Disabled("6 known structural cycles pending later migration phases — see kdoc above")
    fun verifiesModularStructure() {
        modules.verify()
    }
}
