package com.team1.project_lab_backend.media.services

import dev.matrixlab.webp4j.WebPCodec
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.IOException
import java.io.InputStream
import javax.imageio.ImageIO

/**
 * Stock JDK ImageIO can't decode webp/avif input, and native WebP encoding may be
 * unavailable on an unsupported platform (see [WebPCodec.isAvailable]) — [convertToWebp]
 * and [resizeAll] return null/an empty map for either case rather than throwing, so
 * callers can fall back to storing the original file untouched instead of failing the
 * upload.
 */
object ImageResizer {
    val TARGET_WIDTHS = listOf(1024, 768, 512, 248)
    private const val QUALITY = 80f

    /**
     * Decodes and re-encodes the full-size image as WebP with no resizing — used to
     * convert the stored original. Returns null if the input can't be decoded or WebP
     * encoding isn't available on this platform.
     */
    fun convertToWebp(input: InputStream): ByteArray? {
        if (!WebPCodec.isAvailable()) return null
        val source = ImageIO.read(input) ?: return null
        return toWebp(source)
    }

    /**
     * Resizes to every width in [widths] that is narrower than the source, keyed by that width.
     * Widths at or above the source's own width are skipped — that variant would be no smaller
     * than the original, which is already stored separately.
     */
    fun resizeAll(
        input: InputStream,
        widths: List<Int> = TARGET_WIDTHS,
    ): Map<Int, ByteArray> {
        if (!WebPCodec.isAvailable()) return emptyMap()
        val source = ImageIO.read(input) ?: return emptyMap()
        return widths.filter { it < source.width }
            .associateWith { width -> resizeTo(source, width) }
            .filterValues { it != null }
            .mapValues { it.value!! }
    }

    private fun resizeTo(
        source: BufferedImage,
        targetWidth: Int,
    ): ByteArray? {
        val targetHeight = (source.height.toDouble() * targetWidth / source.width).toInt().coerceAtLeast(1)
        val scaled = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB)
        scaled.createGraphics().apply {
            setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            drawImage(source, 0, 0, targetWidth, targetHeight, null)
            dispose()
        }
        return toWebp(scaled)
    }

    private fun toWebp(image: BufferedImage): ByteArray? =
        try {
            WebPCodec.encodeImage(image, QUALITY)
        } catch (_: IOException) {
            null
        }
}
