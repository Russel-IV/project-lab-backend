package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.ProfileResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

@Component
class ProfileFeignClient(
    @Qualifier("identityServiceWebClient") private val webClient: WebClient,
) {

    suspend fun getProfile(userId: Int): ProfileResponse =
        webClient.get().uri("/internal/profile/{userId}", userId).retrieve().awaitBody()

    suspend fun updateProfile(
        userId: Int,
        request: ProfileUpdateRequest,
    ): ProfileResponse =
        webClient.patch().uri("/internal/profile/{userId}", userId).bodyValue(request).retrieve().awaitBody()

    suspend fun updatePictureUrl(
        userId: Int,
        request: UpdatePictureUrlRequest,
    ): ProfileResponse =
        webClient.patch().uri("/internal/profile/{userId}/picture-url", userId).bodyValue(request).retrieve().awaitBody()

    suspend fun changePassword(
        userId: Int,
        request: PasswordChangeRequest,
    ) {
        webClient.patch().uri("/internal/profile/{userId}/password", userId).bodyValue(request).retrieve().awaitBodilessEntity()
    }

    suspend fun deleteAccount(userId: Int) {
        webClient.delete().uri("/internal/profile/{userId}", userId).retrieve().awaitBodilessEntity()
    }
}

data class ProfileUpdateRequest(val name: String, val email: String, val phone: String?)

data class UpdatePictureUrlRequest(val url: String)

data class PasswordChangeRequest(val currentPassword: String, val newPassword: String)
