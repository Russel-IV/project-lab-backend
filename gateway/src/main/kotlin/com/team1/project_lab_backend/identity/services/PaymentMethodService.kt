package com.team1.project_lab_backend.identity.services

import com.team1.project_lab_backend.identity.dto.CreatePaymentMethodRequest
import com.team1.project_lab_backend.identity.dto.PaymentMethodResponse
import com.team1.project_lab_backend.util.FieldValidationException
import com.team1.project_lab_backend.util.feignFieldErrors
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): payment method CRUD and card-format/expiry
 * validation now live in identity-service, reached via paymentMethodFeignClient.
 */
@Service
class PaymentMethodService(private val paymentMethodFeignClient: PaymentMethodFeignClient) {
    fun getPaymentMethods(userId: Int): List<PaymentMethodResponse> = paymentMethodFeignClient.list(userId)

    fun createPaymentMethod(
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
        } catch (e: FeignException.UnprocessableEntity) {
            throw FieldValidationException(feignFieldErrors(e) ?: emptyMap())
        }

    fun setDefaultPaymentMethod(
        userId: Int,
        id: Int,
    ) {
        try {
            paymentMethodFeignClient.setDefault(id, userId)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "payment method not found")
        }
    }

    fun deletePaymentMethod(
        userId: Int,
        id: Int,
    ) {
        try {
            paymentMethodFeignClient.delete(id, userId)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "payment method not found")
        }
    }
}
