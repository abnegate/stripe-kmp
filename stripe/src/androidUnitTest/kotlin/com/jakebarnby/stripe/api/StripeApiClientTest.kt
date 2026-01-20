package com.jakebarnby.stripe.api

import com.jakebarnby.stripe.*
import com.jakebarnby.stripe.model.*
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StripeApiClientTest {
    private fun configuration() = StripeConfiguration(
        publishableKey = "pk_test_123",
        merchantDisplayName = "Demo Shop"
    )

    private fun MockRequestHandleScope.jsonResponse(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK
    ) = respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    @Test
    fun createCardToken_sendsHeaders_andParsesCardToken() = runTest {
        var captured: HttpRequestData? = null
        val engine = MockEngine { request ->
            captured = request
            jsonResponse(
                """
                {
                  "id": "tok_123",
                  "type": "card",
                  "created": 1700000000,
                  "livemode": false,
                  "used": false,
                  "card": {
                    "id": "card_123",
                    "brand": "visa",
                    "last4": "4242",
                    "exp_month": 12,
                    "exp_year": 2030,
                    "funding": "credit",
                    "country": "US"
                  }
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.createCardToken(
                params = CardParams(
                    number = "4242424242424242",
                    expMonth = 12,
                    expYear = 2030,
                    cvc = "123",
                    name = "Jane Doe",
                    addressLine1 = "123 Main St",
                    addressLine2 = "Apt 4",
                    addressCity = "San Francisco",
                    addressState = "CA",
                    addressZip = "94111",
                    addressCountry = "US"
                ),
                idempotencyKey = IdempotencyKey.fromValue("idem_123")
            )

            assertTrue(result.isSuccess())
            val token = result.getOrThrow()
            assertEquals("tok_123", token.id)
            assertEquals("visa", token.card?.brand)
            assertEquals("idem_123", captured?.headers?.get("Idempotency-Key"))
            assertEquals("Bearer pk_test_123", captured?.headers?.get(HttpHeaders.Authorization))
            assertEquals(HttpMethod.Post, captured?.method)
            assertTrue(captured?.url?.toString()?.contains("/v1/tokens") == true)
        } finally {
            client.close()
        }
    }

    @Test
    fun createCardToken_withMinimalFields_usesDefaults() = runTest {
        val engine = MockEngine {
            jsonResponse(
                """
                {
                  "id": "tok_min_123",
                  "created": 1
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.createCardToken(
                params = CardParams(
                    number = "4242424242424242",
                    expMonth = 1,
                    expYear = 2030
                ),
                idempotencyKey = null
            )

            val token = result.getOrThrow()
            assertEquals("tok_min_123", token.id)
            assertEquals("card", token.type)
            assertEquals(1L, token.created)
            assertEquals(false, token.livemode)
            assertEquals(false, token.used)
            assertNull(token.card)
            assertNull(token.bankAccount)
        } finally {
            client.close()
        }
    }

    @Test
    fun createCardToken_missingId_returnsFailure() = runTest {
        val engine = MockEngine { jsonResponse("{}") }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.createCardToken(
                params = CardParams(
                    number = "4242424242424242",
                    expMonth = 1,
                    expYear = 2030
                ),
                idempotencyKey = null
            )
            assertTrue(result.isFailure())
            assertTrue(result.errorOrNull()?.message?.contains("Missing token id") == true)
        } finally {
            client.close()
        }
    }

    @Test
    fun createBankAccountToken_parsesBankAccount() = runTest {
        val engine = MockEngine {
            jsonResponse(
                """
                {
                  "id": "tok_bank_123",
                  "type": "bank_account",
                  "created": 1700000001,
                  "livemode": false,
                  "used": false,
                  "bank_account": {
                    "id": "ba_123",
                    "country": "US",
                    "currency": "usd",
                    "last4": "6789",
                    "bank_name": "Chase",
                    "account_holder_name": "Jess",
                    "account_holder_type": "individual",
                    "routing_number": "110000000"
                  }
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.createBankAccountToken(
                params = BankAccountTokenParams.builder()
                    .country("US")
                    .currency("usd")
                    .accountNumber("000123456789")
                    .routingNumber("110000000")
                    .accountHolderName("Jess")
                    .accountHolderType(BankAccountTokenParams.AccountHolderType.INDIVIDUAL)
                    .build(),
                idempotencyKey = IdempotencyKey.fromValue("idem_bank_123")
            )

            val token = result.getOrThrow()
            assertEquals("tok_bank_123", token.id)
            assertEquals("ba_123", token.bankAccount?.id)
            assertEquals("Chase", token.bankAccount?.bankName)
        } finally {
            client.close()
        }
    }

    @Test
    fun createBankAccountToken_withMinimalFields() = runTest {
        val engine = MockEngine {
            jsonResponse(
                """
                {
                  "id": "tok_bank_min",
                  "created": 1,
                  "bank_account": {
                    "id": "ba_min",
                    "country": "US",
                    "currency": "usd",
                    "last4": "0000"
                  }
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.createBankAccountToken(
                params = BankAccountTokenParams.builder()
                    .country("US")
                    .currency("usd")
                    .accountNumber("000123456789")
                    .build(),
                idempotencyKey = null
            )

            val token = result.getOrThrow()
            assertEquals("tok_bank_min", token.id)
            assertEquals("ba_min", token.bankAccount?.id)
            assertEquals("usd", token.bankAccount?.currency)
        } finally {
            client.close()
        }
    }

    @Test
    fun createPiiToken_parsesMinimalToken() = runTest {
        val engine = MockEngine {
            jsonResponse(
                """
                {
                  "id": "tok_pii_123",
                  "type": "pii",
                  "created": 1700000002,
                  "livemode": false,
                  "used": false
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.createPiiToken(PiiTokenParams("000000000"))
            val token = result.getOrThrow()
            assertEquals("tok_pii_123", token.id)
            assertNull(token.card)
            assertNull(token.bankAccount)
        } finally {
            client.close()
        }
    }

    @Test
    fun createAccountToken_returnsFailure() = runTest {
        val client = StripeApiClient(configuration(), MockEngine { jsonResponse("{}") })
        try {
            val result = client.createAccountToken(
                AccountParams(
                    businessType = AccountParams.BusinessType.INDIVIDUAL,
                    tosShownAndAccepted = true
                )
            )
            assertTrue(result.isFailure())
            assertTrue(result.errorOrNull()?.message?.contains("server-side creation") == true)
        } finally {
            client.close()
        }
    }

    @Test
    fun createSource_parsesResponse() = runTest {
        val engine = MockEngine {
            jsonResponse(
                """
                {
                  "id": "src_123",
                  "type": "bancontact",
                  "status": "chargeable",
                  "amount": 1500,
                  "currency": "eur",
                  "client_secret": "src_secret",
                  "flow": "redirect",
                  "created": 1700000003,
                  "livemode": false
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.createSource(
                params = SourceParams.createBancontactParams(
                    amount = 1500,
                    name = "Jess",
                    returnUrl = "https://example.com/return"
                ),
                idempotencyKey = null
            )

            val source = result.getOrThrow()
            assertEquals("src_123", source.id)
            assertEquals(SourceType.BANCONTACT, source.type)
            assertEquals(SourceStatus.CHARGEABLE, source.status)
            assertEquals(SourceFlow.REDIRECT, source.flow)
        } finally {
            client.close()
        }
    }

    @Test
    fun createSource_withOwnerDetailsAndExtraParams() = runTest {
        val engine = MockEngine {
            jsonResponse(
                """
                {
                  "id": "src_full",
                  "type": "card",
                  "status": "pending",
                  "client_secret": "src_full_secret",
                  "flow": "none",
                  "created": 1700000004,
                  "livemode": false
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.createSource(
                params = SourceParams(
                    type = SourceType.CARD,
                    amount = 2000,
                    currency = "usd",
                    owner = SourceOwner(
                        name = "Jess",
                        email = "jess@example.com",
                        phone = "+15555555555",
                        address = Address(
                            line1 = "1 Main",
                            line2 = "Apt 2",
                            city = "San Francisco",
                            state = "CA",
                            postalCode = "94111",
                            country = "US"
                        )
                    ),
                    redirect = SourceRedirect(returnUrl = "https://example.com/return"),
                    extraParams = mapOf(
                        "statement_descriptor" to "Stripe Test",
                        "custom_flag" to true
                    )
                ),
                idempotencyKey = null
            )

            val source = result.getOrThrow()
            assertEquals("src_full", source.id)
            assertEquals(SourceType.CARD, source.type)
        } finally {
            client.close()
        }
    }

    @Test
    fun retrieveSource_defaultsMissingFields() = runTest {
        val engine = MockEngine {
            jsonResponse(
                """
                {
                  "id": "src_defaults",
                  "client_secret": "src_secret"
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.retrieveSource(
                sourceId = "src_defaults",
                clientSecret = "src_client_secret"
            )
            val source = result.getOrThrow()
            assertEquals("src_defaults", source.id)
            assertEquals(SourceType.CARD, source.type)
            assertEquals(SourceStatus.PENDING, source.status)
            assertEquals(SourceFlow.NONE, source.flow)
            assertEquals(0L, source.created)
            assertEquals(false, source.livemode)
        } finally {
            client.close()
        }
    }

    @Test
    fun retrieveSource_includesClientSecret() = runTest {
        var captured: HttpRequestData? = null
        val engine = MockEngine { request ->
            captured = request
            jsonResponse(
                """
                {
                  "id": "src_456",
                  "type": "card",
                  "status": "pending",
                  "client_secret": "src_secret",
                  "created": 1700000004,
                  "livemode": false
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.retrieveSource(
                sourceId = "src_456",
                clientSecret = "src_secret"
            )
            val source = result.getOrThrow()
            assertEquals("src_456", source.id)
            assertEquals("src_secret", captured?.url?.parameters?.get("client_secret"))
            assertTrue(captured?.url?.toString()?.contains("/v1/sources/src_456") == true)
        } finally {
            client.close()
        }
    }

    @Test
    fun createPaymentMethod_parsesCardAndBillingDetails() = runTest {
        val engine = MockEngine {
            jsonResponse(
                """
                {
                  "id": "pm_123",
                  "type": "card",
                  "created": 1700000005,
                  "livemode": false,
                  "customer": "cus_123",
                  "card": {
                    "brand": "visa",
                    "last4": "4242",
                    "exp_month": 4,
                    "exp_year": 2029,
                    "funding": "credit",
                    "country": "US"
                  },
                  "billing_details": {
                    "name": "Jane Doe",
                    "email": "jane@example.com",
                    "phone": "+123456789",
                    "address": {
                      "line1": "1 Main",
                      "line2": "Apt 2",
                      "city": "SF",
                      "state": "CA",
                      "postal_code": "94111",
                      "country": "US"
                    }
                  }
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.createPaymentMethod(
                params = PaymentMethodCreateParams.createCard(
                    number = "4242424242424242",
                    expMonth = 4,
                    expYear = 2029,
                    cvc = "123"
                ),
                idempotencyKey = null
            )

            val method = result.getOrThrow()
            assertEquals("pm_123", method.id)
            assertEquals(CardBrand.VISA, method.card?.brand)
            assertEquals("Jane Doe", method.billingDetails?.name)
            assertEquals("1 Main", method.billingDetails?.address?.line1)
        } finally {
            client.close()
        }
    }

    @Test
    fun createPaymentMethod_withBillingDetailsAndToken() = runTest {
        val engine = MockEngine {
            jsonResponse(
                """
                {
                  "id": "pm_token_123",
                  "type": "card",
                  "created": 1700000011,
                  "livemode": false
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val billingDetails = BillingDetails(
                name = "Jess",
                email = "jess@example.com",
                phone = "+15555555555",
                address = Address(
                    line1 = "500 Main",
                    line2 = "Suite 10",
                    city = "Denver",
                    state = "CO",
                    postalCode = "80202",
                    country = "US"
                )
            )
            val result = client.createPaymentMethod(
                params = PaymentMethodCreateParams.createCardFromToken(
                    token = "tok_123",
                    billingDetails = billingDetails
                ),
                idempotencyKey = IdempotencyKey.fromValue("idem_pm_123")
            )

            val method = result.getOrThrow()
            assertEquals("pm_token_123", method.id)
            assertEquals(PaymentMethodType.CARD, method.type)
        } finally {
            client.close()
        }
    }

    @Test
    fun retrievePaymentMethod_parsesResponse() = runTest {
        val engine = MockEngine {
            jsonResponse(
                """
                {
                  "id": "pm_456",
                  "type": "card",
                  "created": 1700000006,
                  "livemode": false
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.retrievePaymentMethod("pm_456")
            val method = result.getOrThrow()
            assertEquals("pm_456", method.id)
            assertEquals(PaymentMethodType.CARD, method.type)
        } finally {
            client.close()
        }
    }

    @Test
    fun retrievePaymentIntent_buildsIdFromSecret() = runTest {
        var captured: HttpRequestData? = null
        val engine = MockEngine { request ->
            captured = request
            jsonResponse(
                """
                {
                  "id": "pi_123",
                  "client_secret": "pi_123_secret_456",
                  "amount": 1099,
                  "currency": "usd",
                  "status": "succeeded",
                  "created": 1700000007,
                  "livemode": false,
                  "payment_method": "pm_123",
                  "capture_method": "manual"
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.retrievePaymentIntent("pi_123_secret_456")
            val intent = result.getOrThrow()
            assertEquals("pi_123", intent.id)
            assertEquals("pi_123_secret_456", captured?.url?.parameters?.get("client_secret"))
            assertTrue(captured?.url?.toString()?.contains("/v1/payment_intents/pi_123") == true)
        } finally {
            client.close()
        }
    }

    @Test
    fun retrievePaymentIntent_defaultsMissingFields() = runTest {
        val engine = MockEngine {
            jsonResponse(
                """
                {
                  "id": "pi_defaults",
                  "client_secret": "pi_defaults_secret"
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.retrievePaymentIntent("pi_defaults_secret")
            val intent = result.getOrThrow()
            assertEquals("pi_defaults", intent.id)
            assertEquals(PaymentIntentStatus.REQUIRES_PAYMENT_METHOD, intent.status)
            assertEquals(CaptureMethod.AUTOMATIC, intent.captureMethod)
            assertEquals("usd", intent.currency)
            assertEquals(1L, intent.created)
            assertEquals(false, intent.livemode)
            assertNull(intent.paymentMethodId)
        } finally {
            client.close()
        }
    }

    @Test
    fun confirmPaymentIntent_includesClientSecret() = runTest {
        var captured: HttpRequestData? = null
        val engine = MockEngine { request ->
            captured = request
            jsonResponse(
                """
                {
                  "id": "pi_789",
                  "client_secret": "pi_789_secret_111",
                  "amount": 2500,
                  "currency": "usd",
                  "status": "processing",
                  "created": 1700000008,
                  "livemode": false,
                  "capture_method": "automatic"
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.confirmPaymentIntent(
                params = ConfirmPaymentIntentParams.createWithPaymentMethodId(
                    paymentMethodId = "pm_123",
                    clientSecret = "pi_789_secret_111",
                    returnUrl = "https://example.com/return"
                ),
                idempotencyKey = IdempotencyKey.fromValue("idem_confirm_pi")
            )
            val intent = result.getOrThrow()
            assertEquals("pi_789", intent.id)
            assertEquals("pi_789_secret_111", captured?.url?.parameters?.get("client_secret"))
            assertTrue(captured?.url?.toString()?.contains("/v1/payment_intents/pi_789/confirm") == true)
            assertEquals("idem_confirm_pi", captured?.headers?.get("Idempotency-Key"))
        } finally {
            client.close()
        }
    }

    @Test
    fun confirmPaymentIntent_withPaymentMethodCreateParams() = runTest {
        var captured: HttpRequestData? = null
        val engine = MockEngine { request ->
            captured = request
            jsonResponse(
                """
                {
                  "id": "pi_alt",
                  "client_secret": "pi_alt_secret_123",
                  "amount": 1800,
                  "currency": "usd",
                  "status": "requires_action",
                  "created": 1700000012,
                  "livemode": false,
                  "capture_method": "automatic"
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.confirmPaymentIntent(
                params = ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(
                    paymentMethodCreateParams = PaymentMethodCreateParams.createCard(
                        number = "4242424242424242",
                        expMonth = 10,
                        expYear = 2030,
                        cvc = "321"
                    ),
                    clientSecret = "pi_alt_secret_123"
                ),
                idempotencyKey = null
            )
            val intent = result.getOrThrow()
            assertEquals("pi_alt", intent.id)
            assertEquals("pi_alt_secret_123", captured?.url?.parameters?.get("client_secret"))
            assertTrue(captured?.url?.toString()?.contains("/v1/payment_intents/pi_alt/confirm") == true)
        } finally {
            client.close()
        }
    }

    @Test
    fun handleNextActionForPayment_returnsFailure() = runTest {
        val client = StripeApiClient(configuration(), MockEngine { jsonResponse("{}") })
        try {
            val result = client.handleNextActionForPayment("pi_123_secret_456")
            assertTrue(result.isFailure())
            assertTrue(result.errorOrNull()?.message?.contains("3D Secure") == true)
        } finally {
            client.close()
        }
    }

    @Test
    fun retrieveSetupIntent_buildsIdFromSecret() = runTest {
        var captured: HttpRequestData? = null
        val engine = MockEngine { request ->
            captured = request
            jsonResponse(
                """
                {
                  "id": "seti_123",
                  "client_secret": "seti_123_secret_456",
                  "status": "requires_action",
                  "created": 1700000009,
                  "livemode": false,
                  "payment_method": "pm_456",
                  "usage": "on_session"
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.retrieveSetupIntent("seti_123_secret_456")
            val intent = result.getOrThrow()
            assertEquals("seti_123", intent.id)
            assertEquals("seti_123_secret_456", captured?.url?.parameters?.get("client_secret"))
            assertTrue(captured?.url?.toString()?.contains("/v1/setup_intents/seti_123") == true)
        } finally {
            client.close()
        }
    }

    @Test
    fun retrieveSetupIntent_defaultsMissingFields() = runTest {
        val engine = MockEngine {
            jsonResponse(
                """
                {
                  "id": "seti_defaults",
                  "client_secret": "seti_defaults_secret"
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.retrieveSetupIntent("seti_defaults_secret")
            val intent = result.getOrThrow()
            assertEquals("seti_defaults", intent.id)
            assertEquals(SetupIntentStatus.REQUIRES_PAYMENT_METHOD, intent.status)
            assertEquals(SetupIntentUsage.OFF_SESSION, intent.usage)
            assertEquals(1L, intent.created)
            assertEquals(false, intent.livemode)
            assertNull(intent.paymentMethodId)
        } finally {
            client.close()
        }
    }

    @Test
    fun confirmSetupIntent_includesClientSecret() = runTest {
        var captured: HttpRequestData? = null
        val engine = MockEngine { request ->
            captured = request
            jsonResponse(
                """
                {
                  "id": "seti_789",
                  "client_secret": "seti_789_secret_111",
                  "status": "processing",
                  "created": 1700000010,
                  "livemode": false,
                  "payment_method": "pm_789",
                  "usage": "off_session"
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.confirmSetupIntent(
                params = ConfirmSetupIntentParams.createWithPaymentMethodId(
                    paymentMethodId = "pm_789",
                    clientSecret = "seti_789_secret_111"
                ),
                idempotencyKey = null
            )
            val intent = result.getOrThrow()
            assertEquals("seti_789", intent.id)
            assertEquals("seti_789_secret_111", captured?.url?.parameters?.get("client_secret"))
            assertTrue(captured?.url?.toString()?.contains("/v1/setup_intents/seti_789/confirm") == true)
        } finally {
            client.close()
        }
    }

    @Test
    fun confirmSetupIntent_withReturnUrlAndPaymentMethodCreateParams() = runTest {
        var captured: HttpRequestData? = null
        val engine = MockEngine { request ->
            captured = request
            jsonResponse(
                """
                {
                  "id": "seti_alt",
                  "client_secret": "seti_alt_secret_123",
                  "status": "requires_action",
                  "created": 1700000013,
                  "livemode": false,
                  "usage": "off_session"
                }
                """.trimIndent()
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val params = ConfirmSetupIntentParams.builder("seti_alt_secret_123")
                .paymentMethodCreateParams(
                    PaymentMethodCreateParams.createCard(
                        number = "4242424242424242",
                        expMonth = 3,
                        expYear = 2030,
                        cvc = "123"
                    )
                )
                .returnUrl("https://example.com/return")
                .build()

            val result = client.confirmSetupIntent(
                params = params,
                idempotencyKey = null
            )
            val intent = result.getOrThrow()
            assertEquals("seti_alt", intent.id)
            assertEquals("seti_alt_secret_123", captured?.url?.parameters?.get("client_secret"))
            assertTrue(captured?.url?.toString()?.contains("/v1/setup_intents/seti_alt/confirm") == true)
        } finally {
            client.close()
        }
    }

    @Test
    fun handleNextActionForSetupIntent_returnsFailure() = runTest {
        val client = StripeApiClient(configuration(), MockEngine { jsonResponse("{}") })
        try {
            val result = client.handleNextActionForSetupIntent("seti_123_secret_456")
            assertTrue(result.isFailure())
            assertTrue(result.errorOrNull()?.message?.contains("3D Secure") == true)
        } finally {
            client.close()
        }
    }

    @Test
    fun errorResponse_parsesStripeErrorMessage() = runTest {
        val engine = MockEngine {
            jsonResponse(
                """
                {
                  "error": {
                    "message": "Card declined"
                  }
                }
                """.trimIndent(),
                status = HttpStatusCode.PaymentRequired
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.createCardToken(
                params = CardParams(
                    number = "4242424242424242",
                    expMonth = 12,
                    expYear = 2030,
                    cvc = "123"
                ),
                idempotencyKey = null
            )
            assertTrue(result.isFailure())
            assertEquals("Card declined", result.errorOrNull()?.message)
        } finally {
            client.close()
        }
    }

    @Test
    fun errorResponse_withoutMessage_usesStatus() = runTest {
        val engine = MockEngine {
            jsonResponse(
                """
                {
                  "error": {
                    "code": "card_declined"
                  }
                }
                """.trimIndent(),
                status = HttpStatusCode.PaymentRequired
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.createCardToken(
                params = CardParams(
                    number = "4242424242424242",
                    expMonth = 12,
                    expYear = 2030
                ),
                idempotencyKey = null
            )
            assertTrue(result.isFailure())
            assertTrue(result.errorOrNull()?.message?.contains("Request failed with status") == true)
        } finally {
            client.close()
        }
    }

    @Test
    fun errorResponse_withInvalidJson_returnsGenericMessage() = runTest {
        val engine = MockEngine {
            respond(
                content = "not-json",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = StripeApiClient(configuration(), engine)
        try {
            val result = client.createCardToken(
                params = CardParams(
                    number = "4242424242424242",
                    expMonth = 12,
                    expYear = 2030,
                    cvc = "123"
                ),
                idempotencyKey = null
            )
            assertTrue(result.isFailure())
            assertTrue(result.errorOrNull()?.message?.contains("Request failed with status") == true)
        } finally {
            client.close()
        }
    }
}
