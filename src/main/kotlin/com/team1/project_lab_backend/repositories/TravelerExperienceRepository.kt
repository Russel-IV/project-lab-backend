package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.TravelerExperience
import org.springframework.data.jpa.repository.JpaRepository

interface TravelerExperienceRepository : JpaRepository<TravelerExperience, Int>
