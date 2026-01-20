package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*

/**
 * Shared Stripe API client using Ktor for REST API operations.
 *
 * This client handles all headless Stripe operations via the REST API,
 * eliminating the need for platform-specific SDK implementations for
 * basic payment operations.
 *
 * UI components (PaymentSheet, Apple Pay, Google Pay) still require
 * native SDK implementations on each platform.
 */
internal class StripeApiClient(
    private val configuration: StripeConfiguration,
    httpClientEngine: HttpClientEngine
) {
    private val client = HttpClient(httpClientEngine) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val baseUrl = "https://api.stripe.com/v1"


    suspend fun createCardToken(
        params: CardParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Token> = StripeResult.runCatching {
        val response = client.submitForm(
            url = "$baseUrl/tokens",
            formParameters = parameters {
                append("card[number]", params.number)
                append("card[exp_month]", params.expMonth.toString())
                append("card[exp_year]", params.expYear.toString())
                params.cvc?.let { append("card[cvc]", it) }
                params.name?.let { append("card[name]", it) }
                params.addressLine1?.let { append("card[address_line1]", it) }
                params.addressLine2?.let { append("card[address_line2]", it) }
                params.addressCity?.let { append("card[address_city]", it) }
                params.addressState?.let { append("card[address_state]", it) }
                params.addressZip?.let { append("card[address_zip]", it) }
                params.addressCountry?.let { append("card[address_country]", it) }
            }
        ) {
            header("Authorization", "Bearer ${configuration.publishableKey}")
            header("Stripe-Version", "2023-10-16")
            idempotencyKey?.let { header("Idempotency-Key", it.value) }
        }

        parseTokenResponse(response)
    }

    suspend fun createBankAccountToken(
        params: BankAccountTokenParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Token> = StripeResult.runCatching {
        val response = client.submitForm(
            url = "$baseUrl/tokens",
            formParameters = parameters {
                append("bank_account[country]", params.country)
                append("bank_account[currency]", params.currency)
                append("bank_account[account_number]", params.accountNumber)
                params.routingNumber?.let { append("bank_account[routing_number]", it) }
                params.accountHolderName?.let { append("bank_account[account_holder_name]", it) }
                params.accountHolderType?.let { append("bank_account[account_holder_type]", it.name.lowercase()) }
            }
        ) {
            header("Authorization", "Bearer ${configuration.publishableKey}")
            header("Stripe-Version", "2023-10-16")
            idempotencyKey?.let { header("Idempotency-Key", it.value) }
        }

        parseTokenResponse(response)
    }

    suspend fun createPiiToken(
        params: PiiTokenParams
    ): StripeResult<Token> = StripeResult.runCatching {
        val response = client.submitForm(
            url = "$baseUrl/tokens",
            formParameters = parameters {
                append("pii[personal_id_number]", params.personalIdNumber)
            }
        ) {
            header("Authorization", "Bearer ${configuration.publishableKey}")
            header("Stripe-Version", "2023-10-16")
        }

        parseTokenResponse(response)
    }

    suspend fun createAccountToken(
        params: AccountParams
    ): StripeResult<Token> = StripeResult.failure(
        StripeException("Account tokens require server-side creation with secret key")
    )

    private suspend fun parseTokenResponse(response: HttpResponse): Token {
        if (!response.status.isSuccess()) {
            throw parseError(response)
        }

        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        return Token(
            id = json["id"]?.jsonPrimitive?.content ?: throw StripeException("Missing token id"),
            type = json["type"]?.jsonPrimitive?.content ?: "card",
            created = json["created"]?.jsonPrimitive?.longOrNull ?: 0L,
            livemode = json["livemode"]?.jsonPrimitive?.booleanOrNull ?: false,
            used = json["used"]?.jsonPrimitive?.booleanOrNull ?: false,
            card = json["card"]?.jsonObject?.let { parseCardToken(it) },
            bankAccount = json["bank_account"]?.jsonObject?.let { parseBankAccountToken(it) }
        )
    }


    suspend fun createSource(
        params: SourceParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<Source> = StripeResult.runCatching {
        val response = client.submitForm(
            url = "$baseUrl/sources",
            formParameters = parameters {
                append("type", params.type.value)
                params.amount?.let { append("amount", it.toString()) }
                params.currency?.let { append("currency", it) }
                params.redirect?.returnUrl?.let { append("redirect[return_url]", it) }
                params.owner?.let { owner ->
                    owner.name?.let { append("owner[name]", it) }
                    owner.email?.let { append("owner[email]", it) }
                    owner.phone?.let { append("owner[phone]", it) }
                    owner.address?.let { addr ->
                        addr.line1?.let { append("owner[address][line1]", it) }
                        addr.line2?.let { append("owner[address][line2]", it) }
                        addr.city?.let { append("owner[address][city]", it) }
                        addr.state?.let { append("owner[address][state]", it) }
                        addr.postalCode?.let { append("owner[address][postal_code]", it) }
                        addr.country?.let { append("owner[address][country]", it) }
                    }
                }
                params.extraParams?.forEach { (key, value) ->
                    append(key, value.toString())
                }
            }
        ) {
            header("Authorization", "Bearer ${configuration.publishableKey}")
            header("Stripe-Version", "2023-10-16")
            idempotencyKey?.let { header("Idempotency-Key", it.value) }
        }

        parseSourceResponse(response)
    }

    suspend fun retrieveSource(
        sourceId: String,
        clientSecret: String
    ): StripeResult<Source> = StripeResult.runCatching {
        val response = client.get("$baseUrl/sources/$sourceId") {
            parameter("client_secret", clientSecret)
            header("Authorization", "Bearer ${configuration.publishableKey}")
            header("Stripe-Version", "2023-10-16")
        }

        parseSourceResponse(response)
    }

    private suspend fun parseSourceResponse(response: HttpResponse): Source {
        if (!response.status.isSuccess()) {
            throw parseError(response)
        }

        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        return Source(
            id = json["id"]?.jsonPrimitive?.content ?: throw StripeException("Missing source id"),
            type = SourceType.fromValue(json["type"]?.jsonPrimitive?.content ?: "card"),
            status = SourceStatus.fromValue(json["status"]?.jsonPrimitive?.content ?: "pending") ?: SourceStatus.PENDING,
            amount = json["amount"]?.jsonPrimitive?.longOrNull,
            currency = json["currency"]?.jsonPrimitive?.content,
            clientSecret = json["client_secret"]?.jsonPrimitive?.content ?: "",
            flow = SourceFlow.fromValue(json["flow"]?.jsonPrimitive?.content ?: "none") ?: SourceFlow.NONE,
            created = json["created"]?.jsonPrimitive?.longOrNull ?: 0L,
            livemode = json["livemode"]?.jsonPrimitive?.booleanOrNull ?: false
        )
    }


    suspend fun createPaymentMethod(
        params: PaymentMethodCreateParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<PaymentMethod> = StripeResult.runCatching {
        val formParams = parameters {
            append("type", params.type.value)

            params.card?.let { card ->
                card.number?.let { append("card[number]", it) }
                card.expMonth?.let { append("card[exp_month]", it.toString()) }
                card.expYear?.let { append("card[exp_year]", it.toString()) }
                card.cvc?.let { append("card[cvc]", it) }
                card.token?.let { append("card[token]", it) }
            }

            params.billingDetails?.let { billing ->
                billing.name?.let { append("billing_details[name]", it) }
                billing.email?.let { append("billing_details[email]", it) }
                billing.phone?.let { append("billing_details[phone]", it) }
                billing.address?.let { addr ->
                    addr.line1?.let { append("billing_details[address][line1]", it) }
                    addr.line2?.let { append("billing_details[address][line2]", it) }
                    addr.city?.let { append("billing_details[address][city]", it) }
                    addr.state?.let { append("billing_details[address][state]", it) }
                    addr.postalCode?.let { append("billing_details[address][postal_code]", it) }
                    addr.country?.let { append("billing_details[address][country]", it) }
                }
            }
        }

        val response = client.submitForm(url = "$baseUrl/payment_methods", formParameters = formParams) {
            header("Authorization", "Bearer ${configuration.publishableKey}")
            header("Stripe-Version", "2023-10-16")
            idempotencyKey?.let { header("Idempotency-Key", it.value) }
        }

        parsePaymentMethodResponse(response)
    }

    suspend fun retrievePaymentMethod(
        paymentMethodId: String
    ): StripeResult<PaymentMethod> = StripeResult.runCatching {
        val response = client.get("$baseUrl/payment_methods/$paymentMethodId") {
            header("Authorization", "Bearer ${configuration.publishableKey}")
            header("Stripe-Version", "2023-10-16")
        }

        parsePaymentMethodResponse(response)
    }

    private suspend fun parsePaymentMethodResponse(response: HttpResponse): PaymentMethod {
        if (!response.status.isSuccess()) {
            throw parseError(response)
        }

        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        return PaymentMethod(
            id = json["id"]?.jsonPrimitive?.content ?: throw StripeException("Missing payment method id"),
            type = PaymentMethodType.fromValue(json["type"]?.jsonPrimitive?.content ?: "card"),
            created = json["created"]?.jsonPrimitive?.longOrNull ?: 0L,
            livemode = json["livemode"]?.jsonPrimitive?.booleanOrNull ?: false,
            customer = json["customer"]?.jsonPrimitive?.contentOrNull,
            card = json["card"]?.jsonObject?.let { parseCard(it) },
            billingDetails = json["billing_details"]?.jsonObject?.let { parseBillingDetails(it) }
        )
    }


    suspend fun retrievePaymentIntent(
        clientSecret: String
    ): StripeResult<PaymentIntent> = StripeResult.runCatching {
        val intentId = clientSecret.substringBefore("_secret_")
        val response = client.get("$baseUrl/payment_intents/$intentId") {
            parameter("client_secret", clientSecret)
            header("Authorization", "Bearer ${configuration.publishableKey}")
            header("Stripe-Version", "2023-10-16")
        }

        parsePaymentIntentResponse(response)
    }

    suspend fun confirmPaymentIntent(
        params: ConfirmPaymentIntentParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<PaymentIntent> = StripeResult.runCatching {
        val intentId = params.clientSecret.substringBefore("_secret_")

        val formParams = parameters {
            params.paymentMethodId?.let { append("payment_method", it) }
            params.returnUrl?.let { append("return_url", it) }
        }

        val response = client.submitForm(
            url = "$baseUrl/payment_intents/$intentId/confirm",
            formParameters = formParams
        ) {
            parameter("client_secret", params.clientSecret)
            header("Authorization", "Bearer ${configuration.publishableKey}")
            header("Stripe-Version", "2023-10-16")
            idempotencyKey?.let { header("Idempotency-Key", it.value) }
        }

        parsePaymentIntentResponse(response)
    }

    suspend fun handleNextActionForPayment(
        clientSecret: String
    ): StripeResult<PaymentIntent> = StripeResult.failure(
        StripeException(
            "3D Secure authentication requires native UI. " +
            "Use PaymentSheet or platform-specific payment handler."
        )
    )

    private suspend fun parsePaymentIntentResponse(response: HttpResponse): PaymentIntent {
        if (!response.status.isSuccess()) {
            throw parseError(response)
        }

        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        return PaymentIntent(
            id = json["id"]?.jsonPrimitive?.content ?: throw StripeException("Missing payment intent id"),
            clientSecret = json["client_secret"]?.jsonPrimitive?.content ?: "",
            amount = json["amount"]?.jsonPrimitive?.longOrNull ?: 0L,
            currency = json["currency"]?.jsonPrimitive?.content ?: "usd",
            status = PaymentIntentStatus.fromValue(json["status"]?.jsonPrimitive?.content ?: "requires_payment_method")
                ?: PaymentIntentStatus.REQUIRES_PAYMENT_METHOD,
            created = json["created"]?.jsonPrimitive?.longOrNull ?: 1L,
            livemode = json["livemode"]?.jsonPrimitive?.booleanOrNull ?: false,
            paymentMethodId = json["payment_method"]?.jsonPrimitive?.contentOrNull,
            captureMethod = CaptureMethod.fromValue(json["capture_method"]?.jsonPrimitive?.content ?: "automatic")
                ?: CaptureMethod.AUTOMATIC
        )
    }


    suspend fun retrieveSetupIntent(
        clientSecret: String
    ): StripeResult<SetupIntent> = StripeResult.runCatching {
        val intentId = clientSecret.substringBefore("_secret_")
        val response = client.get("$baseUrl/setup_intents/$intentId") {
            parameter("client_secret", clientSecret)
            header("Authorization", "Bearer ${configuration.publishableKey}")
            header("Stripe-Version", "2023-10-16")
        }

        parseSetupIntentResponse(response)
    }

    suspend fun confirmSetupIntent(
        params: ConfirmSetupIntentParams,
        idempotencyKey: IdempotencyKey?
    ): StripeResult<SetupIntent> = StripeResult.runCatching {
        val intentId = params.clientSecret.substringBefore("_secret_")

        val formParams = parameters {
            params.paymentMethodId?.let { append("payment_method", it) }
            params.returnUrl?.let { append("return_url", it) }
        }

        val response = client.submitForm(
            url = "$baseUrl/setup_intents/$intentId/confirm",
            formParameters = formParams
        ) {
            parameter("client_secret", params.clientSecret)
            header("Authorization", "Bearer ${configuration.publishableKey}")
            header("Stripe-Version", "2023-10-16")
            idempotencyKey?.let { header("Idempotency-Key", it.value) }
        }

        parseSetupIntentResponse(response)
    }

    suspend fun handleNextActionForSetupIntent(
        clientSecret: String
    ): StripeResult<SetupIntent> = StripeResult.failure(
        StripeException(
            "3D Secure authentication requires native UI. " +
            "Use PaymentSheet or platform-specific payment handler."
        )
    )

    private suspend fun parseSetupIntentResponse(response: HttpResponse): SetupIntent {
        if (!response.status.isSuccess()) {
            throw parseError(response)
        }

        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        return SetupIntent(
            id = json["id"]?.jsonPrimitive?.content ?: throw StripeException("Missing setup intent id"),
            clientSecret = json["client_secret"]?.jsonPrimitive?.content ?: "",
            status = SetupIntentStatus.fromValue(json["status"]?.jsonPrimitive?.content ?: "requires_payment_method")
                ?: SetupIntentStatus.REQUIRES_PAYMENT_METHOD,
            created = json["created"]?.jsonPrimitive?.longOrNull ?: 1L,
            livemode = json["livemode"]?.jsonPrimitive?.booleanOrNull ?: false,
            paymentMethodId = json["payment_method"]?.jsonPrimitive?.contentOrNull,
            usage = SetupIntentUsage.fromValue(json["usage"]?.jsonPrimitive?.content ?: "off_session")
                ?: SetupIntentUsage.OFF_SESSION
        )
    }


    private suspend fun parseError(response: HttpResponse): StripeException {
        return try {
            val errorBody = response.bodyAsText()
            val errorJson = Json.parseToJsonElement(errorBody).jsonObject
            val error = errorJson["error"]?.jsonObject
            val message = error?.get("message")?.jsonPrimitive?.contentOrNull
                ?: "Request failed with status ${response.status}"
            StripeException(message)
        } catch (e: Exception) {
            StripeException("Request failed with status ${response.status}")
        }
    }

    private fun parseCardToken(json: JsonObject): CardToken {
        return CardToken(
            id = json["id"]?.jsonPrimitive?.content ?: "",
            brand = json["brand"]?.jsonPrimitive?.content ?: "unknown",
            last4 = json["last4"]?.jsonPrimitive?.content ?: "0000",
            expMonth = json["exp_month"]?.jsonPrimitive?.intOrNull ?: 1,
            expYear = json["exp_year"]?.jsonPrimitive?.intOrNull ?: 2000,
            funding = json["funding"]?.jsonPrimitive?.contentOrNull,
            country = json["country"]?.jsonPrimitive?.contentOrNull
        )
    }

    private fun parseBankAccountToken(json: JsonObject): BankAccountToken {
        return BankAccountToken(
            id = json["id"]?.jsonPrimitive?.content ?: "",
            country = json["country"]?.jsonPrimitive?.content ?: "US",
            currency = json["currency"]?.jsonPrimitive?.content ?: "usd",
            last4 = json["last4"]?.jsonPrimitive?.content ?: "0000",
            bankName = json["bank_name"]?.jsonPrimitive?.contentOrNull,
            accountHolderName = json["account_holder_name"]?.jsonPrimitive?.contentOrNull,
            accountHolderType = json["account_holder_type"]?.jsonPrimitive?.contentOrNull,
            routingNumber = json["routing_number"]?.jsonPrimitive?.contentOrNull
        )
    }

    private fun parseCard(json: JsonObject): Card {
        return Card(
            brand = CardBrand.fromValue(json["brand"]?.jsonPrimitive?.content ?: "unknown"),
            last4 = json["last4"]?.jsonPrimitive?.content ?: "0000",
            expMonth = json["exp_month"]?.jsonPrimitive?.intOrNull ?: 1,
            expYear = json["exp_year"]?.jsonPrimitive?.intOrNull ?: 2000,
            funding = CardFunding.fromValue(json["funding"]?.jsonPrimitive?.content ?: "unknown"),
            country = json["country"]?.jsonPrimitive?.contentOrNull
        )
    }

    private fun parseBillingDetails(json: JsonObject): BillingDetails {
        return BillingDetails(
            name = json["name"]?.jsonPrimitive?.contentOrNull,
            email = json["email"]?.jsonPrimitive?.contentOrNull,
            phone = json["phone"]?.jsonPrimitive?.contentOrNull,
            address = json["address"]?.jsonObject?.let { parseAddress(it) }
        )
    }

    private fun parseAddress(json: JsonObject): Address {
        return Address(
            line1 = json["line1"]?.jsonPrimitive?.contentOrNull,
            line2 = json["line2"]?.jsonPrimitive?.contentOrNull,
            city = json["city"]?.jsonPrimitive?.contentOrNull,
            state = json["state"]?.jsonPrimitive?.contentOrNull,
            postalCode = json["postal_code"]?.jsonPrimitive?.contentOrNull,
            country = json["country"]?.jsonPrimitive?.contentOrNull
        )
    }

    fun close() {
        client.close()
    }
}
