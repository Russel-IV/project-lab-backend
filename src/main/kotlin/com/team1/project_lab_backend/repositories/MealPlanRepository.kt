package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.MealPlan
import org.springframework.data.jpa.repository.JpaRepository

interface MealPlanRepository : JpaRepository<MealPlan, Int>
