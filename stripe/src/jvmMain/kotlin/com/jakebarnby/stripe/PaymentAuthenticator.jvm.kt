package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*

/**
 * JVM implementation of PaymentAuthenticator.
 * 3D Secure authentication UI is not available on JVM as it's a server-side target.
 */
public actual class PaymentAuthenticator {
    public actual companion object {
        private val instance = PaymentAuthenticator()

        public actual fun getInstance(): PaymentAuthenticator = instance
    }

    public actual suspend fun handleNextAction(
        clientSecret: String
    ): AuthenticationResult = AuthenticationResult.Failed(
        StripeException("Payment authentication UI is not available on JVM. Use Android, iOS, or JS target.")
    )

    public actual suspend fun handleNextActionForPayment(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult = AuthenticationResult.Failed(
        StripeException("Payment authentication UI is not available on JVM. Use Android, iOS, or JS target.")
    )

    public actual suspend fun handleNextActionForSetupIntent(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult = AuthenticationResult.Failed(
        StripeException("Payment authentication UI is not available on JVM. Use Android, iOS, or JS target.")
    )

    public actual suspend fun authenticatePayment(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult = AuthenticationResult.Failed(
        StripeException("Payment authentication UI is not available on JVM. Use Android, iOS, or JS target.")
    )

    public actual suspend fun authenticateSetupIntent(
        activity: Any,
        clientSecret: String
    ): AuthenticationResult = AuthenticationResult.Failed(
        StripeException("Payment authentication UI is not available on JVM. Use Android, iOS, or JS target.")
    )

    public actual suspend fun handleChallenge(
        activity: Any,
        challenge: ThreeDSecureChallenge
    ): Stripe3ds2AuthenticationResponse = Stripe3ds2AuthenticationResponse(
        id = "jvm_not_supported",
        state = AuthenticationState.FAILED,
        error = "3D Secure challenge UI is not available on JVM. Use Android, iOS, or JS target."
    )
}
