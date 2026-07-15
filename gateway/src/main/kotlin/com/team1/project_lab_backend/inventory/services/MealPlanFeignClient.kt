package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.MealPlanRequest
import com.team1.project_lab_backend.inventory.models.MealPlan
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "inventory-service", contextId = "mealPlanFeignClient")
interface MealPlanFeignClient {

    @GetMapping("/internal/meal-plans")
    fun list(@RequestParam(required = false) ids: List<Int>?): List<MealPlan>

    @GetMapping("/internal/meal-plans/{id}")
    fun get(@PathVariable id: Int): MealPlan

    @PostMapping("/internal/meal-plans")
    fun create(@RequestBody request: MealPlanRequest): MealPlan

    @PatchMapping("/internal/meal-plans/{id}")
    fun update(@PathVariable id: Int, @RequestBody request: MealPlanRequest): MealPlan

    @DeleteMapping("/internal/meal-plans/{id}")
    fun delete(@PathVariable id: Int)
}
