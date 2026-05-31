package com.team1.project_lab_backend.dto

data class PaymentTypeRequest(
    val paymentType: String
)

data class PaymentTypeResponse(
    val id: Int,
    val paymentType: String
)
