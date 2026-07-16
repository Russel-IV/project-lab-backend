package com.team1.project_lab_backend.review.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
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
import feign.Feign
import feign.FeignException
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.cloud.openfeign.support.SpringMvcContract

/**
 * Exercises the real ReviewFeignClient interface (request paths/params/bodies, response
 * decoding) against a stubbed review-service, per Phase 2's verification requirement —
 * this is the one thing that would silently break if ReviewFeignClient's annotations
 * ever drift out of sync with review-service's actual ReviewController, since there's
 * no shared library between the two modules to keep them compile-time-linked.
 *
 * Built with plain Feign (feign-jackson) rather than Spring Cloud's auto-configured
 * client, matching this repo's no-Spring-context testing convention — the annotation
 * contract being tested (paths, params, HTTP methods, JSON shapes) is identical either
 * way, since Spring Cloud OpenFeign just wraps the same underlying Feign proxy.
 */
class ReviewFeignClientTest {
    private lateinit var wireMock: WireMockServer
    private lateinit var client: ReviewFeignClient

    @BeforeEach
    fun setUp() {
        wireMock = WireMockServer(wireMockConfig().dynamicPort())
        wireMock.start()
        configureFor("localhost", wireMock.port())

        val mapper = ObjectMapper().registerKotlinModule()
        client =
            Feign.builder()
                .client(OkHttpClient())
                .contract(SpringMvcContract())
                .encoder(JacksonEncoder(mapper))
                .decoder(JacksonDecoder(mapper))
                .target(ReviewFeignClient::class.java, "http://localhost:${wireMock.port()}")
    }

    @AfterEach
    fun tearDown() {
        wireMock.stop()
    }

    @Test
    fun listByStaySendsExpectedQueryParamsAndDecodesReviews() {
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
    fun createSendsRequestBodyShapeReviewControllerExpectsAndDecodesResponse() {
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
    fun updateSendsPatchWithRequestingUserId() {
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
    fun deleteSendsRequestingUserIdAsQueryParam() {
        wireMock.stubFor(
            delete(urlPathEqualTo("/internal/reviews/9"))
                .withQueryParam("requestingUserId", equalTo("5"))
                .willReturn(aResponse().withStatus(204)),
        )

        client.delete(9, 5)

        verify(deleteRequestedFor(urlPathEqualTo("/internal/reviews/9")))
    }

    @Test
    fun mineReturns404AsFeignExceptionNotFoundWhenNoneExists() {
        wireMock.stubFor(
            get(urlPathEqualTo("/internal/reviews/mine"))
                .willReturn(aResponse().withStatus(404)),
        )

        assertThrows(FeignException.NotFound::class.java) {
            client.mine(userId = 5, stayId = 2)
        }
    }

    @Test
    fun createReturns409AsFeignExceptionConflictWhenAlreadyReviewed() {
        wireMock.stubFor(
            post(urlPathEqualTo("/internal/reviews"))
                .willReturn(aResponse().withStatus(409)),
        )

        assertThrows(FeignException.Conflict::class.java) {
            client.create(CreateReviewRequest(text = "x", userId = 5, stayId = 2, rating = 3))
        }
    }
}
