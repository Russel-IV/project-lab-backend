package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Int>
