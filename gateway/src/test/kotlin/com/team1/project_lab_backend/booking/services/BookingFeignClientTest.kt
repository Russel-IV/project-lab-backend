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
import com.team1.project_lab_backend.util.assertThrowsSuspend
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

/**
 * Exercises the real BookingFeignClient class (request paths/params/bodies, response
 * decoding) against a stubbed booking-service, mirroring ReviewFeignClientTest/
 * MediaFeignClientTest's approach (docs/adr/0008, docs/adr/0025) — the request shape
 * drifting out of sync with booking-service's real BookingController is the one thing
 * unit tests on either side, alone, wouldn't catch. Constructs the client with a plain
 * WebClient pointed at WireMock's dynamic port, bypassing the @LoadBalanced/Eureka
 * qualified bean production uses (docs/adr/0025, WebClientConfig.kt).
 */
class BookingFeignClientTest {
    private lateinit var wireMock: WireMockServer
    private lateinit var client: BookingFeignClient

    @BeforeEach
    fun setUp() {
        wireMock = WireMockServer(wireMockConfig().dynamicPort())
        wireMock.start()
        configureFor("localhost", wireMock.port())

        client = BookingFeignClient(WebClient.builder().baseUrl("http://localhost:${wireMock.port()}").build())
    }

    @AfterEach
    fun tearDown() {
        wireMock.stop()
    }

    @Test
    fun listByUserSendsExpectedQueryParamsAndDecodesBookings() =
        runTest {
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
    fun createSendsRequestBodyShapeBookingControllerExpectsAndDecodesResponse() =
        runTest {
            wireMock.stubFor(
                post(urlPathEqualTo("/internal/bookings"))
                    .withRequestBody(
                        equalToJson(
                            """{"userId":5,"checkInDate":"2026-08-01","checkOutDate":"2026-08-03","guestsCount":2,""" +
                                """"roomIds":[10],"paymentIntentId":"pi_mock_1"}""",
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
                        paymentIntentId = "pi_mock_1",
                    ),
                )

            assertEquals(9, result.id)
            verify(postRequestedFor(urlPathEqualTo("/internal/bookings")))
        }

    @Test
    fun createPaymentIntentSendsRequestBodyShapePaymentIntentControllerExpectsAndDecodesResponse() =
        runTest {
            wireMock.stubFor(
                post(urlPathEqualTo("/internal/payment-intents"))
                    .withRequestBody(
                        equalToJson(
                            """{"userId":5,"roomIds":[10],"checkInDate":"2026-08-01","checkOutDate":"2026-08-03",""" +
                                """"guestsCount":2,"idempotencyKey":"key-1"}""",
                        ),
                    )
                    .willReturn(
                        aResponse().withStatus(201).withHeader("Content-Type", "application/json")
                            .withBody(
                                """{"paymentIntentId":"pi_mock_1","clientSecret":"pi_mock_1_secret_2",""" +
                                    """"amount":20000,"currency":"usd"}""",
                            ),
                    ),
            )

            val result =
                client.createPaymentIntent(
                    CreatePaymentIntentRequest(
                        userId = 5,
                        roomIds = setOf(10),
                        checkInDate = java.time.LocalDate.of(2026, 8, 1),
                        checkOutDate = java.time.LocalDate.of(2026, 8, 3),
                        guestsCount = 2,
                        idempotencyKey = "key-1",
                    ),
                )

            assertEquals("pi_mock_1", result.paymentIntentId)
            assertEquals(20000, result.amount)
            verify(postRequestedFor(urlPathEqualTo("/internal/payment-intents")))
        }

    @Test
    fun updateStatusSendsPatchToStatusPath() =
        runTest {
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

            val result =
                client.updateStatus(
                    9,
                    BookingStatusUpdateRequest(com.team1.project_lab_backend.booking.models.BookingStatus.CONFIRMED),
                )

            assertEquals(com.team1.project_lab_backend.booking.models.BookingStatus.CONFIRMED, result.status)
            verify(patchRequestedFor(urlPathEqualTo("/internal/bookings/9/status")))
        }

    @Test
    fun deleteSendsRequestingUserIdAsQueryParam() =
        runTest {
            wireMock.stubFor(
                delete(urlPathEqualTo("/internal/bookings/9"))
                    .withQueryParam("requestingUserId", equalTo("5"))
                    .willReturn(aResponse().withStatus(204)),
            )

            client.delete(9, 5)

            verify(deleteRequestedFor(urlPathEqualTo("/internal/bookings/9")))
        }

    @Test
    fun deleteReturns403AsWebClientResponseExceptionForbiddenWhenNotOwner() =
        runTest {
            wireMock.stubFor(
                delete(urlPathEqualTo("/internal/bookings/9"))
                    .willReturn(aResponse().withStatus(403)),
            )

            assertThrowsSuspend<WebClientResponseException.Forbidden> { client.delete(9, 2) }
        }

    @Test
    fun getReturns404AsWebClientResponseExceptionNotFoundWhenMissing() =
        runTest {
            wireMock.stubFor(
                get(urlPathEqualTo("/internal/bookings/99"))
                    .willReturn(aResponse().withStatus(404)),
            )

            assertThrowsSuspend<WebClientResponseException.NotFound> { client.get(99) }
        }

    @Test
    fun hasCompletedBookingForStaySendsQueryParamsAndDecodesBoolean() =
        runTest {
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
