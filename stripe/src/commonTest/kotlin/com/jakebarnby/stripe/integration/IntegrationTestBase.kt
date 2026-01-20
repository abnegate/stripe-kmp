package com.jakebarnby.stripe.integration

import com.jakebarnby.stripe.Stripe
import com.jakebarnby.stripe.StripeConfiguration
import com.jakebarnby.stripe.StripeResult
import kotlin.test.BeforeTest
import kotlin.test.fail

/**
 * Base class for integration tests that interact with the Stripe API.
 *
 * Tests extending this class will automatically skip if no real API key is configured,
 * allowing them to run in CI without requiring credentials.
 */
public abstract class IntegrationTestBase {

    protected lateinit var stripe: Stripe

    @BeforeTest
    fun setUp() {
        if (TestConfiguration.shouldRunIntegrationTests) {
            val config = StripeConfiguration(
                publishableKey = TestConfiguration.publishableKey,
                merchantDisplayName = "Test Merchant",
                enableLogging = true
            )
            stripe = Stripe.initialize(config)
        }
    }

    /**
     * Skip the test if no API key is configured.
     * Call this at the start of tests that require real API access.
     */
    protected fun skipIfNoApiKey() {
        if (!TestConfiguration.shouldRunIntegrationTests) {
            println("⊘ Skipping integration test - no API key configured")
            println("  Set STRIPE_PUBLISHABLE_KEY environment variable to run integration tests")
        }
    }

    /**
     * Assert that a StripeResult is Success, providing detailed error information if it fails.
     * Returns the unwrapped value for convenient use.
     */
    protected fun <T> assertSuccess(result: StripeResult<T>, message: String = "Expected success"): T {
        return when (result) {
            is StripeResult.Success -> result.value
            is StripeResult.Failure -> fail("$message but got error: ${result.error.message}")
        }
    }
}
