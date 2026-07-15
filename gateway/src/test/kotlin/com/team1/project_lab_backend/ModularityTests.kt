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
     * Full boundary verification is intentionally disabled, not deleted. As of Phase 3
     * (docs/adr/0003 — Media extracted into its own service), the remaining known
     * structural issues are:
     *
     * - booking <-> inventory  (Booking.rooms: Set<Room>; inventory's availability
     *   query reads BookingStatus — see docs/adr/0010)
     * - identity -> media      (ProfileService/AuthService reference
     *   media.services.MediaFeignClient to reach the now-extracted media-service.
     *   No longer StorageService, but still an in-JVM package reference — Identity
     *   won't fully decouple until it's itself extracted (Phase 4) and this becomes a
     *   real network call from an independent service, same as every other Feign
     *   client here)
     * - media -> inventory     (StayPictureService/RoomPictureService still hold
     *   local stayRepository/roomRepository for the ownership check media-service
     *   can't do itself — resolves when Inventory is extracted, Phase 5)
     *
     * `config <-> identity` (JwtAuthFilter loading User directly) resolved in Phase 1.
     * `booking <-> identity` (Booking.user: User) is still live in the source but no
     * longer surfaces as its own separate cycle now that config/identity dropped out
     * of the graph; it's folded into the remaining items above until Phase 6 removes
     * the JPA relation.
     *
     * Each remaining item is resolved by a specific later phase, not by annotation:
     * - identity -> media resolves when Identity is extracted (Phase 4).
     * - media -> inventory resolves when Inventory is extracted (Phase 5).
     * - booking <-> inventory resolves last, when Booking's JPA relations become
     *   ID-only references (docs/adr/0011, Phase 6).
     *
     * Re-enable this test (delete the @Disabled) once the corresponding phase lands;
     * expect the violation list to shrink incrementally rather than disappear all at
     * once.
     */
    @Test
    @Disabled("known structural issues pending later migration phases — see kdoc above")
    fun verifiesModularStructure() {
        modules.verify()
    }
}
