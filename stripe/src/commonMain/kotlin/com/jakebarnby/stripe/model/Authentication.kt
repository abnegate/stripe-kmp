package com.jakebarnby.stripe.model

/**
 * 3D Secure challenge data for authentication.
 * Contains the information needed to perform a 3DS2 challenge flow.
 *
 * @property acsUrl URL of the Access Control Server (ACS) for the challenge
 * @property acsSignedContent Signed content from the ACS (3DS2.2+)
 * @property threeDSecureServerTransactionId Server transaction ID for 3DS
 * @property acsTransactionId Transaction ID from the ACS
 * @property version Version of 3D Secure protocol being used
 */
public data class ThreeDSecureChallenge(
    val acsUrl: String,
    val acsSignedContent: String? = null,
    val threeDSecureServerTransactionId: String,
    val acsTransactionId: String? = null,
    val version: ThreeDSecureVersion
) {
    init {
        require(acsUrl.isNotBlank()) { "acsUrl cannot be blank" }
        require(threeDSecureServerTransactionId.isNotBlank()) {
            "threeDSecureServerTransactionId cannot be blank"
        }
    }
}

/**
 * Version of the 3D Secure protocol.
 */
public enum class ThreeDSecureVersion(public val value: String) {
    /** 3D Secure 1.0 */
    V1_0("1.0"),

    /** 3D Secure 2.1 */
    V2_1("2.1"),

    /** 3D Secure 2.2 */
    V2_2("2.2");

    public companion object {
        /**
         * Parse version from string value.
         */
        public fun fromValue(value: String): ThreeDSecureVersion? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * Result of an authentication flow.
 * This represents the final outcome after processing payment or setup authentication.
 */
public sealed class AuthenticationResult {
    /**
     * Authentication completed successfully.
     *
     * @property paymentIntent The resulting PaymentIntent (if authenticating a payment)
     * @property setupIntent The resulting SetupIntent (if authenticating a setup)
     */
    public data class Completed(
        val paymentIntent: PaymentIntent? = null,
        val setupIntent: SetupIntent? = null
    ) : AuthenticationResult() {
        init {
            // At least one should be present
            require(paymentIntent != null || setupIntent != null) {
                "Either paymentIntent or setupIntent must be provided"
            }
        }
    }

    /**
     * Authentication was canceled by the user.
     */
    public data object Canceled : AuthenticationResult()

    /**
     * Authentication failed with an error.
     *
     * @property error The error that occurred during authentication
     * @property paymentIntent The PaymentIntent if available (may contain error details)
     * @property setupIntent The SetupIntent if available (may contain error details)
     */
    public data class Failed(
        val error: StripeException,
        val paymentIntent: PaymentIntent? = null,
        val setupIntent: SetupIntent? = null
    ) : AuthenticationResult()
}

/**
 * Response from Stripe 3DS2 SDK authentication.
 * This represents the state after completing a 3DS2 challenge.
 *
 * @property id Unique identifier for the authentication response
 * @property ares Authentication Response (ARes) message from ACS
 * @property error Error message if authentication failed
 * @property fallbackRedirectUrl Fallback URL if 3DS2 native flow is not supported
 * @property state Current state of the authentication
 */
public data class Stripe3ds2AuthenticationResponse(
    val id: String,
    val ares: String? = null,
    val error: String? = null,
    val fallbackRedirectUrl: String? = null,
    val state: AuthenticationState
) {
    init {
        require(id.isNotBlank()) { "id cannot be blank" }

        // Validate state consistency
        when (state) {
            AuthenticationState.SUCCEEDED -> require(ares != null) {
                "ares must be provided when state is SUCCEEDED"
            }
            AuthenticationState.FAILED -> require(error != null) {
                "error must be provided when state is FAILED"
            }
            AuthenticationState.REDIRECT_REQUIRED -> require(fallbackRedirectUrl != null) {
                "fallbackRedirectUrl must be provided when state is REDIRECT_REQUIRED"
            }
            else -> {}
        }
    }
}

/**
 * State of a 3DS2 authentication attempt.
 */
public enum class AuthenticationState(public val value: String) {
    /** Authentication succeeded */
    SUCCEEDED("succeeded"),

    /** Authentication failed */
    FAILED("failed"),

    /** Challenge required - waiting for user input */
    CHALLENGED("challenged"),

    /** Redirect required - fallback to 3DS1 or browser redirect */
    REDIRECT_REQUIRED("redirect_required");

    public companion object {
        /**
         * Parse authentication state from string value.
         */
        public fun fromValue(value: String): AuthenticationState? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * Parameters for authenticating a payment.
 *
 * @property clientSecret Client secret of the PaymentIntent to authenticate
 * @property returnUrl Optional return URL after authentication (web flows)
 */
public data class AuthenticatePaymentParams(
    val clientSecret: String,
    val returnUrl: String? = null
) {
    init {
        require(clientSecret.isNotBlank()) { "clientSecret cannot be blank" }
    }
}

/**
 * Parameters for authenticating a setup.
 *
 * @property clientSecret Client secret of the SetupIntent to authenticate
 * @property returnUrl Optional return URL after authentication (web flows)
 */
public data class AuthenticateSetupParams(
    val clientSecret: String,
    val returnUrl: String? = null
) {
    init {
        require(clientSecret.isNotBlank()) { "clientSecret cannot be blank" }
    }
}

/**
 * Authentication completion options for handling the final result.
 *
 * @property shouldSavePaymentMethod Whether to save the payment method after authentication
 * @property mandateData Mandate data if saving for future off-session use
 */
public data class AuthenticationCompletionOptions(
    val shouldSavePaymentMethod: Boolean = false,
    val mandateData: MandateData? = null
)
