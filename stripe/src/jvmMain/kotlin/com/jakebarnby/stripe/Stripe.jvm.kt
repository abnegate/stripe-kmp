package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import com.jakebarnby.stripe.http.StripeHttpClient
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
import com.stripe.exception.StripeException as JavaStripeException
import com.stripe.exception.ApiException as JavaApiException
import com.stripe.exception.AuthenticationException as JavaAuthenticationException
import com.stripe.exception.CardException as JavaCardException
import com.stripe.exception.InvalidRequestException as JavaInvalidRequestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

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
    // HTTP client for direct API calls (used with publishable keys for client-side operations)
    private val httpClient = StripeHttpClient(configuration.publishableKey)

    init {
        // Set stripe-java API key globally for server-side operations
        // Note: stripe-java doesn't support publishable keys for client operations,
        // so we use direct HTTP calls for those (see httpClient above)
        StripeJava.apiKey = configuration.publishableKey

        if (configuration.enableLogging) {
            println("[Stripe JVM] Initialized with key: ${configuration.publishableKey.take(12)}...")
            println("[Stripe JVM] Key type: ${if (configuration.publishableKey.startsWith("pk_test_")) "test publishable" else if (configuration.publishableKey.startsWith("sk_test_")) "test secret" else "unknown"}")
            println("[Stripe JVM] Using HTTP client for client-side operations (tokens, payment methods)")
        }
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
        stripeRunCatching {
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

            // Use HTTP client for publishable key operations
            val response = httpClient.post("tokens", tokenParams, idempotencyKey?.value)
            response.toKmpToken()
        }
    }

    public actual suspend fun createBankAccountToken(
        params: BankAccountTokenParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Token> = withContext(Dispatchers.IO) {
        stripeRunCatching {
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

            val response = httpClient.post("tokens", tokenParams, idempotencyKey?.value)
            response.toKmpToken()
        }
    }

    public actual suspend fun createPiiToken(params: PiiTokenParams): StripeResult<Token> =
        withContext(Dispatchers.IO) {
            stripeRunCatching {
                val tokenParams = buildMap<String, Any> {
                    put("pii", buildMap<String, Any> {
                        put("personal_id_number", params.personalIdNumber)
                    })
                }

                val response = httpClient.post("tokens", tokenParams)
                response.toKmpToken()
            }
        }

    public actual suspend fun createAccountToken(params: AccountParams): StripeResult<Token> =
        withContext(Dispatchers.IO) {
            stripeRunCatching {
                val tokenParams = buildMap<String, Any> {
                    put("account", buildMap<String, Any> {
                        put("business_type", params.businessType.value)
                        if (params.tosShownAndAccepted) {
                            put("tos_shown_and_accepted", true)
                        }
                    })
                }

                val response = httpClient.post("tokens", tokenParams)
                response.toKmpToken()
            }
        }

    // ============================================================================
    // Source Creation
    // ============================================================================

    public actual suspend fun createSource(
        params: SourceParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Source> = withContext(Dispatchers.IO) {
        stripeRunCatching {
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
        stripeRunCatching {
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
        stripeRunCatching {
            val pmParams = buildMap<String, Any> {
                put("type", "card")

                params.card?.let { card ->
                    // Card can be created either with a token or with raw card details
                    if (card.token != null) {
                        put("card", mapOf("token" to card.token))
                    } else if (card.number != null && card.expMonth != null && card.expYear != null) {
                        put("card", buildMap<String, Any> {
                            put("number", card.number)
                            put("exp_month", card.expMonth)
                            put("exp_year", card.expYear)
                            card.cvc?.let { put("cvc", it) }
                        })
                    }
                }

                params.billingDetails?.let { billing ->
                    put("billing_details", buildMap<String, Any> {
                        billing.name?.let { put("name", it) }
                        billing.email?.let { put("email", it) }
                        billing.phone?.let { put("phone", it) }
                        billing.address?.let { addr ->
                            put("address", buildMap<String, Any> {
                                addr.line1?.let { put("line1", it) }
                                addr.line2?.let { put("line2", it) }
                                addr.city?.let { put("city", it) }
                                addr.state?.let { put("state", it) }
                                addr.postalCode?.let { put("postal_code", it) }
                                addr.country?.let { put("country", it) }
                            })
                        }
                    })
                }
            }

            val response = httpClient.post("payment_methods", pmParams, idempotencyKey?.value)
            response.toKmpPaymentMethod()
        }
    }

    public actual suspend fun retrievePaymentMethod(paymentMethodId: String): StripeResult<PaymentMethod> =
        withContext(Dispatchers.IO) {
            stripeRunCatching {
                val javaPaymentMethod = JavaPaymentMethod.retrieve(paymentMethodId, requestOptions)
                javaPaymentMethod.toKmpPaymentMethod()
            }
        }

    // ============================================================================
    // PaymentIntent
    // ============================================================================

    public actual suspend fun retrievePaymentIntent(clientSecret: String): StripeResult<PaymentIntent> =
        withContext(Dispatchers.IO) {
            stripeRunCatching {
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
        stripeRunCatching {
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
            stripeRunCatching {
                val setupIntentId = clientSecret.substringBefore("_secret_")
                val javaSetupIntent = JavaSetupIntent.retrieve(setupIntentId, requestOptions)
                javaSetupIntent.toKmpSetupIntent()
            }
        }

    public actual suspend fun confirmSetupIntent(
        params: ConfirmSetupIntentParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<SetupIntent> = withContext(Dispatchers.IO) {
        stripeRunCatching {
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
            stripeRunCatching {
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
            stripeRunCatching {
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

/**
 * Helper to convert stripe-java exceptions to KMP StripeException with detailed error messages
 */
private fun convertStripeException(e: JavaStripeException): StripeException {
    val errorMessage = buildString {
        append(e.message ?: "Stripe API error")

        when (e) {
            is JavaAuthenticationException -> {
                append(" [Authentication Error]")
                append(" - Check that your API key is valid and has not expired.")
                if (e.message?.contains("publishable") == true || e.message?.contains("pk_") == true) {
                    append(" Note: Some operations require a secret key (sk_test_*) instead of publishable key (pk_test_*).")
                }
            }
            is JavaInvalidRequestException -> {
                append(" [Invalid Request]")
                e.param?.let { append(" - Parameter: $it") }
            }
            is JavaCardException -> {
                append(" [Card Error]")
                e.code?.let { append(" - Code: $it") }
                e.param?.let { append(" - Parameter: $it") }
            }
            is JavaApiException -> {
                append(" [API Error]")
                e.statusCode?.let { append(" - Status Code: $it") }
            }
            else -> {
                append(" [${e::class.simpleName}]")
            }
        }

        e.requestId?.let { append(" - Request ID: $it") }
        e.statusCode?.let { append(" - HTTP Status: $it") }
    }

    return StripeException(errorMessage, cause = e)
}

/**
 * Enhanced runCatching that properly handles stripe-java exceptions
 */
private inline fun <T> stripeRunCatching(block: () -> T): StripeResult<T> {
    return try {
        StripeResult.success(block())
    } catch (e: JavaStripeException) {
        StripeResult.failure(convertStripeException(e))
    } catch (e: Exception) {
        val errorMessage = buildString {
            append(e.message ?: "Unknown error")
            append(" [${e::class.simpleName}]")
            e.cause?.let { cause ->
                append(" - Caused by: ${cause.message} [${cause::class.simpleName}]")
            }
        }
        StripeResult.failure(StripeException(errorMessage, cause = e))
    }
}

// ============================================================================
// JSON Response Mappers
// ============================================================================

private fun JsonObject.toKmpToken(): Token {
    val id = this["id"]?.jsonPrimitive?.content ?: ""
    val type = this["type"]?.jsonPrimitive?.content ?: "unknown"
    val created = this["created"]?.jsonPrimitive?.long ?: 0L
    val livemode = this["livemode"]?.jsonPrimitive?.boolean ?: false
    val used = this["used"]?.jsonPrimitive?.boolean ?: false

    val cardJson = this["card"]?.jsonObject
    val card = cardJson?.let {
        CardToken(
            id = it["id"]?.jsonPrimitive?.content ?: "",
            brand = it["brand"]?.jsonPrimitive?.content ?: "unknown",
            last4 = it["last4"]?.jsonPrimitive?.content ?: "",
            expMonth = it["exp_month"]?.jsonPrimitive?.int ?: 0,
            expYear = it["exp_year"]?.jsonPrimitive?.int ?: 0,
            funding = it["funding"]?.jsonPrimitive?.content,
            country = it["country"]?.jsonPrimitive?.content
        )
    }

    val bankAccountJson = this["bank_account"]?.jsonObject
    val bankAccount = bankAccountJson?.let {
        BankAccountToken(
            id = it["id"]?.jsonPrimitive?.content ?: "",
            country = it["country"]?.jsonPrimitive?.content ?: "",
            currency = it["currency"]?.jsonPrimitive?.content ?: "",
            last4 = it["last4"]?.jsonPrimitive?.content ?: "",
            bankName = it["bank_name"]?.jsonPrimitive?.content,
            accountHolderName = it["account_holder_name"]?.jsonPrimitive?.content,
            accountHolderType = it["account_holder_type"]?.jsonPrimitive?.content,
            routingNumber = it["routing_number"]?.jsonPrimitive?.content
        )
    }

    return Token(
        id = id,
        type = type,
        created = created,
        livemode = livemode,
        used = used,
        card = card,
        bankAccount = bankAccount
    )
}

private fun JsonObject.toKmpPaymentMethod(): PaymentMethod {
    val id = this["id"]?.jsonPrimitive?.content ?: ""
    val type = PaymentMethodType.fromValue(this["type"]?.jsonPrimitive?.content ?: "card")
    val created = this["created"]?.jsonPrimitive?.long ?: 0L
    val livemode = this["livemode"]?.jsonPrimitive?.boolean ?: false
    val customer = this["customer"]?.jsonPrimitive?.contentOrNull

    val cardJson = this["card"]?.jsonObject
    val card = cardJson?.let {
        Card(
            brand = CardBrand.fromValue(it["brand"]?.jsonPrimitive?.content ?: "unknown"),
            last4 = it["last4"]?.jsonPrimitive?.content ?: "",
            expMonth = it["exp_month"]?.jsonPrimitive?.int ?: 0,
            expYear = it["exp_year"]?.jsonPrimitive?.int ?: 0,
            funding = CardFunding.fromValue(it["funding"]?.jsonPrimitive?.content ?: "unknown"),
            country = it["country"]?.jsonPrimitive?.content
        )
    }

    val billingDetailsJson = this["billing_details"]?.jsonObject
    val billingDetails = billingDetailsJson?.let { bd ->
        val addressJson = bd["address"]?.jsonObject
        BillingDetails(
            name = bd["name"]?.jsonPrimitive?.contentOrNull,
            email = bd["email"]?.jsonPrimitive?.contentOrNull,
            phone = bd["phone"]?.jsonPrimitive?.contentOrNull,
            address = addressJson?.let { addr ->
                Address(
                    line1 = addr["line1"]?.jsonPrimitive?.contentOrNull,
                    line2 = addr["line2"]?.jsonPrimitive?.contentOrNull,
                    city = addr["city"]?.jsonPrimitive?.contentOrNull,
                    state = addr["state"]?.jsonPrimitive?.contentOrNull,
                    postalCode = addr["postal_code"]?.jsonPrimitive?.contentOrNull,
                    country = addr["country"]?.jsonPrimitive?.contentOrNull
                )
            }
        )
    }

    return PaymentMethod(
        id = id,
        type = type,
        created = created,
        livemode = livemode,
        customer = customer,
        card = card,
        billingDetails = billingDetails
    )
}
