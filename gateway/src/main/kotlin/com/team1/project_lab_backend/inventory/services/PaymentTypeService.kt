package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.PaymentTypeRequest
import com.team1.project_lab_backend.inventory.models.PaymentType
import com.team1.project_lab_backend.util.feignErrorMessage
import feign.FeignException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Orchestration shim (docs/adr/0005): PaymentType CRUD now lives in inventory-service,
 * reached via paymentTypeFeignClient.
 */
@Service
class PaymentTypeService(private val paymentTypeFeignClient: PaymentTypeFeignClient) {

    fun getAllPaymentTypes(): List<PaymentType> = paymentTypeFeignClient.list(ids = null)

    fun createPaymentType(request: PaymentTypeRequest): PaymentType =
        try {
            paymentTypeFeignClient.create(request)
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid payment type")
        }

    fun updatePaymentType(id: Int, request: PaymentTypeRequest): PaymentType =
        try {
            paymentTypeFeignClient.update(id, request)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "payment type not found")
        } catch (e: FeignException.BadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, feignErrorMessage(e) ?: "invalid payment type")
        }

    fun deletePaymentType(id: Int) {
        try {
            paymentTypeFeignClient.delete(id)
        } catch (e: FeignException.NotFound) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "payment type not found")
        }
    }
}
