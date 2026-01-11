package com.jakebarnby.stripe.integration

import com.jakebarnby.stripe.model.*
import com.jakebarnby.stripe.StripeResult
import com.jakebarnby.stripe.IdempotencyKey
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertFailsWith

/**
 * Tests that verify model behavior and validation without API calls.
 * These tests always run regardless of API key availability.
 *
 * These tests validate:
 * - Model validation logic
 * - Data sanitization
 * - Builder patterns
 * - Enum conversions
 * - Result type operations
 */
public class MockStripeServerTest {

    // ============================================================================
    // CardParams Validation Tests
    // ============================================================================

    @Test
    fun testCardParams_validation_withValidCard() {
        val valid = CardParams(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2030,
            cvc = "123"
        )
        assertEquals("4242424242424242", valid.getSanitizedNumber())
        assertEquals(12, valid.expMonth)
        assertEquals(2030, valid.expYear)
    }

    @Test
    fun testCardParams_sanitization_removesSpaces() {
        val withSpaces = CardParams(
            number = "4242 4242 4242 4242",
            expMonth = 12,
            expYear = 2030,
            cvc = "123"
        )
        assertEquals("4242424242424242", withSpaces.getSanitizedNumber())
    }

    @Test
    fun testCardParams_sanitization_removesDashes() {
        val withDashes = CardParams(
            number = "4242-4242-4242-4242",
            expMonth = 12,
            expYear = 2030,
            cvc = "123"
        )
        assertEquals("4242424242424242", withDashes.getSanitizedNumber())
    }

    @Test
    fun testCardParams_validation_rejectsInvalidLuhn() {
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
    fun testCardParams_validation_rejectsInvalidExpMonth() {
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "4242424242424242",
                expMonth = 13,
                expYear = 2030,
                cvc = "123"
            )
        }
    }

    @Test
    fun testCardParams_validation_rejectsShortCvc() {
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "4242424242424242",
                expMonth = 12,
                expYear = 2030,
                cvc = "12"
            )
        }
    }

    @Test
    fun testCardParams_builder_createsValidParams() {
        val params = CardParams.builder()
            .number("4242424242424242")
            .expMonth(12)
            .expYear(2030)
            .cvc("123")
            .name("John Doe")
            .addressLine1("123 Main St")
            .addressCity("San Francisco")
            .addressState("CA")
            .addressZip("94111")
            .addressCountry("US")
            .build()

        assertEquals("4242424242424242", params.number)
        assertEquals("John Doe", params.name)
        assertEquals("123 Main St", params.addressLine1)
        assertEquals("US", params.addressCountry)
    }

    @Test
    fun testCardParams_luhnValidation_acceptsAllTestCards() {
        // Validate all test cards pass Luhn check
        val testCards = listOf(
            TestConfiguration.TestCards.VISA_SUCCESS,
            TestConfiguration.TestCards.MASTERCARD,
            TestConfiguration.TestCards.AMEX,
            TestConfiguration.TestCards.VISA_DECLINED,
            TestConfiguration.TestCards.VISA_REQUIRES_AUTH
        )

        testCards.forEach { cardNumber ->
            val params = CardParams(
                number = cardNumber,
                expMonth = 12,
                expYear = 2030,
                cvc = if (cardNumber.length == 15) "1234" else "123"
            )
            assertNotNull(params, "Card $cardNumber should pass validation")
        }
    }

    // ============================================================================
    // BankAccountTokenParams Validation Tests
    // ============================================================================

    @Test
    fun testBankAccountParams_validation_withValidData() {
        val params = BankAccountTokenParams(
            country = "US",
            currency = "usd",
            accountNumber = "000123456789",
            routingNumber = "110000000",
            accountHolderName = "Test User",
            accountHolderType = BankAccountTokenParams.AccountHolderType.INDIVIDUAL
        )

        assertEquals("US", params.country)
        assertEquals("usd", params.currency)
        assertEquals("000123456789", params.accountNumber)
    }

    @Test
    fun testBankAccountParams_validation_rejectsInvalidCountry() {
        assertFailsWith<IllegalArgumentException> {
            BankAccountTokenParams(
                country = "USA", // Should be 2-letter code
                currency = "usd",
                accountNumber = "000123456789"
            )
        }
    }

    @Test
    fun testBankAccountParams_validation_rejectsInvalidCurrency() {
        assertFailsWith<IllegalArgumentException> {
            BankAccountTokenParams(
                country = "US",
                currency = "dollar", // Should be 3-letter code
                accountNumber = "000123456789"
            )
        }
    }

    @Test
    fun testAccountHolderType_fromValue_convertsCorrectly() {
        assertEquals(
            BankAccountTokenParams.AccountHolderType.INDIVIDUAL,
            BankAccountTokenParams.AccountHolderType.fromValue("individual")
        )
        assertEquals(
            BankAccountTokenParams.AccountHolderType.COMPANY,
            BankAccountTokenParams.AccountHolderType.fromValue("company")
        )
        assertEquals(
            null,
            BankAccountTokenParams.AccountHolderType.fromValue("invalid")
        )
    }

    // ============================================================================
    // StripeResult Tests
    // ============================================================================

    @Test
    fun testStripeResult_success_creation() {
        val result: StripeResult<String> = StripeResult.success("test")
        assertTrue(result is StripeResult.Success)
        assertTrue(result.isSuccess())
        assertEquals("test", result.getOrNull())
    }

    @Test
    fun testStripeResult_failure_creation() {
        val error = StripeException("Test error")
        val result: StripeResult<String> = StripeResult.failure(error)
        assertTrue(result is StripeResult.Failure)
        assertTrue(result.isFailure())
        assertEquals(null, result.getOrNull())
        assertEquals(error, result.errorOrNull())
    }

    @Test
    fun testStripeResult_map_transformsSuccess() {
        val success: StripeResult<String> = StripeResult.success("test")
        val mapped = success.map { it.uppercase() }
        assertTrue(mapped is StripeResult.Success)
        assertEquals("TEST", mapped.value)
    }

    @Test
    fun testStripeResult_map_preservesFailure() {
        val error = StripeException("Test error")
        val failure: StripeResult<String> = StripeResult.failure(error)
        val mapped = failure.map { it.uppercase() }
        assertTrue(mapped is StripeResult.Failure)
        assertEquals(error, mapped.error)
    }

    @Test
    fun testStripeResult_flatMap_transformsSuccess() {
        val success: StripeResult<Int> = StripeResult.success(5)
        val mapped = success.flatMap { StripeResult.success(it * 2) }
        assertTrue(mapped is StripeResult.Success)
        assertEquals(10, mapped.value)
    }

    @Test
    fun testStripeResult_getOrThrow_returnsValueOnSuccess() {
        val success: StripeResult<String> = StripeResult.success("test")
        assertEquals("test", success.getOrThrow())
    }

    @Test
    fun testStripeResult_getOrThrow_throwsOnFailure() {
        val error = StripeException("Test error")
        val failure: StripeResult<String> = StripeResult.failure(error)
        assertFailsWith<StripeException> {
            failure.getOrThrow()
        }
    }

    @Test
    fun testStripeResult_onSuccess_executesOnSuccess() {
        var executed = false
        val success: StripeResult<String> = StripeResult.success("test")
        success.onSuccess { executed = true }
        assertTrue(executed)
    }

    @Test
    fun testStripeResult_onFailure_executesOnFailure() {
        var executed = false
        val failure: StripeResult<String> = StripeResult.failure(StripeException("Test error"))
        failure.onFailure { executed = true }
        assertTrue(executed)
    }

    // ============================================================================
    // IdempotencyKey Tests
    // ============================================================================

    @Test
    fun testIdempotencyKey_generate_createsUniqueKeys() {
        val key1 = IdempotencyKey.generate()
        val key2 = IdempotencyKey.generate()
        assertTrue(key1.value != key2.value, "Generated keys should be unique")
        assertEquals(32, key1.value.length, "Generated key should be 32 characters")
        assertEquals(32, key2.value.length, "Generated key should be 32 characters")
    }

    @Test
    fun testIdempotencyKey_fromValue_acceptsValidValue() {
        val key = IdempotencyKey.fromValue("my-custom-key-123")
        assertEquals("my-custom-key-123", key.value)
    }

    @Test
    fun testIdempotencyKey_fromValue_rejectsBlank() {
        assertFailsWith<IllegalArgumentException> {
            IdempotencyKey.fromValue("")
        }
    }

    @Test
    fun testIdempotencyKey_fromValue_rejectsTooLong() {
        assertFailsWith<IllegalArgumentException> {
            IdempotencyKey.fromValue("a".repeat(256))
        }
    }

    // ============================================================================
    // PaymentIntentStatus Tests
    // ============================================================================

    @Test
    fun testPaymentIntentStatus_fromValue_convertsCorrectly() {
        assertEquals(
            PaymentIntentStatus.SUCCEEDED,
            PaymentIntentStatus.fromValue("succeeded")
        )
        assertEquals(
            PaymentIntentStatus.REQUIRES_ACTION,
            PaymentIntentStatus.fromValue("requires_action")
        )
        assertEquals(
            PaymentIntentStatus.PROCESSING,
            PaymentIntentStatus.fromValue("processing")
        )
        assertEquals(
            PaymentIntentStatus.CANCELED,
            PaymentIntentStatus.fromValue("canceled")
        )
        assertEquals(
            null,
            PaymentIntentStatus.fromValue("invalid")
        )
    }

    // ============================================================================
    // PaymentMethodType Tests
    // ============================================================================

    @Test
    fun testPaymentMethodType_fromValue_convertsCorrectly() {
        assertEquals(
            PaymentMethodType.CARD,
            PaymentMethodType.fromValue("card")
        )
        assertEquals(
            PaymentMethodType.SEPA_DEBIT,
            PaymentMethodType.fromValue("sepa_debit")
        )
        assertEquals(
            PaymentMethodType.IDEAL,
            PaymentMethodType.fromValue("ideal")
        )
        assertEquals(
            PaymentMethodType.UNKNOWN,
            PaymentMethodType.fromValue("invalid")
        )
    }

    // ============================================================================
    // CardBrand Tests
    // ============================================================================

    @Test
    fun testCardBrand_fromValue_convertsCorrectly() {
        assertEquals(CardBrand.VISA, CardBrand.fromValue("visa"))
        assertEquals(CardBrand.MASTERCARD, CardBrand.fromValue("mastercard"))
        assertEquals(CardBrand.AMERICAN_EXPRESS, CardBrand.fromValue("amex"))
        assertEquals(CardBrand.DISCOVER, CardBrand.fromValue("discover"))
        assertEquals(CardBrand.JCB, CardBrand.fromValue("jcb"))
        assertEquals(CardBrand.DINERS_CLUB, CardBrand.fromValue("diners"))
        assertEquals(CardBrand.UNION_PAY, CardBrand.fromValue("unionpay"))
        assertEquals(CardBrand.UNKNOWN, CardBrand.fromValue("invalid"))
    }

    // ============================================================================
    // BillingDetails Validation Tests
    // ============================================================================

    @Test
    fun testBillingDetails_validation_withValidEmail() {
        val billingDetails = BillingDetails(
            name = "Test User",
            email = "test@example.com",
            phone = "+1234567890"
        )
        assertEquals("test@example.com", billingDetails.email)
    }

    @Test
    fun testBillingDetails_validation_rejectsInvalidEmail() {
        assertFailsWith<IllegalArgumentException> {
            BillingDetails(
                email = "not-an-email"
            )
        }
    }

    @Test
    fun testBillingDetails_builder_createsValidDetails() {
        val address = Address.builder()
            .line1("123 Main St")
            .city("San Francisco")
            .state("CA")
            .postalCode("94111")
            .country("US")
            .build()

        val billingDetails = BillingDetails.builder()
            .name("Jane Doe")
            .email("jane@example.com")
            .phone("+1234567890")
            .address(address)
            .build()

        assertEquals("Jane Doe", billingDetails.name)
        assertEquals("jane@example.com", billingDetails.email)
        assertEquals("US", billingDetails.address?.country)
    }

    // ============================================================================
    // Address Validation Tests
    // ============================================================================

    @Test
    fun testAddress_validation_withValidCountry() {
        val address = Address(
            line1 = "123 Main St",
            city = "San Francisco",
            state = "CA",
            postalCode = "94111",
            country = "US"
        )
        assertEquals("US", address.country)
    }

    @Test
    fun testAddress_validation_rejectsInvalidCountry() {
        assertFailsWith<IllegalArgumentException> {
            Address(
                line1 = "123 Main St",
                country = "USA" // Should be 2-letter code
            )
        }
    }
}
