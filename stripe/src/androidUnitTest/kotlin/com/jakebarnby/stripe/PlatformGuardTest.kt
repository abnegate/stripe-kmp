package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.AuthenticationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PlatformGuardTest {

    @Test
    fun stripeInitialize_requiresContext() {
        val config = StripeConfiguration(
            publishableKey = "pk_test_123",
            merchantDisplayName = "Demo Shop"
        )
        assertFailsWith<IllegalArgumentException> {
            Stripe.initialize(config)
        }
    }

    @Test
    fun stripeGetInstance_requiresInitialization() {
        assertFailsWith<IllegalArgumentException> {
            Stripe.getInstance()
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun paymentAuthenticator_requiresActivityContext() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val result = PaymentAuthenticator.getInstance().handleNextAction("pi_123_secret_456")
            assertTrue(result is AuthenticationResult.Failed)
            val error = (result as AuthenticationResult.Failed).error
            assertTrue(error.message.contains("Activity context"))
        } finally {
            Dispatchers.resetMain()
        }
    }

}
