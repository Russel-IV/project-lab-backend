package com.team1.project_lab_backend.inventory.repositories

import com.team1.project_lab_backend.inventory.models.Accessibility
import org.springframework.data.jpa.repository.JpaRepository

interface AccessibilityRepository : JpaRepository<Accessibility, Int>
