package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.FinancialConnectionsSheetConfiguration
import com.jakebarnby.stripe.model.FinancialConnectionsSheetResult
import com.jakebarnby.stripe.model.GooglePayConfiguration
import com.jakebarnby.stripe.model.GooglePayEnvironment
import com.jakebarnby.stripe.model.IdentityVerificationSheetConfiguration
import com.jakebarnby.stripe.model.IdentityVerificationSheetResult
import com.jakebarnby.stripe.model.WalletPaymentRequest
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.w3c.dom.HTMLElement
import kotlin.js.Promise
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class JsUiTest {
    private val paymentIntentConfig = PaymentIntentConfiguration(
        clientSecret = "pi_123_secret_456",
        paymentSheetConfiguration = PaymentSheetConfiguration(
            merchantDisplayName = "JS Test"
        )
    )

    private val setupIntentConfig = SetupIntentConfiguration(
        clientSecret = "seti_123_secret_456",
        paymentSheetConfiguration = PaymentSheetConfiguration(
            merchantDisplayName = "JS Test"
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

    @AfterTest
    fun cleanupDom() {
        document.body?.innerHTML = ""
    }

    @Test
    fun paymentSheet_presentWithPaymentIntent_completesOnSubmit() = runTest {
        installStripeInstance(createStripeElementsStub())

        var result: PaymentSheetResult? = null
        val job = async {
            PaymentSheet().presentWithPaymentIntent(paymentIntentConfig) { result = it }
        }

        awaitAndDispatchEvent("form")
        job.await()

        assertTrue(result is PaymentSheetResult.Completed)
    }

    @Test
    fun paymentSheet_presentWithSetupIntent_returnsFailedOnError() = runTest {
        installStripeInstance(createStripeElementsStub(confirmSetupError = stripeError("setup_failed", "setup_error")))

        var result: PaymentSheetResult? = null
        val job = async {
            PaymentSheet().presentWithSetupIntent(setupIntentConfig) { result = it }
        }

        awaitAndDispatchEvent("form")
        job.await()

        assertTrue(result is PaymentSheetResult.Failed)
        assertEquals("setup_error", (result as PaymentSheetResult.Failed).error.code)
    }

    @Test
    fun paymentSheet_presentWithPaymentIntent_cancelsOnCancelButton() = runTest {
        installStripeInstance(createStripeElementsStub())

        var result: PaymentSheetResult? = null
        val job = async {
            PaymentSheet().presentWithPaymentIntent(paymentIntentConfig) { result = it }
        }

        awaitAndDispatchEvent("button[aria-label='Cancel payment']", useMouseEvent = true)
        job.await()

        assertTrue(result is PaymentSheetResult.Canceled)
    }

    @Test
    fun identityVerification_present_returnsFailed() = runTest {
        val result = IdentityVerificationSheet().present(identityConfig)
        assertTrue(result is IdentityVerificationSheetResult.Failed)
    }

    @Test
    fun identityVerification_openPopup_usesWindowOpen() {
        val marker = js("{ opened: true }")
        window.asDynamic().open = { _: String, _: String, _: String -> marker }
        val result = IdentityVerificationSheet.openVerificationPopup("https://example.com/verify")
        assertTrue(result == marker)
    }

    @Test
    fun financialConnections_present_mapsSession() = runTest {
        val session = createFinancialConnectionsSessionStub()
        installStripeInstance(createStripeFinancialConnectionsStub(session))

        val result = FinancialConnectionsSheet.create(financialConfig).present()
        assertTrue(result is FinancialConnectionsSheetResult.Completed, "Expected Completed but got $result")

        val completed = result as FinancialConnectionsSheetResult.Completed
        assertEquals("fcsess_123", completed.session.id)
        assertTrue(
            completed.session.linkedAccounts.isNotEmpty(),
            "Expected linked accounts but got ${completed.session.linkedAccounts}"
        )
    }

    @Test
    fun financialConnections_presentForToken_returnsFailed() = runTest {
        val session = createFinancialConnectionsSessionStub()
        installStripeInstance(createStripeFinancialConnectionsStub(session))

        val result = FinancialConnectionsSheet.create(financialConfig).presentForToken()
        assertTrue(result is FinancialConnectionsSheetForTokenResult.Failed)
    }

    @Test
    fun googlePay_presentForPaymentIntent_success() = runTest {
        val launcher = GooglePayLauncher()
        launcher.setStripeInstance(createStripePaymentRequestStub())

        val config = GooglePayConfiguration(
            environment = GooglePayEnvironment.TEST,
            merchantName = "Demo",
            merchantCountryCode = "US"
        )

        val result = launcher.presentForPaymentIntent("pi_123_secret_456", config)
        assertTrue(result is com.jakebarnby.stripe.model.WalletPaymentResult.Success)
    }

    @Test
    fun googlePay_createPaymentMethod_success() = runTest {
        val launcher = GooglePayLauncher()
        launcher.setStripeInstance(createStripePaymentRequestStub())

        val config = GooglePayConfiguration(
            environment = GooglePayEnvironment.TEST,
            merchantName = "Demo",
            merchantCountryCode = "US"
        )
        val request = WalletPaymentRequest(
            amount = 1000,
            currencyCode = "USD",
            label = "Demo",
            countryCode = "US"
        )

        val result = launcher.createPaymentMethod(config, request)
        assertTrue(result is com.jakebarnby.stripe.model.WalletPaymentResult.Success)
    }

    @Test
    fun googlePay_presentForSetupIntent_returnsFailed() = runTest {
        val launcher = GooglePayLauncher()
        launcher.setStripeInstance(createStripePaymentRequestStub())

        val config = GooglePayConfiguration(
            environment = GooglePayEnvironment.TEST,
            merchantName = "Demo",
            merchantCountryCode = "US"
        )

        val result = launcher.presentForSetupIntent("seti_123_secret_456", config)
        assertTrue(result is com.jakebarnby.stripe.model.WalletPaymentResult.Failed)
    }

    private suspend fun awaitAndDispatchEvent(selector: String, useMouseEvent: Boolean = false) {
        val element = awaitElement(selector)
        val event = if (useMouseEvent) {
            js("new MouseEvent('click', { bubbles: true, cancelable: true })")
        } else {
            js("new Event('submit', { bubbles: true, cancelable: true })")
        }
        element.dispatchEvent(event)
    }

    private suspend fun awaitElement(selector: String): HTMLElement {
        repeat(50) {
            val element = document.querySelector(selector) as? HTMLElement
            if (element != null) {
                return element
            }
            yield()
        }
        throw AssertionError("Element not found for selector: $selector")
    }

    private fun installStripeInstance(stub: dynamic?) {
        val stripe = Stripe.initialize(
            StripeConfiguration(
                publishableKey = "pk_test_123",
                merchantDisplayName = "JS Test"
            )
        )
        stripe.stripeInstance = stub?.unsafeCast<StripeInstance>()
    }

    private fun stripeError(message: String, code: String? = null): dynamic {
        val error = js("{}")
        error.message = message
        error.code = code
        return error
    }

    private fun createStripeElementsStub(
        confirmPaymentError: dynamic? = null,
        confirmSetupError: dynamic? = null
    ): dynamic {
        val stripe = js("{}")
        stripe.elements = { _: dynamic ->
            val elements = js("{}")
            elements.create = { _: String, _: dynamic ->
                val element = js("{}")
                element.mount = { _: dynamic -> }
                element.unmount = { }
                element.destroy = { }
                element.on = { _: String, _: dynamic -> }
                element
            }
            elements
        }
        stripe.confirmPayment = { _: dynamic ->
            val result = js("{}")
            result.error = confirmPaymentError
            result.paymentIntent = js("{}")
            Promise.resolve<dynamic>(result)
        }
        stripe.confirmSetup = { _: dynamic ->
            val result = js("{}")
            result.error = confirmSetupError
            result.setupIntent = js("{}")
            Promise.resolve<dynamic>(result)
        }
        return stripe
    }

    private fun createStripeFinancialConnectionsStub(session: dynamic): dynamic {
        val stripe = js("{}")
        stripe.collectBankAccountToken = { _: dynamic ->
            val result = js("{}")
            result.error = null
            result.financialConnectionsSession = session
            Promise.resolve<dynamic>(result)
        }
        return stripe
    }

    private fun createStripePaymentRequestStub(): StripeInstance {
        val stripe = js("{}")
        stripe.confirmCardPayment = { _: String, _: dynamic ->
            val result = js("{}")
            result.error = null
            Promise.resolve<dynamic>(result)
        }
        stripe.paymentRequest = { _: dynamic ->
            val request = js("{}")
            var handler: ((dynamic) -> Unit)? = null
            request.canMakePayment = {
                Promise.resolve<dynamic>(js("{ googlePay: true, applePay: false }"))
            }
            request.on = { event: String, callback: dynamic ->
                if (event == "paymentmethod") {
                    handler = callback
                }
            }
            request.show = {
                val event = js("{}")
                event.paymentMethod = js("{}")
                event.paymentMethod.id = "pm_123"
                event.complete = { _: String -> }
                handler?.invoke(event)
                Promise.resolve<dynamic>(Unit)
            }
            request
        }
        return stripe.unsafeCast<StripeInstance>()
    }

    private fun createFinancialConnectionsSessionStub(): dynamic {
        val account = js("{}")
        account.id = "fca_123"
        account.institution_name = "Bank"
        account.display_name = "Checking"
        account.last4 = "6789"
        account.created = 1700000000
        account.category = "cash"
        account.subcategory = "checking"
        account.status = "active"
        account.livemode = false
        account.supported_payment_method_types = arrayOf("us_bank_account")
        val session = js("{}")
        session.id = "fcsess_123"
        session.client_secret = "fcsess_123_secret_456"
        session.livemode = false
        session.return_url = "https://example.com/return"
        session.accounts = arrayOf(account)
        return session
    }
}
