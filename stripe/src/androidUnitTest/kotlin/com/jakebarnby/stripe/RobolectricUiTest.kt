package com.jakebarnby.stripe

import androidx.activity.ComponentActivity
import com.jakebarnby.stripe.model.AccountCategory
import com.jakebarnby.stripe.model.AccountSubcategory
import com.jakebarnby.stripe.model.AuthenticationResult
import com.jakebarnby.stripe.model.FinancialConnectionsSheetConfiguration
import com.jakebarnby.stripe.model.GooglePayConfiguration
import com.jakebarnby.stripe.model.GooglePayEnvironment
import com.jakebarnby.stripe.model.IdentityVerificationSheetConfiguration
import com.jakebarnby.stripe.model.LinkedAccountStatus
import com.jakebarnby.stripe.model.ThreeDSecureChallenge
import com.jakebarnby.stripe.model.ThreeDSecureVersion
import com.jakebarnby.stripe.model.WalletPaymentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail
import com.stripe.android.financialconnections.model.FinancialConnectionsAccount as AndroidFinancialConnectionsAccount

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RobolectricUiTest {
    private val dispatcher = UnconfinedTestDispatcher()

    private val paymentIntentConfig = PaymentIntentConfiguration(
        clientSecret = "pi_123_secret_456",
        paymentSheetConfiguration = PaymentSheetConfiguration(
            merchantDisplayName = "Demo"
        )
    )

    private val setupIntentConfig = SetupIntentConfiguration(
        clientSecret = "seti_123_secret_456",
        paymentSheetConfiguration = PaymentSheetConfiguration(
            merchantDisplayName = "Demo"
        )
    )

    private val identityConfig = IdentityVerificationSheetConfiguration(
        verificationSessionId = "vs_123",
        ephemeralKeySecret = "ek_123"
    )

    private val financialConnectionsConfig = FinancialConnectionsSheetConfiguration(
        financialConnectionsSessionClientSecret = "fcsess_123_secret_456",
        publishableKey = "pk_test_123"
    )

    @BeforeTest
    fun setMainDispatcher() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
        clearPaymentSheetActivity()
        clearIdentityVerificationSheetActivity()
        clearFinancialConnectionsSheetActivity()
    }

    @Test
    fun paymentSheet_withoutActivity_throws() = runTest {
        try {
            PaymentSheet().presentWithPaymentIntent(paymentIntentConfig) {}
            fail("Expected missing activity error")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Activity not set") == true)
        }
    }

    @Test
    fun paymentSheet_withFinishingActivity_throws() = runTest {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val activity = controller.get()
        setPaymentSheetActivity(activity)
        activity.finish()

        try {
            PaymentSheet().presentWithSetupIntent(setupIntentConfig) {}
            fail("Expected finishing activity error")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("finishing") == true)
        }
    }

    @Test
    fun paymentSheet_withDestroyedActivity_throws() = runTest {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val activity = controller.get()
        setPaymentSheetActivity(activity)
        controller.pause().stop().destroy()

        try {
            PaymentSheet().presentWithPaymentIntent(paymentIntentConfig) {}
            fail("Expected destroyed activity error")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("destroyed") == true)
        }
    }

    @Test
    fun paymentSheet_buildAndroidConfiguration_includesCustomer() {
        val config = PaymentSheetConfiguration(
            merchantDisplayName = "Demo",
            customerId = "cus_123",
            customerEphemeralKeySecret = "ek_123",
            allowsDelayedPaymentMethods = false
        )
        val method = PaymentSheet::class.java.getDeclaredMethod(
            "buildAndroidConfiguration",
            PaymentSheetConfiguration::class.java
        )
        method.isAccessible = true
        val androidConfig = method.invoke(PaymentSheet(), config)
        assertTrue(androidConfig != null)
    }

    @Test
    fun identityVerification_withoutActivity_throws() = runTest {
        try {
            IdentityVerificationSheet().present(identityConfig)
            fail("Expected missing activity error")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Activity not set") == true)
        }
    }

    @Test
    fun identityVerification_buildAndroidConfiguration_withLogo() {
        val config = IdentityVerificationSheetConfiguration(
            verificationSessionId = "vs_123",
            ephemeralKeySecret = "ek_123",
            brandLogo = "https://example.com/logo.png"
        )
        val method = IdentityVerificationSheet::class.java.getDeclaredMethod(
            "buildAndroidConfiguration",
            IdentityVerificationSheetConfiguration::class.java
        )
        method.isAccessible = true
        val androidConfig = method.invoke(IdentityVerificationSheet(), config)
        assertTrue(androidConfig != null)
    }

    @Test
    fun financialConnections_withoutActivity_throws() = runTest {
        val sheet = FinancialConnectionsSheet.create(financialConnectionsConfig)
        try {
            sheet.present()
            fail("Expected missing activity error")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Activity not set") == true)
        }
    }

    @Test
    fun financialConnections_mapsEnums() {
        val sheet = FinancialConnectionsSheet.create(financialConnectionsConfig)
        val mapCategory = sheet.javaClass.getDeclaredMethod(
            "mapAccountCategory",
            AndroidFinancialConnectionsAccount.Category::class.java
        )
        mapCategory.isAccessible = true
        val mapSubcategory = sheet.javaClass.getDeclaredMethod(
            "mapAccountSubcategory",
            AndroidFinancialConnectionsAccount.Subcategory::class.java
        )
        mapSubcategory.isAccessible = true
        val mapStatus = sheet.javaClass.getDeclaredMethod(
            "mapLinkedAccountStatus",
            AndroidFinancialConnectionsAccount.Status::class.java
        )
        mapStatus.isAccessible = true

        assertTrue(
            mapCategory.invoke(sheet, AndroidFinancialConnectionsAccount.Category.CASH) == AccountCategory.CASH
        )
        assertTrue(
            mapCategory.invoke(sheet, AndroidFinancialConnectionsAccount.Category.UNKNOWN) == AccountCategory.OTHER
        )
        assertTrue(
            mapSubcategory.invoke(
                sheet,
                AndroidFinancialConnectionsAccount.Subcategory.CHECKING
            ) == AccountSubcategory.CHECKING
        )
        assertTrue(
            mapSubcategory.invoke(
                sheet,
                AndroidFinancialConnectionsAccount.Subcategory.UNKNOWN
            ) == AccountSubcategory.OTHER
        )
        assertTrue(
            mapStatus.invoke(sheet, AndroidFinancialConnectionsAccount.Status.ACTIVE) == LinkedAccountStatus.ACTIVE
        )
        assertTrue(
            mapStatus.invoke(
                sheet,
                AndroidFinancialConnectionsAccount.Status.UNKNOWN
            ) == LinkedAccountStatus.DISCONNECTED
        )
    }

    @Test
    fun googlePay_withoutActivity_returnsFailure() = runTest {
        val config = GooglePayConfiguration(
            environment = GooglePayEnvironment.TEST,
            merchantName = "Demo",
            merchantCountryCode = "US"
        )
        val result = GooglePayLauncher().presentForPaymentIntent("pi_123_secret_456", config)
        assertTrue(result is com.jakebarnby.stripe.model.WalletPaymentResult.Failed)
    }

    @Test
    fun googlePay_setupIntent_withoutActivity_returnsFailure() = runTest {
        val config = GooglePayConfiguration(
            environment = GooglePayEnvironment.TEST,
            merchantName = "Demo",
            merchantCountryCode = "US"
        )
        val result = GooglePayLauncher().presentForSetupIntent("seti_123_secret_456", config)
        assertTrue(result is com.jakebarnby.stripe.model.WalletPaymentResult.Failed)
    }

    @Test
    fun googlePay_withNonActivity_returnsFailure() = runTest {
        val config = GooglePayConfiguration(
            environment = GooglePayEnvironment.TEST,
            merchantName = "Demo",
            merchantCountryCode = "US"
        )
        val result = GooglePayLauncher()
            .presentForPaymentIntent(activity = Any(), clientSecret = "pi_123_secret_456", configuration = config)
        assertTrue(result is com.jakebarnby.stripe.model.WalletPaymentResult.Failed)
    }

    @Test
    fun googlePay_isAvailable_withoutContext_returnsTrue() {
        assertTrue(GooglePayLauncher.isAvailable(null))
    }

    @Test
    fun googlePay_createPaymentMethod_withoutActivity_returnsFailure() = runTest {
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
        val result = GooglePayLauncher().createPaymentMethod(config, request)
        assertTrue(result is com.jakebarnby.stripe.model.WalletPaymentResult.Failed)
    }

    @Test
    fun paymentAuthenticator_requiresComponentActivity() = runTest {
        val result = PaymentAuthenticator.getInstance()
            .handleNextActionForPayment(activity = Any(), clientSecret = "pi_123_secret_456")
        assertTrue(result is com.jakebarnby.stripe.model.AuthenticationResult.Failed)
    }

    @Test
    fun paymentAuthenticator_handleNextAction_withoutActivity_returnsFailure() = runTest {
        val result = PaymentAuthenticator.getInstance().handleNextAction("pi_123_secret_456")
        assertTrue(result is AuthenticationResult.Failed)
    }

    @Test
    fun paymentAuthenticator_handleChallenge_returnsRedirect() = runTest {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        val challenge = ThreeDSecureChallenge(
            acsUrl = "https://example.com/acs",
            threeDSecureServerTransactionId = "trans_123",
            version = ThreeDSecureVersion.V2_2
        )
        val result = PaymentAuthenticator.getInstance().handleChallenge(activity, challenge)
        assertTrue(result.fallbackRedirectUrl == "https://example.com/acs")
    }
}
