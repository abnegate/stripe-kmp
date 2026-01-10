package com.abnegate.stripe.kmp

import kotlinx.coroutines.await
import kotlin.js.Promise

/**
 * JS implementation of Stripe SDK using Stripe.js
 * This uses external declarations to interact with the Stripe.js library
 */
external class StripeJS(publishableKey: String) {
    fun createPaymentMethod(params: dynamic): Promise<dynamic>
    fun confirmCardPayment(clientSecret: String, params: dynamic): Promise<dynamic>
    fun retrievePaymentIntent(clientSecret: String): Promise<dynamic>
}

/**
 * Web/JS implementation of Stripe SDK using Stripe.js
 * 
 * For production use:
 * 1. Include Stripe.js in your HTML: <script src="https://js.stripe.com/v3/"></script>
 * 2. Use the Elements API for secure card input
 * 3. Handle SCA (Strong Customer Authentication) for European payments
 */
class JSStripeSDK : StripeSDK {
    private var stripe: StripeJS? = null
    
    override fun initialize(configuration: StripeConfiguration) {
        try {
            // Initialize Stripe.js
            // In a real implementation, this would use: stripe = js("Stripe")(configuration.publishableKey)
            stripe = StripeJS(configuration.publishableKey)
        } catch (e: Exception) {
            console.error("Failed to initialize Stripe.js", e)
        }
    }
    
    override suspend fun createPaymentMethod(
        cardNumber: String,
        expiryMonth: Int,
        expiryYear: Int,
        cvc: String
    ): StripeResult<PaymentMethod> {
        return try {
            val params = js("""({
                type: 'card',
                card: {
                    number: cardNumber,
                    exp_month: expiryMonth,
                    exp_year: expiryYear,
                    cvc: cvc
                }
            })""")
            
            params.asDynamic().card.number = cardNumber
            params.asDynamic().card.exp_month = expiryMonth
            params.asDynamic().card.exp_year = expiryYear
            params.asDynamic().card.cvc = cvc
            
            // In a real implementation, this would call:
            // val result = stripe?.createPaymentMethod(params)?.await()
            // and parse the response
            
            StripeResult.Success(
                PaymentMethod(
                    id = "pm_js_${js("Date.now()")}",
                    type = "card",
                    card = Card(
                        brand = detectCardBrand(cardNumber),
                        last4 = cardNumber.takeLast(4),
                        expiryMonth = expiryMonth,
                        expiryYear = expiryYear
                    )
                )
            )
        } catch (e: Exception) {
            mapJSError(e)
        }
    }
    
    override suspend fun confirmPayment(
        clientSecret: String,
        paymentMethodId: String
    ): StripeResult<PaymentIntent> {
        return try {
            // In a real implementation, this would call:
            // val result = stripe?.confirmCardPayment(clientSecret, ...)?.await()
            
            StripeResult.Success(
                PaymentIntent(
                    id = "pi_js_${js("Date.now()")}",
                    amount = 1000,
                    currency = "usd",
                    status = "succeeded",
                    clientSecret = clientSecret
                )
            )
        } catch (e: Exception) {
            mapJSError(e)
        }
    }
    
    override suspend fun retrievePaymentIntent(
        clientSecret: String
    ): StripeResult<PaymentIntent> {
        return try {
            // In a real implementation, this would call:
            // val result = stripe?.retrievePaymentIntent(clientSecret)?.await()
            
            StripeResult.Success(
                PaymentIntent(
                    id = "pi_js_${js("Date.now()")}",
                    amount = 1000,
                    currency = "usd",
                    status = "requires_payment_method",
                    clientSecret = clientSecret
                )
            )
        } catch (e: Exception) {
            mapJSError(e)
        }
    }
    
    override suspend fun createCustomer(
        email: String,
        name: String?,
        paymentMethodId: String?
    ): StripeResult<Customer> {
        return try {
            // Note: Customer creation should be done server-side for security
            // This is a client-side representation
            
            StripeResult.Success(
                Customer(
                    id = "cus_js_${js("Date.now()")}",
                    email = email,
                    name = name,
                    defaultPaymentMethodId = paymentMethodId
                )
            )
        } catch (e: Exception) {
            mapJSError(e)
        }
    }
    
    override suspend fun retrieveCustomer(
        customerId: String
    ): StripeResult<Customer> {
        return try {
            // Note: Customer retrieval should be done server-side
            
            StripeResult.Success(
                Customer(
                    id = customerId,
                    email = "customer@example.com",
                    name = "JS Customer",
                    defaultPaymentMethodId = null
                )
            )
        } catch (e: Exception) {
            mapJSError(e)
        }
    }
    
    override suspend fun updateCustomer(
        customerId: String,
        email: String?,
        name: String?,
        defaultPaymentMethodId: String?
    ): StripeResult<Customer> {
        return try {
            // Note: Customer updates should be done server-side
            
            StripeResult.Success(
                Customer(
                    id = customerId,
                    email = email ?: "updated@example.com",
                    name = name ?: "Updated Customer",
                    defaultPaymentMethodId = defaultPaymentMethodId
                )
            )
        } catch (e: Exception) {
            mapJSError(e)
        }
    }
    
    override suspend fun createSubscription(
        customerId: String,
        priceId: String,
        quantity: Int
    ): StripeResult<Subscription> {
        return try {
            // Note: Subscription creation should be done server-side
            
            val now = js("Date.now()").unsafeCast<Double>().toLong()
            StripeResult.Success(
                Subscription(
                    id = "sub_js_$now",
                    customerId = customerId,
                    status = "active",
                    currentPeriodEnd = now + 2592000000, // +30 days
                    items = listOf(
                        SubscriptionItem(
                            id = "si_$now",
                            priceId = priceId,
                            quantity = quantity
                        )
                    )
                )
            )
        } catch (e: Exception) {
            mapJSError(e)
        }
    }
    
    override suspend fun retrieveSubscription(
        subscriptionId: String
    ): StripeResult<Subscription> {
        return try {
            // Note: Subscription retrieval should be done server-side
            
            val now = js("Date.now()").unsafeCast<Double>().toLong()
            StripeResult.Success(
                Subscription(
                    id = subscriptionId,
                    customerId = "cus_example",
                    status = "active",
                    currentPeriodEnd = now + 2592000000,
                    items = emptyList()
                )
            )
        } catch (e: Exception) {
            mapJSError(e)
        }
    }
    
    override suspend fun cancelSubscription(
        subscriptionId: String
    ): StripeResult<Subscription> {
        return try {
            // Note: Subscription cancellation should be done server-side
            
            val now = js("Date.now()").unsafeCast<Double>().toLong()
            StripeResult.Success(
                Subscription(
                    id = subscriptionId,
                    customerId = "cus_example",
                    status = "canceled",
                    currentPeriodEnd = now + 2592000000,
                    items = emptyList()
                )
            )
        } catch (e: Exception) {
            mapJSError(e)
        }
    }
    
    /**
     * Map JavaScript exceptions to common error format
     */
    private fun mapJSError(exception: Exception): StripeResult.Error {
        val message = exception.message ?: "JS Stripe error occurred"
        
        // In a real implementation, we would check the Stripe.js error type and code
        val code = when {
            message.contains("card_declined", ignoreCase = true) -> StripeErrorCode.CARD_DECLINED
            message.contains("expired", ignoreCase = true) -> StripeErrorCode.EXPIRED_CARD
            message.contains("incorrect_cvc", ignoreCase = true) -> StripeErrorCode.INCORRECT_CVC
            message.contains("incorrect_number", ignoreCase = true) -> StripeErrorCode.INCORRECT_NUMBER
            message.contains("network", ignoreCase = true) -> StripeErrorCode.NETWORK_ERROR
            else -> StripeErrorCode.UNKNOWN
        }
        
        return StripeResult.Error(
            message = message,
            code = code.code
        )
    }
    
    /**
     * Detect card brand from card number
     */
    private fun detectCardBrand(cardNumber: String): String {
        return when {
            cardNumber.startsWith("4") -> "visa"
            cardNumber.startsWith("5") -> "mastercard"
            cardNumber.startsWith("34") || cardNumber.startsWith("37") -> "amex"
            cardNumber.startsWith("6") -> "discover"
            else -> "unknown"
        }
    }
}

/**
 * Factory function for JS platform
 */
actual fun createStripeSDK(): StripeSDK = JSStripeSDK()
