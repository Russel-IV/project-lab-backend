package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.models.User
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody
import java.util.UUID

@Component
class UserFeignClient(
    @Qualifier("identityServiceWebClient") private val webClient: WebClient,
) {

    suspend fun list(ids: List<Int>? = null): List<User> =
        webClient.get()
            .uri { b -> b.path("/internal/users").also { if (ids != null) it.queryParam("ids", *ids.toTypedArray()) }.build() }
            .retrieve()
            .awaitBody()

    suspend fun get(id: Int): User =
        webClient.get().uri("/internal/users/{id}", id).retrieve().awaitBody()

    suspend fun getByPublicId(publicId: UUID): User =
        webClient.get().uri("/internal/users/by-public-id/{publicId}", publicId).retrieve().awaitBody()

    suspend fun create(request: UserUpsertRequest): User =
        webClient.post().uri("/internal/users").bodyValue(request).retrieve().awaitBody()

    suspend fun update(
        id: Int,
        requestingUserId: Int,
        request: UserUpsertRequest,
    ): User =
        webClient.patch()
            .uri { b -> b.path("/internal/users/{id}").queryParam("requestingUserId", requestingUserId).build(id) }
            .bodyValue(request)
            .retrieve()
            .awaitBody()

    suspend fun delete(
        id: Int,
        requestingUserId: Int,
    ) {
        webClient.delete()
            .uri { b -> b.path("/internal/users/{id}").queryParam("requestingUserId", requestingUserId).build(id) }
            .retrieve()
            .awaitBodilessEntity()
    }
}

data class UserUpsertRequest(val name: String)
