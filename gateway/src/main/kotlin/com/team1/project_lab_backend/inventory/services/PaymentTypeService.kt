package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.PaymentTypeRequest
import com.team1.project_lab_backend.inventory.models.PaymentType
import com.team1.project_lab_backend.util.webClientErrorMessage
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): PaymentType CRUD now lives in inventory-service,
 * reached via paymentTypeFeignClient.
 */
@Service
class PaymentTypeService(private val paymentTypeFeignClient: PaymentTypeFeignClient) {
    suspend fun getAllPaymentTypes(): List<PaymentType> = paymentTypeFeignClient.list(ids = null)

    suspend fun createPaymentType(request: PaymentTypeRequest): PaymentType =
        try {
            paymentTypeFeignClient.create(request)
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid payment type")
        }

    suspend fun updatePaymentType(
        id: Int,
        request: PaymentTypeRequest,
    ): PaymentType =
        try {
            paymentTypeFeignClient.update(id, request)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "payment type not found")
        } catch (e: WebClientResponseException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, webClientErrorMessage(e) ?: "invalid payment type")
        }

    suspend fun deletePaymentType(id: Int) {
        try {
            paymentTypeFeignClient.delete(id)
        } catch (e: WebClientResponseException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "payment type not found")
        }
    }
}
