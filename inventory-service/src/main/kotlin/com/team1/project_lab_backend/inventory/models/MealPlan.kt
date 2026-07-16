package com.team1.project_lab_backend.inventory.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Entity
@Table(
    name = "meal_plan",
    uniqueConstraints = [UniqueConstraint(columnNames = ["meal_plan_type"])],
)
open class MealPlan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Int = 0,
    @Column(name = "meal_plan_type", nullable = false, unique = true, length = 100)
    @field:NotBlank
    @field:Size(max = 100)
    open val mealPlanType: String,
)
