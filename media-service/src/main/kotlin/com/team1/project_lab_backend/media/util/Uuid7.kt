package com.team1.project_lab_backend.media.util

import java.security.SecureRandom
import java.util.UUID

object Uuid7 {
    private val random = SecureRandom()

    fun randomUUID(): UUID {
        val unixMillis = System.currentTimeMillis() and 0xFFFFFFFFFFFFL
        val randA = random.nextInt(1 shl 12).toLong() and 0xFFFL
        val msb = (unixMillis shl 16) or (0x7L shl 12) or randA

        val randB = random.nextLong() and 0x3FFFFFFFFFFFFFFFL
        val lsb = (0x2L shl 62) or randB

        return UUID(msb, lsb)
    }
}
