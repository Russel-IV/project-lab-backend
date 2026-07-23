package com.team1.project_lab_backend.media.services

import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

/**
 * Stock JDK ImageIO can't decode webp/avif — [resizeAll] returns an empty map for those
 * (and any other undecodable input) rather than throwing, so callers can fall back to the
 * full-size image instead of failing the upload.
 */
object ImageResizer {
    val TARGET_WIDTHS = listOf(1024, 512, 248)

    /**
     * Resizes to every width in [widths] that is narrower than the source, keyed by that width.
     * Widths at or above the source's own width are skipped — that variant would be no smaller
     * than the original, which is already stored separately.
     */
    fun resizeAll(
        input: InputStream,
        formatName: String,
        widths: List<Int> = TARGET_WIDTHS,
    ): Map<Int, ByteArray> {
        val source = ImageIO.read(input) ?: return emptyMap()
        return widths.filter { it < source.width }
            .associateWith { width -> resizeTo(source, width, formatName) }
            .filterValues { it != null }
            .mapValues { it.value!! }
    }

    private fun resizeTo(
        source: BufferedImage,
        targetWidth: Int,
        formatName: String,
    ): ByteArray? {
        val targetHeight = (source.height.toDouble() * targetWidth / source.width).toInt().coerceAtLeast(1)
        val scaled = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB)
        scaled.createGraphics().apply {
            setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            drawImage(source, 0, 0, targetWidth, targetHeight, null)
            dispose()
        }
        return encode(scaled, formatName)
    }

    private fun encode(
        image: BufferedImage,
        formatName: String,
    ): ByteArray? {
        val toWrite =
            if (formatName.equals("jpg", ignoreCase = true) || formatName.equals("jpeg", ignoreCase = true)) {
                flattenToRgb(image)
            } else {
                image
            }
        val out = ByteArrayOutputStream()
        val written = ImageIO.write(toWrite, formatName, out)
        return if (written) out.toByteArray() else null
    }

    private fun flattenToRgb(image: BufferedImage): BufferedImage {
        val rgb = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
        rgb.createGraphics().apply {
            color = Color.WHITE
            fillRect(0, 0, image.width, image.height)
            drawImage(image, 0, 0, null)
            dispose()
        }
        return rgb
    }
}
