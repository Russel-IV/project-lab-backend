package com.team1.project_lab_backend.identity.repositories

import com.team1.project_lab_backend.identity.models.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Int> {
    fun findByEmailAndDeletedAtIsNull(email: String): Optional<User>
}
