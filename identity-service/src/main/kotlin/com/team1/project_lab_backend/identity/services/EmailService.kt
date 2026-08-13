package com.team1.project_lab_backend.identity.services

import org.slf4j.LoggerFactory
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
) {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    // Failures here must never fail signup itself — SMTP delivery is best-effort.
    fun sendWelcomeEmail(
        to: String,
        name: String,
    ) {
        val message =
            SimpleMailMessage().apply {
                setTo(to)
                subject = "Welcome to Project Lab"
                text = "Hi $name,\n\nThanks for signing up!"
            }
        try {
            mailSender.send(message)
        } catch (e: MailException) {
            logger.warn("Failed to send welcome email to {}", to, e)
        }
    }

    fun sendPasswordResetEmail(
        to: String,
        name: String,
        resetLink: String,
    ) {
        val message =
            SimpleMailMessage().apply {
                setTo(to)
                subject = "Reset your Project Lab password"
                text = "Hi $name,\n\nUse this link to reset your password:\n$resetLink\n\n" +
                    "This link expires in 1 hour. If you didn't request this, you can ignore this email."
            }
        try {
            mailSender.send(message)
        } catch (e: MailException) {
            logger.warn("Failed to send password reset email to {}", to, e)
        }
    }

    fun sendAccountConfirmationEmail(
        to: String,
        name: String,
        confirmLink: String,
    ) {
        val message =
            SimpleMailMessage().apply {
                setTo(to)
                subject = "Confirm your Project Lab account"
                text = "Hi $name,\n\nPlease confirm your account by visiting this link:\n$confirmLink\n\n" +
                    "This link expires in 24 hours."
            }
        try {
            mailSender.send(message)
        } catch (e: MailException) {
            logger.warn("Failed to send account confirmation email to {}", to, e)
        }
    }
}
