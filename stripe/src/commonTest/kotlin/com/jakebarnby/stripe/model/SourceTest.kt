package com.jakebarnby.stripe.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class SourceTest {
    @Test
    fun testSourceCreation() {
        val source = Source(
            id = "src_123",
            type = SourceType.CARD,
            status = SourceStatus.CHARGEABLE,
            amount = 1000,
            currency = "eur",
            clientSecret = "src_123_secret_abc",
            flow = SourceFlow.REDIRECT,
            created = 1234567890,
            livemode = false
        )

        assertEquals("src_123", source.id)
        assertEquals(SourceType.CARD, source.type)
        assertEquals(SourceStatus.CHARGEABLE, source.status)
        assertEquals(1000, source.amount)
    }

    @Test
    fun testSourceStatusEnum() {
        assertEquals(SourceStatus.PENDING, SourceStatus.fromValue("pending"))
        assertEquals(SourceStatus.CHARGEABLE, SourceStatus.fromValue("chargeable"))
        assertEquals(SourceStatus.CONSUMED, SourceStatus.fromValue("consumed"))
    }

    @Test
    fun testSourceTypeEnum() {
        assertEquals(SourceType.CARD, SourceType.fromValue("card"))
        assertEquals(SourceType.IDEAL, SourceType.fromValue("ideal"))
        assertEquals(SourceType.BANCONTACT, SourceType.fromValue("bancontact"))
        assertEquals(SourceType.UNKNOWN, SourceType.fromValue("invalid"))
    }
}

class SourceParamsTest {
    @Test
    fun testCreateCardParams() {
        val cardParams = CardParams(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123"
        )

        val sourceParams = SourceParams.createCardParams(cardParams)
        assertEquals(SourceType.CARD, sourceParams.type)
        assertNotNull(sourceParams.extraParams)
    }

    @Test
    fun testCreateBancontactParams() {
        val sourceParams = SourceParams.createBancontactParams(
            amount = 1000,
            name = "John Doe",
            returnUrl = "https://example.com/return",
            statementDescriptor = "ORDER123"
        )

        assertEquals(SourceType.BANCONTACT, sourceParams.type)
        assertEquals(1000, sourceParams.amount)
        assertEquals("eur", sourceParams.currency)
        assertEquals("John Doe", sourceParams.owner?.name)
    }

    @Test
    fun testCreateIdealParams() {
        val sourceParams = SourceParams.createIdealParams(
            amount = 1000,
            name = "John Doe",
            returnUrl = "https://example.com/return",
            bank = "ing"
        )

        assertEquals(SourceType.IDEAL, sourceParams.type)
        assertEquals(1000, sourceParams.amount)
        assertEquals("eur", sourceParams.currency)
    }

    @Test
    fun testCreateSepaDebitParams() {
        val sourceParams = SourceParams.createSepaDebitParams(
            name = "John Doe",
            iban = "DE89370400440532013000",
            addressLine1 = "123 Main St",
            city = "Berlin",
            postalCode = "10115",
            country = "DE"
        )

        assertEquals(SourceType.SEPA_DEBIT, sourceParams.type)
        assertEquals("eur", sourceParams.currency)
        assertEquals("John Doe", sourceParams.owner?.name)
    }
}

class SourceRedirectTest {
    @Test
    fun testSourceRedirectCreation() {
        val redirect = SourceRedirect(
            returnUrl = "https://example.com/return",
            status = "pending",
            url = "https://stripe.com/redirect"
        )

        assertEquals("https://example.com/return", redirect.returnUrl)
        assertEquals("pending", redirect.status)
        assertEquals("https://stripe.com/redirect", redirect.url)
    }

    @Test
    fun testSourceRedirectValidation() {
        assertFailsWith<IllegalArgumentException> {
            SourceRedirect(returnUrl = "")
        }
    }
}
