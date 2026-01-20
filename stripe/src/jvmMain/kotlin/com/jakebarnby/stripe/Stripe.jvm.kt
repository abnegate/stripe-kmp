package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import com.stripe.Stripe as StripeJava
import com.stripe.model.Customer as JavaCustomer
import com.stripe.model.EphemeralKey as JavaEphemeralKey
import com.stripe.model.Token as JavaToken
import com.stripe.model.PaymentMethod as JavaPaymentMethod
import com.stripe.model.PaymentIntent as JavaPaymentIntent
import com.stripe.model.SetupIntent as JavaSetupIntent
import com.stripe.model.Source as JavaSource
import com.stripe.param.EphemeralKeyCreateParams as JavaEphemeralKeyCreateParams
import com.stripe.param.PaymentMethodCreateParams as JavaPaymentMethodCreateParams
import com.stripe.param.PaymentIntentConfirmParams as JavaPaymentIntentConfirmParams
import com.stripe.param.SetupIntentConfirmParams as JavaSetupIntentConfirmParams
import com.stripe.param.SourceCreateParams as JavaSourceCreateParams
import com.stripe.net.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * JVM implementation of Stripe using the official stripe-java SDK.
 *
 * This implementation uses stripe-java for all operations, which properly handles
 * Stripe's security requirements.
 *
 * **API Key Handling:**
 * - The publishable key from configuration is set globally via `Stripe.apiKey`
 * - This allows client-side operations (tokens, payment methods, sources) to work
 * - Only one Stripe instance should be active per application
 * - If you need to change keys, call `Stripe.initialize()` again with new configuration
 *
 * **Server-side operations:**
 * - `retrieveCustomer` and `createEphemeralKey` require a secret key
 * - Call `Stripe.setApiKey("sk_test_xxx")` before using these methods
 * - Note: Setting a secret key will override the publishable key for all operations
 */
public actual class Stripe private constructor(
    public actual val configuration: StripeConfiguration
) {
    init {
        // For client-side operations (tokens, payment methods, sources) with publishable keys,
        // stripe-java requires the key to be set globally rather than via RequestOptions
        StripeJava.apiKey = configuration.publishableKey
    }

    // RequestOptions without API key - will use the global Stripe.apiKey
    private val requestOptions = RequestOptions.builder().build()

    // ============================================================================
    // Token Creation
    // ============================================================================

    public actual suspend fun createCardToken(
        params: CardParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Token> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            val tokenParams = buildMap<String, Any> {
                put("card", buildMap<String, Any> {
                    put("number", params.getSanitizedNumber())
                    put("exp_month", params.expMonth)
                    put("exp_year", params.expYear)
                    params.cvc?.let { put("cvc", it) }
                    params.name?.let { put("name", it) }
                    params.addressLine1?.let { put("address_line1", it) }
                    params.addressLine2?.let { put("address_line2", it) }
                    params.addressCity?.let { put("address_city", it) }
                    params.addressState?.let { put("address_state", it) }
                    params.addressZip?.let { put("address_zip", it) }
                    params.addressCountry?.let { put("address_country", it) }
                    params.currency?.let { put("currency", it) }
                })
            }

            val javaToken = JavaToken.create(tokenParams, requestOptions)
            javaToken.toKmpToken()
        }
    }

    public actual suspend fun createBankAccountToken(
        params: BankAccountTokenParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Token> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            val tokenParams = buildMap<String, Any> {
                put("bank_account", buildMap<String, Any> {
                    put("country", params.country)
                    put("currency", params.currency)
                    put("account_number", params.accountNumber)
                    params.routingNumber?.let { put("routing_number", it) }
                    params.accountHolderName?.let { put("account_holder_name", it) }
                    params.accountHolderType?.let { put("account_holder_type", it.value) }
                })
            }

            val javaToken = JavaToken.create(tokenParams, requestOptions)
            javaToken.toKmpToken()
        }
    }

    public actual suspend fun createPiiToken(params: PiiTokenParams): StripeResult<Token> =
        withContext(Dispatchers.IO) {
            StripeResult.runCatching {
                val tokenParams = buildMap<String, Any> {
                    put("pii", buildMap<String, Any> {
                        put("personal_id_number", params.personalIdNumber)
                    })
                }

                val javaToken = JavaToken.create(tokenParams, requestOptions)
                javaToken.toKmpToken()
            }
        }

    public actual suspend fun createAccountToken(params: AccountParams): StripeResult<Token> =
        withContext(Dispatchers.IO) {
            StripeResult.runCatching {
                val tokenParams = buildMap<String, Any> {
                    put("account", buildMap<String, Any> {
                        put("business_type", params.businessType.value)
                        if (params.tosShownAndAccepted) {
                            put("tos_shown_and_accepted", true)
                        }
                    })
                }

                val javaToken = JavaToken.create(tokenParams, requestOptions)
                javaToken.toKmpToken()
            }
        }

    // ============================================================================
    // Source Creation
    // ============================================================================

    public actual suspend fun createSource(
        params: SourceParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Source> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            val sourceParamsBuilder = JavaSourceCreateParams.builder()
                .setType(params.type.value)

            params.amount?.let { sourceParamsBuilder.setAmount(it) }
            params.currency?.let { sourceParamsBuilder.setCurrency(it) }

            // Handle extraParams for card sources
            params.extraParams?.get("card")?.let { cardParams ->
                @Suppress("UNCHECKED_CAST")
                val cardMap = cardParams as? Map<String, Any>
                cardMap?.get("number")?.let { number ->
                    // For card sources with raw card data, use token approach
                    // stripe-java doesn't support raw card params directly
                }
            }

            params.redirect?.let { redirect ->
                sourceParamsBuilder.setRedirect(
                    JavaSourceCreateParams.Redirect.builder()
                        .setReturnUrl(redirect.returnUrl)
                        .build()
                )
            }

            params.owner?.let { owner ->
                val ownerBuilder = JavaSourceCreateParams.Owner.builder()
                owner.name?.let { ownerBuilder.setName(it) }
                owner.email?.let { ownerBuilder.setEmail(it) }
                owner.phone?.let { ownerBuilder.setPhone(it) }
                sourceParamsBuilder.setOwner(ownerBuilder.build())
            }

            val javaSource = JavaSource.create(sourceParamsBuilder.build(), requestOptions)
            javaSource.toKmpSource()
        }
    }

    public actual suspend fun retrieveSource(
        sourceId: String,
        clientSecret: String
    ): StripeResult<Source> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            val javaSource = JavaSource.retrieve(sourceId, requestOptions)
            javaSource.toKmpSource()
        }
    }

    // ============================================================================
    // PaymentMethod
    // ============================================================================

    public actual suspend fun createPaymentMethod(
        params: PaymentMethodCreateParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<PaymentMethod> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            val pmParamsBuilder = JavaPaymentMethodCreateParams.builder()
                .setType(JavaPaymentMethodCreateParams.Type.CARD)

            params.card?.let { card ->
                // Card can be created either with a token or with raw card details
                if (card.token != null) {
                    pmParamsBuilder.setCard(
                        JavaPaymentMethodCreateParams.Token.builder()
                            .setToken(card.token)
                            .build()
                    )
                } else if (card.number != null && card.expMonth != null && card.expYear != null) {
                    pmParamsBuilder.setCard(
                        JavaPaymentMethodCreateParams.CardDetails.builder()
                            .setNumber(card.number)
                            .setExpMonth(card.expMonth.toLong())
                            .setExpYear(card.expYear.toLong())
                            .setCvc(card.cvc)
                            .build()
                    )
                }
            }

            params.billingDetails?.let { billing ->
                val billingBuilder = JavaPaymentMethodCreateParams.BillingDetails.builder()
                billing.name?.let { billingBuilder.setName(it) }
                billing.email?.let { billingBuilder.setEmail(it) }
                billing.phone?.let { billingBuilder.setPhone(it) }
                billing.address?.let { addr ->
                    billingBuilder.setAddress(
                        JavaPaymentMethodCreateParams.BillingDetails.Address.builder()
                            .setLine1(addr.line1)
                            .setLine2(addr.line2)
                            .setCity(addr.city)
                            .setState(addr.state)
                            .setPostalCode(addr.postalCode)
                            .setCountry(addr.country)
                            .build()
                    )
                }
                pmParamsBuilder.setBillingDetails(billingBuilder.build())
            }

            val javaPaymentMethod = JavaPaymentMethod.create(pmParamsBuilder.build(), requestOptions)
            javaPaymentMethod.toKmpPaymentMethod()
        }
    }

    public actual suspend fun retrievePaymentMethod(paymentMethodId: String): StripeResult<PaymentMethod> =
        withContext(Dispatchers.IO) {
            StripeResult.runCatching {
                val javaPaymentMethod = JavaPaymentMethod.retrieve(paymentMethodId, requestOptions)
                javaPaymentMethod.toKmpPaymentMethod()
            }
        }

    // ============================================================================
    // PaymentIntent
    // ============================================================================

    public actual suspend fun retrievePaymentIntent(clientSecret: String): StripeResult<PaymentIntent> =
        withContext(Dispatchers.IO) {
            StripeResult.runCatching {
                // Extract the payment intent ID from the client secret
                val paymentIntentId = clientSecret.substringBefore("_secret_")
                val javaPaymentIntent = JavaPaymentIntent.retrieve(paymentIntentId, requestOptions)
                javaPaymentIntent.toKmpPaymentIntent()
            }
        }

    public actual suspend fun confirmPaymentIntent(
        params: ConfirmPaymentIntentParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<PaymentIntent> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            val paymentIntentId = params.clientSecret.substringBefore("_secret_")
            val javaPaymentIntent = JavaPaymentIntent.retrieve(paymentIntentId, requestOptions)

            val confirmParams = JavaPaymentIntentConfirmParams.builder()
            params.paymentMethodId?.let { confirmParams.setPaymentMethod(it) }
            params.returnUrl?.let { confirmParams.setReturnUrl(it) }

            val confirmed = javaPaymentIntent.confirm(confirmParams.build(), requestOptions)
            confirmed.toKmpPaymentIntent()
        }
    }

    public actual suspend fun handleNextActionForPayment(clientSecret: String): StripeResult<PaymentIntent> =
        withContext(Dispatchers.IO) {
            StripeResult.failure(
                StripeException(
                    "handleNextActionForPayment requires a UI context for authentication. " +
                    "On JVM/server, handle payment authentication via webhooks or redirect-based flows."
                )
            )
        }

    // ============================================================================
    // SetupIntent
    // ============================================================================

    public actual suspend fun retrieveSetupIntent(clientSecret: String): StripeResult<SetupIntent> =
        withContext(Dispatchers.IO) {
            StripeResult.runCatching {
                val setupIntentId = clientSecret.substringBefore("_secret_")
                val javaSetupIntent = JavaSetupIntent.retrieve(setupIntentId, requestOptions)
                javaSetupIntent.toKmpSetupIntent()
            }
        }

    public actual suspend fun confirmSetupIntent(
        params: ConfirmSetupIntentParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<SetupIntent> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            val setupIntentId = params.clientSecret.substringBefore("_secret_")
            val javaSetupIntent = JavaSetupIntent.retrieve(setupIntentId, requestOptions)

            val confirmParams = JavaSetupIntentConfirmParams.builder()
            params.paymentMethodId?.let { confirmParams.setPaymentMethod(it) }
            params.returnUrl?.let { confirmParams.setReturnUrl(it) }

            val confirmed = javaSetupIntent.confirm(confirmParams.build(), requestOptions)
            confirmed.toKmpSetupIntent()
        }
    }

    public actual suspend fun handleNextActionForSetupIntent(clientSecret: String): StripeResult<SetupIntent> =
        withContext(Dispatchers.IO) {
            StripeResult.failure(
                StripeException(
                    "handleNextActionForSetupIntent requires a UI context for authentication. " +
                    "On JVM/server, handle setup intent authentication via webhooks or redirect-based flows."
                )
            )
        }

    // ============================================================================
    // Customer (Server-side only)
    // ============================================================================

    /**
     * Retrieve a customer by ID.
     *
     * **JVM/Server only** - This method requires a secret API key.
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
                val javaCustomer = JavaCustomer.retrieve(customerId, requestOptions)
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
     * **JVM/Server only** - This method requires a secret API key.
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

                val javaKey = JavaEphemeralKey.create(keyParams, requestOptions)
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
         * Set the API key for stripe-java globally.
         *
         * **Use cases:**
         * - Switch to a secret key for server-side operations like `retrieveCustomer`
         * - Override the key for all subsequent Stripe SDK operations
         *
         * **Warning:**
         * - This overrides the publishable key set during initialization
         * - Affects all Stripe instances in the application
         * - Client-side operations (tokens, payment methods) won't work with secret keys
         *
         * @param apiKey The Stripe API key (publishable or secret)
         */
        public fun setApiKey(apiKey: String) {
            StripeJava.apiKey = apiKey
        }
    }
}


// ============================================================================
// Mappers
// ============================================================================

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

private fun JavaPaymentMethod.toKmpPaymentMethod(): PaymentMethod = PaymentMethod(
    id = id,
    type = PaymentMethodType.fromValue(type ?: "unknown"),
    created = created,
    livemode = livemode,
    customer = customer,
    card = card?.let { card ->
        Card(
            brand = CardBrand.fromValue(card.brand ?: "unknown"),
            last4 = card.last4,
            expMonth = card.expMonth?.toInt() ?: 0,
            expYear = card.expYear?.toInt() ?: 0,
            funding = CardFunding.fromValue(card.funding ?: "unknown"),
            country = card.country
        )
    },
    billingDetails = billingDetails?.let { bd ->
        BillingDetails(
            name = bd.name,
            email = bd.email,
            phone = bd.phone,
            address = bd.address?.let { addr ->
                Address(
                    line1 = addr.line1,
                    line2 = addr.line2,
                    city = addr.city,
                    state = addr.state,
                    postalCode = addr.postalCode,
                    country = addr.country
                )
            }
        )
    }
)

private fun JavaPaymentIntent.toKmpPaymentIntent(): PaymentIntent = PaymentIntent(
    id = id,
    amount = amount,
    currency = currency,
    status = PaymentIntentStatus.fromValue(status) ?: PaymentIntentStatus.REQUIRES_PAYMENT_METHOD,
    clientSecret = clientSecret,
    created = created,
    livemode = livemode,
    paymentMethodId = paymentMethod,
    captureMethod = CaptureMethod.fromValue(captureMethod ?: "automatic") ?: CaptureMethod.AUTOMATIC,
    confirmationMethod = ConfirmationMethod.fromValue(confirmationMethod ?: "automatic") ?: ConfirmationMethod.AUTOMATIC,
    description = description,
    receiptEmail = receiptEmail,
    cancellationReason = cancellationReason,
    canceledAt = canceledAt
)

private fun JavaSetupIntent.toKmpSetupIntent(): SetupIntent = SetupIntent(
    id = id,
    status = SetupIntentStatus.fromValue(status) ?: SetupIntentStatus.REQUIRES_PAYMENT_METHOD,
    clientSecret = clientSecret,
    created = created,
    livemode = livemode,
    paymentMethodId = paymentMethod,
    customerId = customer,
    description = description,
    usage = SetupIntentUsage.fromValue(usage ?: "off_session") ?: SetupIntentUsage.OFF_SESSION,
    cancellationReason = cancellationReason
)

private fun JavaSource.toKmpSource(): Source = Source(
    id = id,
    type = SourceType.fromValue(type ?: "unknown"),
    status = SourceStatus.fromValue(status ?: "pending") ?: SourceStatus.PENDING,
    amount = amount,
    currency = currency,
    clientSecret = clientSecret ?: "",
    flow = SourceFlow.fromValue(flow ?: "none") ?: SourceFlow.NONE,
    created = created,
    livemode = livemode
)
