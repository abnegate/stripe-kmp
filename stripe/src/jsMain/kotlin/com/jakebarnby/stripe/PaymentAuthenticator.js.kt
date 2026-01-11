package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import kotlinx.coroutines.await

/**
 * External interface extensions for Stripe.js authentication methods.
 * These methods are part of the Stripe.js API for handling 3DS and other authentication flows.
 */
public external interface StripeAuthenticationMethods {
    /**
     * Handle next action (modern API - Stripe.js v3)
     */
    public fun handleNextAction(options: dynamic): kotlin.js.Promise<StripePaymentIntentResult>

    /**
     * Handle card action (legacy API for PaymentIntent)
     */
    public fun handleCardAction(clientSecret: String): kotlin.js.Promise<StripePaymentIntentResult>

    /**
     * Handle card setup (legacy API for SetupIntent)
     */
    public fun handleCardSetup(clientSecret: String): kotlin.js.Promise<StripeSetupIntentResult>
}

/**
 * JavaScript implementation of PaymentAuthenticator using Stripe.js.
 *
 * This implementation uses Stripe.js methods for handling authentication:
 * - `stripe.handleNextAction()` - Modern API for handling any required action
 * - `stripe.handleCardAction()` - Legacy API for PaymentIntent authentication
 * - `stripe.handleCardSetup()` - Legacy API for SetupIntent authentication
 * - `stripe.confirmCardPayment()` - Combined confirm + authenticate for PaymentIntent
 * - `stripe.confirmCardSetup()` - Combined confirm + authenticate for SetupIntent
 *
 * Stripe.js handles 3DS challenges by:
 * 1. Detecting when authentication is required
 * 2. Presenting the 3DS challenge in an iframe modal
 * 3. Managing the challenge flow with the issuing bank
 * 4. Returning the authenticated result
 *
 * All methods work in the browser and handle redirects/modals automatically.
 */
public actual class PaymentAuthenticator private constructor() {
    private fun getStripeInstance(): StripeInstance {
        val instance = Stripe.getInstance()
        return instance.stripeInstance ?: throw IllegalStateException(
            "Stripe.js not loaded: ${instance.getLoadError() ?: "Unknown error"}. Call Stripe.initialize() and wait for load."
        )
    }

    public actual companion object {
        private var instance: PaymentAuthenticator? = null

        /**
         * Get the singleton instance of PaymentAuthenticator.
         */
        public actual fun getInstance(): PaymentAuthenticator {
            return instance ?: synchronized(PaymentAuthenticator) {
                instance ?: PaymentAuthenticator().also { instance = it }
            }
        }

        private fun <T> synchronized(lock: Any, block: () -> T): T {
            return block()
        }
    }

    /**
     * Handle next action for a payment intent.
     *
     * This is the primary method for handling authentication in JavaScript.
     * It automatically presents 3DS challenges, redirects, or other required actions.
     */
    public actual suspend fun handleNextAction(
        clientSecret: String
    ): AuthenticationResult {
        return try {
            val stripe = getStripeInstance()
            val options = js("{}")
            options.clientSecret = clientSecret

            val authMethods = stripe.unsafeCast<StripeAuthenticationMethods>()
            val result = authMethods.handleNextAction(options).await()

            val error = result.asDynamic().error
            val paymentIntent = result.asDynamic().paymentIntent

            if (error != null && error != undefined) {
                AuthenticationResult.Failed(
                    error = StripeException(
                        message = (error.message as? String) ?: "Authentication failed",
                        stripeError = StripeError(
                            type = (error.type as? String) ?: "unknown",
                            code = error.code as? String,
                            message = (error.message as? String) ?: "Unknown error"
                        )
                    ),
                    paymentIntent = if (paymentIntent != null && paymentIntent != undefined) {
                        convertJsPaymentIntent(paymentIntent)
                    } else null
                )
            } else if (paymentIntent != null && paymentIntent != undefined) {
                AuthenticationResult.Completed(
                    paymentIntent = convertJsPaymentIntent(paymentIntent)
                )
            } else {
                AuthenticationResult.Failed(
                    error = StripeException("No payment intent returned from authentication")
                )
            }
        } catch (e: Exception) {
            AuthenticationResult.Failed(
                error = StripeException(
                    message = e.message ?: "Authentication failed",
                    cause = e
                )
            )
        }
    }

    /**
     * Handle next action for a payment intent with activity context.
     *
     * On JavaScript, the activity parameter is ignored as Stripe.js handles
     * UI presentation automatically in the browser.
     */
    public actual suspend fun handleNextActionForPayment(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult {
        // In JS, we don't need the activity context - Stripe.js handles UI automatically
        return handleNextAction(clientSecret)
    }

    /**
     * Handle next action for a setup intent with activity context.
     *
     * On JavaScript, the activity parameter is ignored as Stripe.js handles
     * UI presentation automatically in the browser.
     */
    public actual suspend fun handleNextActionForSetupIntent(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult {
        return try {
            val stripe = getStripeInstance()
            // Use handleCardSetup for SetupIntent
            val authMethods = stripe.unsafeCast<StripeAuthenticationMethods>()
            val result = authMethods.handleCardSetup(clientSecret).await()

            val error = result.asDynamic().error
            val setupIntent = result.asDynamic().setupIntent

            if (error != null && error != undefined) {
                AuthenticationResult.Failed(
                    error = StripeException(
                        message = (error.message as? String) ?: "Authentication failed",
                        stripeError = StripeError(
                            type = (error.type as? String) ?: "unknown",
                            code = error.code as? String,
                            message = (error.message as? String) ?: "Unknown error"
                        )
                    ),
                    setupIntent = if (setupIntent != null && setupIntent != undefined) {
                        convertJsSetupIntent(setupIntent)
                    } else null
                )
            } else if (setupIntent != null && setupIntent != undefined) {
                AuthenticationResult.Completed(
                    setupIntent = convertJsSetupIntent(setupIntent)
                )
            } else {
                AuthenticationResult.Failed(
                    error = StripeException("No setup intent returned from authentication")
                )
            }
        } catch (e: Exception) {
            AuthenticationResult.Failed(
                error = StripeException(
                    message = e.message ?: "Authentication failed",
                    cause = e
                )
            )
        }
    }

    /**
     * Authenticate a payment intent end-to-end.
     *
     * This retrieves the PaymentIntent and handles any required actions.
     */
    public actual suspend fun authenticatePayment(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult {
        return try {
            val stripe = getStripeInstance()
            // First retrieve the payment intent to check if action is required
            val retrieveResult = stripe.retrievePaymentIntent(clientSecret).await()

            if (retrieveResult.error != null) {
                return AuthenticationResult.Failed(
                    error = StripeException(
                        message = retrieveResult.error.message as? String ?: "Failed to retrieve PaymentIntent"
                    )
                )
            }

            val paymentIntent = retrieveResult.paymentIntent
                ?: return AuthenticationResult.Failed(
                    error = StripeException("No PaymentIntent returned")
                )

            val status = paymentIntent.status as String

            when (status) {
                "succeeded" -> AuthenticationResult.Completed(
                    paymentIntent = convertJsPaymentIntent(paymentIntent)
                )
                "requires_action" -> handleNextAction(clientSecret)
                else -> AuthenticationResult.Completed(
                    paymentIntent = convertJsPaymentIntent(paymentIntent)
                )
            }
        } catch (e: Exception) {
            AuthenticationResult.Failed(
                error = StripeException(
                    message = e.message ?: "Authentication failed",
                    cause = e
                )
            )
        }
    }

    /**
     * Authenticate a setup intent end-to-end.
     *
     * This retrieves the SetupIntent and handles any required actions.
     */
    public actual suspend fun authenticateSetupIntent(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult {
        return try {
            val stripe = getStripeInstance()
            // First retrieve the setup intent to check if action is required
            val retrieveResult = stripe.retrieveSetupIntent(clientSecret).await()

            if (retrieveResult.error != null) {
                return AuthenticationResult.Failed(
                    error = StripeException(
                        message = retrieveResult.error.message as? String ?: "Failed to retrieve SetupIntent"
                    )
                )
            }

            val setupIntent = retrieveResult.setupIntent
                ?: return AuthenticationResult.Failed(
                    error = StripeException("No SetupIntent returned")
                )

            val status = setupIntent.status as String

            when (status) {
                "succeeded" -> AuthenticationResult.Completed(
                    setupIntent = convertJsSetupIntent(setupIntent)
                )
                "requires_action" -> handleNextActionForSetupIntent(activity, clientSecret)
                else -> AuthenticationResult.Completed(
                    setupIntent = convertJsSetupIntent(setupIntent)
                )
            }
        } catch (e: Exception) {
            AuthenticationResult.Failed(
                error = StripeException(
                    message = e.message ?: "Authentication failed",
                    cause = e
                )
            )
        }
    }

    /**
     * Handle 3DS2 challenge specifically.
     *
     * On JavaScript, Stripe.js automatically handles 3DS2 challenges when using
     * handleNextAction. This method returns information about the challenge
     * but the actual handling is done by Stripe.js automatically.
     */
    public actual suspend fun handleChallenge(
        activity: Any,
        challenge: ThreeDSecureChallenge
    ): Stripe3ds2AuthenticationResponse {
        // Stripe.js handles 3DS2 challenges automatically
        // We return a response indicating the challenge needs to be handled via normal flow
        return Stripe3ds2AuthenticationResponse(
            id = challenge.threeDSecureServerTransactionId,
            ares = null,
            error = null,
            fallbackRedirectUrl = challenge.acsUrl,
            state = AuthenticationState.CHALLENGED
        )
    }

    // ============================================================================
    // Private helper methods for converting JS objects to KMP models
    // ============================================================================

    private fun convertJsPaymentIntent(jsIntent: dynamic): PaymentIntent {
        val status = when (jsIntent.status as String) {
            "requires_payment_method" -> PaymentIntentStatus.REQUIRES_PAYMENT_METHOD
            "requires_confirmation" -> PaymentIntentStatus.REQUIRES_CONFIRMATION
            "requires_action" -> PaymentIntentStatus.REQUIRES_ACTION
            "processing" -> PaymentIntentStatus.PROCESSING
            "requires_capture" -> PaymentIntentStatus.REQUIRES_CAPTURE
            "canceled" -> PaymentIntentStatus.CANCELED
            "succeeded" -> PaymentIntentStatus.SUCCEEDED
            else -> PaymentIntentStatus.PROCESSING
        }

        return PaymentIntent(
            id = jsIntent.id as String,
            clientSecret = jsIntent.client_secret as String,
            amount = (jsIntent.amount as Number).toLong(),
            currency = jsIntent.currency as String,
            status = status,
            created = (jsIntent.created as Number).toLong(),
            livemode = jsIntent.livemode as Boolean,
            paymentMethodId = jsIntent.payment_method as? String
        )
    }

    private fun convertJsSetupIntent(jsIntent: dynamic): SetupIntent {
        val status = when (jsIntent.status as String) {
            "requires_payment_method" -> SetupIntentStatus.REQUIRES_PAYMENT_METHOD
            "requires_confirmation" -> SetupIntentStatus.REQUIRES_CONFIRMATION
            "requires_action" -> SetupIntentStatus.REQUIRES_ACTION
            "processing" -> SetupIntentStatus.PROCESSING
            "canceled" -> SetupIntentStatus.CANCELED
            "succeeded" -> SetupIntentStatus.SUCCEEDED
            else -> SetupIntentStatus.PROCESSING
        }

        return SetupIntent(
            id = jsIntent.id as String,
            clientSecret = jsIntent.client_secret as String,
            created = (jsIntent.created as Number).toLong(),
            livemode = jsIntent.livemode as Boolean,
            status = status,
            paymentMethodId = jsIntent.payment_method as? String
        )
    }
}
