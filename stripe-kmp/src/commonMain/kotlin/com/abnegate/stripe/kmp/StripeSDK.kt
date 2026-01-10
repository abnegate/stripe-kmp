package com.abnegate.stripe.kmp

/**
 * Stripe SDK configuration
 */
data class StripeConfiguration(
    val publishableKey: String,
    val merchantIdentifier: String? = null
)

/**
 * Result wrapper for Stripe operations
 */
sealed class StripeResult<out T> {
    data class Success<T>(val data: T) : StripeResult<T>()
    data class Error(val message: String, val code: String? = null) : StripeResult<Nothing>()
}

/**
 * Payment method details
 */
data class PaymentMethod(
    val id: String,
    val type: String,
    val card: Card? = null
)

/**
 * Card details
 */
data class Card(
    val brand: String,
    val last4: String,
    val expiryMonth: Int,
    val expiryYear: Int
)

/**
 * Payment intent details
 */
data class PaymentIntent(
    val id: String,
    val amount: Long,
    val currency: String,
    val status: String,
    val clientSecret: String
)

/**
 * Common interface for Stripe SDK across all platforms
 */
interface StripeSDK {
    /**
     * Initialize the Stripe SDK with configuration
     */
    fun initialize(configuration: StripeConfiguration)
    
    /**
     * Create a payment method from card details
     */
    suspend fun createPaymentMethod(
        cardNumber: String,
        expiryMonth: Int,
        expiryYear: Int,
        cvc: String
    ): StripeResult<PaymentMethod>
    
    /**
     * Confirm a payment intent
     */
    suspend fun confirmPayment(
        clientSecret: String,
        paymentMethodId: String
    ): StripeResult<PaymentIntent>
    
    /**
     * Retrieve a payment intent
     */
    suspend fun retrievePaymentIntent(
        clientSecret: String
    ): StripeResult<PaymentIntent>
}

/**
 * Factory function to create platform-specific Stripe SDK instance
 */
expect fun createStripeSDK(): StripeSDK
