package com.team1.project_lab_backend.media.services

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
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.fasterxml.jackson.databind.ObjectMapper
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
 * Exercises the JSON-shaped MediaFeignClient methods (list/update/delete) against a
 * stubbed media-service, mirroring ReviewFeignClientTest's approach (docs/adr/0008) —
 * the annotation contract (paths, params, HTTP methods, JSON shapes) drifting out of
 * sync with media-service's real MediaController is the one thing unit tests on either
 * side, alone, wouldn't catch.
 *
 * The multipart `upload()` method isn't contract-tested here: Spring Cloud OpenFeign's
 * multipart forwarding relies on feign-form-spring's SpringFormEncoder, which needs
 * Spring's actual HttpMessageConverters wiring to behave correctly — a plain
 * hand-built Feign client (as used here and in ReviewFeignClientTest, matching this
 * repo's no-Spring-context test convention) can't reproduce that faithfully. Upload is
 * covered by live docker-compose E2E verification instead (see Phase 3 plan outcome).
 */
class MediaFeignClientTest {

    private lateinit var wireMock: WireMockServer
    private lateinit var client: MediaFeignClient

    @BeforeEach
    fun setUp() {
        wireMock = WireMockServer(wireMockConfig().dynamicPort())
        wireMock.start()
        configureFor("localhost", wireMock.port())

        val mapper = ObjectMapper().registerKotlinModule()
        client = Feign.builder()
            .client(OkHttpClient())
            .contract(SpringMvcContract())
            .encoder(JacksonEncoder(mapper))
            .decoder(JacksonDecoder(mapper))
            .target(MediaFeignClient::class.java, "http://localhost:${wireMock.port()}")
    }

    @AfterEach
    fun tearDown() {
        wireMock.stop()
    }

    @Test
    fun listForOwnerDecodesResponse() {
        wireMock.stubFor(
            get(urlPathEqualTo("/api/v1/media/STAY/2"))
                .willReturn(
                    aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("""[{"id":1,"ownerType":"STAY","ownerId":2,"url":"http://localhost:8080/uploads/stays/2/a.jpg","caption":null,"isPrimary":true,"displayOrder":0}]"""),
                ),
        )

        val result = client.listForOwner("STAY", 2)

        assertEquals(1, result.size)
        assertEquals(2, result[0].ownerId)
        assertEquals(true, result[0].isPrimary)
    }

    @Test
    fun listForOwnersSendsBulkQueryParams() {
        wireMock.stubFor(
            get(urlPathEqualTo("/api/v1/media"))
                .withQueryParam("ownerType", equalTo("ROOM"))
                .willReturn(
                    aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("""[{"id":1,"ownerType":"ROOM","ownerId":5,"url":"http://localhost:8080/uploads/rooms/5/a.jpg","caption":null,"isPrimary":false,"displayOrder":0}]"""),
                ),
        )

        val result = client.listForOwners("ROOM", listOf(5, 6))

        assertEquals(1, result.size)
        assertEquals(5, result[0].ownerId)
    }

    @Test
    fun updateSendsPatchToOwnerScopedPath() {
        wireMock.stubFor(
            patch(urlPathEqualTo("/api/v1/media/STAY/2/1"))
                .withRequestBody(equalToJson("""{"caption":"New","isPrimary":false,"displayOrder":3}"""))
                .willReturn(
                    aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("""{"id":1,"ownerType":"STAY","ownerId":2,"url":"http://localhost:8080/uploads/stays/2/a.jpg","caption":"New","isPrimary":false,"displayOrder":3}"""),
                ),
        )

        val result = client.update("STAY", 2, 1, UpdateMediaRequest(caption = "New", isPrimary = false, displayOrder = 3))

        assertEquals("New", result.caption)
        verify(patchRequestedFor(urlPathEqualTo("/api/v1/media/STAY/2/1")))
    }

    @Test
    fun deleteSendsOwnerScopedPath() {
        wireMock.stubFor(
            delete(urlPathEqualTo("/api/v1/media/ROOM/5/1"))
                .willReturn(aResponse().withStatus(204)),
        )

        client.delete("ROOM", 5, 1)

        verify(deleteRequestedFor(urlPathEqualTo("/api/v1/media/ROOM/5/1")))
    }

    @Test
    fun updateReturns404AsFeignExceptionNotFoundWhenIdBelongsToDifferentOwner() {
        wireMock.stubFor(
            patch(urlPathEqualTo("/api/v1/media/STAY/999/1"))
                .willReturn(aResponse().withStatus(404)),
        )

        assertThrows(FeignException.NotFound::class.java) {
            client.update("STAY", 999, 1, UpdateMediaRequest(null, false, 0))
        }
    }
}
