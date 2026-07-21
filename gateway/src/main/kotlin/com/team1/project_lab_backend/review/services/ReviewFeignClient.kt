package com.team1.project_lab_backend.review.services

import com.team1.project_lab_backend.review.dto.ReviewSummary
import com.team1.project_lab_backend.review.models.Review
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

@Component
class ReviewFeignClient(
    @Qualifier("reviewServiceWebClient") private val webClient: WebClient,
) {

    suspend fun list(
        stayId: Int? = null,
        userId: Int? = null,
        ids: List<Int>? = null,
        page: Int = 0,
        size: Int = 20,
    ): List<Review> =
        webClient.get()
            .uri { b ->
                b.path("/internal/reviews").queryParam("page", page).queryParam("size", size)
                if (stayId != null) b.queryParam("stayId", stayId)
                if (userId != null) b.queryParam("userId", userId)
                if (ids != null) b.queryParam("ids", *ids.toTypedArray())
                b.build()
            }
            .retrieve()
            .awaitBody()

    suspend fun summary(stayId: Int): ReviewSummary =
        webClient.get()
            .uri { b -> b.path("/internal/reviews/summary").queryParam("stayId", stayId).build() }
            .retrieve()
            .awaitBody()

    suspend fun mine(
        userId: Int,
        stayId: Int,
    ): Review =
        webClient.get()
            .uri { b -> b.path("/internal/reviews/mine").queryParam("userId", userId).queryParam("stayId", stayId).build() }
            .retrieve()
            .awaitBody()

    suspend fun create(request: CreateReviewRequest): Review =
        webClient.post().uri("/internal/reviews").bodyValue(request).retrieve().awaitBody()

    suspend fun update(
        id: Int,
        request: UpdateReviewRequest,
    ): Review =
        webClient.patch().uri("/internal/reviews/{id}", id).bodyValue(request).retrieve().awaitBody()

    suspend fun delete(
        id: Int,
        requestingUserId: Int,
    ) {
        webClient.delete()
            .uri { b -> b.path("/internal/reviews/{id}").queryParam("requestingUserId", requestingUserId).build(id) }
            .retrieve()
            .awaitBodilessEntity()
    }
}

data class CreateReviewRequest(val text: String, val userId: Int, val stayId: Int, val rating: Int)

data class UpdateReviewRequest(val text: String, val stayId: Int, val rating: Int, val requestingUserId: Int)
