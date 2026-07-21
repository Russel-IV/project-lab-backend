package com.team1.project_lab_backend.booking.services

import com.stripe.net.RequestOptions
import com.stripe.param.PaymentIntentCreateParams
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import com.stripe.model.PaymentIntent as StripeApiPaymentIntent

data class StripePaymentIntentResult(
    val id: String,
    val clientSecret: String,
)

/**
 * Thin wrapper around stripe-java's static PaymentIntent calls, so
 * PaymentIntentService/BookingService depend on an injectable, mockable
 * collaborator instead of the SDK's statics, and so the SDK's PaymentIntent
 * type — which collides by name with our own models.PaymentIntent entity —
 * never leaks past this file.
 */
@Component
class StripeClient(
    @Value("\${stripe.secret-key}") private val secretKey: String,
) {
    fun createPaymentIntent(
        amount: Int,
        currency: String,
        idempotencyKey: String,
        metadata: Map<String, String>,
    ): StripePaymentIntentResult {
        val params =
            PaymentIntentCreateParams.builder()
                .setAmount(amount.toLong())
                .setCurrency(currency)
                // Pins the frontend's Payment Element to card-only. Omitting this in
                // favor of automatic_payment_methods surfaces Klarna/Cash App
                // Pay/Amazon Pay, which need redirect-return handling we haven't built.
                .addPaymentMethodType("card")
                .putAllMetadata(metadata)
                .build()
        val requestOptions =
            RequestOptions.builder()
                .setApiKey(secretKey)
                .setIdempotencyKey(idempotencyKey)
                .build()
        val intent = StripeApiPaymentIntent.create(params, requestOptions)
        return StripePaymentIntentResult(id = intent.id, clientSecret = intent.clientSecret)
    }

    fun retrieveStatus(paymentIntentId: String): String {
        val requestOptions = RequestOptions.builder().setApiKey(secretKey).build()
        return StripeApiPaymentIntent.retrieve(paymentIntentId, requestOptions).status
    }
}
