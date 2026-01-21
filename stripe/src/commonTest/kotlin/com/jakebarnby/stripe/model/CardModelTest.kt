package com.jakebarnby.stripe.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Comprehensive tests for Card-related models.
 */
class CardModelTest {


    @Test
    fun testCard_creation_withAllFields() {
        val card = Card(
            brand = CardBrand.VISA,
            last4 = "4242",
            expMonth = 12,
            expYear = 2025,
            funding = CardFunding.CREDIT,
            country = "US",
            fingerprint = "abc123fingerprint",
            checks = CardChecks(
                addressLine1Check = "pass",
                addressPostalCodeCheck = "pass",
                cvcCheck = "pass"
            ),
            wallet = CardWallet(type = "apple_pay"),
            threeDSecureUsage = ThreeDSecureUsage(supported = true),
            networks = CardNetworks(
                available = listOf("visa", "cartes_bancaires"),
                preferred = "visa"
            )
        )

        assertEquals(CardBrand.VISA, card.brand)
        assertEquals("4242", card.last4)
        assertEquals(12, card.expMonth)
        assertEquals(2025, card.expYear)
        assertEquals(CardFunding.CREDIT, card.funding)
        assertEquals("US", card.country)
        assertNotNull(card.checks)
        assertNotNull(card.wallet)
        assertTrue(card.threeDSecureUsage?.supported == true)
    }

    @Test
    fun testCard_creation_withMinimalFields() {
        val card = Card(
            brand = CardBrand.MASTERCARD,
            last4 = "5555",
            expMonth = 6,
            expYear = 2027,
            funding = CardFunding.DEBIT
        )

        assertEquals(CardBrand.MASTERCARD, card.brand)
        assertNull(card.checks)
        assertNull(card.wallet)
        assertNull(card.networks)
    }


    @Test
    fun testCardBrand_fromValue_allBrands() {
        assertEquals(CardBrand.VISA, CardBrand.fromValue("visa"))
        assertEquals(CardBrand.MASTERCARD, CardBrand.fromValue("mastercard"))
        assertEquals(CardBrand.AMERICAN_EXPRESS, CardBrand.fromValue("amex"))
        assertEquals(CardBrand.DISCOVER, CardBrand.fromValue("discover"))
        assertEquals(CardBrand.JCB, CardBrand.fromValue("jcb"))
        assertEquals(CardBrand.DINERS_CLUB, CardBrand.fromValue("diners"))
        assertEquals(CardBrand.UNION_PAY, CardBrand.fromValue("unionpay"))
        assertEquals(CardBrand.UNKNOWN, CardBrand.fromValue("unknown"))
        assertEquals(CardBrand.UNKNOWN, CardBrand.fromValue("invalid_brand"))
    }

    @Test
    fun testCardBrand_value_allBrands() {
        assertEquals("visa", CardBrand.VISA.value)
        assertEquals("mastercard", CardBrand.MASTERCARD.value)
        assertEquals("amex", CardBrand.AMERICAN_EXPRESS.value)
        assertEquals("discover", CardBrand.DISCOVER.value)
        assertEquals("jcb", CardBrand.JCB.value)
        assertEquals("diners", CardBrand.DINERS_CLUB.value)
        assertEquals("unionpay", CardBrand.UNION_PAY.value)
        assertEquals("unknown", CardBrand.UNKNOWN.value)
    }


    @Test
    fun testCardFunding_fromValue_allTypes() {
        assertEquals(CardFunding.CREDIT, CardFunding.fromValue("credit"))
        assertEquals(CardFunding.DEBIT, CardFunding.fromValue("debit"))
        assertEquals(CardFunding.PREPAID, CardFunding.fromValue("prepaid"))
        assertEquals(CardFunding.UNKNOWN, CardFunding.fromValue("unknown"))
        assertEquals(CardFunding.UNKNOWN, CardFunding.fromValue("invalid"))
    }

    @Test
    fun testCardFunding_value_allTypes() {
        assertEquals("credit", CardFunding.CREDIT.value)
        assertEquals("debit", CardFunding.DEBIT.value)
        assertEquals("prepaid", CardFunding.PREPAID.value)
        assertEquals("unknown", CardFunding.UNKNOWN.value)
    }


    @Test
    fun testCardChecks_creation_withAllPassed() {
        val checks = CardChecks(
            addressLine1Check = "pass",
            addressPostalCodeCheck = "pass",
            cvcCheck = "pass"
        )

        assertEquals("pass", checks.addressLine1Check)
        assertEquals("pass", checks.addressPostalCodeCheck)
        assertEquals("pass", checks.cvcCheck)
    }

    @Test
    fun testCardChecks_creation_withFailures() {
        val checks = CardChecks(
            addressLine1Check = "fail",
            addressPostalCodeCheck = "unchecked",
            cvcCheck = "unavailable"
        )

        assertEquals("fail", checks.addressLine1Check)
        assertEquals("unchecked", checks.addressPostalCodeCheck)
        assertEquals("unavailable", checks.cvcCheck)
    }

    @Test
    fun testCardChecks_creation_withNulls() {
        val checks = CardChecks(
            addressLine1Check = null,
            addressPostalCodeCheck = null,
            cvcCheck = null
        )

        assertNull(checks.addressLine1Check)
        assertNull(checks.addressPostalCodeCheck)
        assertNull(checks.cvcCheck)
    }


    @Test
    fun testCardWallet_applePay() {
        val wallet = CardWallet(type = "apple_pay")
        assertEquals("apple_pay", wallet.type)
    }

    @Test
    fun testCardWallet_googlePay() {
        val wallet = CardWallet(type = "google_pay")
        assertEquals("google_pay", wallet.type)
    }

    @Test
    fun testCardWallet_samsungPay() {
        val wallet = CardWallet(type = "samsung_pay")
        assertEquals("samsung_pay", wallet.type)
    }

    @Test
    fun testCardWallet_link() {
        val wallet = CardWallet(type = "link")
        assertEquals("link", wallet.type)
    }


    @Test
    fun testThreeDSecureUsage_supported() {
        val usage = ThreeDSecureUsage(supported = true)
        assertTrue(usage.supported)
    }

    @Test
    fun testThreeDSecureUsage_notSupported() {
        val usage = ThreeDSecureUsage(supported = false)
        assertFalse(usage.supported)
    }


    @Test
    fun testCardNetworks_singleNetwork() {
        val networks = CardNetworks(
            available = listOf("visa"),
            preferred = "visa"
        )

        assertEquals(1, networks.available.size)
        assertEquals("visa", networks.available[0])
        assertEquals("visa", networks.preferred)
    }

    @Test
    fun testCardNetworks_multipleNetworks() {
        val networks = CardNetworks(
            available = listOf("visa", "cartes_bancaires", "eftpos"),
            preferred = "cartes_bancaires"
        )

        assertEquals(3, networks.available.size)
        assertTrue(networks.available.contains("visa"))
        assertTrue(networks.available.contains("cartes_bancaires"))
        assertEquals("cartes_bancaires", networks.preferred)
    }

    @Test
    fun testCardNetworks_noPreferred() {
        val networks = CardNetworks(
            available = listOf("mastercard"),
            preferred = null
        )

        assertEquals(1, networks.available.size)
        assertNull(networks.preferred)
    }


    @Test
    fun testCardToken_creation_withAllFields() {
        val cardToken = CardToken(
            id = "card_test_123",
            brand = "visa",
            last4 = "4242",
            expMonth = 12,
            expYear = 2025,
            funding = "credit",
            country = "US"
        )

        assertEquals("card_test_123", cardToken.id)
        assertEquals("visa", cardToken.brand)
        assertEquals("4242", cardToken.last4)
        assertEquals(12, cardToken.expMonth)
        assertEquals(2025, cardToken.expYear)
        assertEquals("credit", cardToken.funding)
        assertEquals("US", cardToken.country)
    }

    @Test
    fun testCardToken_creation_withNullableFields() {
        val cardToken = CardToken(
            id = "card_test_456",
            brand = "mastercard",
            last4 = "5555",
            expMonth = 6,
            expYear = 2027,
            funding = null,
            country = null
        )

        assertNull(cardToken.funding)
        assertNull(cardToken.country)
    }


    @Test
    fun testCardPaymentMethodCreateParams_withRawCardData() {
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
        assertNull(params.token)
    }

    @Test
    fun testCardPaymentMethodCreateParams_withToken() {
        val params = CardPaymentMethodCreateParams(
            token = "tok_visa"
        )

        assertEquals("tok_visa", params.token)
        assertNull(params.number)
        assertNull(params.expMonth)
        assertNull(params.expYear)
        assertNull(params.cvc)
    }
}
