package com.team1.project_lab_backend.booking.services

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.patch
import com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import feign.Feign
import feign.FeignException
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
import org.springframework.cloud.openfeign.support.SpringMvcContract
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Exercises the real BookingFeignClient interface (request paths/params/bodies,
 * response decoding) against a stubbed booking-service, mirroring
 * ReviewFeignClientTest/MediaFeignClientTest's approach (docs/adr/0008) — the
 * annotation contract drifting out of sync with booking-service's real
 * BookingController is the one thing unit tests on either side, alone, wouldn't catch.
 */
class BookingFeignClientTest {

    private lateinit var wireMock: WireMockServer
    private lateinit var client: BookingFeignClient

    @BeforeEach
    fun setUp() {
        wireMock = WireMockServer(wireMockConfig().dynamicPort())
        wireMock.start()
        configureFor("localhost", wireMock.port())

        val mapper =
            ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        client = Feign.builder()
            .client(OkHttpClient())
            .contract(SpringMvcContract())
            .encoder(JacksonEncoder(mapper))
            .decoder(JacksonDecoder(mapper))
            .target(BookingFeignClient::class.java, "http://localhost:${wireMock.port()}")
    }

    @AfterEach
    fun tearDown() {
        wireMock.stop()
    }

    @Test
    fun listByUserSendsExpectedQueryParamsAndDecodesBookings() {
        wireMock.stubFor(
            get(urlPathEqualTo("/internal/bookings"))
                .withQueryParam("userId", equalTo("5"))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("size", equalTo("20"))
                .willReturn(
                    aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(
                            """[{"id":1,"userId":5,"checkInDate":"2026-08-01","checkOutDate":"2026-08-03",""" +
                                """"status":"CONFIRMED","guestsCount":2,"createdAt":"2026-07-01T10:00:00",""" +
                                """"totalPrice":200.00,"roomIds":[10]}]""",
                        ),
                ),
        )

        val result = client.list(ids = null, userId = 5, page = 0, size = 20)

        assertEquals(1, result.size)
        assertEquals(setOf(10), result[0].roomIds)
    }

    @Test
    fun createSendsRequestBodyShapeBookingControllerExpectsAndDecodesResponse() {
        wireMock.stubFor(
            post(urlPathEqualTo("/internal/bookings"))
                .withRequestBody(
                    equalToJson(
                        """{"userId":5,"checkInDate":"2026-08-01","checkOutDate":"2026-08-03","guestsCount":2,"roomIds":[10]}""",
                    ),
                )
                .willReturn(
                    aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(
                            """{"id":9,"userId":5,"checkInDate":"2026-08-01","checkOutDate":"2026-08-03",""" +
                                """"status":"PENDING","guestsCount":2,"createdAt":"2026-07-01T10:00:00",""" +
                                """"totalPrice":200.00,"roomIds":[10]}""",
                        ),
                ),
        )

        val result =
            client.create(
                CreateBookingRequest(
                    userId = 5,
                    checkInDate = java.time.LocalDate.of(2026, 8, 1),
                    checkOutDate = java.time.LocalDate.of(2026, 8, 3),
                    guestsCount = 2,
                    roomIds = setOf(10),
                ),
            )

        assertEquals(9, result.id)
        verify(postRequestedFor(urlPathEqualTo("/internal/bookings")))
    }

    @Test
    fun updateStatusSendsPatchToStatusPath() {
        wireMock.stubFor(
            patch(urlPathEqualTo("/internal/bookings/9/status"))
                .withRequestBody(equalToJson("""{"status":"CONFIRMED"}"""))
                .willReturn(
                    aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(
                            """{"id":9,"userId":5,"checkInDate":"2026-08-01","checkOutDate":"2026-08-03",""" +
                                """"status":"CONFIRMED","guestsCount":2,"createdAt":"2026-07-01T10:00:00",""" +
                                """"totalPrice":200.00,"roomIds":[10]}""",
                        ),
                ),
        )

        val result = client.updateStatus(9, BookingStatusUpdateRequest(com.team1.project_lab_backend.booking.models.BookingStatus.CONFIRMED))

        assertEquals(com.team1.project_lab_backend.booking.models.BookingStatus.CONFIRMED, result.status)
        verify(patchRequestedFor(urlPathEqualTo("/internal/bookings/9/status")))
    }

    @Test
    fun deleteSendsRequestingUserIdAsQueryParam() {
        wireMock.stubFor(
            delete(urlPathEqualTo("/internal/bookings/9"))
                .withQueryParam("requestingUserId", equalTo("5"))
                .willReturn(aResponse().withStatus(204)),
        )

        client.delete(9, 5)

        verify(deleteRequestedFor(urlPathEqualTo("/internal/bookings/9")))
    }

    @Test
    fun deleteReturns403AsFeignExceptionForbiddenWhenNotOwner() {
        wireMock.stubFor(
            delete(urlPathEqualTo("/internal/bookings/9"))
                .willReturn(aResponse().withStatus(403)),
        )

        assertThrows(FeignException.Forbidden::class.java) {
            client.delete(9, 2)
        }
    }

    @Test
    fun getReturns404AsFeignExceptionNotFoundWhenMissing() {
        wireMock.stubFor(
            get(urlPathEqualTo("/internal/bookings/99"))
                .willReturn(aResponse().withStatus(404)),
        )

        assertThrows(FeignException.NotFound::class.java) {
            client.get(99)
        }
    }

    @Test
    fun hasCompletedBookingForStaySendsQueryParamsAndDecodesBoolean() {
        wireMock.stubFor(
            get(urlPathEqualTo("/internal/bookings/completed-for-stay"))
                .withQueryParam("userId", equalTo("5"))
                .withQueryParam("stayId", equalTo("2"))
                .willReturn(
                    aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("true"),
                ),
        )

        val result = client.hasCompletedBookingForStay(5, 2)

        assertEquals(true, result)
    }
}
