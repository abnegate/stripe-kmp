package com.jakebarnby.stripe.model

import kotlin.test.*

/**
 * Comprehensive tests for 3D Secure authentication models.
 */
class AuthenticationTest {

    // ============================================================================
    // ThreeDSecureChallenge Tests
    // ============================================================================

    @Test
    fun testThreeDSecureChallengeCreation() {
        val challenge = ThreeDSecureChallenge(
            acsUrl = "https://acs.example.com/challenge",
            acsSignedContent = "signed_content_abc123",
            threeDSecureServerTransactionId = "server_txn_123",
            acsTransactionId = "acs_txn_456",
            version = ThreeDSecureVersion.V2_2
        )

        assertEquals("https://acs.example.com/challenge", challenge.acsUrl)
        assertEquals("signed_content_abc123", challenge.acsSignedContent)
        assertEquals("server_txn_123", challenge.threeDSecureServerTransactionId)
        assertEquals("acs_txn_456", challenge.acsTransactionId)
        assertEquals(ThreeDSecureVersion.V2_2, challenge.version)
    }

    @Test
    fun testThreeDSecureChallengeWithoutOptionalFields() {
        val challenge = ThreeDSecureChallenge(
            acsUrl = "https://acs.example.com/challenge",
            acsSignedContent = null,
            threeDSecureServerTransactionId = "server_txn_123",
            acsTransactionId = null,
            version = ThreeDSecureVersion.V2_1
        )

        assertNull(challenge.acsSignedContent)
        assertNull(challenge.acsTransactionId)
        assertEquals(ThreeDSecureVersion.V2_1, challenge.version)
    }

    @Test
    fun testThreeDSecureChallengeBlankAcsUrlThrows() {
        assertFailsWith<IllegalArgumentException> {
            ThreeDSecureChallenge(
                acsUrl = "",
                threeDSecureServerTransactionId = "server_txn_123",
                version = ThreeDSecureVersion.V2_2
            )
        }
    }

    @Test
    fun testThreeDSecureChallengeBlankServerTransactionIdThrows() {
        assertFailsWith<IllegalArgumentException> {
            ThreeDSecureChallenge(
                acsUrl = "https://acs.example.com/challenge",
                threeDSecureServerTransactionId = "",
                version = ThreeDSecureVersion.V2_2
            )
        }
    }

    // ============================================================================
    // ThreeDSecureVersion Tests
    // ============================================================================

    @Test
    fun testThreeDSecureVersionValues() {
        assertEquals("1.0", ThreeDSecureVersion.V1_0.value)
        assertEquals("2.1", ThreeDSecureVersion.V2_1.value)
        assertEquals("2.2", ThreeDSecureVersion.V2_2.value)
    }

    @Test
    fun testThreeDSecureVersionFromValue() {
        assertEquals(ThreeDSecureVersion.V1_0, ThreeDSecureVersion.fromValue("1.0"))
        assertEquals(ThreeDSecureVersion.V2_1, ThreeDSecureVersion.fromValue("2.1"))
        assertEquals(ThreeDSecureVersion.V2_2, ThreeDSecureVersion.fromValue("2.2"))
        assertNull(ThreeDSecureVersion.fromValue("3.0"))
    }

    // ============================================================================
    // AuthenticationResult Tests
    // ============================================================================

    @Test
    fun testAuthenticationResultCompletedWithPaymentIntent() {
        val paymentIntent = createTestPaymentIntent()

        val result = AuthenticationResult.Completed(
            paymentIntent = paymentIntent,
            setupIntent = null
        )

        assertTrue(result is AuthenticationResult.Completed)
        assertEquals(paymentIntent, result.paymentIntent)
        assertNull(result.setupIntent)
    }

    @Test
    fun testAuthenticationResultCompletedWithSetupIntent() {
        val setupIntent = createTestSetupIntent()

        val result = AuthenticationResult.Completed(
            paymentIntent = null,
            setupIntent = setupIntent
        )

        assertTrue(result is AuthenticationResult.Completed)
        assertNull(result.paymentIntent)
        assertEquals(setupIntent, result.setupIntent)
    }

    @Test
    fun testAuthenticationResultCompletedRequiresEitherIntent() {
        assertFailsWith<IllegalArgumentException> {
            AuthenticationResult.Completed(
                paymentIntent = null,
                setupIntent = null
            )
        }
    }

    @Test
    fun testAuthenticationResultCanceled() {
        val result = AuthenticationResult.Canceled

        assertTrue(result is AuthenticationResult.Canceled)
        assertSame(AuthenticationResult.Canceled, result)
    }

    @Test
    fun testAuthenticationResultFailed() {
        val error = StripeException("Authentication failed")
        val paymentIntent = createTestPaymentIntent()

        val result = AuthenticationResult.Failed(
            error = error,
            paymentIntent = paymentIntent,
            setupIntent = null
        )

        assertTrue(result is AuthenticationResult.Failed)
        assertEquals(error, result.error)
        assertEquals(paymentIntent, result.paymentIntent)
        assertNull(result.setupIntent)
    }

    @Test
    fun testAuthenticationResultFailedWithoutIntents() {
        val error = StripeException("Authentication failed")

        val result = AuthenticationResult.Failed(
            error = error,
            paymentIntent = null,
            setupIntent = null
        )

        assertTrue(result is AuthenticationResult.Failed)
        assertEquals(error, result.error)
        assertNull(result.paymentIntent)
        assertNull(result.setupIntent)
    }

    // ============================================================================
    // Stripe3ds2AuthenticationResponse Tests
    // ============================================================================

    @Test
    fun testStripe3ds2AuthenticationResponseSucceeded() {
        val response = Stripe3ds2AuthenticationResponse(
            id = "auth_123",
            ares = "authentication_response_data",
            error = null,
            fallbackRedirectUrl = null,
            state = AuthenticationState.SUCCEEDED
        )

        assertEquals("auth_123", response.id)
        assertEquals("authentication_response_data", response.ares)
        assertNull(response.error)
        assertEquals(AuthenticationState.SUCCEEDED, response.state)
    }

    @Test
    fun testStripe3ds2AuthenticationResponseFailed() {
        val response = Stripe3ds2AuthenticationResponse(
            id = "auth_123",
            ares = null,
            error = "Authentication declined by issuer",
            fallbackRedirectUrl = null,
            state = AuthenticationState.FAILED
        )

        assertEquals("auth_123", response.id)
        assertNull(response.ares)
        assertEquals("Authentication declined by issuer", response.error)
        assertEquals(AuthenticationState.FAILED, response.state)
    }

    @Test
    fun testStripe3ds2AuthenticationResponseRedirectRequired() {
        val response = Stripe3ds2AuthenticationResponse(
            id = "auth_123",
            ares = null,
            error = null,
            fallbackRedirectUrl = "https://redirect.example.com",
            state = AuthenticationState.REDIRECT_REQUIRED
        )

        assertEquals("auth_123", response.id)
        assertEquals("https://redirect.example.com", response.fallbackRedirectUrl)
        assertEquals(AuthenticationState.REDIRECT_REQUIRED, response.state)
    }

    @Test
    fun testStripe3ds2AuthenticationResponseChallenged() {
        val response = Stripe3ds2AuthenticationResponse(
            id = "auth_123",
            ares = null,
            error = null,
            fallbackRedirectUrl = null,
            state = AuthenticationState.CHALLENGED
        )

        assertEquals(AuthenticationState.CHALLENGED, response.state)
    }

    @Test
    fun testStripe3ds2AuthenticationResponseBlankIdThrows() {
        assertFailsWith<IllegalArgumentException> {
            Stripe3ds2AuthenticationResponse(
                id = "",
                ares = "data",
                state = AuthenticationState.SUCCEEDED
            )
        }
    }

    @Test
    fun testStripe3ds2AuthenticationResponseSucceededRequiresAres() {
        assertFailsWith<IllegalArgumentException> {
            Stripe3ds2AuthenticationResponse(
                id = "auth_123",
                ares = null,
                state = AuthenticationState.SUCCEEDED
            )
        }
    }

    @Test
    fun testStripe3ds2AuthenticationResponseFailedRequiresError() {
        assertFailsWith<IllegalArgumentException> {
            Stripe3ds2AuthenticationResponse(
                id = "auth_123",
                error = null,
                state = AuthenticationState.FAILED
            )
        }
    }

    @Test
    fun testStripe3ds2AuthenticationResponseRedirectRequiresFallbackUrl() {
        assertFailsWith<IllegalArgumentException> {
            Stripe3ds2AuthenticationResponse(
                id = "auth_123",
                fallbackRedirectUrl = null,
                state = AuthenticationState.REDIRECT_REQUIRED
            )
        }
    }

    // ============================================================================
    // AuthenticationState Tests
    // ============================================================================

    @Test
    fun testAuthenticationStateValues() {
        assertEquals("succeeded", AuthenticationState.SUCCEEDED.value)
        assertEquals("failed", AuthenticationState.FAILED.value)
        assertEquals("challenged", AuthenticationState.CHALLENGED.value)
        assertEquals("redirect_required", AuthenticationState.REDIRECT_REQUIRED.value)
    }

    @Test
    fun testAuthenticationStateFromValue() {
        assertEquals(AuthenticationState.SUCCEEDED, AuthenticationState.fromValue("succeeded"))
        assertEquals(AuthenticationState.FAILED, AuthenticationState.fromValue("failed"))
        assertEquals(AuthenticationState.CHALLENGED, AuthenticationState.fromValue("challenged"))
        assertEquals(AuthenticationState.REDIRECT_REQUIRED, AuthenticationState.fromValue("redirect_required"))
        assertNull(AuthenticationState.fromValue("unknown"))
    }

    // ============================================================================
    // AuthenticatePaymentParams Tests
    // ============================================================================

    @Test
    fun testAuthenticatePaymentParamsCreation() {
        val params = AuthenticatePaymentParams(
            clientSecret = "pi_123_secret_456",
            returnUrl = "https://example.com/return"
        )

        assertEquals("pi_123_secret_456", params.clientSecret)
        assertEquals("https://example.com/return", params.returnUrl)
    }

    @Test
    fun testAuthenticatePaymentParamsWithoutReturnUrl() {
        val params = AuthenticatePaymentParams(
            clientSecret = "pi_123_secret_456",
            returnUrl = null
        )

        assertEquals("pi_123_secret_456", params.clientSecret)
        assertNull(params.returnUrl)
    }

    @Test
    fun testAuthenticatePaymentParamsBlankClientSecretThrows() {
        assertFailsWith<IllegalArgumentException> {
            AuthenticatePaymentParams(
                clientSecret = "",
                returnUrl = "https://example.com/return"
            )
        }
    }

    // ============================================================================
    // AuthenticateSetupParams Tests
    // ============================================================================

    @Test
    fun testAuthenticateSetupParamsCreation() {
        val params = AuthenticateSetupParams(
            clientSecret = "seti_123_secret_456",
            returnUrl = "https://example.com/return"
        )

        assertEquals("seti_123_secret_456", params.clientSecret)
        assertEquals("https://example.com/return", params.returnUrl)
    }

    @Test
    fun testAuthenticateSetupParamsWithoutReturnUrl() {
        val params = AuthenticateSetupParams(
            clientSecret = "seti_123_secret_456",
            returnUrl = null
        )

        assertEquals("seti_123_secret_456", params.clientSecret)
        assertNull(params.returnUrl)
    }

    @Test
    fun testAuthenticateSetupParamsBlankClientSecretThrows() {
        assertFailsWith<IllegalArgumentException> {
            AuthenticateSetupParams(
                clientSecret = "",
                returnUrl = "https://example.com/return"
            )
        }
    }

    // ============================================================================
    // AuthenticationCompletionOptions Tests
    // ============================================================================

    @Test
    fun testAuthenticationCompletionOptionsDefaults() {
        val options = AuthenticationCompletionOptions()

        assertFalse(options.shouldSavePaymentMethod)
        assertNull(options.mandateData)
    }

    @Test
    fun testAuthenticationCompletionOptionsWithSavePaymentMethod() {
        val options = AuthenticationCompletionOptions(
            shouldSavePaymentMethod = true,
            mandateData = null
        )

        assertTrue(options.shouldSavePaymentMethod)
        assertNull(options.mandateData)
    }

    @Test
    fun testAuthenticationCompletionOptionsWithMandateData() {
        val mandateData = MandateData(
            customerAcceptance = CustomerAcceptance(
                type = "online",
                acceptedAt = 1234567890L,
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
        assertEquals(mandateData, options.mandateData)
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private fun createTestPaymentIntent(): PaymentIntent {
        return PaymentIntent(
            id = "pi_123",
            clientSecret = "pi_123_secret_456",
            amount = 1000,
            currency = "usd",
            status = PaymentIntentStatus.REQUIRES_ACTION,
            created = 1234567890L,
            livemode = false
        )
    }

    private fun createTestSetupIntent(): SetupIntent {
        return SetupIntent(
            id = "seti_123",
            clientSecret = "seti_123_secret_456",
            created = 1234567890L,
            livemode = false,
            status = SetupIntentStatus.REQUIRES_ACTION
        )
    }
}
