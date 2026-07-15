package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.CreatePaymentMethodRequest
import com.team1.project_lab_backend.identity.dto.PaymentMethodResponse
import com.team1.project_lab_backend.util.FieldValidationException
import feign.FeignException
import feign.Request
import feign.RequestTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets
import java.time.Year

class PaymentMethodServiceTest {
    private val paymentMethodFeignClient = Mockito.mock(PaymentMethodFeignClient::class.java)
    private val service = PaymentMethodService(paymentMethodFeignClient)

    private val futureYear = Year.now().value + 5

    private fun baseRequest(cardNumber: String = "4111111111111111") = CreatePaymentMethodRequest(
        cardholderName = "Ada Lovelace",
        cardNumber = cardNumber,
        expiryMonth = 12,
        expiryYear = futureYear,
        cvv = "123",
    )

    private fun feignUnprocessable(body: String) = FeignException.UnprocessableEntity(
        "unprocessable", Request.create(Request.HttpMethod.POST, "/", emptyMap(), null, RequestTemplate()),
        body.toByteArray(StandardCharsets.UTF_8), emptyMap(),
    )

    @Test
    fun getPaymentMethodsReturnsMappedList() {
        val response = PaymentMethodResponse(
            id = 1, stripePaymentMethodId = "pm_mock_abc", brand = "visa", lastFour = "1111",
            type = "credit_card", expiryMonth = 12, expiryYear = futureYear, isDefault = true,
        )
        Mockito.`when`(paymentMethodFeignClient.list(10)).thenReturn(listOf(response))

        val result = service.getPaymentMethods(10)

        assertEquals(1, result.size)
        assertEquals("pm_mock_abc", result[0].stripePaymentMethodId)
    }

    @Test
    fun createPaymentMethodMapsFeignFieldErrors() {
        val request = baseRequest(cardNumber = "not-a-card")
        Mockito.`when`(
            paymentMethodFeignClient.create(
                10,
                PaymentMethodCreateRequest(request.cardholderName, request.cardNumber, request.expiryMonth, request.expiryYear, request.cvv),
            ),
        ).thenThrow(feignUnprocessable("""{"errors":{"cardNumber":"card number must be 13-19 digits"}}"""))

        val ex = assertThrows(FieldValidationException::class.java) {
            service.createPaymentMethod(10, request)
        }
        assertTrue(ex.errors.containsKey("cardNumber"))
    }

    @Test
    fun createPaymentMethodReturnsMappedResponseOnSuccess() {
        val request = baseRequest()
        val response = PaymentMethodResponse(
            id = 1, stripePaymentMethodId = "pm_mock_abc", brand = "visa", lastFour = "1111",
            type = "credit_card", expiryMonth = 12, expiryYear = futureYear, isDefault = true,
        )
        Mockito.`when`(
            paymentMethodFeignClient.create(
                10,
                PaymentMethodCreateRequest(request.cardholderName, request.cardNumber, request.expiryMonth, request.expiryYear, request.cvv),
            ),
        ).thenReturn(response)

        val result = service.createPaymentMethod(10, request)

        assertEquals("visa", result.brand)
    }

    @Test
    fun setDefaultPaymentMethodReturnsNotFoundWhenMissing() {
        Mockito.`when`(paymentMethodFeignClient.setDefault(99, 10)).thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.setDefaultPaymentMethod(10, 99)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    @Test
    fun deletePaymentMethodReturnsNotFoundWhenMissing() {
        Mockito.`when`(paymentMethodFeignClient.delete(99, 10)).thenThrow(FeignException.NotFound::class.java)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.deletePaymentMethod(10, 99)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }
}
