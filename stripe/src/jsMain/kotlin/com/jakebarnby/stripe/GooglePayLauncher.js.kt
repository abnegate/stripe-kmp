package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import kotlinx.coroutines.await
import kotlin.js.Promise

/**
 * External declarations for Payment Request API (used for Google Pay on web).
 */
@JsModule("@stripe/stripe-js")
@JsNonModule
public external interface StripePaymentRequest {
    public fun canMakePayment(): Promise<CanMakePaymentResult?>
    public fun show(): Promise<Unit>
    public fun on(event: String, handler: (dynamic) -> Unit)
}

public external interface CanMakePaymentResult {
    public val applePay: Boolean
    public val googlePay: Boolean
}

/**
 * JavaScript implementation of GooglePayLauncher using Stripe.js Payment Request API.
 *
 * This implementation uses the Payment Request Button API which supports
 * Google Pay on compatible browsers.
 */
public actual class GooglePayLauncher {
    private var stripeInstance: StripeInstance? = null

    /**
     * Initialize the launcher with a Stripe instance.
     *
     * @param stripe The Stripe instance to use for payment processing
     */
    public fun setStripeInstance(stripe: StripeInstance) {
        stripeInstance = stripe
    }

    public actual suspend fun presentForPaymentIntent(
        clientSecret: String,
        configuration: GooglePayConfiguration
    ): WalletPaymentResult {
        val stripe = stripeInstance
            ?: return WalletPaymentResult.Failed(
                StripeException("Stripe instance not set. Call setStripeInstance() first.")
            )

        try {
            // Create payment request
            val paymentRequest = createPaymentRequest(stripe, configuration, null)

            // Check if Google Pay is available
            val canMakePayment = paymentRequest.canMakePayment().await()
            if (canMakePayment?.googlePay != true) {
                return WalletPaymentResult.Failed(
                    StripeException("Google Pay is not available in this browser")
                )
            }

            // Handle payment method event
            var result: WalletPaymentResult? = null
            paymentRequest.on("paymentmethod") { event ->
                val paymentMethodId = event.paymentMethod.id as? String
                if (paymentMethodId != null) {
                    // Confirm payment with the payment method
                    stripe.confirmCardPayment(clientSecret, js("{payment_method: event.paymentMethod.id}"))
                        .then { confirmResult ->
                            if (confirmResult.error != null) {
                                result = WalletPaymentResult.Failed(
                                    StripeException(confirmResult.error.message as? String ?: "Payment failed")
                                )
                                event.complete("fail")
                            } else {
                                result = WalletPaymentResult.Success(
                                    paymentMethodId = paymentMethodId
                                )
                                event.complete("success")
                            }
                        }
                } else {
                    result = WalletPaymentResult.Failed(
                        StripeException("No payment method ID received")
                    )
                    event.complete("fail")
                }
            }

            // Show payment request UI
            paymentRequest.show().await()

            return result ?: WalletPaymentResult.Canceled
        } catch (e: Exception) {
            return WalletPaymentResult.Failed(
                if (e is StripeException) e else StripeException(
                    message = "Failed to present Google Pay: ${e.message}",
                    cause = e
                )
            )
        }
    }

    public actual suspend fun presentForSetupIntent(
        clientSecret: String,
        configuration: GooglePayConfiguration
    ): WalletPaymentResult {
        return WalletPaymentResult.Failed(
            StripeException("SetupIntent with Google Pay not fully supported in Payment Request API")
        )
    }

    public actual suspend fun createPaymentMethod(
        configuration: GooglePayConfiguration,
        request: WalletPaymentRequest
    ): WalletPaymentResult {
        val stripe = stripeInstance
            ?: return WalletPaymentResult.Failed(
                StripeException("Stripe instance not set. Call setStripeInstance() first.")
            )

        try {
            // Create payment request
            val paymentRequest = createPaymentRequest(stripe, configuration, request)

            // Check if Google Pay is available
            val canMakePayment = paymentRequest.canMakePayment().await()
            if (canMakePayment?.googlePay != true) {
                return WalletPaymentResult.Failed(
                    StripeException("Google Pay is not available in this browser")
                )
            }

            // Handle payment method event
            var result: WalletPaymentResult? = null
            paymentRequest.on("paymentmethod") { event ->
                val paymentMethodId = event.paymentMethod.id as? String
                if (paymentMethodId != null) {
                    result = WalletPaymentResult.Success(paymentMethodId = paymentMethodId)
                    event.complete("success")
                } else {
                    result = WalletPaymentResult.Failed(
                        StripeException("No payment method ID received")
                    )
                    event.complete("fail")
                }
            }

            // Show payment request UI
            paymentRequest.show().await()

            return result ?: WalletPaymentResult.Canceled
        } catch (e: Exception) {
            return WalletPaymentResult.Failed(
                if (e is StripeException) e else StripeException(
                    message = "Failed to create payment method: ${e.message}",
                    cause = e
                )
            )
        }
    }

    private fun createPaymentRequest(
        stripe: StripeInstance,
        configuration: GooglePayConfiguration,
        request: WalletPaymentRequest?
    ): StripePaymentRequest {
        val params = js("{}")
        params.country = configuration.merchantCountryCode

        if (request != null) {
            params.currency = request.currencyCode.lowercase()
            params.total = js("{}")
            params.total.label = request.label
            params.total.amount = request.amount
        } else {
            params.currency = "usd"
            params.total = js("{}")
            params.total.label = configuration.merchantName
            params.total.amount = 0
        }

        params.requestPayerName = configuration.billingAddressRequired
        params.requestPayerEmail = configuration.emailRequired
        params.requestShipping = configuration.shippingAddressRequired

        return stripe.asDynamic().paymentRequest(params) as StripePaymentRequest
    }

    public actual companion object {
        public actual fun isAvailable(context: Any?): Boolean {
            // Check if Payment Request API is available
            return js("typeof window !== 'undefined' && 'PaymentRequest' in window") as Boolean
        }
    }
}
