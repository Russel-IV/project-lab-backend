package com.team1.project_lab_backend.identity.services

import jakarta.mail.MessagingException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.time.Year

private const val FRUI_ORANGE = "#e8660d"
private const val FRUI_CREAM = "#fff8f3"
private const val FRUI_BLUE = "#121529"
private const val FRUI_WHITE = "#ffffff"

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
            text = "Hi $name, reset your Frui password here: $resetLink (expires in 1 hour). " +
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
                        <span style="color:$FRUI_ORANGE;font-size:22px;font-weight:bold;">Frui</span>
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
