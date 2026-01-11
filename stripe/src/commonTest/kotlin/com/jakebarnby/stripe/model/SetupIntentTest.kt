package com.jakebarnby.stripe.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SetupIntentTest {
    @Test
    fun testSetupIntentCreation() {
        val setupIntent = SetupIntent(
            id = "seti_123",
            clientSecret = "seti_123_secret_abc",
            created = 1234567890,
            livemode = false,
            status = SetupIntentStatus.REQUIRES_PAYMENT_METHOD
        )

        assertEquals("seti_123", setupIntent.id)
        assertEquals("seti_123_secret_abc", setupIntent.clientSecret)
        assertEquals(SetupIntentStatus.REQUIRES_PAYMENT_METHOD, setupIntent.status)
    }

    @Test
    fun testSetupIntentValidation() {
        assertFailsWith<IllegalArgumentException> {
            SetupIntent(
                id = "",
                clientSecret = "seti_123_secret_abc",
                created = 1234567890,
                livemode = false,
                status = SetupIntentStatus.SUCCEEDED
            )
        }
    }

    @Test
    fun testSetupIntentStatusEnum() {
        assertEquals(SetupIntentStatus.REQUIRES_PAYMENT_METHOD, SetupIntentStatus.fromValue("requires_payment_method"))
        assertEquals(SetupIntentStatus.SUCCEEDED, SetupIntentStatus.fromValue("succeeded"))
        assertEquals(SetupIntentStatus.CANCELED, SetupIntentStatus.fromValue("canceled"))
    }

    @Test
    fun testSetupIntentUsageEnum() {
        assertEquals(SetupIntentUsage.ON_SESSION, SetupIntentUsage.fromValue("on_session"))
        assertEquals(SetupIntentUsage.OFF_SESSION, SetupIntentUsage.fromValue("off_session"))
    }
}

class ConfirmSetupIntentParamsTest {
    @Test
    fun testConfirmSetupIntentParamsWithPaymentMethodId() {
        val params = ConfirmSetupIntentParams.createWithPaymentMethodId(
            paymentMethodId = "pm_123",
            clientSecret = "seti_123_secret_abc"
        )

        assertEquals("seti_123_secret_abc", params.clientSecret)
        assertEquals("pm_123", params.paymentMethodId)
        assertNull(params.paymentMethodCreateParams)
    }

    @Test
    fun testConfirmSetupIntentParamsBuilder() {
        val params = ConfirmSetupIntentParams.builder("seti_123_secret_abc")
            .paymentMethodId("pm_123")
            .returnUrl("https://example.com/return")
            .build()

        assertEquals("seti_123_secret_abc", params.clientSecret)
        assertEquals("pm_123", params.paymentMethodId)
        assertEquals("https://example.com/return", params.returnUrl)
    }
}
