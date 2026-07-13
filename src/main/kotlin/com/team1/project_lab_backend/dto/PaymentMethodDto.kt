package com.team1.project_lab_backend.dto

data class PaymentMethodResponse(
    val id: Int,
    val stripePaymentMethodId: String,
    val brand: String,
    val lastFour: String,
    val type: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val isDefault: Boolean,
)

// Deliberately never persisted or logged as-is: cardNumber/cvv exist only
// long enough to derive brand/lastFour and validate expiry, then are
// discarded. See PaymentMethodService for the PCI-scope note.
data class CreatePaymentMethodRequest(
    val cardholderName: String,
    val cardNumber: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cvv: String,
)
