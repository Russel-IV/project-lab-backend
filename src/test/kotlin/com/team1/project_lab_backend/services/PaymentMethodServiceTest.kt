package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.CreatePaymentMethodRequest
import com.team1.project_lab_backend.models.PaymentMethod
import com.team1.project_lab_backend.repositories.PaymentMethodRepository
import com.team1.project_lab_backend.util.FieldValidationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import java.time.Year

class PaymentMethodServiceTest {

    private val paymentMethodRepository = Mockito.mock(PaymentMethodRepository::class.java)
    private val service = PaymentMethodService(paymentMethodRepository)

    private val futureYear = Year.now().value + 5

    private fun baseRequest(
        cardNumber: String = "4111111111111111",
        expiryMonth: Int = 12,
        expiryYear: Int = futureYear,
    ) = CreatePaymentMethodRequest(
        cardholderName = "Ada Lovelace",
        cardNumber = cardNumber,
        expiryMonth = expiryMonth,
        expiryYear = expiryYear,
        cvv = "123",
    )

    private fun stubSaveReturnsInput() {
        Mockito.`when`(paymentMethodRepository.save(Mockito.any(PaymentMethod::class.java)))
            .thenAnswer { it.arguments[0] }
    }

    // ---- getPaymentMethods ----

    @Test
    fun getPaymentMethodsReturnsMappedList() {
        val saved = PaymentMethod(
            id = 1, userId = 10, stripePaymentMethodId = "pm_mock_abc",
            brand = "visa", lastFour = "1111", type = "credit_card",
            expiryMonth = 12, expiryYear = futureYear,
        )
        Mockito.`when`(paymentMethodRepository.findByUserId(10)).thenReturn(listOf(saved))

        val result = service.getPaymentMethods(10)

        assertEquals(1, result.size)
        assertEquals("pm_mock_abc", result[0].stripePaymentMethodId)
        assertEquals("1111", result[0].lastFour)
    }

    // ---- createPaymentMethod ----

    @Test
    fun createPaymentMethodRejectsNonNumericCardNumber() {
        val ex = assertThrows(FieldValidationException::class.java) {
            service.createPaymentMethod(10, baseRequest(cardNumber = "not-a-card"))
        }
        assertTrue(ex.errors.containsKey("cardNumber"))
    }

    @Test
    fun createPaymentMethodRejectsWrongLengthCardNumber() {
        val ex = assertThrows(FieldValidationException::class.java) {
            service.createPaymentMethod(10, baseRequest(cardNumber = "4111"))
        }
        assertTrue(ex.errors.containsKey("cardNumber"))
    }

    @Test
    fun createPaymentMethodRejectsExpiredCard() {
        val ex = assertThrows(FieldValidationException::class.java) {
            service.createPaymentMethod(10, baseRequest(expiryYear = 2020, expiryMonth = 1))
        }
        assertTrue(ex.errors.containsKey("expiryYear"))
    }

    @Test
    fun createPaymentMethodRejectsInvalidMonth() {
        val ex = assertThrows(FieldValidationException::class.java) {
            service.createPaymentMethod(10, baseRequest(expiryMonth = 13))
        }
        assertTrue(ex.errors.containsKey("expiryYear"))
    }

    @Test
    fun createPaymentMethodNeverPersistsRawCardNumberOrCvv() {
        stubSaveReturnsInput()

        service.createPaymentMethod(10, baseRequest(cardNumber = "4111 1111 1111 1111"))

        val captor = ArgumentCaptor.forClass(PaymentMethod::class.java)
        Mockito.verify(paymentMethodRepository).save(captor.capture())
        assertEquals("1111", captor.value.lastFour)
        assertTrue(captor.value.stripePaymentMethodId.startsWith("pm_mock_"))
    }

    @Test
    fun createPaymentMethodDerivesVisaBrand() {
        stubSaveReturnsInput()
        val result = service.createPaymentMethod(10, baseRequest(cardNumber = "4111111111111111"))
        assertEquals("visa", result.brand)
    }

    @Test
    fun createPaymentMethodDerivesMastercardBrand() {
        stubSaveReturnsInput()
        val result = service.createPaymentMethod(10, baseRequest(cardNumber = "5500000000000004"))
        assertEquals("mastercard", result.brand)
    }

    @Test
    fun createPaymentMethodDerivesAmexBrand() {
        stubSaveReturnsInput()
        val result = service.createPaymentMethod(10, baseRequest(cardNumber = "340000000000009"))
        assertEquals("amex", result.brand)
    }

    @Test
    fun createPaymentMethodDerivesDiscoverBrand() {
        stubSaveReturnsInput()
        val result = service.createPaymentMethod(10, baseRequest(cardNumber = "6011000000000004"))
        assertEquals("discover", result.brand)
    }

    @Test
    fun createPaymentMethodFallsBackToUnknownBrand() {
        stubSaveReturnsInput()
        val result = service.createPaymentMethod(10, baseRequest(cardNumber = "9999999999999999"))
        assertEquals("unknown", result.brand)
    }
}
