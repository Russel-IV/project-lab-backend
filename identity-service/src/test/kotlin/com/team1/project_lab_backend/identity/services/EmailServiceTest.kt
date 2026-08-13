package com.team1.project_lab_backend.identity.services

import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSender
import java.util.Properties

class EmailServiceTest {
    private val mailSender = Mockito.mock(JavaMailSender::class.java)
    private val service = EmailService(mailSender, "http://localhost:5173")

    @BeforeEach
    fun stubCreateMimeMessage() {
        Mockito.`when`(mailSender.createMimeMessage())
            .thenAnswer { MimeMessage(Session.getDefaultInstance(Properties())) }
    }

    private fun capturedMessage(): MimeMessage {
        val captor = ArgumentCaptor.forClass(MimeMessage::class.java)
        Mockito.verify(mailSender).send(captor.capture())
        return captor.value
    }

    @Test
    fun sendWelcomeEmailSendsMessageToRecipient() {
        service.sendWelcomeEmail("ada@example.com", "Ada")

        val message = capturedMessage()
        assertTrue(message.allRecipients.any { it.toString() == "ada@example.com" })
        assertTrue(message.subject.contains("Frui"))
    }

    @Test
    fun sendWelcomeEmailSwallowsMailException() {
        Mockito.`when`(mailSender.send(any(MimeMessage::class.java)))
            .thenThrow(MailSendException("smtp down"))

        assertDoesNotThrow {
            service.sendWelcomeEmail("ada@example.com", "Ada")
        }
    }

    @Test
    fun sendPasswordResetEmailSendsMessageToRecipient() {
        service.sendPasswordResetEmail("ada@example.com", "Ada", "http://localhost:5173/reset-password?token=abc")

        val message = capturedMessage()
        assertTrue(message.allRecipients.any { it.toString() == "ada@example.com" })
        assertTrue(message.subject.contains("Frui"))
    }

    @Test
    fun sendPasswordResetEmailSwallowsMailException() {
        Mockito.`when`(mailSender.send(any(MimeMessage::class.java)))
            .thenThrow(MailSendException("smtp down"))

        assertDoesNotThrow {
            service.sendPasswordResetEmail("ada@example.com", "Ada", "http://localhost:5173/reset-password?token=abc")
        }
    }

    @Test
    fun sendAccountConfirmationEmailSendsMessageToRecipient() {
        service.sendAccountConfirmationEmail("ada@example.com", "Ada", "http://localhost:5173/confirm-account?token=abc")

        val message = capturedMessage()
        assertTrue(message.allRecipients.any { it.toString() == "ada@example.com" })
        assertTrue(message.subject.contains("Frui"))
    }

    @Test
    fun sendAccountConfirmationEmailSwallowsMailException() {
        Mockito.`when`(mailSender.send(any(MimeMessage::class.java)))
            .thenThrow(MailSendException("smtp down"))

        assertDoesNotThrow {
            service.sendAccountConfirmationEmail("ada@example.com", "Ada", "http://localhost:5173/confirm-account?token=abc")
        }
    }
}
