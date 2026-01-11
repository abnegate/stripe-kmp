package com.jakebarnby.stripe.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class PaymentMethodTest {
    @Test
    fun testPaymentMethodCreation() {
        val card = Card(
            brand = CardBrand.VISA,
            last4 = "4242",
            expMonth = 12,
            expYear = 2025,
            funding = CardFunding.CREDIT,
            country = "US"
        )

        val paymentMethod = PaymentMethod(
            id = "pm_123",
            type = PaymentMethodType.CARD,
            created = 1234567890,
            livemode = false,
            card = card
        )

        assertEquals("pm_123", paymentMethod.id)
        assertEquals(PaymentMethodType.CARD, paymentMethod.type)
        assertNotNull(paymentMethod.card)
        assertEquals("4242", paymentMethod.card?.last4)
    }

    @Test
    fun testPaymentMethodTypeEnum() {
        assertEquals(PaymentMethodType.CARD, PaymentMethodType.fromValue("card"))
        assertEquals(PaymentMethodType.IDEAL, PaymentMethodType.fromValue("ideal"))
        assertEquals(PaymentMethodType.PAYPAL, PaymentMethodType.fromValue("paypal"))
        assertEquals(PaymentMethodType.UNKNOWN, PaymentMethodType.fromValue("invalid"))
    }
}

class CardTest {
    @Test
    fun testCardCreation() {
        val card = Card(
            brand = CardBrand.VISA,
            last4 = "4242",
            expMonth = 12,
            expYear = 2025,
            funding = CardFunding.CREDIT,
            country = "US"
        )

        assertEquals(CardBrand.VISA, card.brand)
        assertEquals("4242", card.last4)
        assertEquals(12, card.expMonth)
        assertEquals(2025, card.expYear)
        assertEquals(CardFunding.CREDIT, card.funding)
        assertEquals("US", card.country)
    }

    @Test
    fun testCardValidation() {
        assertFailsWith<IllegalArgumentException> {
            Card(
                brand = CardBrand.VISA,
                last4 = "424",
                expMonth = 12,
                expYear = 2025,
                funding = CardFunding.CREDIT
            )
        }

        assertFailsWith<IllegalArgumentException> {
            Card(
                brand = CardBrand.VISA,
                last4 = "4242",
                expMonth = 13,
                expYear = 2025,
                funding = CardFunding.CREDIT
            )
        }

        assertFailsWith<IllegalArgumentException> {
            Card(
                brand = CardBrand.VISA,
                last4 = "4242",
                expMonth = 12,
                expYear = 25,
                funding = CardFunding.CREDIT
            )
        }
    }

    @Test
    fun testCardBrandEnum() {
        assertEquals(CardBrand.VISA, CardBrand.fromValue("visa"))
        assertEquals(CardBrand.MASTERCARD, CardBrand.fromValue("mastercard"))
        assertEquals(CardBrand.AMERICAN_EXPRESS, CardBrand.fromValue("amex"))
        assertEquals(CardBrand.UNKNOWN, CardBrand.fromValue("invalid"))
    }
}

class PaymentMethodCreateParamsTest {
    @Test
    fun testCreateCardPaymentMethod() {
        val params = PaymentMethodCreateParams.createCard(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123"
        )

        assertEquals(PaymentMethodType.CARD, params.type)
        assertNotNull(params.card)
        assertEquals("4242424242424242", params.card?.number)
    }

    @Test
    fun testCreateIdealPaymentMethod() {
        val billingDetails = BillingDetails(
            name = "John Doe",
            email = "john@example.com"
        )

        val params = PaymentMethodCreateParams.createIdeal(
            bank = "ing",
            billingDetails = billingDetails
        )

        assertEquals(PaymentMethodType.IDEAL, params.type)
        assertEquals(billingDetails, params.billingDetails)
    }

    @Test
    fun testPaymentMethodCreateParamsBuilder() {
        val cardParams = CardPaymentMethodCreateParams(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123"
        )

        val params = PaymentMethodCreateParams.builder()
            .type(PaymentMethodType.CARD)
            .card(cardParams)
            .build()

        assertEquals(PaymentMethodType.CARD, params.type)
        assertEquals(cardParams, params.card)
    }
}

class CardPaymentMethodCreateParamsTest {
    @Test
    fun testCardPaymentMethodCreateParamsWithDetails() {
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
    }

    @Test
    fun testCardPaymentMethodCreateParamsWithToken() {
        val params = CardPaymentMethodCreateParams(
            token = "tok_123"
        )

        assertEquals("tok_123", params.token)
    }

    @Test
    fun testCardPaymentMethodCreateParamsValidation() {
        // Cannot provide both card details and token
        assertFailsWith<IllegalArgumentException> {
            CardPaymentMethodCreateParams(
                number = "4242424242424242",
                expMonth = 12,
                expYear = 2025,
                token = "tok_123"
            )
        }

        // Must provide either card details or token
        assertFailsWith<IllegalArgumentException> {
            CardPaymentMethodCreateParams()
        }
    }
}
