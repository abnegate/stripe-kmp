package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import kotlinx.coroutines.await
import kotlin.js.Promise

/**
 * JavaScript implementation of ApplePayLauncher using Stripe.js Payment Request API.
 *
 * This implementation uses the Payment Request Button API which supports
 * Apple Pay on Safari and compatible browsers.
 */
public actual class ApplePayLauncher {
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
        configuration: ApplePayConfiguration
    ): WalletPaymentResult {
        val stripe = stripeInstance
            ?: return WalletPaymentResult.Failed(
                StripeException("Stripe instance not set. Call setStripeInstance() first.")
            )

        try {
            // Create payment request
            val paymentRequest = createPaymentRequest(stripe, configuration, null)

            // Check if Apple Pay is available
            val canMakePayment = paymentRequest.canMakePayment().await()
            if (canMakePayment?.applePay != true) {
                return WalletPaymentResult.Failed(
                    StripeException("Apple Pay is not available in this browser")
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
                    message = "Failed to present Apple Pay: ${e.message}",
                    cause = e
                )
            )
        }
    }

    public actual suspend fun presentForSetupIntent(
        clientSecret: String,
        configuration: ApplePayConfiguration
    ): WalletPaymentResult {
        return WalletPaymentResult.Failed(
            StripeException("SetupIntent with Apple Pay not fully supported in Payment Request API")
        )
    }

    public actual suspend fun createPaymentMethod(
        configuration: ApplePayConfiguration,
        request: WalletPaymentRequest
    ): WalletPaymentResult {
        val stripe = stripeInstance
            ?: return WalletPaymentResult.Failed(
                StripeException("Stripe instance not set. Call setStripeInstance() first.")
            )

        try {
            // Create payment request
            val paymentRequest = createPaymentRequest(stripe, configuration, request)

            // Check if Apple Pay is available
            val canMakePayment = paymentRequest.canMakePayment().await()
            if (canMakePayment?.applePay != true) {
                return WalletPaymentResult.Failed(
                    StripeException("Apple Pay is not available in this browser")
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
        configuration: ApplePayConfiguration,
        request: WalletPaymentRequest?
    ): StripePaymentRequest {
        val params = js("{}")
        params.country = configuration.merchantCountryCode
        params.currency = configuration.currencyCode.lowercase()

        if (request != null) {
            params.total = js("{}")
            params.total.label = request.label
            params.total.amount = request.amount
        } else {
            params.total = js("{}")
            params.total.label = configuration.merchantIdentifier
            params.total.amount = 0
        }

        params.requestPayerName = configuration.requiredBillingContactFields.contains(ApplePayContactField.NAME)
        params.requestPayerEmail = configuration.requiredBillingContactFields.contains(ApplePayContactField.EMAIL)
        params.requestPayerPhone = configuration.requiredBillingContactFields.contains(ApplePayContactField.PHONE)
        params.requestShipping = configuration.requiredShippingContactFields.isNotEmpty()

        return stripe.asDynamic().paymentRequest(params) as StripePaymentRequest
    }

    public actual companion object {
        public actual fun isAvailable(): Boolean {
            // Check if Apple Pay is available (Safari on Apple devices)
            return js(
                "typeof window !== 'undefined' && " +
                "'ApplePaySession' in window && " +
                "window.ApplePaySession.canMakePayments()"
            ) as Boolean
        }

        public actual fun canMakePayments(): Boolean {
            return js(
                "typeof window !== 'undefined' && " +
                "'ApplePaySession' in window && " +
                "window.ApplePaySession.canMakePayments()"
            ) as Boolean
        }

        public actual fun canMakePaymentsWithNetworks(networks: List<CardBrand>): Boolean {
            // In web environment, we can't easily check specific networks
            // Just return the general availability
            return canMakePayments()
        }
    }
}
