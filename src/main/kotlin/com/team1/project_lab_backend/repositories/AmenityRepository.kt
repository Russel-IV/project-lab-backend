package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.Amenity
import org.springframework.data.jpa.repository.JpaRepository

interface AmenityRepository : JpaRepository<Amenity, Int>
