package com.team1.project_lab_backend.review.services

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
 * Exercises the real ReviewFeignClient class (request paths/params/bodies, response
 * decoding) against a stubbed review-service, per Phase 2's verification requirement —
 * this is the one thing that would silently break if ReviewFeignClient's requests ever
 * drift out of sync with review-service's actual ReviewController, since there's no
 * shared library between the two modules to keep them compile-time-linked.
 *
 * Built with a plain WebClient pointed at WireMock's dynamic port (docs/adr/0025),
 * bypassing the @LoadBalanced/Eureka qualified bean production uses.
 */
class ReviewFeignClientTest {
    private lateinit var wireMock: WireMockServer
    private lateinit var client: ReviewFeignClient

    @BeforeEach
    fun setUp() {
        wireMock = WireMockServer(wireMockConfig().dynamicPort())
        wireMock.start()
        configureFor("localhost", wireMock.port())

        client = ReviewFeignClient(WebClient.builder().baseUrl("http://localhost:${wireMock.port()}").build())
    }

    @AfterEach
    fun tearDown() {
        wireMock.stop()
    }

    @Test
    fun listByStaySendsExpectedQueryParamsAndDecodesReviews() =
        runTest {
            wireMock.stubFor(
                get(urlPathEqualTo("/internal/reviews"))
                    .withQueryParam("stayId", equalTo("2"))
                    .withQueryParam("page", equalTo("0"))
                    .withQueryParam("size", equalTo("20"))
                    .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                            .withBody("""[{"id":1,"text":"Great","userId":5,"stayId":2,"rating":4}]"""),
                    ),
            )

            val result = client.list(stayId = 2, userId = null, ids = null, page = 0, size = 20)

            assertEquals(1, result.size)
            assertEquals("Great", result[0].text)
            assertEquals(4, result[0].rating)
        }

    @Test
    fun createSendsRequestBodyShapeReviewControllerExpectsAndDecodesResponse() =
        runTest {
            wireMock.stubFor(
                post(urlPathEqualTo("/internal/reviews"))
                    .withRequestBody(equalToJson("""{"text":"Nice stay","userId":5,"stayId":2,"rating":5}"""))
                    .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                            .withBody("""{"id":9,"text":"Nice stay","userId":5,"stayId":2,"rating":5}"""),
                    ),
            )

            val result = client.create(CreateReviewRequest(text = "Nice stay", userId = 5, stayId = 2, rating = 5))

            assertEquals(9, result.id)
            verify(postRequestedFor(urlPathEqualTo("/internal/reviews")))
        }

    @Test
    fun updateSendsPatchWithRequestingUserId() =
        runTest {
            wireMock.stubFor(
                patch(urlPathEqualTo("/internal/reviews/9"))
                    .withRequestBody(equalToJson("""{"text":"Updated","stayId":2,"rating":3,"requestingUserId":5}"""))
                    .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                            .withBody("""{"id":9,"text":"Updated","userId":5,"stayId":2,"rating":3}"""),
                    ),
            )

            val result = client.update(9, UpdateReviewRequest(text = "Updated", stayId = 2, rating = 3, requestingUserId = 5))

            assertEquals("Updated", result.text)
            verify(patchRequestedFor(urlPathEqualTo("/internal/reviews/9")))
        }

    @Test
    fun deleteSendsRequestingUserIdAsQueryParam() =
        runTest {
            wireMock.stubFor(
                delete(urlPathEqualTo("/internal/reviews/9"))
                    .withQueryParam("requestingUserId", equalTo("5"))
                    .willReturn(aResponse().withStatus(204)),
            )

            client.delete(9, 5)

            verify(deleteRequestedFor(urlPathEqualTo("/internal/reviews/9")))
        }

    @Test
    fun mineReturns404AsWebClientResponseExceptionNotFoundWhenNoneExists() =
        runTest {
            wireMock.stubFor(
                get(urlPathEqualTo("/internal/reviews/mine"))
                    .willReturn(aResponse().withStatus(404)),
            )

            assertThrowsSuspend<WebClientResponseException.NotFound> { client.mine(userId = 5, stayId = 2) }
        }

    @Test
    fun createReturns409AsWebClientResponseExceptionConflictWhenAlreadyReviewed() =
        runTest {
            wireMock.stubFor(
                post(urlPathEqualTo("/internal/reviews"))
                    .willReturn(aResponse().withStatus(409)),
            )

            assertThrowsSuspend<WebClientResponseException.Conflict> {
                client.create(CreateReviewRequest(text = "x", userId = 5, stayId = 2, rating = 3))
            }
        }
}
