package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*

/**
 * WASM Stripe implementation.
 *
 * WARNING: WASM support for Stripe is currently experimental and limited.
 * For production use, please use the JS, Android, or iOS platforms.
 *
 * WASM-JS interop for Stripe.js is not yet fully functional. Consider using
 * the regular JS target instead if you need web support.
 *
 * All API methods return unsupported errors on WASM platform.
 */
public actual class Stripe private constructor(
    public actual val configuration: StripeConfiguration
) {
    // ============================================================================
    // Token Creation
    // ============================================================================

    public actual suspend fun createCardToken(params: CardParams, idempotencyKey: IdempotencyKey?): StripeResult<Token> {
        return StripeResult.failure(
            StripeException("WASM platform not supported. Use JS, Android, or iOS instead.")
        )
    }

    public actual suspend fun createBankAccountToken(params: BankAccountTokenParams, idempotencyKey: IdempotencyKey?): StripeResult<Token> {
        return StripeResult.failure(
            StripeException("WASM platform not supported. Use JS, Android, or iOS instead.")
        )
    }

    public actual suspend fun createPiiToken(params: PiiTokenParams): StripeResult<Token> {
        return StripeResult.failure(
            StripeException("WASM platform not supported. Use JS, Android, or iOS instead.")
        )
    }

    public actual suspend fun createAccountToken(params: AccountParams): StripeResult<Token> {
        return StripeResult.failure(
            StripeException("WASM platform not supported. Use JS, Android, or iOS instead.")
        )
    }

    // ============================================================================
    // Source Creation
    // ============================================================================

    public actual suspend fun createSource(params: SourceParams, idempotencyKey: IdempotencyKey?): StripeResult<Source> {
        return StripeResult.failure(
            StripeException("WASM platform not supported. Use JS, Android, or iOS instead.")
        )
    }

    public actual suspend fun retrieveSource(sourceId: String, clientSecret: String): StripeResult<Source> {
        return StripeResult.failure(
            StripeException("WASM platform not supported. Use JS, Android, or iOS instead.")
        )
    }

    // ============================================================================
    // PaymentMethod
    // ============================================================================

    public actual suspend fun createPaymentMethod(params: PaymentMethodCreateParams, idempotencyKey: IdempotencyKey?): StripeResult<PaymentMethod> {
        return StripeResult.failure(
            StripeException("WASM platform not supported. Use JS, Android, or iOS instead.")
        )
    }

    public actual suspend fun retrievePaymentMethod(paymentMethodId: String): StripeResult<PaymentMethod> {
        return StripeResult.failure(
            StripeException("WASM platform not supported. Use JS, Android, or iOS instead.")
        )
    }

    // ============================================================================
    // PaymentIntent
    // ============================================================================

    public actual suspend fun retrievePaymentIntent(clientSecret: String): StripeResult<PaymentIntent> {
        return StripeResult.failure(
            StripeException("WASM platform not supported. Use JS, Android, or iOS instead.")
        )
    }

    public actual suspend fun confirmPaymentIntent(params: ConfirmPaymentIntentParams, idempotencyKey: IdempotencyKey?): StripeResult<PaymentIntent> {
        return StripeResult.failure(
            StripeException("WASM platform not supported. Use JS, Android, or iOS instead.")
        )
    }

    public actual suspend fun handleNextActionForPayment(clientSecret: String): StripeResult<PaymentIntent> {
        return StripeResult.failure(
            StripeException("WASM platform not supported. Use JS, Android, or iOS instead.")
        )
    }

    // ============================================================================
    // SetupIntent
    // ============================================================================

    public actual suspend fun retrieveSetupIntent(clientSecret: String): StripeResult<SetupIntent> {
        return StripeResult.failure(
            StripeException("WASM platform not supported. Use JS, Android, or iOS instead.")
        )
    }

    public actual suspend fun confirmSetupIntent(params: ConfirmSetupIntentParams, idempotencyKey: IdempotencyKey?): StripeResult<SetupIntent> {
        return StripeResult.failure(
            StripeException("WASM platform not supported. Use JS, Android, or iOS instead.")
        )
    }

    public actual suspend fun handleNextActionForSetupIntent(clientSecret: String): StripeResult<SetupIntent> {
        return StripeResult.failure(
            StripeException("WASM platform not supported. Use JS, Android, or iOS instead.")
        )
    }

    // ============================================================================
    // Customer
    // ============================================================================

    public actual suspend fun retrieveCustomer(customerId: String): StripeResult<Customer> {
        return StripeResult.failure(
            StripeException("WASM platform not supported. Use JS, Android, or iOS instead.")
        )
    }

    public actual suspend fun createEphemeralKey(params: EphemeralKeyCreateParams): StripeResult<EphemeralKey> {
        return StripeResult.failure(
            StripeException("WASM platform not supported. Use JS, Android, or iOS instead.")
        )
    }

    public actual companion object {
        private var instance: Stripe? = null

        public actual fun initialize(configuration: StripeConfiguration): Stripe {
            // CRITICAL-03: Make it clear WASM is not fully supported
            if (configuration.enableLogging) {
                println("WARNING: WASM support is experimental. Use JS, Android, or iOS for production.")
            }

            val stripe = Stripe(configuration)
            instance = stripe
            return stripe
        }

        /**
         * Get the current Stripe instance.
         *
         * @return The current Stripe instance
         * @throws IllegalStateException if Stripe has not been initialized
         */
        public actual fun getInstance(): Stripe {
            return requireNotNull(instance) {
                "Stripe has not been initialized. Call Stripe.initialize() first."
            }
        }
    }
}
