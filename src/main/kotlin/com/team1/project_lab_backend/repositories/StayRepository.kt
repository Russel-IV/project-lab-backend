package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.Stay
import org.springframework.data.jpa.repository.JpaRepository

interface StayRepository : JpaRepository<Stay, Int>
