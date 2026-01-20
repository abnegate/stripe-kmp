package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*

/**
 * WASM Stripe implementation using shared REST API client.
 *
 * Headless operations (tokens, payment methods, intents) use the REST API via Ktor.
 * UI components (PaymentSheet, Apple Pay, Google Pay) are not supported on WASM.
 *
 * Note: Browser-hosted WASM is subject to CORS and Stripe client-side restrictions.
 */
public actual class Stripe private constructor(
    public actual val configuration: StripeConfiguration
) {
    private val apiClient = StripeApiClient(configuration, createHttpClientEngine())


    public actual suspend fun createCardToken(
        params: CardParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Token> = apiClient.createCardToken(params, idempotencyKey)

    public actual suspend fun createBankAccountToken(
        params: BankAccountTokenParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Token> = apiClient.createBankAccountToken(params, idempotencyKey)

    public actual suspend fun createPiiToken(
        params: PiiTokenParams
    ): StripeResult<Token> = apiClient.createPiiToken(params)

    public actual suspend fun createAccountToken(
        params: AccountParams
    ): StripeResult<Token> = apiClient.createAccountToken(params)


    public actual suspend fun createSource(
        params: SourceParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Source> = apiClient.createSource(params, idempotencyKey)

    public actual suspend fun retrieveSource(
        sourceId: String,
        clientSecret: String
    ): StripeResult<Source> = apiClient.retrieveSource(sourceId, clientSecret)


    public actual suspend fun createPaymentMethod(
        params: PaymentMethodCreateParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<PaymentMethod> = apiClient.createPaymentMethod(params, idempotencyKey)

    public actual suspend fun retrievePaymentMethod(
        paymentMethodId: String
    ): StripeResult<PaymentMethod> = apiClient.retrievePaymentMethod(paymentMethodId)


    public actual suspend fun retrievePaymentIntent(
        clientSecret: String
    ): StripeResult<PaymentIntent> = apiClient.retrievePaymentIntent(clientSecret)

    public actual suspend fun confirmPaymentIntent(
        params: ConfirmPaymentIntentParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<PaymentIntent> = apiClient.confirmPaymentIntent(params, idempotencyKey)

    public actual suspend fun handleNextActionForPayment(
        clientSecret: String
    ): StripeResult<PaymentIntent> = apiClient.handleNextActionForPayment(clientSecret)


    public actual suspend fun retrieveSetupIntent(
        clientSecret: String
    ): StripeResult<SetupIntent> = apiClient.retrieveSetupIntent(clientSecret)

    public actual suspend fun confirmSetupIntent(
        params: ConfirmSetupIntentParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<SetupIntent> = apiClient.confirmSetupIntent(params, idempotencyKey)

    public actual suspend fun handleNextActionForSetupIntent(
        clientSecret: String
    ): StripeResult<SetupIntent> = apiClient.handleNextActionForSetupIntent(clientSecret)

    public actual companion object {
        private var instance: Stripe? = null

        public actual fun initialize(configuration: StripeConfiguration): Stripe {
            if (configuration.enableLogging) {
                println("WARNING: WASM support is experimental. Browser environments may require Stripe.js due to CORS.")
            }

            val stripe = Stripe(configuration)
            instance = stripe
            return stripe
        }

        public actual fun getInstance(): Stripe {
            return requireNotNull(instance) {
                "Stripe has not been initialized. Call Stripe.initialize() first."
            }
        }
    }
}
