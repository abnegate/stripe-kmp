package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.AuthenticationResult
import com.jakebarnby.stripe.model.FinancialConnectionsSheetConfiguration
import com.jakebarnby.stripe.model.FinancialConnectionsSheetResult
import com.jakebarnby.stripe.model.IdentityVerificationSheetConfiguration
import com.jakebarnby.stripe.model.IdentityVerificationSheetResult
import com.jakebarnby.stripe.model.ThreeDSecureChallenge
import com.jakebarnby.stripe.model.ThreeDSecureVersion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class IosUiSheetTest {
    private val paymentIntentConfig = PaymentIntentConfiguration(
        clientSecret = "pi_123_secret_456",
        paymentSheetConfiguration = PaymentSheetConfiguration(
            merchantDisplayName = "iOS Test"
        )
    )

    private val setupIntentConfig = SetupIntentConfiguration(
        clientSecret = "seti_123_secret_456",
        paymentSheetConfiguration = PaymentSheetConfiguration(
            merchantDisplayName = "iOS Test"
        )
    )

    private val identityConfig = IdentityVerificationSheetConfiguration(
        verificationSessionId = "vs_123",
        ephemeralKeySecret = "ek_123"
    )

    private val financialConfig = FinancialConnectionsSheetConfiguration(
        financialConnectionsSessionClientSecret = "fcsess_123_secret_456",
        publishableKey = "pk_test_123"
    )

    @Test
    fun paymentSheet_presentWithPaymentIntent_returnsFailed() = runTest {
        var result: PaymentSheetResult? = null
        PaymentSheet().presentWithPaymentIntent(paymentIntentConfig) { result = it }
        assertTrue(result is PaymentSheetResult.Failed)
        assertTrue((result as PaymentSheetResult.Failed).error.code == "ios_requires_swift_bridge")
    }

    @Test
    fun paymentSheet_presentWithSetupIntent_returnsFailed() = runTest {
        var result: PaymentSheetResult? = null
        PaymentSheet().presentWithSetupIntent(setupIntentConfig) { result = it }
        assertTrue(result is PaymentSheetResult.Failed)
        assertTrue((result as PaymentSheetResult.Failed).error.code == "ios_requires_swift_bridge")
    }

    @Test
    fun identityVerification_present_returnsFailed() = runTest {
        val result = IdentityVerificationSheet().present(identityConfig)
        assertTrue(result is IdentityVerificationSheetResult.Failed)
    }

    @Test
    fun financialConnections_present_returnsFailed() = runTest {
        val result = FinancialConnectionsSheet.create(financialConfig).present()
        assertTrue(result is FinancialConnectionsSheetResult.Failed)
    }

    @Test
    fun financialConnections_presentForToken_returnsFailed() = runTest {
        val result = FinancialConnectionsSheet.create(financialConfig).presentForToken()
        assertTrue(result is FinancialConnectionsSheetForTokenResult.Failed)
    }

    @Test
    fun paymentAuthenticator_handleNextAction_returnsFailed() = runTest {
        val result = PaymentAuthenticator.getInstance().handleNextAction("pi_123_secret_456")
        assertTrue(result is AuthenticationResult.Failed)
    }

    @Test
    fun paymentAuthenticator_handleChallenge_returnsRedirect() = runTest {
        val challenge = ThreeDSecureChallenge(
            acsUrl = "https://example.com/acs",
            threeDSecureServerTransactionId = "trans_123",
            version = ThreeDSecureVersion.V2_2
        )
        val result = PaymentAuthenticator.getInstance().handleChallenge(activity = Any(), challenge = challenge)
        assertTrue(result.fallbackRedirectUrl == "https://example.com/acs")
    }
}
