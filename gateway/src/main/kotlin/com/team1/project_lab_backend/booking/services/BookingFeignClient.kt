package com.team1.project_lab_backend.booking.services

import com.team1.project_lab_backend.booking.models.Booking
import com.team1.project_lab_backend.booking.models.BookingStatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody
import java.time.LocalDate

@Component
class BookingFeignClient(
    @Qualifier("bookingServiceWebClient") private val webClient: WebClient,
) {

    suspend fun list(
        ids: List<Int>? = null,
        userId: Int? = null,
        page: Int = 0,
        size: Int = 20,
    ): List<Booking> =
        webClient.get()
            .uri { b ->
                b.path("/internal/bookings").queryParam("page", page).queryParam("size", size)
                if (ids != null) b.queryParam("ids", *ids.toTypedArray())
                if (userId != null) b.queryParam("userId", userId)
                b.build()
            }
            .retrieve()
            .awaitBody()

    suspend fun get(id: Int): Booking =
        webClient.get().uri("/internal/bookings/{id}", id).retrieve().awaitBody()

    suspend fun hasCompletedBookingForStay(
        userId: Int,
        stayId: Int,
    ): Boolean =
        webClient.get()
            .uri { b ->
                b.path("/internal/bookings/completed-for-stay").queryParam("userId", userId).queryParam("stayId", stayId).build()
            }
            .retrieve()
            .awaitBody()

    suspend fun create(request: CreateBookingRequest): Booking =
        webClient.post().uri("/internal/bookings").bodyValue(request).retrieve().awaitBody()

    suspend fun updateStatus(
        id: Int,
        request: BookingStatusUpdateRequest,
    ): Booking =
        webClient.patch().uri("/internal/bookings/{id}/status", id).bodyValue(request).retrieve().awaitBody()

    suspend fun delete(
        id: Int,
        requestingUserId: Int,
    ) {
        webClient.delete()
            .uri { b -> b.path("/internal/bookings/{id}").queryParam("requestingUserId", requestingUserId).build(id) }
            .retrieve()
            .awaitBodilessEntity()
    }
}

data class CreateBookingRequest(
    val userId: Int,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val guestsCount: Int,
    val roomIds: Set<Int>,
)

data class BookingStatusUpdateRequest(val status: BookingStatus)
