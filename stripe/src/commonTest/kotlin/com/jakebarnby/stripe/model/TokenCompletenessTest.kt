package com.jakebarnby.stripe.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertFailsWith

/**
 * Comprehensive tests for Token creation parameters.
 * Ensures all token types and their parameters are properly tested.
 */
class TokenCompletenessTest {

    @Test
    fun testCardParamsAllFields() {
        val params = CardParams(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123",
            name = "John Doe",
            addressLine1 = "123 Main St",
            addressLine2 = "Apt 4",
            addressCity = "San Francisco",
            addressState = "CA",
            addressZip = "94102",
            addressCountry = "US",
            currency = "usd"
        )

        // Verify all fields are accessible
        assertNotNull(params.number)
        assertEquals("4242424242424242", params.number)
        assertNotNull(params.expMonth)
        assertEquals(12, params.expMonth)
        assertNotNull(params.expYear)
        assertEquals(2025, params.expYear)
        assertEquals("123", params.cvc)
        assertEquals("John Doe", params.name)
        assertEquals("123 Main St", params.addressLine1)
        assertEquals("Apt 4", params.addressLine2)
        assertEquals("San Francisco", params.addressCity)
        assertEquals("CA", params.addressState)
        assertEquals("94102", params.addressZip)
        assertEquals("US", params.addressCountry)
        assertEquals("usd", params.currency)
    }

    @Test
    fun testCardParamsMinimalFields() {
        val params = CardParams(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025
        )

        assertNotNull(params.number)
        assertEquals("4242424242424242", params.number)
        assertEquals(12, params.expMonth)
        assertEquals(2025, params.expYear)
        assertEquals(null, params.cvc)
        assertEquals(null, params.name)
    }

    @Test
    fun testCardParamsBuilder() {
        val params = CardParams.builder()
            .number("4242424242424242")
            .expMonth(12)
            .expYear(2025)
            .cvc("123")
            .name("John Doe")
            .addressLine1("123 Main St")
            .addressCity("San Francisco")
            .addressState("CA")
            .addressZip("94102")
            .addressCountry("US")
            .build()

        assertEquals("4242424242424242", params.number)
        assertEquals(12, params.expMonth)
        assertEquals(2025, params.expYear)
        assertEquals("123", params.cvc)
        assertEquals("John Doe", params.name)
    }

    @Test
    fun testCardParamsLuhnValidation() {
        // Valid card numbers
        val validCards = listOf(
            "4242424242424242", // Visa
            "5555555555554444", // Mastercard
            "378282246310005",  // Amex
            "6011111111111117", // Discover
            "3056930009020004", // Diners
            "3566002020360505"  // JCB
        )

        validCards.forEach { number ->
            val params = CardParams(
                number = number,
                expMonth = 12,
                expYear = 2025
            )
            assertNotNull(params, "Valid card $number should create params")
        }

        // Invalid Luhn check
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "4242424242424243", // Invalid Luhn
                expMonth = 12,
                expYear = 2025
            )
        }
    }

    @Test
    fun testCardParamsNumberFormatting() {
        // Should accept cards with spaces
        val withSpaces = CardParams(
            number = "4242 4242 4242 4242",
            expMonth = 12,
            expYear = 2025
        )
        assertEquals("4242424242424242", withSpaces.getSanitizedNumber())

        // Should accept cards with dashes
        val withDashes = CardParams(
            number = "4242-4242-4242-4242",
            expMonth = 12,
            expYear = 2025
        )
        assertEquals("4242424242424242", withDashes.getSanitizedNumber())
    }

    @Test
    fun testCardParamsValidation() {
        // Blank number
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "",
                expMonth = 12,
                expYear = 2025
            )
        }

        // Invalid expMonth (too low)
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "4242424242424242",
                expMonth = 0,
                expYear = 2025
            )
        }

        // Invalid expMonth (too high)
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "4242424242424242",
                expMonth = 13,
                expYear = 2025
            )
        }

        // Invalid expYear
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "4242424242424242",
                expMonth = 12,
                expYear = 0
            )
        }

        // Invalid CVC length
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "4242424242424242",
                expMonth = 12,
                expYear = 2025,
                cvc = "12" // Too short
            )
        }

        // Invalid country code
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "4242424242424242",
                expMonth = 12,
                expYear = 2025,
                addressCountry = "USA" // Should be US
            )
        }

        // Invalid currency code
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "4242424242424242",
                expMonth = 12,
                expYear = 2025,
                currency = "US" // Should be USD
            )
        }

        // Card number too short
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "424242424242", // Only 12 digits
                expMonth = 12,
                expYear = 2025
            )
        }

        // Card number too long
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "42424242424242424242", // 20 digits
                expMonth = 12,
                expYear = 2025
            )
        }
    }

    @Test
    fun testBankAccountTokenParamsAllFields() {
        val params = BankAccountTokenParams(
            country = "US",
            currency = "usd",
            accountNumber = "000123456789",
            routingNumber = "110000000",
            accountHolderName = "Jane Doe",
            accountHolderType = BankAccountTokenParams.AccountHolderType.INDIVIDUAL
        )

        // Verify all fields
        assertNotNull(params.country)
        assertEquals("US", params.country)
        assertNotNull(params.currency)
        assertEquals("usd", params.currency)
        assertNotNull(params.accountNumber)
        assertEquals("000123456789", params.accountNumber)
        assertEquals("110000000", params.routingNumber)
        assertEquals("Jane Doe", params.accountHolderName)
        assertEquals(BankAccountTokenParams.AccountHolderType.INDIVIDUAL, params.accountHolderType)
    }

    @Test
    fun testBankAccountTokenParamsMinimalFields() {
        val params = BankAccountTokenParams(
            country = "US",
            currency = "usd",
            accountNumber = "000123456789"
        )

        assertEquals("US", params.country)
        assertEquals("usd", params.currency)
        assertEquals("000123456789", params.accountNumber)
        assertEquals(null, params.routingNumber)
        assertEquals(null, params.accountHolderName)
        assertEquals(null, params.accountHolderType)
    }

    @Test
    fun testBankAccountTokenParamsBuilder() {
        val params = BankAccountTokenParams.builder()
            .country("US")
            .currency("usd")
            .accountNumber("000123456789")
            .routingNumber("110000000")
            .accountHolderName("Jane Doe")
            .accountHolderType(BankAccountTokenParams.AccountHolderType.COMPANY)
            .build()

        assertEquals("US", params.country)
        assertEquals("usd", params.currency)
        assertEquals("000123456789", params.accountNumber)
        assertEquals("110000000", params.routingNumber)
        assertEquals("Jane Doe", params.accountHolderName)
        assertEquals(BankAccountTokenParams.AccountHolderType.COMPANY, params.accountHolderType)
    }

    @Test
    fun testBankAccountTokenParamsValidation() {
        // Invalid country code (not 2 letters)
        assertFailsWith<IllegalArgumentException> {
            BankAccountTokenParams(
                country = "USA",
                currency = "usd",
                accountNumber = "000123456789"
            )
        }

        // Invalid currency code (not 3 letters)
        assertFailsWith<IllegalArgumentException> {
            BankAccountTokenParams(
                country = "US",
                currency = "us",
                accountNumber = "000123456789"
            )
        }

        // Blank account number
        assertFailsWith<IllegalArgumentException> {
            BankAccountTokenParams(
                country = "US",
                currency = "usd",
                accountNumber = ""
            )
        }
    }

    @Test
    fun testPiiTokenParamsAllFields() {
        val params = PiiTokenParams(
            personalIdNumber = "000-00-0000"
        )

        assertNotNull(params.personalIdNumber)
        assertEquals("000-00-0000", params.personalIdNumber)
    }

    @Test
    fun testPiiTokenParamsValidation() {
        // Blank personal ID number
        assertFailsWith<IllegalArgumentException> {
            PiiTokenParams(personalIdNumber = "")
        }
    }

    @Test
    fun testPiiTokenParamsVariousFormats() {
        val validFormats = listOf(
            "000-00-0000",
            "000000000",
            "AB123456C",
            "123-45-6789"
        )

        validFormats.forEach { format ->
            val params = PiiTokenParams(personalIdNumber = format)
            assertEquals(format, params.personalIdNumber)
        }
    }

    @Test
    fun testAccountParamsAllFields() {
        val params = AccountParams(
            businessType = AccountParams.BusinessType.INDIVIDUAL,
            tosShownAndAccepted = true
        )

        assertNotNull(params.businessType)
        assertEquals(AccountParams.BusinessType.INDIVIDUAL, params.businessType)
        assertEquals(true, params.tosShownAndAccepted)
    }

    @Test
    fun testAccountParamsDefaults() {
        val params = AccountParams(
            businessType = AccountParams.BusinessType.COMPANY
        )

        assertEquals(AccountParams.BusinessType.COMPANY, params.businessType)
        assertEquals(false, params.tosShownAndAccepted) // Default value
    }

    @Test
    fun testAccountParamsBothBusinessTypes() {
        val individual = AccountParams(
            businessType = AccountParams.BusinessType.INDIVIDUAL,
            tosShownAndAccepted = true
        )

        val company = AccountParams(
            businessType = AccountParams.BusinessType.COMPANY,
            tosShownAndAccepted = false
        )

        assertEquals(AccountParams.BusinessType.INDIVIDUAL, individual.businessType)
        assertEquals(AccountParams.BusinessType.COMPANY, company.businessType)
        assertEquals(true, individual.tosShownAndAccepted)
        assertEquals(false, company.tosShownAndAccepted)
    }

    @Test
    fun testCardPaymentMethodCreateParamsWithCardDetails() {
        val params = CardPaymentMethodCreateParams(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123"
        )

        assertEquals("4242424242424242", params.number)
        assertEquals(12, params.expMonth)
        assertEquals(2025, params.expYear)
        assertEquals("123", params.cvc)
        assertEquals(null, params.token)
    }

    @Test
    fun testCardPaymentMethodCreateParamsWithToken() {
        val params = CardPaymentMethodCreateParams(
            token = "tok_visa"
        )

        assertEquals("tok_visa", params.token)
        assertEquals(null, params.number)
        assertEquals(null, params.expMonth)
        assertEquals(null, params.expYear)
        assertEquals(null, params.cvc)
    }

    @Test
    fun testCardPaymentMethodCreateParamsBuilder() {
        val withDetails = CardPaymentMethodCreateParams.builder()
            .number("4242424242424242")
            .expMonth(12)
            .expYear(2025)
            .cvc("123")
            .build()

        assertEquals("4242424242424242", withDetails.number)

        val withToken = CardPaymentMethodCreateParams.builder()
            .token("tok_visa")
            .build()

        assertEquals("tok_visa", withToken.token)
    }

    @Test
    fun testCardPaymentMethodCreateParamsValidation() {
        // Cannot provide both details and token
        assertFailsWith<IllegalArgumentException> {
            CardPaymentMethodCreateParams(
                number = "4242424242424242",
                expMonth = 12,
                expYear = 2025,
                token = "tok_visa"
            )
        }

        // Must provide either details or token
        assertFailsWith<IllegalArgumentException> {
            CardPaymentMethodCreateParams()
        }

        // Invalid card number (blank)
        assertFailsWith<IllegalArgumentException> {
            CardPaymentMethodCreateParams(
                number = "",
                expMonth = 12,
                expYear = 2025
            )
        }

        // Invalid expMonth
        assertFailsWith<IllegalArgumentException> {
            CardPaymentMethodCreateParams(
                number = "4242424242424242",
                expMonth = 13,
                expYear = 2025
            )
        }

        // Invalid expYear
        assertFailsWith<IllegalArgumentException> {
            CardPaymentMethodCreateParams(
                number = "4242424242424242",
                expMonth = 12,
                expYear = -1
            )
        }

        // Invalid CVC
        assertFailsWith<IllegalArgumentException> {
            CardPaymentMethodCreateParams(
                number = "4242424242424242",
                expMonth = 12,
                expYear = 2025,
                cvc = "12" // Too short
            )
        }

        assertFailsWith<IllegalArgumentException> {
            CardPaymentMethodCreateParams(
                number = "4242424242424242",
                expMonth = 12,
                expYear = 2025,
                cvc = "12345" // Too long
            )
        }

        // Blank token
        assertFailsWith<IllegalArgumentException> {
            CardPaymentMethodCreateParams(token = "")
        }
    }

    @Test
    fun testTokenTypeCreation() {
        // Verify Token can be created with different types
        val cardToken = Token(
            id = "tok_card",
            type = "card",
            created = 1234567890,
            livemode = false,
            used = false,
            card = CardToken(
                id = "card_123",
                brand = "visa",
                last4 = "4242",
                expMonth = 12,
                expYear = 2025
            )
        )

        val bankToken = Token(
            id = "tok_bank",
            type = "bank_account",
            created = 1234567890,
            livemode = false,
            used = false,
            bankAccount = BankAccountToken(
                id = "ba_123",
                country = "US",
                currency = "usd",
                last4 = "6789"
            )
        )

        val piiToken = Token(
            id = "tok_pii",
            type = "pii",
            created = 1234567890,
            livemode = false,
            used = false
        )

        val accountToken = Token(
            id = "tok_account",
            type = "account",
            created = 1234567890,
            livemode = false,
            used = false
        )

        assertEquals("card", cardToken.type)
        assertNotNull(cardToken.card)
        assertEquals("bank_account", bankToken.type)
        assertNotNull(bankToken.bankAccount)
        assertEquals("pii", piiToken.type)
        assertEquals("account", accountToken.type)
    }

    @Test
    fun testAllTokenParameterTypes() {
        // Verify all 4 token types can be created via their parameter classes

        // 1. Card token
        val cardParams = CardParams(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123"
        )
        assertNotNull(cardParams)

        // 2. Bank account token
        val bankParams = BankAccountTokenParams(
            country = "US",
            currency = "usd",
            accountNumber = "000123456789"
        )
        assertNotNull(bankParams)

        // 3. PII token
        val piiParams = PiiTokenParams(
            personalIdNumber = "000-00-0000"
        )
        assertNotNull(piiParams)

        // 4. Account token
        val accountParams = AccountParams(
            businessType = AccountParams.BusinessType.INDIVIDUAL,
            tosShownAndAccepted = true
        )
        assertNotNull(accountParams)
    }
}
