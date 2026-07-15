package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.dto.PaymentTypeRequest
import com.team1.project_lab_backend.inventory.models.PaymentType
import com.team1.project_lab_backend.inventory.repositories.PaymentTypeRepository
import com.team1.project_lab_backend.util.orNotFound
import com.team1.project_lab_backend.util.requireExistsById
import com.team1.project_lab_backend.util.requireNotBlank
import com.team1.project_lab_backend.util.requirePositive
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentTypeService(
    private val paymentTypeRepository: PaymentTypeRepository,
) {
    @Transactional(readOnly = true)
    fun getAllPaymentTypes(): List<PaymentType> = paymentTypeRepository.findAll()

    @Transactional(readOnly = true)
    fun getPaymentTypeById(id: Int): PaymentType {
        id.requirePositive()
        return paymentTypeRepository.findById(id).orNotFound("payment type not found")
    }

    @Transactional
    fun createPaymentType(request: PaymentTypeRequest): PaymentType {
        request.paymentType.requireNotBlank("paymentType")
        return paymentTypeRepository.save(PaymentType(paymentType = request.paymentType))
    }

    @Transactional
    fun updatePaymentType(
        id: Int,
        request: PaymentTypeRequest,
    ): PaymentType {
        id.requirePositive()
        request.paymentType.requireNotBlank("paymentType")
        paymentTypeRepository.requireExistsById(id, "payment type not found")
        return paymentTypeRepository.save(PaymentType(id = id, paymentType = request.paymentType))
    }

    @Transactional
    fun deletePaymentType(id: Int) {
        id.requirePositive()
        paymentTypeRepository.requireExistsById(id, "payment type not found")
        paymentTypeRepository.deleteById(id)
    }
}
