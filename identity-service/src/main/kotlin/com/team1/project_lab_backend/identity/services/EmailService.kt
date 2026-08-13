package com.team1.project_lab_backend.identity.services

import jakarta.mail.MessagingException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Year
import java.time.format.DateTimeFormatter

private const val FRUI_ORANGE = "#e8660d"
private const val FRUI_CREAM = "#fff8f3"
private const val FRUI_BLUE = "#121529"
private const val FRUI_WHITE = "#ffffff"

// The Frui "swoosh" mark, recolored per usage site (the source asset is solid
// #121529 — invisible against the header's own $FRUI_BLUE background) since email
// clients that do render inline SVG apply the fill wherever it's placed.
// Split into sub-90-char chunks purely to satisfy ktlint's max-line-length rule —
// concatenation reproduces the source path data exactly, unmodified.
private const val FRUI_LOGO_PATH_1 =
    "M.06,189.76c7.36-.17,17.36-1.89,29.19-3.91,10.85-1.85,23.33-3.78,35.15-8.77,10.43-4.4," +
        "19.98-10.01,21.19-10.72,9.92-5.86,16-10.72,26.55-19.06,0,0,6.93-5.47,48-37.62,10.33-8.09," +
        "18.28-14.24,22.64-17.62,11.81-9.13,14.15-10.86,22.13-17.02,21.34-16.47,25.78-20.48," +
        "45.28-35.32,12.05-9.17,17.97-13.44,26.3-18.3,4.88-2.85,12.19-7.07,22.47-11.15,14.17-5.62," +
        "25.9-7.76,30.55-8.51,9.34-1.51,14.07-1.28,40.51-1.19,7.73.03,19.16.05,33.19,0-12.48," +
        "10.5-24.96,20.99-37.45,31.49-12.65-.19-23.5-.12-32.26,0-2.45.03-9.08.16-14.47,2.3-3.76," +
        "1.49-7.72,3.7-7.72,3.7-1.12.63-2.04,1.18-2.67,1.57-10.95,9.08-21.9,18.16-32.85,27.23-1.73," +
        "1.94-1.42,3.82-.51,5.45.94,1.69,2.8,2.26,3.26,2.35.86.16,3.72.43,4.55.44l74.4-.41c-9.28," +
        "7.4-18.55,14.81-27.83,22.21-47.09,8.85-80.54,15-91.66,16.6-2.2.32-9.2,1.27-18.13," +
        "4.09-2.28.72-4.34,1.45-6.16,2.15-3.25,1.24-6.3,2.94-9.06,5.06-33.14,25.43-66.28," +
        "50.86-99.42,76.28-33.36-3.83-71.83-7.49-105.19-11.32"

private const val FRUI_LOGO_PATH_2 =
    "M169.91,91.51l-38.81,29.78-101.87,16.09,81.55-31.56c.63-.24,1.28-.44,1.94-.61l57.19-13.7Z"

private fun friuLogoIcon(fill: String): String =
    """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 404.58 201.6" width="44" height="22" """ +
        """style="vertical-align:middle;display:inline-block;" fill="$fill">""" +
        """<path d="$FRUI_LOGO_PATH_1"/><path d="$FRUI_LOGO_PATH_2"/></svg>"""

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @Value("\${app.frontend-url}") private val frontendUrl: String,
) {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    fun sendWelcomeEmail(
        to: String,
        name: String,
    ) {
        val html =
            emailTemplate(
                heading = "Welcome to Frui, $name!",
                bodyHtml =
                    """
                    <p style="margin:0 0 12px;">Thanks for joining Frui — we're glad you're here.</p>
                    <p style="margin:0;">Browse thousands of stays around the world and book your next trip in minutes.</p>
                    """.trimIndent(),
                ctaText = "Start exploring",
                ctaLink = frontendUrl,
            )
        send(
            to = to,
            subject = "Welcome to Frui",
            html = html,
            text = "Welcome to Frui, $name! Thanks for joining — start exploring stays at $frontendUrl",
        )
    }

    fun sendPasswordResetEmail(
        to: String,
        name: String,
        resetLink: String,
    ) {
        val html =
            emailTemplate(
                heading = "Reset your password",
                bodyHtml =
                    """
                    <p style="margin:0 0 12px;">Hi $name,</p>
                    <p style="margin:0 0 12px;">We received a request to reset your Frui password. Click the button below to choose a new one — this link expires in 1 hour.</p>
                    <p style="margin:0;">If you didn't request this, you can safely ignore this email and your password will stay the same.</p>
                    """.trimIndent(),
                ctaText = "Reset password",
                ctaLink = resetLink,
            )
        send(
            to = to,
            subject = "Reset your Frui password",
            html = html,
            text =
                "Hi $name, reset your Frui password here: $resetLink (expires in 1 hour). " +
                    "If you didn't request this, you can ignore this email.",
        )
    }

    fun sendAccountConfirmationEmail(
        to: String,
        name: String,
        confirmLink: String,
    ) {
        val html =
            emailTemplate(
                heading = "Confirm your email",
                bodyHtml =
                    """
                    <p style="margin:0 0 12px;">Hi $name,</p>
                    <p style="margin:0;">Please confirm this is your email address to finish setting up your Frui account — this link expires in 24 hours.</p>
                    """.trimIndent(),
                ctaText = "Confirm your email",
                ctaLink = confirmLink,
            )
        send(
            to = to,
            subject = "Confirm your Frui account",
            html = html,
            text = "Hi $name, confirm your Frui account here: $confirmLink (expires in 24 hours).",
        )
    }

    fun sendBookingConfirmationEmail(
        to: String,
        name: String,
        stayName: String,
        cityCountry: String,
        checkInDate: LocalDate,
        checkOutDate: LocalDate,
        totalPrice: BigDecimal,
        bookingId: Int,
    ) {
        val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        val checkIn = checkInDate.format(dateFormatter)
        val checkOut = checkOutDate.format(dateFormatter)
        val price = "$${totalPrice.setScale(2, RoundingMode.HALF_UP)}"
        val html =
            emailTemplate(
                heading = "Booking confirmed!",
                bodyHtml =
                    """
                    <p style="margin:0 0 12px;">Hi $name,</p>
                    <p style="margin:0 0 16px;">Your stay at <strong>$stayName</strong> is booked. Here are the details:</p>
                    <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="margin:0 0 16px;">
                      <tr><td style="padding:4px 0;color:#8a8a8a;">Confirmation #</td><td style="padding:4px 0;text-align:right;">$bookingId</td></tr>
                      <tr><td style="padding:4px 0;color:#8a8a8a;">Location</td><td style="padding:4px 0;text-align:right;">$cityCountry</td></tr>
                      <tr><td style="padding:4px 0;color:#8a8a8a;">Check-in</td><td style="padding:4px 0;text-align:right;">$checkIn</td></tr>
                      <tr><td style="padding:4px 0;color:#8a8a8a;">Check-out</td><td style="padding:4px 0;text-align:right;">$checkOut</td></tr>
                      <tr><td style="padding:4px 0;color:#8a8a8a;">Total paid</td><td style="padding:4px 0;text-align:right;font-weight:bold;">$price</td></tr>
                    </table>
                    <p style="margin:0;">We hope you have a great trip!</p>
                    """.trimIndent(),
                ctaText = "View your trips",
                ctaLink = frontendUrl,
            )
        send(
            to = to,
            subject = "Your Frui booking is confirmed",
            html = html,
            text =
                "Hi $name, your stay at $stayName ($cityCountry) is confirmed. " +
                    "Check-in: $checkIn, Check-out: $checkOut, Total paid: $price. Confirmation #$bookingId.",
        )
    }

    // Failures here must never fail signup/reset/confirm themselves — SMTP delivery is best-effort.
    private fun send(
        to: String,
        subject: String,
        html: String,
        text: String,
    ) {
        try {
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            helper.setTo(to)
            helper.setSubject(subject)
            // Plain-text part first, HTML second — MimeMessageHelper builds a
            // multipart/alternative so clients without HTML rendering still get something readable.
            helper.setText(text, html)
            mailSender.send(message)
        } catch (e: MailException) {
            logger.warn("Failed to send email \"{}\" to {}", subject, to, e)
        } catch (e: MessagingException) {
            logger.warn("Failed to build email \"{}\" to {}", subject, to, e)
        }
    }

    private fun emailTemplate(
        heading: String,
        bodyHtml: String,
        ctaText: String,
        ctaLink: String,
    ): String =
        """
        <!DOCTYPE html>
        <html>
          <body style="margin:0;padding:0;background-color:$FRUI_CREAM;font-family:Arial,Helvetica,sans-serif;">
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:$FRUI_CREAM;padding:32px 0;">
              <tr>
                <td align="center">
                  <table role="presentation" width="480" cellpadding="0" cellspacing="0" style="background-color:$FRUI_WHITE;border-radius:12px;overflow:hidden;">
                    <tr>
                      <td style="background-color:$FRUI_BLUE;padding:24px 32px;">
                        ${friuLogoIcon(FRUI_ORANGE)}
                        <span style="color:$FRUI_ORANGE;font-size:22px;font-weight:bold;vertical-align:middle;margin-left:8px;">Frui</span>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:32px;">
                        <h1 style="margin:0 0 16px;color:$FRUI_BLUE;font-size:20px;">$heading</h1>
                        <div style="color:#333333;font-size:14px;line-height:1.6;">$bodyHtml</div>
                        <table role="presentation" cellpadding="0" cellspacing="0" style="margin-top:24px;">
                          <tr>
                            <td style="border-radius:999px;background-color:$FRUI_ORANGE;">
                              <a href="$ctaLink" style="display:inline-block;padding:12px 28px;color:$FRUI_WHITE;font-size:14px;font-weight:bold;text-decoration:none;">$ctaText</a>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:16px 32px;background-color:$FRUI_CREAM;">
                        <p style="margin:0;color:#8a8a8a;font-size:12px;">© ${Year.now().value} Frui. All rights reserved.</p>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
          </body>
        </html>
        """.trimIndent()
}
