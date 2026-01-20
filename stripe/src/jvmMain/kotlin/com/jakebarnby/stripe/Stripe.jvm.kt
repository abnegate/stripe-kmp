package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import com.stripe.Stripe as StripeJava
import com.stripe.model.Customer as JavaCustomer
import com.stripe.model.EphemeralKey as JavaEphemeralKey
import com.stripe.model.Token as JavaToken
import com.stripe.param.EphemeralKeyCreateParams as JavaEphemeralKeyCreateParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * JVM implementation of Stripe using both StripeApiClient (for client operations)
 * and the official stripe-java SDK (for server-side operations).
 *
 * Client operations (tokens, payment methods, etc.) use StripeApiClient with
 * the publishable key, consistent with other platforms (Android, iOS, JS).
 *
 * Server-side operations (retrieveCustomer, createEphemeralKey) require a
 * secret key set via Stripe.setApiKey("sk_test_xxx") before calling.
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

    public actual suspend fun createPiiToken(params: PiiTokenParams): StripeResult<Token> =
        apiClient.createPiiToken(params)

    public actual suspend fun createAccountToken(params: AccountParams): StripeResult<Token> =
        withContext(Dispatchers.IO) {
            StripeResult.runCatching {
                val tokenParams = buildMap {
                    put("account", buildMap {
                        params.businessType?.let { put("business_type", it) }
                        params.tosShownAndAccepted?.let { put("tos_shown_and_accepted", it) }
                    })
                }

                val javaToken = JavaToken.create(tokenParams)
                javaToken.toKmpToken()
            }
        }


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

    public actual suspend fun retrievePaymentMethod(paymentMethodId: String): StripeResult<PaymentMethod> =
        apiClient.retrievePaymentMethod(paymentMethodId)


    public actual suspend fun retrievePaymentIntent(clientSecret: String): StripeResult<PaymentIntent> =
        apiClient.retrievePaymentIntent(clientSecret)

    public actual suspend fun confirmPaymentIntent(
        params: ConfirmPaymentIntentParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<PaymentIntent> = apiClient.confirmPaymentIntent(params, idempotencyKey)

    public actual suspend fun handleNextActionForPayment(clientSecret: String): StripeResult<PaymentIntent> =
        apiClient.handleNextActionForPayment(clientSecret)


    public actual suspend fun retrieveSetupIntent(clientSecret: String): StripeResult<SetupIntent> =
        apiClient.retrieveSetupIntent(clientSecret)

    public actual suspend fun confirmSetupIntent(
        params: ConfirmSetupIntentParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<SetupIntent> = apiClient.confirmSetupIntent(params, idempotencyKey)

    public actual suspend fun handleNextActionForSetupIntent(clientSecret: String): StripeResult<SetupIntent> =
        apiClient.handleNextActionForSetupIntent(clientSecret)


    /**
     * Retrieve a customer by ID.
     *
     * **JVM/Server only** - This method requires a secret API key and is only
     * available on the JVM target. It is not visible on Android, iOS, JS, or WASM.
     *
     * Before calling this method, set your secret key:
     * ```kotlin
     * Stripe.setApiKey("sk_test_xxx")
     * ```
     *
     * @param customerId The ID of the customer to retrieve
     * @return Result containing the customer or an error
     */
    public suspend fun retrieveCustomer(customerId: String): StripeResult<Customer> =
        withContext(Dispatchers.IO) {
            StripeResult.runCatching {
                val javaCustomer = JavaCustomer.retrieve(customerId)
                Customer(
                    id = javaCustomer.id,
                    email = javaCustomer.email,
                    name = javaCustomer.name,
                    phone = javaCustomer.phone,
                    created = javaCustomer.created,
                    livemode = javaCustomer.livemode,
                    defaultSource = javaCustomer.defaultSource,
                    metadata = javaCustomer.metadata
                )
            }
        }

    /**
     * Create an ephemeral key for a customer.
     *
     * **JVM/Server only** - This method requires a secret API key and is only
     * available on the JVM target. It is not visible on Android, iOS, JS, or WASM.
     *
     * Ephemeral keys are short-lived API keys that grant limited access to
     * customer data. They are used to securely access customer information
     * from client-side code without exposing your secret key.
     *
     * Before calling this method, set your secret key:
     * ```kotlin
     * Stripe.setApiKey("sk_test_xxx")
     * ```
     *
     * @param params Parameters for creating the ephemeral key
     * @return Result containing the ephemeral key or an error
     */
    public suspend fun createEphemeralKey(params: EphemeralKeyCreateParams): StripeResult<EphemeralKey> =
        withContext(Dispatchers.IO) {
            StripeResult.runCatching {
                val keyParams = JavaEphemeralKeyCreateParams.builder()
                    .setCustomer(params.customerId)
                    .setStripeVersion(params.stripeVersion)
                    .build()

                val javaKey = JavaEphemeralKey.create(keyParams)
                EphemeralKey(
                    id = javaKey.id,
                    secret = javaKey.secret,
                    created = javaKey.created,
                    expires = javaKey.expires,
                    livemode = javaKey.livemode
                )
            }
        }

    public actual companion object {
        private var instance: Stripe? = null

        public actual fun initialize(configuration: StripeConfiguration): Stripe {
            val stripe = Stripe(configuration)
            instance = stripe
            return stripe
        }

        public actual fun getInstance(): Stripe {
            return requireNotNull(instance) {
                "Stripe has not been initialized. Call Stripe.initialize() first."
            }
        }

        /**
         * Set the API key for stripe-java directly.
         * Use this for server-side operations with your secret key.
         */
        public fun setApiKey(apiKey: String) {
            StripeJava.apiKey = apiKey
        }
    }
}


private fun JavaToken.toKmpToken(): Token = Token(
    id = id,
    type = type ?: "unknown",
    created = created,
    livemode = livemode,
    used = used,
    card = card?.let { card ->
        CardToken(
            id = card.id,
            brand = card.brand,
            last4 = card.last4,
            expMonth = card.expMonth.toInt(),
            expYear = card.expYear.toInt(),
            funding = card.funding,
            country = card.country
        )
    },
    bankAccount = bankAccount?.let { ba ->
        BankAccountToken(
            id = ba.id,
            country = ba.country,
            currency = ba.currency,
            last4 = ba.last4,
            bankName = ba.bankName,
            accountHolderName = ba.accountHolderName,
            accountHolderType = ba.accountHolderType,
            routingNumber = ba.routingNumber
        )
    }
)
