package com.jakebarnby.stripe.api

import com.jakebarnby.stripe.*
import com.jakebarnby.stripe.model.*
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Tests that verify all public API methods exist with correct signatures.
 * These tests will fail if the Stripe SDK changes its API.
 *
 * This ensures backward compatibility when upgrading Stripe SDK versions.
 */
class StripeApiSignatureTest {

    @Test
    fun verifyStripeCompanionMethods() {
        // Verify Stripe.Companion exists
        val companion = Stripe.Companion
        assertNotNull(companion)

        // Verify initialize method exists with correct signature
        val initMethod: (StripeConfiguration) -> Stripe = Stripe::initialize
        assertNotNull(initMethod)

        // Verify getInstance method exists
        val getInstanceMethod: () -> Stripe = Stripe::getInstance
        assertNotNull(getInstanceMethod)
    }

    @Test
    fun verifyStripeTokenCreationMethods() {
        // Verify all token creation methods exist with correct signatures
        val config = StripeConfiguration(
            publishableKey = "pk_test_51Abc123DefGhi456Jkl789MnoPqr012Stu345Vwx678Yz",
            merchantDisplayName = "Test"
        )

        // These method references prove the methods exist with correct signatures
        val createCardTokenMethod: suspend Stripe.(CardParams, IdempotencyKey?) -> StripeResult<Token> =
            Stripe::createCardToken
        assertNotNull(createCardTokenMethod)

        val createBankAccountTokenMethod: suspend Stripe.(BankAccountTokenParams, IdempotencyKey?) -> StripeResult<Token> =
            Stripe::createBankAccountToken
        assertNotNull(createBankAccountTokenMethod)

        val createPiiTokenMethod: suspend Stripe.(PiiTokenParams) -> StripeResult<Token> =
            Stripe::createPiiToken
        assertNotNull(createPiiTokenMethod)

        val createAccountTokenMethod: suspend Stripe.(AccountParams) -> StripeResult<Token> =
            Stripe::createAccountToken
        assertNotNull(createAccountTokenMethod)
    }

    @Test
    fun verifyStripeSourceMethods() {
        // Verify Source creation and retrieval methods
        val createSourceMethod: suspend Stripe.(SourceParams, IdempotencyKey?) -> StripeResult<Source> =
            Stripe::createSource
        assertNotNull(createSourceMethod)

        val retrieveSourceMethod: suspend Stripe.(String, String) -> StripeResult<Source> =
            Stripe::retrieveSource
        assertNotNull(retrieveSourceMethod)
    }

    @Test
    fun verifyStripePaymentMethodMethods() {
        // Verify PaymentMethod methods
        val createPaymentMethodMethod: suspend Stripe.(PaymentMethodCreateParams, IdempotencyKey?) -> StripeResult<PaymentMethod> =
            Stripe::createPaymentMethod
        assertNotNull(createPaymentMethodMethod)

        val retrievePaymentMethodMethod: suspend Stripe.(String) -> StripeResult<PaymentMethod> =
            Stripe::retrievePaymentMethod
        assertNotNull(retrievePaymentMethodMethod)
    }

    @Test
    fun verifyStripePaymentIntentMethods() {
        // Verify PaymentIntent methods
        val retrievePaymentIntentMethod: suspend Stripe.(String) -> StripeResult<PaymentIntent> =
            Stripe::retrievePaymentIntent
        assertNotNull(retrievePaymentIntentMethod)

        val confirmPaymentIntentMethod: suspend Stripe.(ConfirmPaymentIntentParams, IdempotencyKey?) -> StripeResult<PaymentIntent> =
            Stripe::confirmPaymentIntent
        assertNotNull(confirmPaymentIntentMethod)

        val handleNextActionForPaymentMethod: suspend Stripe.(String) -> StripeResult<PaymentIntent> =
            Stripe::handleNextActionForPayment
        assertNotNull(handleNextActionForPaymentMethod)
    }

    @Test
    fun verifyStripeSetupIntentMethods() {
        // Verify SetupIntent methods
        val retrieveSetupIntentMethod: suspend Stripe.(String) -> StripeResult<SetupIntent> =
            Stripe::retrieveSetupIntent
        assertNotNull(retrieveSetupIntentMethod)

        val confirmSetupIntentMethod: suspend Stripe.(ConfirmSetupIntentParams, IdempotencyKey?) -> StripeResult<SetupIntent> =
            Stripe::confirmSetupIntent
        assertNotNull(confirmSetupIntentMethod)

        val handleNextActionForSetupIntentMethod: suspend Stripe.(String) -> StripeResult<SetupIntent> =
            Stripe::handleNextActionForSetupIntent
        assertNotNull(handleNextActionForSetupIntentMethod)
    }

    @Test
    fun verifyStripeCustomerMethods() {
        // Verify Customer methods
        val retrieveCustomerMethod: suspend Stripe.(String) -> StripeResult<Customer> =
            Stripe::retrieveCustomer
        assertNotNull(retrieveCustomerMethod)

        val createEphemeralKeyMethod: suspend Stripe.(EphemeralKeyCreateParams) -> StripeResult<EphemeralKey> =
            Stripe::createEphemeralKey
        assertNotNull(createEphemeralKeyMethod)
    }

    @Test
    fun verifyStripeConfigurationProperty() {
        // Verify configuration property exists
        val config = StripeConfiguration(
            publishableKey = "pk_test_51Abc123DefGhi456Jkl789MnoPqr012Stu345Vwx678Yz",
            merchantDisplayName = "Test"
        )

        // This property reference proves the configuration property exists
        val configProperty: Stripe.() -> StripeConfiguration = Stripe::configuration
        assertNotNull(configProperty)
    }

    @Test
    fun verifyPaymentAuthenticatorSignatures() {
        // Verify PaymentAuthenticator.getInstance exists
        val getInstanceMethod = PaymentAuthenticator.Companion::getInstance
        assertNotNull(getInstanceMethod)

        // Verify authentication methods exist with correct signatures
        val handleNextActionMethod: suspend PaymentAuthenticator.(String) -> AuthenticationResult =
            PaymentAuthenticator::handleNextAction
        assertNotNull(handleNextActionMethod)

        val handleNextActionForPaymentMethod: suspend PaymentAuthenticator.(Any, String) -> AuthenticationResult =
            PaymentAuthenticator::handleNextActionForPayment
        assertNotNull(handleNextActionForPaymentMethod)

        val handleNextActionForSetupIntentMethod: suspend PaymentAuthenticator.(Any, String) -> AuthenticationResult =
            PaymentAuthenticator::handleNextActionForSetupIntent
        assertNotNull(handleNextActionForSetupIntentMethod)

        val authenticatePaymentMethod: suspend PaymentAuthenticator.(Any, String) -> AuthenticationResult =
            PaymentAuthenticator::authenticatePayment
        assertNotNull(authenticatePaymentMethod)

        val authenticateSetupIntentMethod: suspend PaymentAuthenticator.(Any, String) -> AuthenticationResult =
            PaymentAuthenticator::authenticateSetupIntent
        assertNotNull(authenticateSetupIntentMethod)

        val handleChallengeMethod: suspend PaymentAuthenticator.(Any, ThreeDSecureChallenge) -> Stripe3ds2AuthenticationResponse =
            PaymentAuthenticator::handleChallenge
        assertNotNull(handleChallengeMethod)
    }

    @Test
    fun verifySecretStringSignatures() {
        // Verify SecretString.wrap exists
        val wrapMethod: (String) -> SecretString = SecretString::wrap
        assertNotNull(wrapMethod)

        // Create an instance to verify instance methods
        val secret = SecretString.wrap("test")

        // Verify reveal method exists
        val revealMethod: SecretString.() -> String = SecretString::reveal
        assertNotNull(revealMethod)

        // Verify toString, equals, hashCode exist (inherited but overridden)
        assertNotNull(secret.toString())
        assertNotNull(secret.hashCode())
        assertNotNull(secret.equals(secret))
    }
}
