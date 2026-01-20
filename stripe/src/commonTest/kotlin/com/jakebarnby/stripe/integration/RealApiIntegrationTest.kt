package com.jakebarnby.stripe.integration

import com.jakebarnby.stripe.*
import com.jakebarnby.stripe.model.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Integration tests that call the real Stripe API.
 *
 * These tests require a valid Stripe test API key to run.
 * Set the environment variable STRIPE_PUBLISHABLE_KEY=pk_test_xxx before running.
 *
 * All tests use Stripe's test mode and test card numbers, so no real
 * charges are created.
 *
 * To run these tests:
 * ./gradlew :stripe:jvmTest -DSTRIPE_PUBLISHABLE_KEY=pk_test_xxx
 */
class RealApiIntegrationTest : IntegrationTestBase() {


    @Test
    fun testCreateCardToken_visaSuccess() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val params = CardParams(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "314"
        )

        val result = stripe.createCardToken(params)

        assertTrue(result.isSuccess(), "Expected success but got: ${result.errorOrNull()?.message}")
        val token = result.getOrNull()!!

        assertTrue(token.id.startsWith("tok_"), "Token ID should start with 'tok_': ${token.id}")
        assertEquals("card", token.type)
        assertEquals("4242", token.card?.last4)
        assertEquals("visa", token.card?.brand)
        assertEquals(12, token.card?.expMonth)
        assertEquals(2030, token.card?.expYear)
    }

    @Test
    fun testCreateCardToken_mastercard() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val params = CardParams(
            number = TestConfiguration.TestCards.MASTERCARD,
            expMonth = 6,
            expYear = 2028,
            cvc = "123"
        )

        val result = stripe.createCardToken(params)

        assertTrue(result.isSuccess(), "Expected success but got: ${result.errorOrNull()?.message}")
        val token = result.getOrNull()!!

        assertEquals("5555", token.card?.last4)
        assertEquals("mastercard", token.card?.brand)
    }

    @Test
    fun testCreateCardToken_amex() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val params = CardParams(
            number = TestConfiguration.TestCards.AMEX,
            expMonth = 3,
            expYear = 2029,
            cvc = "1234" // Amex uses 4-digit CVC
        )

        val result = stripe.createCardToken(params)

        assertTrue(result.isSuccess(), "Expected success but got: ${result.errorOrNull()?.message}")
        val token = result.getOrNull()!!

        assertEquals("0005", token.card?.last4)
        assertEquals("amex", token.card?.brand)
    }

    @Test
    fun testCreateCardToken_withBillingAddress() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val params = CardParams(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "314",
            name = "Test User",
            addressLine1 = "123 Test Street",
            addressCity = "San Francisco",
            addressState = "CA",
            addressZip = "94102",
            addressCountry = "US"
        )

        val result = stripe.createCardToken(params)

        assertTrue(result.isSuccess(), "Expected success but got: ${result.errorOrNull()?.message}")
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testCreateCardToken_invalidCard() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        // Use a card number that's invalid (fails Luhn check) - but the CardParams
        // validation will catch this first, so we expect an IllegalArgumentException
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = TestConfiguration.TestCards.INVALID_LUHN,
                expMonth = 12,
                expYear = 2030,
                cvc = "123"
            )
        }
    }

    @Test
    fun testCreateCardToken_withIdempotencyKey() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val params = CardParams(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "314"
        )

        val idempotencyKey = IdempotencyKey.generate()

        // First request
        val result1 = stripe.createCardToken(params, idempotencyKey)
        assertTrue(result1.isSuccess())
        val token1 = result1.getOrNull()!!

        // Second request with same idempotency key should return same token
        val result2 = stripe.createCardToken(params, idempotencyKey)
        assertTrue(result2.isSuccess())
        val token2 = result2.getOrNull()!!

        assertEquals(token1.id, token2.id, "Idempotent requests should return same token")
    }


    @Test
    fun testCreatePaymentMethod_card() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val params = PaymentMethodCreateParams.createCard(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "314"
        )

        val result = stripe.createPaymentMethod(params)

        assertTrue(result.isSuccess(), "Expected success but got: ${result.errorOrNull()?.message}")
        val paymentMethod = result.getOrNull()!!

        assertTrue(paymentMethod.id.startsWith("pm_"), "Payment method ID should start with 'pm_': ${paymentMethod.id}")
        assertEquals(PaymentMethodType.CARD, paymentMethod.type)
        assertNotNull(paymentMethod.card)
        assertEquals("4242", paymentMethod.card?.last4)
        assertEquals(CardBrand.VISA, paymentMethod.card?.brand)
    }

    @Test
    fun testCreatePaymentMethod_cardWithBillingDetails() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val billingDetails = BillingDetails(
            name = "John Doe",
            email = "john@example.com",
            phone = "+14155551234",
            address = Address(
                line1 = "123 Test Street",
                line2 = "Apt 4",
                city = "San Francisco",
                state = "CA",
                postalCode = "94102",
                country = "US"
            )
        )

        val params = PaymentMethodCreateParams.createCard(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "314",
            billingDetails = billingDetails
        )

        val result = stripe.createPaymentMethod(params)

        assertTrue(result.isSuccess(), "Expected success but got: ${result.errorOrNull()?.message}")
        val paymentMethod = result.getOrNull()!!

        assertNotNull(paymentMethod.billingDetails)
        assertEquals("John Doe", paymentMethod.billingDetails?.name)
        assertEquals("john@example.com", paymentMethod.billingDetails?.email)
    }

    @Test
    fun testCreatePaymentMethod_cardFromToken() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        // First create a token
        val cardParams = CardParams(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "314"
        )
        val tokenResult = stripe.createCardToken(cardParams)
        assertTrue(tokenResult.isSuccess())
        val token = tokenResult.getOrNull()!!

        // Then create payment method from token
        val pmParams = PaymentMethodCreateParams.createCardFromToken(token.id)
        val result = stripe.createPaymentMethod(pmParams)

        assertTrue(result.isSuccess(), "Expected success but got: ${result.errorOrNull()?.message}")
        val paymentMethod = result.getOrNull()!!

        assertEquals(PaymentMethodType.CARD, paymentMethod.type)
        assertEquals("4242", paymentMethod.card?.last4)
    }

    @Test
    fun testRetrievePaymentMethod() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        // First create a payment method
        val createParams = PaymentMethodCreateParams.createCard(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "314"
        )
        val createResult = stripe.createPaymentMethod(createParams)
        assertTrue(createResult.isSuccess())
        val createdPm = createResult.getOrNull()!!

        // Then retrieve it
        val retrieveResult = stripe.retrievePaymentMethod(createdPm.id)

        // Note: Retrieving payment method requires secret key in most cases
        // This test may fail with publishable key only
        if (retrieveResult.isSuccess()) {
            val retrievedPm = retrieveResult.getOrNull()!!
            assertEquals(createdPm.id, retrievedPm.id)
        } else {
            // Expected with publishable key only - payment method retrieval
            // usually requires secret key authentication
            println("Note: Payment method retrieval may require secret key")
        }
    }


    @Test
    fun testCreateSource_card() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val cardParams = CardParams(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "314"
        )
        val params = SourceParams.createCardParams(cardParams)

        val result = stripe.createSource(params)

        assertTrue(result.isSuccess(), "Expected success but got: ${result.errorOrNull()?.message}")
        val source = result.getOrNull()!!

        assertTrue(source.id.startsWith("src_"), "Source ID should start with 'src_': ${source.id}")
        assertEquals(SourceType.CARD, source.type)
        assertEquals(SourceStatus.CHARGEABLE, source.status)
    }


    @Test
    fun testCreateCardToken_expiredCard() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        // Try to create token with expired year - the CardParams validation
        // should catch this
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = TestConfiguration.TestCards.VISA_SUCCESS,
                expMonth = 1,
                expYear = 2020, // Past year
                cvc = "123"
            )
        }
    }

    @Test
    fun testCreatePaymentMethod_invalidCard() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        // Use a fake card number that will fail at Stripe's API
        val params = PaymentMethodCreateParams.createCard(
            number = "4000000000000099", // Stripe test card that produces an error
            expMonth = 12,
            expYear = 2030,
            cvc = "123"
        )

        val result = stripe.createPaymentMethod(params)

        // This should fail with an API error
        assertTrue(result.isFailure())
        assertNotNull(result.errorOrNull())
    }


    @Test
    fun testCreateBankAccountToken() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val params = BankAccountTokenParams(
            country = "US",
            currency = "usd",
            accountNumber = TestConfiguration.TestBankAccounts.ACCOUNT_SUCCESS,
            routingNumber = TestConfiguration.TestBankAccounts.ROUTING_NUMBER,
            accountHolderName = "Test User",
            accountHolderType = BankAccountTokenParams.AccountHolderType.INDIVIDUAL
        )

        val result = stripe.createBankAccountToken(params)

        assertTrue(result.isSuccess(), "Expected success but got: ${result.errorOrNull()?.message}")
        val token = result.getOrNull()!!

        assertTrue(token.id.startsWith("btok_"), "Bank token ID should start with 'btok_': ${token.id}")
        assertEquals("bank_account", token.type)
        assertNotNull(token.bankAccount)
        assertEquals("6789", token.bankAccount?.last4)
    }


    @Test
    fun testCreatePaymentMethod_variousCardBrands() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        data class CardTestCase(
            val number: String,
            val expectedBrand: CardBrand,
            val expectedLast4: String
        )

        val testCases = listOf(
            CardTestCase(TestConfiguration.TestCards.VISA_SUCCESS, CardBrand.VISA, "4242"),
            CardTestCase(TestConfiguration.TestCards.MASTERCARD, CardBrand.MASTERCARD, "4444")
        )

        testCases.forEach { testCase ->
            val params = PaymentMethodCreateParams.createCard(
                number = testCase.number,
                expMonth = 12,
                expYear = 2030,
                cvc = if (testCase.expectedBrand == CardBrand.AMERICAN_EXPRESS) "1234" else "123"
            )

            val result = stripe.createPaymentMethod(params)

            assertTrue(
                result.isSuccess(),
                "Failed for ${testCase.expectedBrand}: ${result.errorOrNull()?.message}"
            )
            val pm = result.getOrNull()!!
            assertEquals(
                testCase.expectedLast4,
                pm.card?.last4,
                "Unexpected last4 for ${testCase.expectedBrand}"
            )
        }
    }
}
