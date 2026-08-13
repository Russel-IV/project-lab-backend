package com.team1.project_lab_backend.identity.services

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.springframework.mail.MailSendException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

class EmailServiceTest {
    private val mailSender = Mockito.mock(JavaMailSender::class.java)
    private val service = EmailService(mailSender)

    @Test
    fun sendWelcomeEmailSendsMessageToRecipient() {
        service.sendWelcomeEmail("ada@example.com", "Ada")

        val captor = ArgumentCaptor.forClass(SimpleMailMessage::class.java)
        Mockito.verify(mailSender).send(captor.capture())
        assertTrue(captor.value.to?.contains("ada@example.com") == true)
    }

    @Test
    fun sendWelcomeEmailSwallowsMailException() {
        Mockito.`when`(mailSender.send(any(SimpleMailMessage::class.java)))
            .thenThrow(MailSendException("smtp down"))

        assertDoesNotThrow {
            service.sendWelcomeEmail("ada@example.com", "Ada")
        }
    }

    @Test
    fun sendPasswordResetEmailSendsMessageToRecipient() {
        service.sendPasswordResetEmail("ada@example.com", "Ada", "http://localhost:5173/reset-password?token=abc")

        val captor = ArgumentCaptor.forClass(SimpleMailMessage::class.java)
        Mockito.verify(mailSender).send(captor.capture())
        assertTrue(captor.value.to?.contains("ada@example.com") == true)
    }

    @Test
    fun sendPasswordResetEmailSwallowsMailException() {
        Mockito.`when`(mailSender.send(any(SimpleMailMessage::class.java)))
            .thenThrow(MailSendException("smtp down"))

        assertDoesNotThrow {
            service.sendPasswordResetEmail("ada@example.com", "Ada", "http://localhost:5173/reset-password?token=abc")
        }
    }

    @Test
    fun sendAccountConfirmationEmailSendsMessageToRecipient() {
        service.sendAccountConfirmationEmail("ada@example.com", "Ada", "http://localhost:5173/confirm-account?token=abc")

        val captor = ArgumentCaptor.forClass(SimpleMailMessage::class.java)
        Mockito.verify(mailSender).send(captor.capture())
        assertTrue(captor.value.to?.contains("ada@example.com") == true)
    }

    @Test
    fun sendAccountConfirmationEmailSwallowsMailException() {
        Mockito.`when`(mailSender.send(any(SimpleMailMessage::class.java)))
            .thenThrow(MailSendException("smtp down"))

        assertDoesNotThrow {
            service.sendAccountConfirmationEmail("ada@example.com", "Ada", "http://localhost:5173/confirm-account?token=abc")
        }
    }
}
