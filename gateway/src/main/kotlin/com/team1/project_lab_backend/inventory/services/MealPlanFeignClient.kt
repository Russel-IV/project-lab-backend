package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.MealPlanRequest
import com.team1.project_lab_backend.inventory.models.MealPlan
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

@Component
class MealPlanFeignClient(
    @Qualifier("inventoryServiceWebClient") private val webClient: WebClient,
) {

    suspend fun list(ids: List<Int>? = null): List<MealPlan> =
        webClient.get()
            .uri { b -> b.path("/internal/meal-plans").also { if (ids != null) it.queryParam("ids", *ids.toTypedArray()) }.build() }
            .retrieve()
            .awaitBody()

    suspend fun get(id: Int): MealPlan =
        webClient.get().uri("/internal/meal-plans/{id}", id).retrieve().awaitBody()

    suspend fun create(request: MealPlanRequest): MealPlan =
        webClient.post().uri("/internal/meal-plans").bodyValue(request).retrieve().awaitBody()

    suspend fun update(
        id: Int,
        request: MealPlanRequest,
    ): MealPlan =
        webClient.patch().uri("/internal/meal-plans/{id}", id).bodyValue(request).retrieve().awaitBody()

    suspend fun delete(id: Int) {
        webClient.delete().uri("/internal/meal-plans/{id}", id).retrieve().awaitBodilessEntity()
    }
}
