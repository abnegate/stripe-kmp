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
 * Customer details
 */
data class Customer(
    val id: String,
    val email: String?,
    val name: String?,
    val defaultPaymentMethodId: String?
)

/**
 * Subscription details
 */
data class Subscription(
    val id: String,
    val customerId: String,
    val status: String,
    val currentPeriodEnd: Long,
    val items: List<SubscriptionItem>
)

/**
 * Subscription item details
 */
data class SubscriptionItem(
    val id: String,
    val priceId: String,
    val quantity: Int
)

/**
 * Stripe error codes
 */
enum class StripeErrorCode(val code: String) {
    CARD_DECLINED("card_declined"),
    EXPIRED_CARD("expired_card"),
    INCORRECT_CVC("incorrect_cvc"),
    PROCESSING_ERROR("processing_error"),
    INCORRECT_NUMBER("incorrect_number"),
    INVALID_REQUEST("invalid_request_error"),
    API_ERROR("api_error"),
    AUTHENTICATION_ERROR("authentication_error"),
    RATE_LIMIT("rate_limit_error"),
    NETWORK_ERROR("network_error"),
    UNKNOWN("unknown_error");
    
    companion object {
        fun fromCode(code: String?): StripeErrorCode {
            return values().find { it.code == code } ?: UNKNOWN
        }
    }
}

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
    
    /**
     * Create a customer
     */
    suspend fun createCustomer(
        email: String,
        name: String? = null,
        paymentMethodId: String? = null
    ): StripeResult<Customer>
    
    /**
     * Retrieve a customer
     */
    suspend fun retrieveCustomer(
        customerId: String
    ): StripeResult<Customer>
    
    /**
     * Update a customer
     */
    suspend fun updateCustomer(
        customerId: String,
        email: String? = null,
        name: String? = null,
        defaultPaymentMethodId: String? = null
    ): StripeResult<Customer>
    
    /**
     * Create a subscription
     */
    suspend fun createSubscription(
        customerId: String,
        priceId: String,
        quantity: Int = 1
    ): StripeResult<Subscription>
    
    /**
     * Retrieve a subscription
     */
    suspend fun retrieveSubscription(
        subscriptionId: String
    ): StripeResult<Subscription>
    
    /**
     * Cancel a subscription
     */
    suspend fun cancelSubscription(
        subscriptionId: String
    ): StripeResult<Subscription>
}

/**
 * Factory function to create platform-specific Stripe SDK instance
 */
expect fun createStripeSDK(): StripeSDK
