package com.jakebarnby.stripe.integration

import com.jakebarnby.stripe.StripeResult
import com.jakebarnby.stripe.model.PaymentMethodCreateParams
import com.jakebarnby.stripe.model.CardPaymentMethodCreateParams
import com.jakebarnby.stripe.model.BillingDetails
import com.jakebarnby.stripe.model.Address
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

/**
 * Integration tests for PaymentMethod creation and retrieval APIs.
 * These tests interact with the real Stripe API in test mode.
 */
public class PaymentMethodIntegrationTest : IntegrationTestBase() {

    @Test
    fun testCreatePaymentMethod_withCard_succeeds() = runTest {
        skipOnJvm()
        if (isJvmPlatform()) return@runTest
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val cardParams = CardPaymentMethodCreateParams(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "123"
        )

        val params = PaymentMethodCreateParams(
            type = com.jakebarnby.stripe.model.PaymentMethodType.CARD,
            card = cardParams,
            billingDetails = BillingDetails(name = "Test User")
        )

        val result = stripe.createPaymentMethod(params)
        val pm = assertSuccess(result, "Expected successful payment method creation")
        assertNotNull(pm.id, "Payment method ID should not be null")
        assertTrue(pm.id.startsWith("pm_"), "Payment method ID should start with 'pm_'")
        assertEquals("4242", pm.card?.last4, "Last 4 digits should match test card")
        assertEquals(12, pm.card?.expMonth, "Expiration month should match")
        assertEquals(2030, pm.card?.expYear, "Expiration year should match")
    }

    @Test
    fun testCreatePaymentMethod_withCardAndFullBillingDetails_succeeds() = runTest {
        skipOnJvm()
        if (isJvmPlatform()) return@runTest
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val address = Address(
            line1 = "123 Main St",
            line2 = "Apt 4B",
            city = "San Francisco",
            state = "CA",
            postalCode = "94111",
            country = "US"
        )

        val billingDetails = BillingDetails(
            name = "Jane Smith",
            email = "jane@example.com",
            phone = "+1234567890",
            address = address
        )

        val cardParams = CardPaymentMethodCreateParams(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 6,
            expYear = 2028,
            cvc = "456"
        )

        val params = PaymentMethodCreateParams(
            type = com.jakebarnby.stripe.model.PaymentMethodType.CARD,
            card = cardParams,
            billingDetails = billingDetails
        )

        val result = stripe.createPaymentMethod(params)
        val pm = assertSuccess(result, "Expected successful payment method creation")
        assertTrue(pm.id.startsWith("pm_"), "Payment method ID should start with 'pm_'")
        assertEquals("Jane Smith", pm.billingDetails?.name, "Billing name should match")
        assertEquals("jane@example.com", pm.billingDetails?.email, "Billing email should match")
    }

    @Test
    fun testCreatePaymentMethod_withMastercard_succeeds() = runTest {
        skipOnJvm()
        if (isJvmPlatform()) return@runTest
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val cardParams = CardPaymentMethodCreateParams(
            number = TestConfiguration.TestCards.MASTERCARD,
            expMonth = 12,
            expYear = 2030,
            cvc = "123"
        )

        val params = PaymentMethodCreateParams(
            type = com.jakebarnby.stripe.model.PaymentMethodType.CARD,
            card = cardParams,
            billingDetails = BillingDetails(name = "Test User")
        )

        val result = stripe.createPaymentMethod(params)
        val pm = assertSuccess(result, "Expected successful payment method creation")
        assertTrue(pm.id.startsWith("pm_"), "Payment method ID should start with 'pm_'")
        assertEquals("4444", pm.card?.last4, "Last 4 digits should match Mastercard")
        assertEquals(
            com.jakebarnby.stripe.model.CardBrand.MASTERCARD,
            pm.card?.brand,
            "Brand should be Mastercard"
        )
    }

    @Test
    fun testCreatePaymentMethod_withAmex_succeeds() = runTest {
        skipOnJvm()
        if (isJvmPlatform()) return@runTest
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val cardParams = CardPaymentMethodCreateParams(
            number = TestConfiguration.TestCards.AMEX,
            expMonth = 3,
            expYear = 2029,
            cvc = "1234" // Amex uses 4-digit CVC
        )

        val params = PaymentMethodCreateParams(
            type = com.jakebarnby.stripe.model.PaymentMethodType.CARD,
            card = cardParams,
            billingDetails = BillingDetails(name = "Test User")
        )

        val result = stripe.createPaymentMethod(params)
        val pm = assertSuccess(result, "Expected successful payment method creation")
        assertTrue(pm.id.startsWith("pm_"), "Payment method ID should start with 'pm_'")
        assertEquals("0005", pm.card?.last4, "Last 4 digits should match Amex")
        assertEquals(
            com.jakebarnby.stripe.model.CardBrand.AMERICAN_EXPRESS,
            pm.card?.brand,
            "Brand should be American Express"
        )
    }

    @Test
    fun testCreatePaymentMethod_withMetadata_succeeds() = runTest {
        skipOnJvm()
        if (isJvmPlatform()) return@runTest
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val cardParams = CardPaymentMethodCreateParams(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "123"
        )

        val metadata = mapOf(
            "order_id" to "order_123",
            "customer_name" to "Test Customer"
        )

        val params = PaymentMethodCreateParams(
            type = com.jakebarnby.stripe.model.PaymentMethodType.CARD,
            card = cardParams,
            billingDetails = BillingDetails(name = "Test User"),
            metadata = metadata
        )

        val result = stripe.createPaymentMethod(params)
        val pm = assertSuccess(result, "Expected successful payment method creation")
        assertTrue(pm.id.startsWith("pm_"), "Payment method ID should start with 'pm_'")
        // Note: Metadata verification depends on API response structure
    }

    @Test
    fun testRetrievePaymentMethod_afterCreation_succeeds() = runTest {
        skipOnJvm()
        if (isJvmPlatform()) return@runTest
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        // First create a payment method
        val cardParams = CardPaymentMethodCreateParams(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "123"
        )

        val createParams = PaymentMethodCreateParams(
            type = com.jakebarnby.stripe.model.PaymentMethodType.CARD,
            card = cardParams,
            billingDetails = BillingDetails(name = "Test User")
        )

        val createResult = stripe.createPaymentMethod(createParams)
        val createdPm = assertSuccess(createResult, "Payment method creation should succeed")

        // Now retrieve it
        val retrieveResult = stripe.retrievePaymentMethod(createdPm.id)
        val retrievedPm = assertSuccess(retrieveResult, "Payment method retrieval should succeed")
        assertEquals(createdPm.id, retrievedPm.id, "Retrieved payment method should have same ID")
        assertEquals("4242", retrievedPm.card?.last4, "Card details should match")
    }

    @Test
    fun testCreatePaymentMethod_usingBuilder_succeeds() = runTest {
        skipOnJvm()
        if (isJvmPlatform()) return@runTest
        skipIfNoApiKey()
        if (!TestConfiguration.shouldRunIntegrationTests) return@runTest

        val params = PaymentMethodCreateParams.createCard(
            number = TestConfiguration.TestCards.VISA_SUCCESS,
            expMonth = 12,
            expYear = 2030,
            cvc = "123",
            billingDetails = BillingDetails(name = "Builder Test User")
        )

        val result = stripe.createPaymentMethod(params)
        val pm = assertSuccess(result, "Expected successful payment method creation")
        assertTrue(pm.id.startsWith("pm_"), "Payment method ID should start with 'pm_'")
        assertEquals("4242", pm.card?.last4, "Last 4 digits should match test card")
    }
}
