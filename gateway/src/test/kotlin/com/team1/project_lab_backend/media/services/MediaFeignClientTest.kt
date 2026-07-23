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
import com.team1.project_lab_backend.util.assertThrowsSuspend
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

/**
 * Exercises the JSON-shaped MediaFeignClient methods (list/update/delete) against a
 * stubbed media-service, mirroring ReviewFeignClientTest's approach (docs/adr/0008,
 * docs/adr/0025) — the request shape (paths, params, HTTP methods, JSON shapes)
 * drifting out of sync with media-service's real MediaController is the one thing unit
 * tests on either side, alone, wouldn't catch.
 *
 * The multipart `upload()` method isn't contract-tested here, same as before the
 * WebFlux migration: it needs a real FilePart streaming through Spring's multipart
 * codecs to behave faithfully, which a plain WebClient-against-WireMock unit test
 * can't reproduce. Upload is covered by live docker-compose E2E verification instead.
 */
class MediaFeignClientTest {
    private lateinit var wireMock: WireMockServer
    private lateinit var client: MediaFeignClient

    @BeforeEach
    fun setUp() {
        wireMock = WireMockServer(wireMockConfig().dynamicPort())
        wireMock.start()
        configureFor("localhost", wireMock.port())

        client = MediaFeignClient(WebClient.builder().baseUrl("http://localhost:${wireMock.port()}").build())
    }

    @AfterEach
    fun tearDown() {
        wireMock.stop()
    }

    @Test
    fun listForOwnerDecodesResponse() =
        runTest {
            wireMock.stubFor(
                get(urlPathEqualTo("/api/v1/media/STAY/2"))
                    .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                            .withBody(
                                """[{"id":1,"ownerType":"STAY","ownerId":2,"url":"http://localhost:8080/uploads/stays/2/a.jpg",""" +
                                    """"thumbnailUrl":"http://localhost:8080/uploads/stays/2/a.jpg",""" +
                                    """"caption":null,"isPrimary":true,"displayOrder":0}]""",
                            ),
                    ),
            )

            val result = client.listForOwner("STAY", 2)

            assertEquals(1, result.size)
            assertEquals(2, result[0].ownerId)
            assertEquals(true, result[0].isPrimary)
        }

    @Test
    fun listForOwnersSendsBulkQueryParams() =
        runTest {
            wireMock.stubFor(
                get(urlPathEqualTo("/api/v1/media"))
                    .withQueryParam("ownerType", equalTo("ROOM"))
                    .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                            .withBody(
                                """[{"id":1,"ownerType":"ROOM","ownerId":5,"url":"http://localhost:8080/uploads/rooms/5/a.jpg",""" +
                                    """"thumbnailUrl":"http://localhost:8080/uploads/rooms/5/a.jpg",""" +
                                    """"caption":null,"isPrimary":false,"displayOrder":0}]""",
                            ),
                    ),
            )

            val result = client.listForOwners("ROOM", listOf(5, 6))

            assertEquals(1, result.size)
            assertEquals(5, result[0].ownerId)
        }

    @Test
    fun updateSendsPatchToOwnerScopedPath() =
        runTest {
            wireMock.stubFor(
                patch(urlPathEqualTo("/api/v1/media/STAY/2/1"))
                    .withRequestBody(equalToJson("""{"caption":"New","isPrimary":false,"displayOrder":3}"""))
                    .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                            .withBody(
                                """{"id":1,"ownerType":"STAY","ownerId":2,"url":"http://localhost:8080/uploads/stays/2/a.jpg",""" +
                                    """"thumbnailUrl":"http://localhost:8080/uploads/stays/2/a.jpg",""" +
                                    """"caption":"New","isPrimary":false,"displayOrder":3}""",
                            ),
                    ),
            )

            val result = client.update("STAY", 2, 1, UpdateMediaRequest(caption = "New", isPrimary = false, displayOrder = 3))

            assertEquals("New", result.caption)
            verify(patchRequestedFor(urlPathEqualTo("/api/v1/media/STAY/2/1")))
        }

    @Test
    fun deleteSendsOwnerScopedPath() =
        runTest {
            wireMock.stubFor(
                delete(urlPathEqualTo("/api/v1/media/ROOM/5/1"))
                    .willReturn(aResponse().withStatus(204)),
            )

            client.delete("ROOM", 5, 1)

            verify(deleteRequestedFor(urlPathEqualTo("/api/v1/media/ROOM/5/1")))
        }

    @Test
    fun updateReturns404AsWebClientResponseExceptionNotFoundWhenIdBelongsToDifferentOwner() =
        runTest {
            wireMock.stubFor(
                patch(urlPathEqualTo("/api/v1/media/STAY/999/1"))
                    .willReturn(aResponse().withStatus(404)),
            )

            assertThrowsSuspend<WebClientResponseException.NotFound> {
                client.update("STAY", 999, 1, UpdateMediaRequest(null, false, 0))
            }
        }
}
