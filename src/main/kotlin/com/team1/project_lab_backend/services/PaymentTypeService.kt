package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.PaymentTypeRequest
import com.team1.project_lab_backend.dto.PaymentTypeResponse
import com.team1.project_lab_backend.models.PaymentType
import com.team1.project_lab_backend.repositories.PaymentTypeRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentTypeService(
    private val paymentTypeRepository: PaymentTypeRepository
) {
    @Transactional(readOnly = true)
    fun getAllPaymentTypes(): List<PaymentTypeResponse> =
        paymentTypeRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getPaymentTypeById(id: Int): PaymentTypeResponse {
        id.requirePositive()
        return paymentTypeRepository.findById(id).orNotFound("payment type not found").toResponse()
    }

    @Transactional
    fun createPaymentType(request: PaymentTypeRequest): PaymentTypeResponse {
        request.paymentType.requireNotBlank("paymentType")
        val paymentType = PaymentType(paymentType = request.paymentType)
        return paymentTypeRepository.save(paymentType).toResponse()
    }

    @Transactional
    fun updatePaymentType(id: Int, request: PaymentTypeRequest): PaymentTypeResponse {
        id.requirePositive()
        request.paymentType.requireNotBlank("paymentType")
        paymentTypeRepository.requireExistsById(id, "payment type not found")
        val paymentType = PaymentType(id = id, paymentType = request.paymentType)
        return paymentTypeRepository.save(paymentType).toResponse()
    }

    @Transactional
    fun deletePaymentType(id: Int) {
        id.requirePositive()
        paymentTypeRepository.requireExistsById(id, "payment type not found")
        paymentTypeRepository.deleteById(id)
    }
}

private fun PaymentType.toResponse(): PaymentTypeResponse =
    PaymentTypeResponse(id = id, paymentType = paymentType)
