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
     * Full boundary verification is intentionally disabled, not deleted. As of Phase 1
     * (docs/adr/0009 — JwtAuthFilter replaced by the OAuth2 Resource Server),
     * ApplicationModules.verify() is down to 3 real structural cycles, all current,
     * still-monolithic architecture, not a test bug:
     *
     * - booking <-> inventory  (Booking.rooms: Set<Room>; inventory's availability
     *   query reads BookingStatus — see docs/adr/0010)
     * - identity <-> media     (StorageService / profile picture URLs — not yet
     *   covered by any ADR; flag as a gap alongside RoomPicture's Media ownership)
     * - media <-> inventory    (ownership checks resolve Stay/Room; Stay.pictures
     *   batch-resolves into media)
     *
     * `config <-> identity` (JwtAuthFilter loading User directly) resolved in Phase 1 —
     * that's the one row this table lost since Phase 0. `booking <-> identity`
     * (Booking.user: User) is still live in the source but no longer surfaces as its
     * own separate cycle now that config/identity dropped out of the graph; it's
     * folded into the remaining cycles above until Phase 6 removes the JPA relation.
     *
     * Each remaining cycle is resolved by a specific later phase, not by annotation:
     * - identity <-> media and media <-> inventory resolve as Media and Inventory are
     *   extracted into their own services (docs/adr/0003, Phase 3 / Phase 5).
     * - booking <-> inventory resolves last, when Booking's JPA relations become
     *   ID-only references (docs/adr/0011, Phase 6).
     *
     * Re-enable this test (delete the @Disabled) once the corresponding phase lands;
     * expect the violation list to shrink incrementally rather than disappear all at
     * once.
     */
    @Test
    @Disabled("3 known structural cycles pending later migration phases — see kdoc above")
    fun verifiesModularStructure() {
        modules.verify()
    }
}
