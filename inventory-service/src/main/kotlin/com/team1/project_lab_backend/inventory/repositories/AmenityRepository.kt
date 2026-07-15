package com.team1.project_lab_backend.inventory.repositories

import com.team1.project_lab_backend.inventory.models.Amenity
import org.springframework.data.jpa.repository.JpaRepository

interface AmenityRepository : JpaRepository<Amenity, Int>
