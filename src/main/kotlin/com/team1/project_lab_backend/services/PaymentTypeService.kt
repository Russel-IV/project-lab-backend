package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.PaymentTypeRequest
import com.team1.project_lab_backend.dto.PaymentTypeResponse
import com.team1.project_lab_backend.models.PaymentType
import com.team1.project_lab_backend.repositories.PaymentTypeRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class PaymentTypeService(
    private val paymentTypeRepository: PaymentTypeRepository
) {
    @Transactional(readOnly = true)
    fun getAllPaymentTypes(): List<PaymentTypeResponse> =
        paymentTypeRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getPaymentTypeById(id: Int): PaymentTypeResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        return paymentTypeRepository.findById(id)
            .map { it.toResponse() }
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "payment type not found") }
    }

    @Transactional
    fun createPaymentType(request: PaymentTypeRequest): PaymentTypeResponse {
        if (request.paymentType.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "paymentType must not be blank")
        }
        val paymentType = PaymentType(paymentType = request.paymentType)
        return paymentTypeRepository.save(paymentType).toResponse()
    }

    @Transactional
    fun updatePaymentType(id: Int, request: PaymentTypeRequest): PaymentTypeResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (request.paymentType.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "paymentType must not be blank")
        }
        if (!paymentTypeRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "payment type not found")
        }
        val paymentType = PaymentType(id = id, paymentType = request.paymentType)
        return paymentTypeRepository.save(paymentType).toResponse()
    }

    @Transactional
    fun deletePaymentType(id: Int) {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }
        if (!paymentTypeRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "payment type not found")
        }
        paymentTypeRepository.deleteById(id)
    }
}

private fun PaymentType.toResponse(): PaymentTypeResponse =
    PaymentTypeResponse(id = id, paymentType = paymentType)
