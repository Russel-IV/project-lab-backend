package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.PropertyBrand
import org.springframework.data.jpa.repository.JpaRepository

interface PropertyBrandRepository : JpaRepository<PropertyBrand, Int>
