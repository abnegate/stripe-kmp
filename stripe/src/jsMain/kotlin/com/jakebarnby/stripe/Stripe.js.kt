package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import kotlinx.coroutines.await
import kotlin.js.Promise

/**
 * External declaration for Stripe.js loader.
 * HIGH-06: Fixed external interface - use proper @JsModule with @JsNonModule for UMD support
 */
@JsModule("@stripe/stripe-js")
@JsNonModule
public external object StripeJS {
    public fun loadStripe(publishableKey: String): Promise<StripeInstance?>
}

public external interface StripeInstance {
    public val publishableKey: String

    // Token creation
    public fun createToken(type: String, data: dynamic = definedExternally): Promise<StripeTokenResult>

    // Source creation
    public fun createSource(params: dynamic): Promise<StripeSourceResult>
    public fun retrieveSource(params: dynamic): Promise<StripeSourceResult>

    // PaymentMethod
    public fun createPaymentMethod(params: dynamic): Promise<StripePaymentMethodResult>
    public fun retrievePaymentMethod(id: String): Promise<StripePaymentMethodResult>

    // PaymentIntent
    public fun retrievePaymentIntent(clientSecret: String): Promise<StripePaymentIntentResult>
    public fun confirmCardPayment(clientSecret: String, data: dynamic = definedExternally): Promise<StripePaymentIntentResult>
    public fun handleCardAction(clientSecret: String): Promise<StripePaymentIntentResult>

    // SetupIntent
    public fun retrieveSetupIntent(clientSecret: String): Promise<StripeSetupIntentResult>
    public fun confirmCardSetup(clientSecret: String, data: dynamic = definedExternally): Promise<StripeSetupIntentResult>
    public fun handleCardSetup(clientSecret: String): Promise<StripeSetupIntentResult>
}

public external interface StripeTokenResult {
    public val token: dynamic
    public val error: dynamic
}

public external interface StripeSourceResult {
    public val source: dynamic
    public val error: dynamic
}

public external interface StripePaymentMethodResult {
    public val paymentMethod: dynamic
    public val error: dynamic
}

public external interface StripePaymentIntentResult {
    public val paymentIntent: dynamic
    public val error: dynamic
}

public external interface StripeSetupIntentResult {
    public val setupIntent: dynamic
    public val error: dynamic
}

/**
 * JS Stripe implementation using Stripe.js.
 *
 * This implementation loads Stripe.js dynamically from the CDN.
 * Ensure you have internet connectivity when initializing.
 */
public actual class Stripe private constructor(
    public actual val configuration: StripeConfiguration
) {
    internal var stripeInstance: StripeInstance? = null
    private var loadError: String? = null

    /**
     * Check if Stripe.js has been loaded successfully.
     *
     * @return true if loaded, false otherwise
     */
    public fun isLoaded(): Boolean = stripeInstance != null

    /**
     * Get any load error that occurred.
     *
     * @return error message if loading failed, null otherwise
     */
    public fun getLoadError(): String? = loadError

    /**
     * Wait for Stripe.js to finish loading.
     * Suspends until the instance is loaded or an error occurs.
     *
     * @throws IllegalStateException if loading failed
     */
    public suspend fun awaitLoad() {
        if (stripeInstance != null) return
        if (loadError != null) {
            throw IllegalStateException("Stripe.js failed to load: $loadError")
        }

        // Wait for load to complete (with timeout would be better in production)
        var attempts = 0
        while (stripeInstance == null && loadError == null && attempts < 50) {
            kotlinx.coroutines.delay(100)
            attempts++
        }

        if (stripeInstance == null) {
            throw IllegalStateException(loadError ?: "Stripe.js load timeout")
        }
    }

    private fun getStripe(): StripeInstance {
        return requireNotNull(stripeInstance) {
            "Stripe.js not loaded. Call awaitLoad() first or use initializeAndAwait()."
        }
    }

    // ============================================================================
    // Token Creation
    // ============================================================================

    public actual suspend fun createCardToken(params: CardParams, idempotencyKey: IdempotencyKey?): StripeResult<Token> {
        return StripeResult.runCatching {
            val stripe = getStripe()
            val cardData = js("{}")
            cardData.number = params.number
            cardData.exp_month = params.expMonth
            cardData.exp_year = params.expYear
            cardData.cvc = params.cvc
            cardData.name = params.name

            // Note: Stripe.js client-side token creation does not support idempotency keys
            // Idempotency is handled server-side. Log warning if key is provided.
            if (idempotencyKey != null && configuration.enableLogging) {
                console.warn("Idempotency keys are not supported for client-side token creation in Stripe.js. The key will be ignored.")
            }

            val result = stripe.createToken("card", cardData).await()
            if (result.error != null) {
                throw StripeException(result.error.message as String)
            }

            // Convert JS token to KMP Token
            val token = result.token
            Token(
                id = token.id as String,
                type = token.type as String,
                created = (token.created as Number).toLong(),
                livemode = token.livemode as Boolean,
                used = token.used as Boolean,
                card = token.card?.let { card ->
                    CardToken(
                        id = card.id as String,
                        brand = card.brand as String,
                        last4 = card.last4 as String,
                        expMonth = (card.exp_month as Number).toInt(),
                        expYear = (card.exp_year as Number).toInt(),
                        funding = card.funding as? String,
                        country = card.country as? String
                    )
                }
            )
        }
    }

    public actual suspend fun createBankAccountToken(params: BankAccountTokenParams, idempotencyKey: IdempotencyKey?): StripeResult<Token> {
        return StripeResult.runCatching {
            val stripe = getStripe()
            val bankAccountData = js("{}")
            bankAccountData.country = params.country
            bankAccountData.currency = params.currency
            bankAccountData.account_number = params.accountNumber
            bankAccountData.routing_number = params.routingNumber
            bankAccountData.account_holder_name = params.accountHolderName
            bankAccountData.account_holder_type = params.accountHolderType?.value

            if (idempotencyKey != null && configuration.enableLogging) {
                console.warn("Idempotency keys are not supported for client-side token creation in Stripe.js. The key will be ignored.")
            }

            val result = stripe.createToken("bank_account", js("{ bank_account: bankAccountData }")).await()
            if (result.error != null) {
                throw StripeException(result.error.message as String)
            }

            val token = result.token
            Token(
                id = token.id as String,
                type = token.type as String,
                created = (token.created as Number).toLong(),
                livemode = token.livemode as Boolean,
                used = token.used as Boolean,
                bankAccount = token.bank_account?.let { ba ->
                    BankAccountToken(
                        id = ba.id as String,
                        country = ba.country as String,
                        currency = ba.currency as String,
                        last4 = ba.last4 as String,
                        bankName = ba.bank_name as? String,
                        accountHolderName = ba.account_holder_name as? String,
                        accountHolderType = ba.account_holder_type as? String,
                        routingNumber = ba.routing_number as? String
                    )
                }
            )
        }
    }

    public actual suspend fun createPiiToken(params: PiiTokenParams): StripeResult<Token> {
        return StripeResult.runCatching {
            val stripe = getStripe()
            // Properly construct the PII data object by assigning Kotlin variable to JS object
            val piiData = js("{}")
            piiData.pii = js("{}")
            piiData.pii.personal_id_number = params.personalIdNumber

            val result = stripe.createToken("pii", piiData).await()
            if (result.error != null) {
                throw StripeException(result.error.message as String)
            }

            val token = result.token
            Token(
                id = token.id as String,
                type = token.type as String,
                created = (token.created as Number).toLong(),
                livemode = token.livemode as Boolean,
                used = token.used as Boolean
            )
        }
    }

    public actual suspend fun createAccountToken(params: AccountParams): StripeResult<Token> {
        return StripeResult.failure(
            StripeException("Account token creation not supported in Stripe.js")
        )
    }

    // ============================================================================
    // Source Creation
    // ============================================================================

    public actual suspend fun createSource(params: SourceParams, idempotencyKey: IdempotencyKey?): StripeResult<Source> {
        return StripeResult.runCatching {
            val stripe = getStripe()

            if (idempotencyKey != null && configuration.enableLogging) {
                console.warn("Idempotency keys are not supported for client-side source creation in Stripe.js. The key will be ignored.")
            }

            // Build the source params object for Stripe.js
            val sourceData = js("{}")
            sourceData.type = params.type.value

            params.amount?.let { sourceData.amount = it }
            params.currency?.let { sourceData.currency = it }

            // Add owner information
            params.owner?.let { owner ->
                val ownerData = js("{}")
                owner.name?.let { ownerData.name = it }
                owner.email?.let { ownerData.email = it }
                owner.phone?.let { ownerData.phone = it }
                owner.address?.let { addr ->
                    val addressData = js("{}")
                    addr.line1?.let { addressData.line1 = it }
                    addr.line2?.let { addressData.line2 = it }
                    addr.city?.let { addressData.city = it }
                    addr.state?.let { addressData.state = it }
                    addr.postalCode?.let { addressData.postal_code = it }
                    addr.country?.let { addressData.country = it }
                    ownerData.address = addressData
                }
                sourceData.owner = ownerData
            }

            // Add redirect information
            params.redirect?.let { redirect ->
                val redirectData = js("{}")
                redirectData.return_url = redirect.returnUrl
                sourceData.redirect = redirectData
            }

            // Add metadata
            params.metadata?.let { meta ->
                val metadataObj = js("{}")
                meta.forEach { (key, value) -> metadataObj[key] = value }
                sourceData.metadata = metadataObj
            }

            // Add extra params (type-specific parameters)
            params.extraParams?.forEach { (key, value) ->
                sourceData[key] = value
            }

            val result = stripe.createSource(sourceData).await()

            if (result.error != null) {
                throw StripeException(result.error.message as String)
            }

            // Convert JS source to KMP Source
            val source = result.source ?: throw StripeException("No source returned")
            convertJsSource(source)
        }
    }

    public actual suspend fun retrieveSource(sourceId: String, clientSecret: String): StripeResult<Source> {
        return StripeResult.runCatching {
            val stripe = getStripe()

            val params = js("{}")
            params.id = sourceId
            params.client_secret = clientSecret

            val result = stripe.retrieveSource(params).await()

            if (result.error != null) {
                throw StripeException(result.error.message as String)
            }

            val source = result.source ?: throw StripeException("No source returned")
            convertJsSource(source)
        }
    }

    // ============================================================================
    // PaymentMethod
    // ============================================================================

    public actual suspend fun createPaymentMethod(params: PaymentMethodCreateParams, idempotencyKey: IdempotencyKey?): StripeResult<PaymentMethod> {
        return StripeResult.runCatching {
            val stripe = getStripe()

            if (idempotencyKey != null && configuration.enableLogging) {
                console.warn("Idempotency keys are not supported for client-side PaymentMethod creation in Stripe.js. The key will be ignored.")
            }

            // Build the payment method params object for Stripe.js
            val paymentMethodData = js("{}")
            paymentMethodData.type = params.type.value

            // Add billing details
            params.billingDetails?.let { billing ->
                val billingData = js("{}")
                billing.name?.let { billingData.name = it }
                billing.email?.let { billingData.email = it }
                billing.phone?.let { billingData.phone = it }
                billing.address?.let { addr ->
                    val addressData = js("{}")
                    addr.line1?.let { addressData.line1 = it }
                    addr.line2?.let { addressData.line2 = it }
                    addr.city?.let { addressData.city = it }
                    addr.state?.let { addressData.state = it }
                    addr.postalCode?.let { addressData.postal_code = it }
                    addr.country?.let { addressData.country = it }
                    billingData.address = addressData
                }
                paymentMethodData.billing_details = billingData
            }

            // Add card details
            params.card?.let { card ->
                val cardData = js("{}")

                if (card.token != null) {
                    // Use token-based creation
                    cardData.token = card.token
                } else {
                    // Use direct card details
                    card.number?.let { cardData.number = it }
                    card.expMonth?.let { cardData.exp_month = it }
                    card.expYear?.let { cardData.exp_year = it }
                    card.cvc?.let { cardData.cvc = it }
                }

                paymentMethodData.card = cardData
            }

            // Add metadata
            params.metadata?.let { meta ->
                val metadataObj = js("{}")
                meta.forEach { (key, value) -> metadataObj[key] = value }
                paymentMethodData.metadata = metadataObj
            }

            val result = stripe.createPaymentMethod(paymentMethodData).await()

            if (result.error != null) {
                throw StripeException(result.error.message as String)
            }

            // Convert JS payment method to KMP PaymentMethod
            val paymentMethod = result.paymentMethod ?: throw StripeException("No payment method returned")
            convertJsPaymentMethod(paymentMethod)
        }
    }

    public actual suspend fun retrievePaymentMethod(paymentMethodId: String): StripeResult<PaymentMethod> {
        return StripeResult.runCatching {
            val stripe = getStripe()

            val result = stripe.retrievePaymentMethod(paymentMethodId).await()

            if (result.error != null) {
                throw StripeException(result.error.message as String)
            }

            val paymentMethod = result.paymentMethod ?: throw StripeException("No payment method returned")
            convertJsPaymentMethod(paymentMethod)
        }
    }

    // ============================================================================
    // PaymentIntent
    // ============================================================================

    public actual suspend fun retrievePaymentIntent(clientSecret: String): StripeResult<PaymentIntent> {
        return StripeResult.runCatching {
            val stripe = getStripe()

            val result = stripe.retrievePaymentIntent(clientSecret).await()

            if (result.error != null) {
                throw StripeException(result.error.message as String)
            }

            val paymentIntent = result.paymentIntent ?: throw StripeException("No payment intent returned")
            convertJsPaymentIntentFull(paymentIntent)
        }
    }

    public actual suspend fun confirmPaymentIntent(params: ConfirmPaymentIntentParams, idempotencyKey: IdempotencyKey?): StripeResult<PaymentIntent> {
        return StripeResult.runCatching {
            val stripe = getStripe()

            if (idempotencyKey != null && configuration.enableLogging) {
                console.warn("Idempotency keys are not supported for client-side PaymentIntent confirmation in Stripe.js. The key will be ignored.")
            }

            // Build the confirmation params object
            val confirmData = js("{}")

            // Add payment method (either ID or creation params)
            params.paymentMethodId?.let { pmId ->
                confirmData.payment_method = pmId
            } ?: params.paymentMethodCreateParams?.let { pmParams ->
                val paymentMethodData = js("{}")
                paymentMethodData.type = pmParams.type.value

                // Add billing details
                pmParams.billingDetails?.let { billing ->
                    val billingData = js("{}")
                    billing.name?.let { billingData.name = it }
                    billing.email?.let { billingData.email = it }
                    billing.phone?.let { billingData.phone = it }
                    billing.address?.let { addr ->
                        val addressData = js("{}")
                        addr.line1?.let { addressData.line1 = it }
                        addr.line2?.let { addressData.line2 = it }
                        addr.city?.let { addressData.city = it }
                        addr.state?.let { addressData.state = it }
                        addr.postalCode?.let { addressData.postal_code = it }
                        addr.country?.let { addressData.country = it }
                        billingData.address = addressData
                    }
                    paymentMethodData.billing_details = billingData
                }

                // Add card details
                pmParams.card?.let { card ->
                    val cardData = js("{}")
                    if (card.token != null) {
                        cardData.token = card.token
                    } else {
                        card.number?.let { cardData.number = it }
                        card.expMonth?.let { cardData.exp_month = it }
                        card.expYear?.let { cardData.exp_year = it }
                        card.cvc?.let { cardData.cvc = it }
                    }
                    paymentMethodData.card = cardData
                }

                confirmData.payment_method = paymentMethodData
            }

            // Add return URL
            params.returnUrl?.let { confirmData.return_url = it }

            // Add shipping details
            params.shipping?.let { shipping ->
                val shippingData = js("{}")
                shippingData.name = shipping.name

                val addressData = js("{}")
                shipping.address.line1?.let { addressData.line1 = it }
                shipping.address.line2?.let { addressData.line2 = it }
                shipping.address.city?.let { addressData.city = it }
                shipping.address.state?.let { addressData.state = it }
                shipping.address.postalCode?.let { addressData.postal_code = it }
                shipping.address.country?.let { addressData.country = it }
                shippingData.address = addressData

                shipping.carrier?.let { shippingData.carrier = it }
                shipping.phone?.let { shippingData.phone = it }
                shipping.trackingNumber?.let { shippingData.tracking_number = it }

                confirmData.shipping = shippingData
            }

            // Add receipt email
            params.receiptEmail?.let { confirmData.receipt_email = it }

            // Add setup future usage
            params.setupFutureUsage?.let { confirmData.setup_future_usage = it.value }

            // Add mandate data
            params.mandateData?.let { mandate ->
                val mandateDataObj = js("{}")
                val customerAcceptance = js("{}")
                customerAcceptance.type = mandate.customerAcceptance.type
                customerAcceptance.accepted_at = mandate.customerAcceptance.acceptedAt

                mandate.customerAcceptance.online?.let { online ->
                    val onlineData = js("{}")
                    onlineData.ip_address = online.ipAddress
                    onlineData.user_agent = online.userAgent
                    customerAcceptance.online = onlineData
                }

                mandateDataObj.customer_acceptance = customerAcceptance
                confirmData.mandate_data = mandateDataObj
            }

            val result = stripe.confirmCardPayment(params.clientSecret, confirmData).await()

            if (result.error != null) {
                throw StripeException(result.error.message as String)
            }

            val paymentIntent = result.paymentIntent ?: throw StripeException("No payment intent returned")
            convertJsPaymentIntentFull(paymentIntent)
        }
    }

    public actual suspend fun handleNextActionForPayment(clientSecret: String): StripeResult<PaymentIntent> {
        return StripeResult.runCatching {
            val stripe = getStripe()

            val result = stripe.handleCardAction(clientSecret).await()

            if (result.error != null) {
                throw StripeException(result.error.message as String)
            }

            val paymentIntent = result.paymentIntent ?: throw StripeException("No payment intent returned")
            convertJsPaymentIntentFull(paymentIntent)
        }
    }

    // ============================================================================
    // SetupIntent
    // ============================================================================

    public actual suspend fun retrieveSetupIntent(clientSecret: String): StripeResult<SetupIntent> {
        return StripeResult.runCatching {
            val stripe = getStripe()

            val result = stripe.retrieveSetupIntent(clientSecret).await()

            if (result.error != null) {
                throw StripeException(result.error.message as String)
            }

            val setupIntent = result.setupIntent ?: throw StripeException("No setup intent returned")
            convertJsSetupIntentFull(setupIntent)
        }
    }

    public actual suspend fun confirmSetupIntent(params: ConfirmSetupIntentParams, idempotencyKey: IdempotencyKey?): StripeResult<SetupIntent> {
        return StripeResult.runCatching {
            val stripe = getStripe()

            if (idempotencyKey != null && configuration.enableLogging) {
                console.warn("Idempotency keys are not supported for client-side SetupIntent confirmation in Stripe.js. The key will be ignored.")
            }

            // Build the confirmation params object
            val confirmData = js("{}")

            // Add payment method (either ID or creation params)
            params.paymentMethodId?.let { pmId ->
                confirmData.payment_method = pmId
            } ?: params.paymentMethodCreateParams?.let { pmParams ->
                val paymentMethodData = js("{}")
                paymentMethodData.type = pmParams.type.value

                // Add billing details
                pmParams.billingDetails?.let { billing ->
                    val billingData = js("{}")
                    billing.name?.let { billingData.name = it }
                    billing.email?.let { billingData.email = it }
                    billing.phone?.let { billingData.phone = it }
                    billing.address?.let { addr ->
                        val addressData = js("{}")
                        addr.line1?.let { addressData.line1 = it }
                        addr.line2?.let { addressData.line2 = it }
                        addr.city?.let { addressData.city = it }
                        addr.state?.let { addressData.state = it }
                        addr.postalCode?.let { addressData.postal_code = it }
                        addr.country?.let { addressData.country = it }
                        billingData.address = addressData
                    }
                    paymentMethodData.billing_details = billingData
                }

                // Add card details
                pmParams.card?.let { card ->
                    val cardData = js("{}")
                    if (card.token != null) {
                        cardData.token = card.token
                    } else {
                        card.number?.let { cardData.number = it }
                        card.expMonth?.let { cardData.exp_month = it }
                        card.expYear?.let { cardData.exp_year = it }
                        card.cvc?.let { cardData.cvc = it }
                    }
                    paymentMethodData.card = cardData
                }

                confirmData.payment_method = paymentMethodData
            }

            // Add return URL
            params.returnUrl?.let { confirmData.return_url = it }

            // Add mandate data
            params.mandateData?.let { mandate ->
                val mandateDataObj = js("{}")
                val customerAcceptance = js("{}")
                customerAcceptance.type = mandate.customerAcceptance.type
                customerAcceptance.accepted_at = mandate.customerAcceptance.acceptedAt

                mandate.customerAcceptance.online?.let { online ->
                    val onlineData = js("{}")
                    onlineData.ip_address = online.ipAddress
                    onlineData.user_agent = online.userAgent
                    customerAcceptance.online = onlineData
                }

                mandateDataObj.customer_acceptance = customerAcceptance
                confirmData.mandate_data = mandateDataObj
            }

            val result = stripe.confirmCardSetup(params.clientSecret, confirmData).await()

            if (result.error != null) {
                throw StripeException(result.error.message as String)
            }

            val setupIntent = result.setupIntent ?: throw StripeException("No setup intent returned")
            convertJsSetupIntentFull(setupIntent)
        }
    }

    public actual suspend fun handleNextActionForSetupIntent(clientSecret: String): StripeResult<SetupIntent> {
        return StripeResult.runCatching {
            val stripe = getStripe()

            val result = stripe.handleCardSetup(clientSecret).await()

            if (result.error != null) {
                throw StripeException(result.error.message as String)
            }

            val setupIntent = result.setupIntent ?: throw StripeException("No setup intent returned")
            convertJsSetupIntentFull(setupIntent)
        }
    }

    // ============================================================================
    // Customer
    // ============================================================================

    public actual suspend fun retrieveCustomer(customerId: String): StripeResult<Customer> {
        return StripeResult.failure(
            StripeException("Customer retrieval requires server-side implementation for security")
        )
    }

    public actual suspend fun createEphemeralKey(params: EphemeralKeyCreateParams): StripeResult<EphemeralKey> {
        return StripeResult.failure(
            StripeException("Ephemeral key creation requires server-side implementation for security")
        )
    }

    // ============================================================================
    // Private helper methods for converting JS objects to KMP models
    // ============================================================================

    private fun convertJsSource(jsSource: dynamic): Source {
        val status = SourceStatus.fromValue(jsSource.status as String)
            ?: throw StripeException("Unknown source status: ${jsSource.status}")

        val flow = SourceFlow.fromValue(jsSource.flow as String)
            ?: throw StripeException("Unknown source flow: ${jsSource.flow}")

        return Source(
            id = jsSource.id as String,
            type = SourceType.fromValue(jsSource.type as String),
            status = status,
            amount = (jsSource.amount as? Number)?.toLong(),
            currency = jsSource.currency as? String,
            clientSecret = jsSource.client_secret as String,
            flow = flow,
            redirect = jsSource.redirect?.let { redirect ->
                SourceRedirect(
                    returnUrl = redirect.return_url as String,
                    status = redirect.status as? String,
                    url = redirect.url as? String
                )
            },
            owner = jsSource.owner?.let { owner ->
                SourceOwner(
                    name = owner.name as? String,
                    email = owner.email as? String,
                    phone = owner.phone as? String,
                    address = owner.address?.let { addr ->
                        Address(
                            line1 = addr.line1 as? String,
                            line2 = addr.line2 as? String,
                            city = addr.city as? String,
                            state = addr.state as? String,
                            postalCode = addr.postal_code as? String,
                            country = addr.country as? String
                        )
                    }
                )
            },
            created = (jsSource.created as Number).toLong(),
            livemode = jsSource.livemode as Boolean
        )
    }

    private fun convertJsPaymentMethod(jsPaymentMethod: dynamic): PaymentMethod {
        val type = PaymentMethodType.fromValue(jsPaymentMethod.type as String)

        return PaymentMethod(
            id = jsPaymentMethod.id as String,
            type = type,
            created = (jsPaymentMethod.created as Number).toLong(),
            livemode = jsPaymentMethod.livemode as Boolean,
            billingDetails = jsPaymentMethod.billing_details?.let { billing ->
                BillingDetails(
                    name = billing.name as? String,
                    email = billing.email as? String,
                    phone = billing.phone as? String,
                    address = billing.address?.let { addr ->
                        Address(
                            line1 = addr.line1 as? String,
                            line2 = addr.line2 as? String,
                            city = addr.city as? String,
                            state = addr.state as? String,
                            postalCode = addr.postal_code as? String,
                            country = addr.country as? String
                        )
                    }
                )
            },
            card = jsPaymentMethod.card?.let { card ->
                Card(
                    brand = CardBrand.fromValue(card.brand as String),
                    last4 = card.last4 as String,
                    expMonth = (card.exp_month as Number).toInt(),
                    expYear = (card.exp_year as Number).toInt(),
                    funding = CardFunding.fromValue(card.funding as? String ?: "unknown"),
                    country = card.country as? String,
                    fingerprint = card.fingerprint as? String,
                    checks = card.checks?.let { checks ->
                        CardChecks(
                            addressLine1Check = checks.address_line1_check as? String,
                            addressPostalCodeCheck = checks.address_postal_code_check as? String,
                            cvcCheck = checks.cvc_check as? String
                        )
                    },
                    wallet = card.wallet?.let { wallet ->
                        CardWallet(type = wallet.type as String)
                    },
                    threeDSecureUsage = card.three_d_secure_usage?.let { usage ->
                        ThreeDSecureUsage(supported = usage.supported as Boolean)
                    },
                    networks = card.networks?.let { networks ->
                        CardNetworks(
                            available = (networks.available as Array<String>).toList(),
                            preferred = networks.preferred as? String
                        )
                    }
                )
            },
            customer = jsPaymentMethod.customer as? String,
            metadata = jsPaymentMethod.metadata?.let { meta ->
                val map = mutableMapOf<String, String>()
                val keys = js("Object").keys(meta) as Array<String>
                keys.forEach { key ->
                    map[key] = meta[key] as String
                }
                map
            }
        )
    }

    private fun convertJsPaymentIntentFull(jsIntent: dynamic): PaymentIntent {
        val status = PaymentIntentStatus.fromValue(jsIntent.status as String)
            ?: throw StripeException("Unknown payment intent status: ${jsIntent.status}")

        return PaymentIntent(
            id = jsIntent.id as String,
            clientSecret = jsIntent.client_secret as String,
            amount = (jsIntent.amount as Number).toLong(),
            currency = jsIntent.currency as String,
            status = status,
            created = (jsIntent.created as Number).toLong(),
            livemode = jsIntent.livemode as Boolean,
            paymentMethodId = jsIntent.payment_method as? String,
            paymentMethodTypes = (jsIntent.payment_method_types as? Array<String>)?.toList() ?: emptyList(),
            confirmationMethod = ConfirmationMethod.fromValue(jsIntent.confirmation_method as? String ?: "automatic")
                ?: ConfirmationMethod.AUTOMATIC,
            captureMethod = CaptureMethod.fromValue(jsIntent.capture_method as? String ?: "automatic")
                ?: CaptureMethod.AUTOMATIC,
            description = jsIntent.description as? String,
            receiptEmail = jsIntent.receipt_email as? String,
            setupFutureUsage = (jsIntent.setup_future_usage as? String)?.let { SetupFutureUsage.fromValue(it) },
            lastPaymentError = jsIntent.last_payment_error?.let { error ->
                PaymentIntentError(
                    type = error.type as String,
                    code = error.code as? String,
                    declineCode = error.decline_code as? String,
                    message = error.message as String
                )
            },
            nextAction = jsIntent.next_action?.let { action ->
                val type = NextActionType.fromValue(action.type as String)
                    ?: throw StripeException("Unknown next action type: ${action.type}")
                NextAction(
                    type = type,
                    redirectToUrl = action.redirect_to_url?.let { redirect ->
                        RedirectToUrl(
                            url = redirect.url as String,
                            returnUrl = redirect.return_url as? String
                        )
                    },
                    useStripeSdk = action.use_stripe_sdk?.let { sdk ->
                        val map = mutableMapOf<String, Any>()
                        val keys = js("Object").keys(sdk) as Array<String>
                        keys.forEach { key ->
                            map[key] = sdk[key]
                        }
                        map
                    }
                )
            },
            canceledAt = (jsIntent.canceled_at as? Number)?.toLong(),
            cancellationReason = jsIntent.cancellation_reason as? String,
            metadata = jsIntent.metadata?.let { meta ->
                val map = mutableMapOf<String, String>()
                val keys = js("Object").keys(meta) as Array<String>
                keys.forEach { key ->
                    map[key] = meta[key] as String
                }
                map
            }
        )
    }

    private fun convertJsSetupIntentFull(jsIntent: dynamic): SetupIntent {
        val status = SetupIntentStatus.fromValue(jsIntent.status as String)
            ?: throw StripeException("Unknown setup intent status: ${jsIntent.status}")

        return SetupIntent(
            id = jsIntent.id as String,
            clientSecret = jsIntent.client_secret as String,
            created = (jsIntent.created as Number).toLong(),
            livemode = jsIntent.livemode as Boolean,
            status = status,
            paymentMethodId = jsIntent.payment_method as? String,
            paymentMethodTypes = (jsIntent.payment_method_types as? Array<String>)?.toList() ?: emptyList(),
            description = jsIntent.description as? String,
            usage = SetupIntentUsage.fromValue(jsIntent.usage as? String ?: "off_session")
                ?: SetupIntentUsage.OFF_SESSION,
            customerId = jsIntent.customer as? String,
            lastSetupError = jsIntent.last_setup_error?.let { error ->
                SetupIntentError(
                    type = error.type as String,
                    code = error.code as? String,
                    declineCode = error.decline_code as? String,
                    message = error.message as String
                )
            },
            nextAction = jsIntent.next_action?.let { action ->
                val type = SetupNextActionType.fromValue(action.type as String)
                    ?: throw StripeException("Unknown next action type: ${action.type}")
                SetupNextAction(
                    type = type,
                    redirectToUrl = action.redirect_to_url?.let { redirect ->
                        RedirectToUrl(
                            url = redirect.url as String,
                            returnUrl = redirect.return_url as? String
                        )
                    },
                    useStripeSdk = action.use_stripe_sdk?.let { sdk ->
                        val map = mutableMapOf<String, Any>()
                        val keys = js("Object").keys(sdk) as Array<String>
                        keys.forEach { key ->
                            map[key] = sdk[key]
                        }
                        map
                    }
                )
            },
            cancellationReason = jsIntent.cancellation_reason as? String,
            metadata = jsIntent.metadata?.let { meta ->
                val map = mutableMapOf<String, String>()
                val keys = js("Object").keys(meta) as Array<String>
                keys.forEach { key ->
                    map[key] = meta[key] as String
                }
                map
            }
        )
    }

    public actual companion object {
        private var instance: Stripe? = null

        /**
         * Initialize the Stripe SDK with the provided configuration.
         *
         * CRITICAL-08: This now properly handles async loading.
         * The Stripe.js library is loaded asynchronously. You should call
         * awaitLoad() on the returned instance before using PaymentSheet.
         *
         * HIGH-07: Proper error handling for load failures.
         *
         * @param configuration The Stripe configuration
         * @return A Stripe instance (Stripe.js will load asynchronously)
         */
        public actual fun initialize(configuration: StripeConfiguration): Stripe {
            val stripe = Stripe(configuration)

            // CRITICAL-08: Load Stripe.js asynchronously with proper error handling
            StripeJS.loadStripe(configuration.publishableKey).then { stripeJs ->
                if (stripeJs != null) {
                    stripe.stripeInstance = stripeJs
                    if (configuration.enableLogging) {
                        console.log("Stripe.js loaded successfully")
                    }
                } else {
                    // HIGH-07: Handle null response from loadStripe
                    val error = "Stripe.js returned null - check your publishable key and network connection"
                    stripe.loadError = error
                    console.error(error)
                }
            }.catch { error ->
                // HIGH-07: Propagate errors properly
                val errorMsg = "Failed to load Stripe.js: ${error}"
                stripe.loadError = errorMsg
                console.error(errorMsg)
            }

            instance = stripe
            return stripe
        }

        /**
         * Initialize and wait for Stripe.js to load.
         * This is a convenience method that initializes and waits for load to complete.
         *
         * @param configuration The Stripe configuration
         * @return A fully loaded Stripe instance
         * @throws IllegalStateException if loading fails
         */
        public suspend fun initializeAndAwait(configuration: StripeConfiguration): Stripe {
            val stripe = initialize(configuration)
            stripe.awaitLoad()
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
