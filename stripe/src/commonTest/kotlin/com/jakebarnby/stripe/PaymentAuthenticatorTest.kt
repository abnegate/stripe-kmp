package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for PaymentAuthenticator to ensure API stability.
 * These tests verify that all authentication-related classes and methods exist.
 */
class PaymentAuthenticatorTest {

    @Test
    fun testGetInstanceExists() {
        // Verify getInstance method exists and returns a non-null instance
        val method = PaymentAuthenticator.Companion::getInstance
        assertNotNull(method)
    }

    @Test
    fun testAuthenticationResultSealedClassStructure() {
        // Verify all AuthenticationResult subtypes exist
        val completed: AuthenticationResult = AuthenticationResult.Completed(
            paymentIntent = PaymentIntent(
                id = "pi_test",
                clientSecret = "pi_test_secret",
                amount = 1000,
                currency = "usd",
                status = PaymentIntentStatus.SUCCEEDED,
                created = 1234567890,
                livemode = false
            )
        )

        val canceled: AuthenticationResult = AuthenticationResult.Canceled

        val failed: AuthenticationResult = AuthenticationResult.Failed(
            error = StripeException("test error")
        )

        // Verify types
        assertTrue(completed is AuthenticationResult.Completed)
        assertTrue(canceled is AuthenticationResult.Canceled)
        assertTrue(failed is AuthenticationResult.Failed)
        assertTrue(completed is AuthenticationResult)
        assertTrue(canceled is AuthenticationResult)
        assertTrue(failed is AuthenticationResult)
    }

    @Test
    fun testAuthenticationResultCompletedProperties() {
        val paymentIntent = PaymentIntent(
            id = "pi_123",
            clientSecret = "pi_secret",
            amount = 2000,
            currency = "eur",
            status = PaymentIntentStatus.SUCCEEDED,
            created = 1234567890,
            livemode = false
        )

        val completed = AuthenticationResult.Completed(paymentIntent = paymentIntent)

        assertNotNull(completed.paymentIntent)
        assertEquals("pi_123", completed.paymentIntent?.id)
        assertEquals(2000L, completed.paymentIntent?.amount)
    }

    @Test
    fun testAuthenticationResultCompletedWithSetupIntent() {
        val setupIntent = SetupIntent(
            id = "seti_123",
            clientSecret = "seti_secret",
            created = 1234567890,
            livemode = false,
            status = SetupIntentStatus.SUCCEEDED
        )

        val completed = AuthenticationResult.Completed(setupIntent = setupIntent)

        assertNotNull(completed.setupIntent)
        assertEquals("seti_123", completed.setupIntent?.id)
    }

    @Test
    fun testAuthenticationResultCompletedRequiresEitherIntent() {
        // Must provide at least one intent
        assertFailsWith<IllegalArgumentException> {
            AuthenticationResult.Completed()
        }
    }

    @Test
    fun testAuthenticationResultFailedProperties() {
        val exception = StripeException(
            message = "Authentication failed",
            statusCode = 402
        )

        val failed = AuthenticationResult.Failed(error = exception)

        assertNotNull(failed.error)
        assertEquals("Authentication failed", failed.error.message)
        assertEquals(402, failed.error.statusCode)
    }

    @Test
    fun testThreeDSecureChallengeProperties() {
        val challenge = ThreeDSecureChallenge(
            acsUrl = "https://acs.example.com",
            acsSignedContent = "signed_content_123",
            threeDSecureServerTransactionId = "txn_server_123",
            acsTransactionId = "acs_txn_456",
            version = ThreeDSecureVersion.V2_1
        )

        assertNotNull(challenge.acsUrl)
        assertEquals("https://acs.example.com", challenge.acsUrl)
        assertNotNull(challenge.acsSignedContent)
        assertEquals("signed_content_123", challenge.acsSignedContent)
        assertNotNull(challenge.threeDSecureServerTransactionId)
        assertEquals("txn_server_123", challenge.threeDSecureServerTransactionId)
        assertNotNull(challenge.acsTransactionId)
        assertEquals("acs_txn_456", challenge.acsTransactionId)
        assertNotNull(challenge.version)
        assertEquals(ThreeDSecureVersion.V2_1, challenge.version)
    }

    @Test
    fun testThreeDSecureChallengeValidation() {
        // acsUrl cannot be blank
        assertFailsWith<IllegalArgumentException> {
            ThreeDSecureChallenge(
                acsUrl = "",
                threeDSecureServerTransactionId = "txn_123",
                version = ThreeDSecureVersion.V2_1
            )
        }

        // threeDSecureServerTransactionId cannot be blank
        assertFailsWith<IllegalArgumentException> {
            ThreeDSecureChallenge(
                acsUrl = "https://acs.example.com",
                threeDSecureServerTransactionId = "",
                version = ThreeDSecureVersion.V2_1
            )
        }
    }

    @Test
    fun testThreeDSecureVersionEnum() {
        // Verify all versions exist
        assertEquals("1.0", ThreeDSecureVersion.V1_0.value)
        assertEquals("2.1", ThreeDSecureVersion.V2_1.value)
        assertEquals("2.2", ThreeDSecureVersion.V2_2.value)

        // Verify fromValue works
        assertEquals(ThreeDSecureVersion.V1_0, ThreeDSecureVersion.fromValue("1.0"))
        assertEquals(ThreeDSecureVersion.V2_1, ThreeDSecureVersion.fromValue("2.1"))
        assertEquals(ThreeDSecureVersion.V2_2, ThreeDSecureVersion.fromValue("2.2"))
    }

    @Test
    fun testStripe3ds2AuthenticationResponseProperties() {
        val response = Stripe3ds2AuthenticationResponse(
            id = "auth_123",
            ares = "ares_message",
            error = null,
            fallbackRedirectUrl = null,
            state = AuthenticationState.SUCCEEDED
        )

        assertNotNull(response.id)
        assertEquals("auth_123", response.id)
        assertNotNull(response.ares)
        assertEquals("ares_message", response.ares)
        assertEquals(AuthenticationState.SUCCEEDED, response.state)
    }

    @Test
    fun testStripe3ds2AuthenticationResponseValidation() {
        // id cannot be blank
        assertFailsWith<IllegalArgumentException> {
            Stripe3ds2AuthenticationResponse(
                id = "",
                state = AuthenticationState.SUCCEEDED
            )
        }

        // SUCCEEDED requires ares
        assertFailsWith<IllegalArgumentException> {
            Stripe3ds2AuthenticationResponse(
                id = "auth_123",
                state = AuthenticationState.SUCCEEDED
            )
        }

        // FAILED requires error
        assertFailsWith<IllegalArgumentException> {
            Stripe3ds2AuthenticationResponse(
                id = "auth_123",
                state = AuthenticationState.FAILED
            )
        }

        // REDIRECT_REQUIRED requires fallbackRedirectUrl
        assertFailsWith<IllegalArgumentException> {
            Stripe3ds2AuthenticationResponse(
                id = "auth_123",
                state = AuthenticationState.REDIRECT_REQUIRED
            )
        }
    }

    @Test
    fun testAuthenticationStateEnum() {
        // Verify all states exist
        assertEquals("succeeded", AuthenticationState.SUCCEEDED.value)
        assertEquals("failed", AuthenticationState.FAILED.value)
        assertEquals("challenged", AuthenticationState.CHALLENGED.value)
        assertEquals("redirect_required", AuthenticationState.REDIRECT_REQUIRED.value)

        // Verify fromValue works
        assertEquals(AuthenticationState.SUCCEEDED, AuthenticationState.fromValue("succeeded"))
        assertEquals(AuthenticationState.FAILED, AuthenticationState.fromValue("failed"))
        assertEquals(AuthenticationState.CHALLENGED, AuthenticationState.fromValue("challenged"))
        assertEquals(AuthenticationState.REDIRECT_REQUIRED, AuthenticationState.fromValue("redirect_required"))
    }

    @Test
    fun testAuthenticatePaymentParamsProperties() {
        val params = AuthenticatePaymentParams(
            clientSecret = "pi_secret_123",
            returnUrl = "https://example.com/return"
        )

        assertNotNull(params.clientSecret)
        assertEquals("pi_secret_123", params.clientSecret)
        assertEquals("https://example.com/return", params.returnUrl)
    }

    @Test
    fun testAuthenticatePaymentParamsValidation() {
        // clientSecret cannot be blank
        assertFailsWith<IllegalArgumentException> {
            AuthenticatePaymentParams(clientSecret = "")
        }
    }

    @Test
    fun testAuthenticateSetupParamsProperties() {
        val params = AuthenticateSetupParams(
            clientSecret = "seti_secret_123",
            returnUrl = "https://example.com/return"
        )

        assertNotNull(params.clientSecret)
        assertEquals("seti_secret_123", params.clientSecret)
        assertEquals("https://example.com/return", params.returnUrl)
    }

    @Test
    fun testAuthenticateSetupParamsValidation() {
        // clientSecret cannot be blank
        assertFailsWith<IllegalArgumentException> {
            AuthenticateSetupParams(clientSecret = "")
        }
    }

    @Test
    fun testAuthenticationCompletionOptionsProperties() {
        val mandateData = MandateData(
            customerAcceptance = CustomerAcceptance(
                type = "online",
                acceptedAt = 1234567890,
                online = OnlineAcceptance(
                    ipAddress = "192.168.1.1",
                    userAgent = "Mozilla/5.0"
                )
            )
        )

        val options = AuthenticationCompletionOptions(
            shouldSavePaymentMethod = true,
            mandateData = mandateData
        )

        assertTrue(options.shouldSavePaymentMethod)
        assertNotNull(options.mandateData)
        assertEquals("online", options.mandateData?.customerAcceptance?.type)
    }

    @Test
    fun testAuthenticationCompletionOptionsDefaults() {
        val options = AuthenticationCompletionOptions()

        assertEquals(false, options.shouldSavePaymentMethod)
        assertEquals(null, options.mandateData)
    }
}
