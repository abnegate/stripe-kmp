package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Tests for JVM platform implementations.
 * These test the JVM-specific actual implementations.
 */
class JvmPlatformTest {

    // SecureRandom is internal, so we test it indirectly via IdempotencyKey
    @Test
    fun testSecureRandom_viaIdempotencyKey_generatesUniqueKeys() {
        val keys = (1..10).map { IdempotencyKey.generate() }
        val uniqueValues = keys.map { it.value }.toSet()
        // All keys should be unique
        assertEquals(10, uniqueValues.size)
    }

    @Test
    fun testSecureRandom_viaIdempotencyKey_generatesCorrectLength() {
        val key = IdempotencyKey.generate()
        // IdempotencyKey uses 16 bytes = 32 hex chars
        assertEquals(32, key.value.length)
    }

    // PaymentSheet JVM stub tests
    @Test
    fun testPaymentSheet_presentWithPaymentIntent_returnsNotSupported() = runTest {
        val sheet = PaymentSheet()
        val config = PaymentIntentConfiguration(
            clientSecret = "pi_test123_secret_abc123",
            paymentSheetConfiguration = PaymentSheetConfiguration(
                merchantDisplayName = "Test Merchant"
            )
        )

        var result: PaymentSheetResult? = null
        sheet.presentWithPaymentIntent(config) { result = it }

        assertIs<PaymentSheetResult.Failed>(result)
        assertTrue((result as PaymentSheetResult.Failed).error.message.contains("not available on JVM"))
    }

    @Test
    fun testPaymentSheet_presentWithSetupIntent_returnsNotSupported() = runTest {
        val sheet = PaymentSheet()
        val config = SetupIntentConfiguration(
            clientSecret = "seti_test123_secret_abc123",
            paymentSheetConfiguration = PaymentSheetConfiguration(
                merchantDisplayName = "Test Merchant"
            )
        )

        var result: PaymentSheetResult? = null
        sheet.presentWithSetupIntent(config) { result = it }

        assertIs<PaymentSheetResult.Failed>(result)
        assertTrue((result as PaymentSheetResult.Failed).error.message.contains("not available on JVM"))
    }

    // PaymentAuthenticator JVM stub tests
    @Test
    fun testPaymentAuthenticator_getInstance_returnsSingleton() {
        val instance1 = PaymentAuthenticator.getInstance()
        val instance2 = PaymentAuthenticator.getInstance()
        assertNotNull(instance1)
        assertEquals(instance1, instance2)
    }

    @Test
    fun testPaymentAuthenticator_handleNextAction_returnsNotSupported() = runTest {
        val authenticator = PaymentAuthenticator.getInstance()
        val result = authenticator.handleNextAction("pi_test_secret")

        assertIs<AuthenticationResult.Failed>(result)
        assertTrue(result.error.message.contains("not available on JVM"))
    }

    @Test
    fun testPaymentAuthenticator_handleNextActionForPayment_returnsNotSupported() = runTest {
        val authenticator = PaymentAuthenticator.getInstance()
        val result = authenticator.handleNextActionForPayment(Unit, "pi_test_secret")

        assertIs<AuthenticationResult.Failed>(result)
        assertTrue(result.error.message.contains("not available on JVM"))
    }

    @Test
    fun testPaymentAuthenticator_handleNextActionForSetupIntent_returnsNotSupported() = runTest {
        val authenticator = PaymentAuthenticator.getInstance()
        val result = authenticator.handleNextActionForSetupIntent(Unit, "seti_test_secret")

        assertIs<AuthenticationResult.Failed>(result)
        assertTrue(result.error.message.contains("not available on JVM"))
    }

    @Test
    fun testPaymentAuthenticator_authenticatePayment_returnsNotSupported() = runTest {
        val authenticator = PaymentAuthenticator.getInstance()
        val result = authenticator.authenticatePayment(Unit, "pi_test_secret")

        assertIs<AuthenticationResult.Failed>(result)
        assertTrue(result.error.message.contains("not available on JVM"))
    }

    @Test
    fun testPaymentAuthenticator_authenticateSetupIntent_returnsNotSupported() = runTest {
        val authenticator = PaymentAuthenticator.getInstance()
        val result = authenticator.authenticateSetupIntent(Unit, "seti_test_secret")

        assertIs<AuthenticationResult.Failed>(result)
        assertTrue(result.error.message.contains("not available on JVM"))
    }

    @Test
    fun testPaymentAuthenticator_handleChallenge_returnsFailedState() = runTest {
        val authenticator = PaymentAuthenticator.getInstance()
        val challenge = ThreeDSecureChallenge(
            acsUrl = "https://acs.example.com",
            threeDSecureServerTransactionId = "txn_123",
            version = ThreeDSecureVersion.V2_1
        )
        val response = authenticator.handleChallenge(Unit, challenge)

        assertEquals(AuthenticationState.FAILED, response.state)
        assertNotNull(response.error)
        assertTrue(response.error!!.contains("not available on JVM"))
    }

    // FinancialConnectionsSheet JVM stub tests
    @Test
    fun testFinancialConnectionsSheet_create_returnsInstance() {
        val config = FinancialConnectionsSheetConfiguration(
            financialConnectionsSessionClientSecret = "fcsess_client_secret_test123",
            publishableKey = "pk_test_51Abc123"
        )
        val sheet = FinancialConnectionsSheet.create(config)
        assertNotNull(sheet)
    }

    @Test
    fun testFinancialConnectionsSheet_present_returnsNotSupported() = runTest {
        val config = FinancialConnectionsSheetConfiguration(
            financialConnectionsSessionClientSecret = "fcsess_client_secret_test123",
            publishableKey = "pk_test_51Abc123"
        )
        val sheet = FinancialConnectionsSheet.create(config)
        val result = sheet.present()

        assertIs<FinancialConnectionsSheetResult.Failed>(result)
        assertTrue(result.error.message.contains("not available on JVM"))
    }

    @Test
    fun testFinancialConnectionsSheet_presentForToken_returnsNotSupported() = runTest {
        val config = FinancialConnectionsSheetConfiguration(
            financialConnectionsSessionClientSecret = "fcsess_client_secret_test123",
            publishableKey = "pk_test_51Abc123"
        )
        val sheet = FinancialConnectionsSheet.create(config)
        val result = sheet.presentForToken()

        assertIs<FinancialConnectionsSheetForTokenResult.Failed>(result)
        assertTrue(result.error.message.contains("not available on JVM"))
    }

    // IdentityVerificationSheet JVM stub tests
    @Test
    fun testIdentityVerificationSheet_present_returnsNotSupported() = runTest {
        val sheet = IdentityVerificationSheet()
        val config = IdentityVerificationSheetConfiguration(
            verificationSessionId = "vs_test123",
            ephemeralKeySecret = "ek_test_abc123"
        )
        val result = sheet.present(config)

        assertIs<IdentityVerificationSheetResult.Failed>(result)
        assertTrue(result.error.message.contains("not available on JVM"))
    }
}
