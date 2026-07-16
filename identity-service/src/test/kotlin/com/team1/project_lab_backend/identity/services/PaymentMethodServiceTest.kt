package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.CreatePaymentMethodRequest
import com.team1.project_lab_backend.identity.models.PaymentMethod
import com.team1.project_lab_backend.identity.repositories.PaymentMethodRepository
import com.team1.project_lab_backend.util.FieldValidationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.Year

class PaymentMethodServiceTest {
    private val paymentMethodRepository = Mockito.mock(PaymentMethodRepository::class.java)
    private val service = PaymentMethodService(paymentMethodRepository)

    private fun validRequest(cardNumber: String = "4111111111111111") =
        CreatePaymentMethodRequest(
            cardholderName = "Ada Lovelace",
            cardNumber = cardNumber,
            expiryMonth = 12,
            expiryYear = Year.now().value + 2,
            cvv = "123",
        )

    @Test
    fun createPaymentMethodRejectsInvalidCardNumber() {
        val ex =
            assertThrows(FieldValidationException::class.java) {
                service.createPaymentMethod(1, validRequest(cardNumber = "abc"))
            }
        assertEquals(true, ex.errors.containsKey("cardNumber"))
    }

    @Test
    fun createPaymentMethodRejectsExpiredCard() {
        val ex =
            assertThrows(FieldValidationException::class.java) {
                service.createPaymentMethod(1, validRequest().copy(expiryYear = 2020))
            }
        assertEquals("card has expired", ex.errors["expiryYear"])
    }

    @Test
    fun createPaymentMethodNeverPersistsRawCardNumber() {
        Mockito.`when`(paymentMethodRepository.findByUserId(1)).thenReturn(emptyList())
        Mockito.`when`(paymentMethodRepository.save(Mockito.any(PaymentMethod::class.java))).thenAnswer { it.arguments[0] }

        val result = service.createPaymentMethod(1, validRequest())

        assertEquals("1111", result.lastFour)
        assertEquals("visa", result.brand)
        assertEquals(true, result.isDefault)
    }

    @Test
    fun setDefaultPaymentMethodRejectsUnknown() {
        Mockito.`when`(paymentMethodRepository.findByIdAndUserId(99, 1)).thenReturn(null)
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.setDefaultPaymentMethod(1, 99)
            }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deletePaymentMethodRejectsUnknown() {
        Mockito.`when`(paymentMethodRepository.findByIdAndUserId(99, 1)).thenReturn(null)
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                service.deletePaymentMethod(1, 99)
            }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }
}
