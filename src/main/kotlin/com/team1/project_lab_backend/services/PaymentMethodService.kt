package com.team1.project_lab_backend.services

import com.team1.project_lab_backend.dto.CreatePaymentMethodRequest
import com.team1.project_lab_backend.dto.PaymentMethodResponse
import com.team1.project_lab_backend.models.PaymentMethod
import com.team1.project_lab_backend.repositories.PaymentMethodRepository
import com.team1.project_lab_backend.util.FieldValidationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.YearMonth
import java.util.UUID

@Service
class PaymentMethodService(
    private val paymentMethodRepository: PaymentMethodRepository,
) {
    @Transactional(readOnly = true)
    fun getPaymentMethods(userId: Int): List<PaymentMethodResponse> =
        paymentMethodRepository.findByUserId(userId).map { it.toResponse() }

    // Accepting a raw cardNumber/cvv over our own API (rather than tokenizing
    // client-side via Stripe Elements) is a real PCI-scope concern; acceptable
    // only because stripePaymentMethodId is mocked here. Neither the number
    // nor the cvv is persisted or logged — only brand/lastFour/expiry survive
    // past this method, and a real Stripe integration should replace this
    // wholesale rather than extend it.
    @Transactional
    fun createPaymentMethod(userId: Int, request: CreatePaymentMethodRequest): PaymentMethodResponse {
        val errors = mutableMapOf<String, String>()
        val digits = request.cardNumber.replace(Regex("\\s+"), "")

        if (!digits.matches(Regex("^\\d{13,19}$"))) {
            errors["cardNumber"] = "card number must be 13-19 digits"
        }
        if (request.expiryMonth !in 1..12) {
            errors["expiryYear"] = "expiry date is invalid"
        } else if (YearMonth.of(request.expiryYear, request.expiryMonth).isBefore(YearMonth.now())) {
            errors["expiryYear"] = "card has expired"
        }
        if (errors.isNotEmpty()) throw FieldValidationException(errors)

        val saved = paymentMethodRepository.save(
            PaymentMethod(
                userId = userId,
                stripePaymentMethodId = "pm_mock_${UUID.randomUUID()}",
                brand = brandFor(digits),
                lastFour = digits.takeLast(4),
                type = "credit_card",
                expiryMonth = request.expiryMonth,
                expiryYear = request.expiryYear,
            ),
        )
        return saved.toResponse()
    }

    private fun brandFor(digits: String): String = when {
        digits.startsWith("4") -> "visa"
        digits.take(2).toInt() in 51..55 || digits.take(4).toInt() in 2221..2720 -> "mastercard"
        digits.take(2) in setOf("34", "37") -> "amex"
        digits.startsWith("6011") || digits.startsWith("65") -> "discover"
        else -> "unknown"
    }

    private fun PaymentMethod.toResponse() = PaymentMethodResponse(
        id = id,
        stripePaymentMethodId = stripePaymentMethodId,
        brand = brand,
        lastFour = lastFour,
        type = type,
        expiryMonth = expiryMonth,
        expiryYear = expiryYear,
    )
}
