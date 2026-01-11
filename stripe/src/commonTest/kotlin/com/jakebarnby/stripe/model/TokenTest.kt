package com.jakebarnby.stripe.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TokenTest {
    @Test
    fun testTokenCreation() {
        val token = Token(
            id = "tok_123",
            type = "card",
            created = 1234567890,
            livemode = false,
            used = false
        )

        assertEquals("tok_123", token.id)
        assertEquals("card", token.type)
        assertEquals(1234567890, token.created)
        assertEquals(false, token.livemode)
        assertEquals(false, token.used)
    }

    @Test
    fun testTokenValidation() {
        assertFailsWith<IllegalArgumentException> {
            Token(
                id = "",
                type = "card",
                created = 1234567890,
                livemode = false,
                used = false
            )
        }

        assertFailsWith<IllegalArgumentException> {
            Token(
                id = "tok_123",
                type = "card",
                created = -1,
                livemode = false,
                used = false
            )
        }
    }
}

class CardParamsTest {
    @Test
    fun testCardParamsCreation() {
        val params = CardParams(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123"
        )

        assertEquals("4242424242424242", params.number)
        assertEquals(12, params.expMonth)
        assertEquals(2025, params.expYear)
        assertEquals("123", params.cvc)
    }

    @Test
    fun testCardParamsValidation() {
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "",
                expMonth = 12,
                expYear = 2025
            )
        }

        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "4242424242424242",
                expMonth = 13,
                expYear = 2025
            )
        }

        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "4242424242424242",
                expMonth = 12,
                expYear = 2025,
                cvc = "12"
            )
        }
    }

    @Test
    fun testCardParamsBuilder() {
        val params = CardParams.builder()
            .number("4242424242424242")
            .expMonth(12)
            .expYear(2025)
            .cvc("123")
            .name("John Doe")
            .build()

        assertEquals("4242424242424242", params.number)
        assertEquals("John Doe", params.name)
    }
}

class BankAccountTokenParamsTest {
    @Test
    fun testBankAccountTokenParamsCreation() {
        val params = BankAccountTokenParams(
            country = "US",
            currency = "usd",
            accountNumber = "000123456789",
            routingNumber = "110000000",
            accountHolderName = "John Doe",
            accountHolderType = BankAccountTokenParams.AccountHolderType.INDIVIDUAL
        )

        assertEquals("US", params.country)
        assertEquals("usd", params.currency)
        assertEquals("000123456789", params.accountNumber)
        assertEquals("110000000", params.routingNumber)
        assertEquals("John Doe", params.accountHolderName)
        assertEquals(BankAccountTokenParams.AccountHolderType.INDIVIDUAL, params.accountHolderType)
    }

    @Test
    fun testBankAccountTokenParamsValidation() {
        assertFailsWith<IllegalArgumentException> {
            BankAccountTokenParams(
                country = "USA",
                currency = "usd",
                accountNumber = "000123456789"
            )
        }

        assertFailsWith<IllegalArgumentException> {
            BankAccountTokenParams(
                country = "US",
                currency = "dollar",
                accountNumber = "000123456789"
            )
        }
    }

    @Test
    fun testAccountHolderTypeEnum() {
        val individual = BankAccountTokenParams.AccountHolderType.fromValue("individual")
        assertEquals(BankAccountTokenParams.AccountHolderType.INDIVIDUAL, individual)

        val company = BankAccountTokenParams.AccountHolderType.fromValue("company")
        assertEquals(BankAccountTokenParams.AccountHolderType.COMPANY, company)
    }
}

class PiiTokenParamsTest {
    @Test
    fun testPiiTokenParamsCreation() {
        val params = PiiTokenParams(personalIdNumber = "000000000")
        assertEquals("000000000", params.personalIdNumber)
    }

    @Test
    fun testPiiTokenParamsValidation() {
        assertFailsWith<IllegalArgumentException> {
            PiiTokenParams(personalIdNumber = "")
        }
    }
}
