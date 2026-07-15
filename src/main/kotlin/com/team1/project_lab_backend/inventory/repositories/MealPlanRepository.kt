package com.team1.project_lab_backend.inventory.repositories

import com.team1.project_lab_backend.inventory.models.MealPlan
import org.springframework.data.jpa.repository.JpaRepository

interface MealPlanRepository : JpaRepository<MealPlan, Int>
