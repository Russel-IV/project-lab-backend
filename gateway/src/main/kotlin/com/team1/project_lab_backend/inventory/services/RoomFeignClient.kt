package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.RoomRequest
import com.team1.project_lab_backend.inventory.models.Room
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody
import java.time.LocalDate

@Component
class RoomFeignClient(
    @Qualifier("inventoryServiceWebClient") private val webClient: WebClient,
) {

    suspend fun list(
        ids: List<Int>? = null,
        stayId: Int? = null,
        stayIds: List<Int>? = null,
        page: Int = 0,
        size: Int = 20,
    ): List<Room> =
        webClient.get()
            .uri { b ->
                b.path("/internal/rooms").queryParam("page", page).queryParam("size", size)
                if (ids != null) b.queryParam("ids", *ids.toTypedArray())
                if (stayId != null) b.queryParam("stayId", stayId)
                if (stayIds != null) b.queryParam("stayIds", *stayIds.toTypedArray())
                b.build()
            }
            .retrieve()
            .awaitBody()

    suspend fun get(id: Int): Room =
        webClient.get().uri("/internal/rooms/{id}", id).retrieve().awaitBody()

    suspend fun available(
        stayId: Int,
        checkIn: LocalDate,
        checkOut: LocalDate,
        guests: Int? = null,
    ): List<Room> =
        webClient.get()
            .uri { b ->
                b.path("/internal/rooms/available")
                    .queryParam("stayId", stayId)
                    .queryParam("checkIn", checkIn)
                    .queryParam("checkOut", checkOut)
                if (guests != null) b.queryParam("guests", guests)
                b.build()
            }
            .retrieve()
            .awaitBody()

    suspend fun create(
        stayId: Int,
        request: RoomRequest,
        requestingUserId: Int,
    ): Room =
        webClient.post()
            .uri { b -> b.path("/internal/rooms").queryParam("stayId", stayId).queryParam("requestingUserId", requestingUserId).build() }
            .bodyValue(request)
            .retrieve()
            .awaitBody()

    suspend fun update(
        id: Int,
        request: RoomRequest,
        requestingUserId: Int,
    ): Room =
        webClient.patch()
            .uri { b -> b.path("/internal/rooms/{id}").queryParam("requestingUserId", requestingUserId).build(id) }
            .bodyValue(request)
            .retrieve()
            .awaitBody()

    suspend fun delete(
        id: Int,
        requestingUserId: Int,
    ) {
        webClient.delete()
            .uri { b -> b.path("/internal/rooms/{id}").queryParam("requestingUserId", requestingUserId).build(id) }
            .retrieve()
            .awaitBodilessEntity()
    }
}
