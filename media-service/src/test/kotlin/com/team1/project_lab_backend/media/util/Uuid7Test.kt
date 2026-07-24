package com.team1.project_lab_backend.media.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class Uuid7Test {
    @Test
    fun randomUUIDHasVersion7AndIetfVariant() {
        val id = Uuid7.randomUUID()

        assertEquals(7, id.version())
        assertEquals(2, id.variant())
    }

    @Test
    fun randomUUIDsAreUniqueAcrossManyCalls() {
        val ids = (1..1000).map { Uuid7.randomUUID() }

        assertEquals(1000, ids.toSet().size)
    }

    @Test
    fun timestampsOfConsecutiveIdsAreMonotonicallyNonDecreasing() {
        val ids = (1..50).map { Uuid7.randomUUID() }
        val timestamps = ids.map { it.mostSignificantBits ushr 16 }

        for (i in 1 until timestamps.size) {
            assertTrue(
                timestamps[i] >= timestamps[i - 1],
                "timestamp went backwards at index $i: ${timestamps[i]} < ${timestamps[i - 1]}",
            )
        }
    }
}
