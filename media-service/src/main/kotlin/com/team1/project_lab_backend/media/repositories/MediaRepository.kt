package com.team1.project_lab_backend.media.repositories

import com.team1.project_lab_backend.media.models.Media
import com.team1.project_lab_backend.media.models.MediaOwnerType
import org.springframework.data.jpa.repository.JpaRepository

interface MediaRepository : JpaRepository<Media, Int> {
    fun findByOwnerTypeAndOwnerId(
        ownerType: MediaOwnerType,
        ownerId: Int,
    ): List<Media>

    fun findByOwnerTypeAndOwnerIdIn(
        ownerType: MediaOwnerType,
        ownerIds: Collection<Int>,
    ): List<Media>

    fun findByOwnerTypeAndOwnerIdAndId(
        ownerType: MediaOwnerType,
        ownerId: Int,
        id: Int,
    ): Media?

    fun existsByOwnerTypeAndOwnerIdAndIsPrimary(
        ownerType: MediaOwnerType,
        ownerId: Int,
        isPrimary: Boolean,
    ): Boolean
}
