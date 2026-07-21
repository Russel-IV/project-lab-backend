package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.CreatePaymentMethodRequest
import com.team1.project_lab_backend.identity.dto.PaymentMethodResponse
import com.team1.project_lab_backend.util.FieldValidationException
import com.team1.project_lab_backend.util.webClientFieldErrors
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): payment method CRUD and card-format/expiry
 * validation now live in identity-service, reached via paymentMethodFeignClient.
 */
@Service
class PaymentMethodService(private val paymentMethodFeignClient: PaymentMethodFeignClient) {
    suspend fun getPaymentMethods(userId: Int): List<PaymentMethodResponse> = paymentMethodFeignClient.list(userId)

    suspend fun createPaymentMethod(
        userId: Int,
        request: CreatePaymentMethodRequest,
    ): PaymentMethodResponse =
        try {
            paymentMethodFeignClient.create(
                userId,
                PaymentMethodCreateRequest(
                    cardholderName = request.cardholderName,
                    cardNumber = request.cardNumber,
                    expiryMonth = request.expiryMonth,
                    expiryYear = request.expiryYear,
                    cvv = request.cvv,
                ),
            )
        } catch (e: WebClientResponseException.UnprocessableContent) {
            throw FieldValidationException(webClientFieldErrors(e) ?: emptyMap())
        }

    suspend fun setDefaultPaymentMethod(
        userId: Int,
        id: Int,
    ) {
        try {
            paymentMethodFeignClient.setDefault(id, userId)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "payment method not found")
        }
    }

    suspend fun deletePaymentMethod(
        userId: Int,
        id: Int,
    ) {
        try {
            paymentMethodFeignClient.delete(id, userId)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "payment method not found")
        }
    }
}
