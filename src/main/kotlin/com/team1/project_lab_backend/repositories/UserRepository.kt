package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Int> {
    fun findByEmailAndDeletedAtIsNull(email: String): Optional<User>
}
