package com.team1.project_lab_backend.inventory.repositories

import com.team1.project_lab_backend.inventory.models.View
import org.springframework.data.jpa.repository.JpaRepository

interface ViewRepository : JpaRepository<View, Int>
