package com.jakebarnby.stripe.integration

import com.jakebarnby.stripe.StripeResult
import com.jakebarnby.stripe.model.CardParams
import com.jakebarnby.stripe.model.BankAccountTokenParams
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * Integration tests for token creation APIs.
 * These tests interact with the real Stripe API in test mode.
 */
public class TokenIntegrationTest : IntegrationTestBase() {

    @Test
    fun testCreateCardToken_withValidCard_succeeds() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val params = CardParams(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "123"
        )

        val result = stripe.createCardToken(params)

        assertTrue(result is StripeResult.Success, "Expected successful token creation")
        val token = result.value
        assertNotNull(token.id, "Token ID should not be null")
        assertTrue(token.id.startsWith("tok_"), "Token ID should start with 'tok_'")
        assertNotNull(token.card, "Card details should be present")
        assertTrue(token.card!!.last4 == "4242", "Last 4 digits should match test card")
    }

    @Test
    fun testCreateCardToken_withInvalidLuhn_fails() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        // This should fail validation before API call
        try {
            CardParams(
                number = TestConfiguration.TestCards.INVALID_LUHN,
                expMonth = 12,
                expYear = 2030,
                cvc = "123"
            )
            assertTrue(false, "Should have thrown validation exception")
        } catch (e: IllegalArgumentException) {
            assertTrue(
                e.message?.contains("Luhn") == true,
                "Exception should mention Luhn validation"
            )
        }
    }

    @Test
    fun testCreateCardToken_withMastercard_succeeds() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val params = CardParams(
            number = TestConfiguration.TestCards.MASTERCARD,
            expMonth = 12,
            expYear = 2030,
            cvc = "123"
        )

        val result = stripe.createCardToken(params)

        assertTrue(result is StripeResult.Success, "Expected successful token creation")
        val token = result.value
        assertTrue(token.id.startsWith("tok_"), "Token ID should start with 'tok_'")
        assertTrue(token.card!!.last4 == "4444", "Last 4 digits should match Mastercard")
    }

    @Test
    fun testCreateCardToken_withAmex_succeeds() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val params = CardParams(
            number = TestConfiguration.TestCards.AMEX,
            expMonth = 12,
            expYear = 2030,
            cvc = "1234" // Amex uses 4-digit CVC
        )

        val result = stripe.createCardToken(params)

        assertTrue(result is StripeResult.Success, "Expected successful token creation")
        val token = result.value
        assertTrue(token.id.startsWith("tok_"), "Token ID should start with 'tok_'")
        assertTrue(token.card!!.last4 == "0005", "Last 4 digits should match Amex")
    }

    @Test
    fun testCreateBankAccountToken_withValidAccount_succeeds() = runTest {
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

        assertTrue(result is StripeResult.Success, "Expected successful bank account token creation")
        val token = result.value
        assertNotNull(token.id, "Token ID should not be null")
        assertTrue(token.id.startsWith("btok_"), "Bank account token ID should start with 'btok_'")
        assertNotNull(token.bankAccount, "Bank account details should be present")
        assertTrue(token.bankAccount!!.last4 == "6789", "Last 4 digits should match test account")
    }

    @Test
    fun testCreateCardToken_withCardholderName_succeeds() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val params = CardParams(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "123",
            name = "John Doe"
        )

        val result = stripe.createCardToken(params)

        assertTrue(result is StripeResult.Success, "Expected successful token creation")
        val token = result.value
        assertTrue(token.id.startsWith("tok_"), "Token ID should start with 'tok_'")
        // Note: Card name might not be returned in all token responses
    }

    @Test
    fun testCreateCardToken_withAddress_succeeds() = runTest {
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val params = CardParams(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "123",
            addressLine1 = "123 Main St",
            addressCity = "San Francisco",
            addressState = "CA",
            addressZip = "94111",
            addressCountry = "US"
        )

        val result = stripe.createCardToken(params)

        assertTrue(result is StripeResult.Success, "Expected successful token creation")
        val token = result.value
        assertTrue(token.id.startsWith("tok_"), "Token ID should start with 'tok_'")
    }
}
