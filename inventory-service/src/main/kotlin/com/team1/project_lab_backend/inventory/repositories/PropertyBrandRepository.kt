package com.team1.project_lab_backend.inventory.repositories

import com.team1.project_lab_backend.inventory.models.PropertyBrand
import org.springframework.data.jpa.repository.JpaRepository

interface PropertyBrandRepository : JpaRepository<PropertyBrand, Int>
