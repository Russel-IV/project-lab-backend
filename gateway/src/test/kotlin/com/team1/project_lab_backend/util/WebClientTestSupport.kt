package com.team1.project_lab_backend.util

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.nio.file.Path

/**
 * docs/adr/0025: replaces raw `FeignException.NotFound::class.java`/`FeignException
 * .BadRequest(...)` construction in tests. WebClientResponseException's per-status
 * subclasses (BadRequest, NotFound, Forbidden, ...) only expose package-private
 * constructors — Mockito's `thenThrow(Class)` overload can't instantiate them via
 * reflection (no accessible no-arg constructor), so tests must always build a real
 * instance via the public `create(...)` factory and use `thenThrow(instance)`.
 */
fun webClientException(
    status: Int,
    body: String = "{}",
): WebClientResponseException =
    WebClientResponseException.create(status, "", HttpHeaders.EMPTY, body.toByteArray(StandardCharsets.UTF_8), StandardCharsets.UTF_8)

/**
 * docs/adr/0025: replaces MockMultipartFile (servlet-only) in upload tests. Only what
 * StayPictureService/RoomPictureService/ProfileService actually read (filename, bytes)
 * is implemented — Mockito matches by reference identity here, not content, so a
 * minimal fake is enough as long as the same instance is reused between stub and call.
 */
class FakeFilePart(
    private val partName: String,
    private val fileName: String,
    private val bytes: ByteArray = ByteArray(0),
) : FilePart {
    override fun filename(): String = fileName

    override fun name(): String = partName

    override fun headers(): HttpHeaders = HttpHeaders.EMPTY

    override fun content(): Flux<DataBuffer> = Flux.just(DefaultDataBufferFactory().wrap(bytes))

    override fun transferTo(dest: Path): Mono<Void> = Mono.empty()
}
