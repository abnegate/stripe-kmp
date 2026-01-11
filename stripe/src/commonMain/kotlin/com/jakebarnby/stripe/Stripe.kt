package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*

/**
 * Main Stripe SDK interface for Kotlin Multiplatform.
 * This provides a unified API across Android, iOS, JS, and WASM platforms.
 */
public expect class Stripe {
    public companion object {
        /**
         * Initialize the Stripe SDK with the provided configuration.
         *
         * @param configuration The Stripe configuration containing publishable key and settings
         * @return A configured Stripe instance
         * @throws IllegalArgumentException if configuration is invalid
         */
        public fun initialize(configuration: StripeConfiguration): Stripe

        /**
         * Get the current Stripe instance.
         *
         * @return The current Stripe instance
         * @throws IllegalStateException if Stripe has not been initialized
         */
        public fun getInstance(): Stripe
    }

    /**
     * Get the configuration used to initialize this Stripe instance.
     */
    public val configuration: StripeConfiguration

    // ============================================================================
    // Token Creation
    // ============================================================================

    /**
     * Create a card token.
     * Tokens are single-use and can be used to create charges or save payment methods.
     *
     * @param params Card parameters including number, expiry, and CVC
     * @param idempotencyKey Optional idempotency key for safe request retries
     * @return Result containing the created token or an error
     */
    public suspend fun createCardToken(params: CardParams, idempotencyKey: IdempotencyKey? = null): StripeResult<Token>

    /**
     * Create a bank account token.
     *
     * @param params Bank account parameters
     * @param idempotencyKey Optional idempotency key for safe request retries
     * @return Result containing the created token or an error
     */
    public suspend fun createBankAccountToken(params: BankAccountTokenParams, idempotencyKey: IdempotencyKey? = null): StripeResult<Token>

    /**
     * Create a PII (Personal Identifiable Information) token.
     * Used for identity verification purposes.
     *
     * @param params PII token parameters
     * @return Result containing the created token or an error
     */
    public suspend fun createPiiToken(params: PiiTokenParams): StripeResult<Token>

    /**
     * Create an account token for Stripe Connect.
     *
     * @param params Account parameters
     * @return Result containing the created token or an error
     */
    public suspend fun createAccountToken(params: AccountParams): StripeResult<Token>

    // ============================================================================
    // Source Creation (Legacy - prefer PaymentMethod)
    // ============================================================================

    /**
     * Create a source.
     * Sources are a legacy payment method type - prefer PaymentMethod for new integrations.
     *
     * @param params Source creation parameters
     * @param idempotencyKey Optional idempotency key for safe request retries
     * @return Result containing the created source or an error
     */
    public suspend fun createSource(params: SourceParams, idempotencyKey: IdempotencyKey? = null): StripeResult<Source>

    /**
     * Retrieve a source by ID and client secret.
     *
     * @param sourceId The ID of the source
     * @param clientSecret The client secret for the source
     * @return Result containing the source or an error
     */
    public suspend fun retrieveSource(sourceId: String, clientSecret: String): StripeResult<Source>

    // ============================================================================
    // PaymentMethod
    // ============================================================================

    /**
     * Create a payment method.
     * PaymentMethods are the preferred way to handle payment instruments.
     *
     * @param params Payment method creation parameters
     * @param idempotencyKey Optional idempotency key for safe request retries
     * @return Result containing the created payment method or an error
     */
    public suspend fun createPaymentMethod(params: PaymentMethodCreateParams, idempotencyKey: IdempotencyKey? = null): StripeResult<PaymentMethod>

    /**
     * Retrieve a payment method by ID.
     *
     * @param paymentMethodId The ID of the payment method
     * @return Result containing the payment method or an error
     */
    public suspend fun retrievePaymentMethod(paymentMethodId: String): StripeResult<PaymentMethod>

    // ============================================================================
    // PaymentIntent
    // ============================================================================

    /**
     * Retrieve a PaymentIntent by client secret.
     *
     * @param clientSecret The client secret for the PaymentIntent
     * @return Result containing the PaymentIntent or an error
     */
    public suspend fun retrievePaymentIntent(clientSecret: String): StripeResult<PaymentIntent>

    /**
     * Confirm a PaymentIntent.
     * This will attempt to complete the payment, potentially requiring additional authentication.
     *
     * @param params Parameters for confirming the PaymentIntent
     * @param idempotencyKey Optional idempotency key for safe request retries
     * @return Result containing the confirmed PaymentIntent or an error
     */
    public suspend fun confirmPaymentIntent(params: ConfirmPaymentIntentParams, idempotencyKey: IdempotencyKey? = null): StripeResult<PaymentIntent>

    /**
     * Handle the next action for a PaymentIntent.
     * Use this when a PaymentIntent requires additional authentication or actions.
     *
     * @param clientSecret The client secret for the PaymentIntent
     * @return Result containing the updated PaymentIntent or an error
     */
    public suspend fun handleNextActionForPayment(clientSecret: String): StripeResult<PaymentIntent>

    // ============================================================================
    // SetupIntent
    // ============================================================================

    /**
     * Retrieve a SetupIntent by client secret.
     *
     * @param clientSecret The client secret for the SetupIntent
     * @return Result containing the SetupIntent or an error
     */
    public suspend fun retrieveSetupIntent(clientSecret: String): StripeResult<SetupIntent>

    /**
     * Confirm a SetupIntent.
     * This will set up a payment method for future use without charging.
     *
     * @param params Parameters for confirming the SetupIntent
     * @param idempotencyKey Optional idempotency key for safe request retries
     * @return Result containing the confirmed SetupIntent or an error
     */
    public suspend fun confirmSetupIntent(params: ConfirmSetupIntentParams, idempotencyKey: IdempotencyKey? = null): StripeResult<SetupIntent>

    /**
     * Handle the next action for a SetupIntent.
     * Use this when a SetupIntent requires additional authentication or actions.
     *
     * @param clientSecret The client secret for the SetupIntent
     * @return Result containing the updated SetupIntent or an error
     */
    public suspend fun handleNextActionForSetupIntent(clientSecret: String): StripeResult<SetupIntent>

    // ============================================================================
    // Customer
    // ============================================================================

    /**
     * Retrieve a customer by ID.
     *
     * @param customerId The ID of the customer
     * @return Result containing the customer or an error
     */
    public suspend fun retrieveCustomer(customerId: String): StripeResult<Customer>

    /**
     * Create an ephemeral key.
     * Ephemeral keys are short-lived API keys used for client-side operations.
     *
     * Note: This typically requires a server-side endpoint to create the key securely.
     *
     * @param params Parameters for creating the ephemeral key
     * @return Result containing the ephemeral key or an error
     */
    public suspend fun createEphemeralKey(params: EphemeralKeyCreateParams): StripeResult<EphemeralKey>
}
