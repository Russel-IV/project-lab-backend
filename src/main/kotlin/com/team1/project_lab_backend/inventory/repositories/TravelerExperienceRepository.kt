package com.team1.project_lab_backend.inventory.repositories

import com.team1.project_lab_backend.inventory.models.TravelerExperience
import org.springframework.data.jpa.repository.JpaRepository

interface TravelerExperienceRepository : JpaRepository<TravelerExperience, Int>
