package com.team1.project_lab_backend.identity.repositories

import com.team1.project_lab_backend.identity.models.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface UserRepository : JpaRepository<User, Int> {
    fun findByEmailAndDeletedAtIsNull(email: String): Optional<User>

    fun findByPublicId(publicId: UUID): Optional<User>
}
