package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import com.stripe.Stripe as StripeJava
import com.stripe.model.Customer as JavaCustomer
import com.stripe.model.EphemeralKey as JavaEphemeralKey
import com.stripe.model.PaymentIntent as JavaPaymentIntent
import com.stripe.model.PaymentMethod as JavaPaymentMethod
import com.stripe.model.SetupIntent as JavaSetupIntent
import com.stripe.model.Source as JavaSource
import com.stripe.model.Token as JavaToken
import com.stripe.param.EphemeralKeyCreateParams as JavaEphemeralKeyCreateParams
import com.stripe.param.PaymentIntentConfirmParams
import com.stripe.param.PaymentIntentRetrieveParams
import com.stripe.param.PaymentMethodCreateParams as JavaPaymentMethodCreateParams
import com.stripe.param.SetupIntentConfirmParams
import com.stripe.param.SetupIntentRetrieveParams
import com.stripe.param.SourceRetrieveParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * JVM implementation of Stripe using the official stripe-java SDK.
 *
 * This implementation is designed for server-side usage and provides access
 * to stripe-java functionality through a unified KMP API.
 *
 * IMPORTANT: For server-side usage, you should configure your API key using
 * either the publishable key in StripeConfiguration or set the secret key
 * directly via Stripe.apiKey in stripe-java.
 */
public actual class Stripe private constructor(
    public actual val configuration: StripeConfiguration
) {
    init {
        // Set the API key in stripe-java
        StripeJava.apiKey = configuration.publishableKey
    }


    public actual suspend fun createCardToken(
        params: CardParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Token> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            val tokenParams = mapOf(
                "card" to mapOf(
                    "number" to params.number,
                    "exp_month" to params.expMonth.toString(),
                    "exp_year" to params.expYear.toString(),
                    "cvc" to params.cvc,
                    "name" to params.name
                ).filterValues { it != null }
            )

            val requestOptions = idempotencyKey?.let {
                com.stripe.net.RequestOptions.builder()
                    .setIdempotencyKey(it.value)
                    .build()
            }

            val javaToken = if (requestOptions != null) {
                JavaToken.create(tokenParams, requestOptions)
            } else {
                JavaToken.create(tokenParams)
            }

            javaToken.toKmpToken()
        }
    }

    public actual suspend fun createBankAccountToken(
        params: BankAccountTokenParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Token> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            val tokenParams = mapOf(
                "bank_account" to mapOf(
                    "country" to params.country,
                    "currency" to params.currency,
                    "account_number" to params.accountNumber,
                    "routing_number" to params.routingNumber,
                    "account_holder_name" to params.accountHolderName,
                    "account_holder_type" to params.accountHolderType?.value
                ).filterValues { it != null }
            )

            val requestOptions = idempotencyKey?.let {
                com.stripe.net.RequestOptions.builder()
                    .setIdempotencyKey(it.value)
                    .build()
            }

            val javaToken = if (requestOptions != null) {
                JavaToken.create(tokenParams, requestOptions)
            } else {
                JavaToken.create(tokenParams)
            }

            javaToken.toKmpToken()
        }
    }

    public actual suspend fun createPiiToken(params: PiiTokenParams): StripeResult<Token> =
        withContext(Dispatchers.IO) {
            StripeResult.runCatching {
                val tokenParams = mapOf(
                    "pii" to mapOf(
                        "personal_id_number" to params.personalIdNumber
                    )
                )

                val javaToken = JavaToken.create(tokenParams)
                javaToken.toKmpToken()
            }
        }

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
    ): StripeResult<Source> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            val sourceParams = buildMap<String, Any> {
                put("type", params.type.value)
                params.amount?.let { put("amount", it) }
                params.currency?.let { put("currency", it) }
                params.owner?.let { owner ->
                    put("owner", buildMap {
                        owner.name?.let { put("name", it) }
                        owner.email?.let { put("email", it) }
                        owner.phone?.let { put("phone", it) }
                        owner.address?.let { addr ->
                            put("address", buildMap {
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
                params.redirect?.let { redirect ->
                    put("redirect", mapOf("return_url" to redirect.returnUrl))
                }
                params.metadata?.let { put("metadata", it) }
            }

            val requestOptions = idempotencyKey?.let {
                com.stripe.net.RequestOptions.builder()
                    .setIdempotencyKey(it.value)
                    .build()
            }

            val javaSource = if (requestOptions != null) {
                JavaSource.create(sourceParams, requestOptions)
            } else {
                JavaSource.create(sourceParams)
            }

            javaSource.toKmpSource()
        }
    }

    public actual suspend fun retrieveSource(
        sourceId: String,
        clientSecret: String
    ): StripeResult<Source> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            val params = SourceRetrieveParams.builder()
                .setClientSecret(clientSecret)
                .build()

            val javaSource = JavaSource.retrieve(sourceId, params, null)
            javaSource.toKmpSource()
        }
    }


    public actual suspend fun createPaymentMethod(
        params: PaymentMethodCreateParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<PaymentMethod> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            val builder = JavaPaymentMethodCreateParams.builder()
                .setType(JavaPaymentMethodCreateParams.Type.valueOf(params.type.value.uppercase()))

            params.billingDetails?.let { billing ->
                val billingBuilder = JavaPaymentMethodCreateParams.BillingDetails.builder()
                billing.name?.let { billingBuilder.setName(it) }
                billing.email?.let { billingBuilder.setEmail(it) }
                billing.phone?.let { billingBuilder.setPhone(it) }
                billing.address?.let { addr ->
                    val addressBuilder = JavaPaymentMethodCreateParams.BillingDetails.Address.builder()
                    addr.line1?.let { addressBuilder.setLine1(it) }
                    addr.line2?.let { addressBuilder.setLine2(it) }
                    addr.city?.let { addressBuilder.setCity(it) }
                    addr.state?.let { addressBuilder.setState(it) }
                    addr.postalCode?.let { addressBuilder.setPostalCode(it) }
                    addr.country?.let { addressBuilder.setCountry(it) }
                    billingBuilder.setAddress(addressBuilder.build())
                }
                builder.setBillingDetails(billingBuilder.build())
            }

            params.card?.let { card ->
                if (card.token != null) {
                    builder.setCard(
                        JavaPaymentMethodCreateParams.Token.builder()
                            .setToken(card.token)
                            .build()
                    )
                } else {
                    val cardBuilder = JavaPaymentMethodCreateParams.CardDetails.builder()
                    card.number?.let { cardBuilder.setNumber(it) }
                    card.expMonth?.let { cardBuilder.setExpMonth(it.toLong()) }
                    card.expYear?.let { cardBuilder.setExpYear(it.toLong()) }
                    card.cvc?.let { cardBuilder.setCvc(it) }
                    builder.setCard(cardBuilder.build())
                }
            }

            params.metadata?.let { meta ->
                meta.forEach { (key, value) -> builder.putMetadata(key, value) }
            }

            val requestOptions = idempotencyKey?.let {
                com.stripe.net.RequestOptions.builder()
                    .setIdempotencyKey(it.value)
                    .build()
            }

            val javaPaymentMethod = if (requestOptions != null) {
                JavaPaymentMethod.create(builder.build(), requestOptions)
            } else {
                JavaPaymentMethod.create(builder.build())
            }

            javaPaymentMethod.toKmpPaymentMethod()
        }
    }

    public actual suspend fun retrievePaymentMethod(paymentMethodId: String): StripeResult<PaymentMethod> =
        withContext(Dispatchers.IO) {
            StripeResult.runCatching {
                val javaPaymentMethod = JavaPaymentMethod.retrieve(paymentMethodId)
                javaPaymentMethod.toKmpPaymentMethod()
            }
        }


    public actual suspend fun retrievePaymentIntent(clientSecret: String): StripeResult<PaymentIntent> =
        withContext(Dispatchers.IO) {
            StripeResult.runCatching {
                // Extract the PaymentIntent ID from the client secret
                val paymentIntentId = clientSecret.substringBefore("_secret_")

                val params = PaymentIntentRetrieveParams.builder()
                    .setClientSecret(clientSecret)
                    .build()

                val javaPaymentIntent = JavaPaymentIntent.retrieve(paymentIntentId, params, null)
                javaPaymentIntent.toKmpPaymentIntent()
            }
        }

    public actual suspend fun confirmPaymentIntent(
        params: ConfirmPaymentIntentParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<PaymentIntent> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            val paymentIntentId = params.clientSecret.substringBefore("_secret_")

            val confirmParams = PaymentIntentConfirmParams.builder()
            params.paymentMethodId?.let { confirmParams.setPaymentMethod(it) }
            params.returnUrl?.let { confirmParams.setReturnUrl(it) }
            params.receiptEmail?.let { confirmParams.setReceiptEmail(it) }
            params.setupFutureUsage?.let {
                confirmParams.setSetupFutureUsage(
                    PaymentIntentConfirmParams.SetupFutureUsage.valueOf(it.value.uppercase().replace("-", "_"))
                )
            }

            val intent = JavaPaymentIntent.retrieve(paymentIntentId)

            val requestOptions = idempotencyKey?.let {
                com.stripe.net.RequestOptions.builder()
                    .setIdempotencyKey(it.value)
                    .build()
            }

            val confirmed = if (requestOptions != null) {
                intent.confirm(confirmParams.build(), requestOptions)
            } else {
                intent.confirm(confirmParams.build())
            }

            confirmed.toKmpPaymentIntent()
        }
    }

    public actual suspend fun handleNextActionForPayment(clientSecret: String): StripeResult<PaymentIntent> =
        StripeResult.failure(
            StripeException("handleNextActionForPayment requires client-side implementation. Use Android, iOS, or JS target.")
        )


    public actual suspend fun retrieveSetupIntent(clientSecret: String): StripeResult<SetupIntent> =
        withContext(Dispatchers.IO) {
            StripeResult.runCatching {
                val setupIntentId = clientSecret.substringBefore("_secret_")

                val params = SetupIntentRetrieveParams.builder()
                    .setClientSecret(clientSecret)
                    .build()

                val javaSetupIntent = JavaSetupIntent.retrieve(setupIntentId, params, null)
                javaSetupIntent.toKmpSetupIntent()
            }
        }

    public actual suspend fun confirmSetupIntent(
        params: ConfirmSetupIntentParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<SetupIntent> = withContext(Dispatchers.IO) {
        StripeResult.runCatching {
            val setupIntentId = params.clientSecret.substringBefore("_secret_")

            val confirmParams = SetupIntentConfirmParams.builder()
            params.paymentMethodId?.let { confirmParams.setPaymentMethod(it) }
            params.returnUrl?.let { confirmParams.setReturnUrl(it) }

            val intent = JavaSetupIntent.retrieve(setupIntentId)

            val requestOptions = idempotencyKey?.let {
                com.stripe.net.RequestOptions.builder()
                    .setIdempotencyKey(it.value)
                    .build()
            }

            val confirmed = if (requestOptions != null) {
                intent.confirm(confirmParams.build(), requestOptions)
            } else {
                intent.confirm(confirmParams.build())
            }

            confirmed.toKmpSetupIntent()
        }
    }

    public actual suspend fun handleNextActionForSetupIntent(clientSecret: String): StripeResult<SetupIntent> =
        StripeResult.failure(
            StripeException("handleNextActionForSetupIntent requires client-side implementation. Use Android, iOS, or JS target.")
        )


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

private fun JavaSource.toKmpSource(): Source = Source(
    id = id,
    type = SourceType.fromValue(type),
    status = SourceStatus.fromValue(status) ?: SourceStatus.PENDING,
    amount = amount,
    currency = currency,
    clientSecret = clientSecret,
    flow = SourceFlow.fromValue(flow) ?: SourceFlow.NONE,
    redirect = redirect?.let { r ->
        SourceRedirect(
            returnUrl = r.returnUrl,
            status = r.status,
            url = r.url
        )
    },
    owner = owner?.let { o ->
        SourceOwner(
            name = o.name,
            email = o.email,
            phone = o.phone,
            address = o.address?.let { a ->
                Address(
                    line1 = a.line1,
                    line2 = a.line2,
                    city = a.city,
                    state = a.state,
                    postalCode = a.postalCode,
                    country = a.country
                )
            }
        )
    },
    created = created,
    livemode = livemode
)

private fun JavaPaymentMethod.toKmpPaymentMethod(): PaymentMethod = PaymentMethod(
    id = id,
    type = PaymentMethodType.fromValue(type),
    created = created,
    livemode = livemode,
    billingDetails = billingDetails?.let { b ->
        BillingDetails(
            name = b.name,
            email = b.email,
            phone = b.phone,
            address = b.address?.let { a ->
                Address(
                    line1 = a.line1,
                    line2 = a.line2,
                    city = a.city,
                    state = a.state,
                    postalCode = a.postalCode,
                    country = a.country
                )
            }
        )
    },
    card = card?.let { c ->
        Card(
            brand = CardBrand.fromValue(c.brand),
            last4 = c.last4,
            expMonth = c.expMonth.toInt(),
            expYear = c.expYear.toInt(),
            funding = CardFunding.fromValue(c.funding ?: "unknown"),
            country = c.country,
            fingerprint = c.fingerprint,
            checks = c.checks?.let { ch ->
                CardChecks(
                    addressLine1Check = ch.addressLine1Check,
                    addressPostalCodeCheck = ch.addressPostalCodeCheck,
                    cvcCheck = ch.cvcCheck
                )
            },
            wallet = c.wallet?.let { w -> CardWallet(type = w.type) },
            threeDSecureUsage = c.threeDSecureUsage?.let { t ->
                ThreeDSecureUsage(supported = t.supported)
            },
            networks = c.networks?.let { n ->
                CardNetworks(
                    available = n.available,
                    preferred = n.preferred
                )
            }
        )
    },
    customer = customer,
    metadata = metadata
)

private fun JavaPaymentIntent.toKmpPaymentIntent(): PaymentIntent = PaymentIntent(
    id = id,
    clientSecret = clientSecret,
    amount = amount,
    currency = currency,
    status = PaymentIntentStatus.fromValue(status) ?: PaymentIntentStatus.REQUIRES_PAYMENT_METHOD,
    created = created,
    livemode = livemode,
    paymentMethodId = paymentMethod,
    paymentMethodTypes = paymentMethodTypes ?: emptyList(),
    confirmationMethod = ConfirmationMethod.fromValue(confirmationMethod) ?: ConfirmationMethod.AUTOMATIC,
    captureMethod = CaptureMethod.fromValue(captureMethod) ?: CaptureMethod.AUTOMATIC,
    description = description,
    receiptEmail = receiptEmail,
    setupFutureUsage = setupFutureUsage?.let { SetupFutureUsage.fromValue(it) },
    lastPaymentError = lastPaymentError?.let { e ->
        PaymentIntentError(
            type = e.type ?: "unknown",
            code = e.code,
            declineCode = e.declineCode,
            message = e.message ?: "Unknown error"
        )
    },
    nextAction = nextAction?.let { a ->
        NextAction(
            type = NextActionType.fromValue(a.type) ?: NextActionType.REDIRECT_TO_URL,
            redirectToUrl = a.redirectToUrl?.let { r ->
                RedirectToUrl(
                    url = r.url,
                    returnUrl = r.returnUrl
                )
            },
            useStripeSdk = null
        )
    },
    canceledAt = canceledAt,
    cancellationReason = cancellationReason,
    metadata = metadata
)

private fun JavaSetupIntent.toKmpSetupIntent(): SetupIntent = SetupIntent(
    id = id,
    clientSecret = clientSecret,
    created = created,
    livemode = livemode,
    status = SetupIntentStatus.fromValue(status) ?: SetupIntentStatus.REQUIRES_PAYMENT_METHOD,
    paymentMethodId = paymentMethod,
    paymentMethodTypes = paymentMethodTypes ?: emptyList(),
    description = description,
    usage = SetupIntentUsage.fromValue(usage) ?: SetupIntentUsage.OFF_SESSION,
    customerId = customer,
    lastSetupError = lastSetupError?.let { e ->
        SetupIntentError(
            type = e.type ?: "unknown",
            code = e.code,
            declineCode = e.declineCode,
            message = e.message ?: "Unknown error"
        )
    },
    nextAction = nextAction?.let { a ->
        SetupNextAction(
            type = SetupNextActionType.fromValue(a.type) ?: SetupNextActionType.REDIRECT_TO_URL,
            redirectToUrl = a.redirectToUrl?.let { r ->
                RedirectToUrl(
                    url = r.url,
                    returnUrl = r.returnUrl
                )
            },
            useStripeSdk = null
        )
    },
    cancellationReason = cancellationReason,
    metadata = metadata
)
