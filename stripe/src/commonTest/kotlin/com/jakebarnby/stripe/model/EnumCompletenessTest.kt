package com.jakebarnby.stripe.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests that verify all enum values exist and fromValue() works correctly.
 * Will fail if Stripe adds/removes enum values.
 *
 * These tests ensure backward compatibility when upgrading Stripe SDK versions.
 */
class EnumCompletenessTest {

    @Test
    fun testPaymentMethodTypeCompleteness() {
        // Verify ALL 35 payment method types exist
        val expectedTypes = listOf(
            PaymentMethodType.CARD,
            PaymentMethodType.CARD_PRESENT,
            PaymentMethodType.FPX,
            PaymentMethodType.IDEAL,
            PaymentMethodType.SEPA_DEBIT,
            PaymentMethodType.AU_BECS_DEBIT,
            PaymentMethodType.BACS_DEBIT,
            PaymentMethodType.BANCONTACT,
            PaymentMethodType.EPS,
            PaymentMethodType.GIROPAY,
            PaymentMethodType.GRABPAY,
            PaymentMethodType.KLARNA,
            PaymentMethodType.OXXO,
            PaymentMethodType.P24,
            PaymentMethodType.SOFORT,
            PaymentMethodType.ALIPAY,
            PaymentMethodType.WECHAT_PAY,
            PaymentMethodType.AFFIRM,
            PaymentMethodType.AFTERPAY_CLEARPAY,
            PaymentMethodType.AMAZON_PAY,
            PaymentMethodType.BLIK,
            PaymentMethodType.BOLETO,
            PaymentMethodType.CASHAPP,
            PaymentMethodType.CUSTOMER_BALANCE,
            PaymentMethodType.KONBINI,
            PaymentMethodType.LINK,
            PaymentMethodType.MOBILEPAY,
            PaymentMethodType.PAYPAL,
            PaymentMethodType.PROMPTPAY,
            PaymentMethodType.REVOLUT_PAY,
            PaymentMethodType.SWISH,
            PaymentMethodType.TWINT,
            PaymentMethodType.US_BANK_ACCOUNT,
            PaymentMethodType.ZIP,
            PaymentMethodType.UNKNOWN
        )

        assertEquals(35, PaymentMethodType.entries.size, "PaymentMethodType should have 35 entries")

        expectedTypes.forEach { type ->
            assertTrue(PaymentMethodType.entries.contains(type), "Missing PaymentMethodType: $type")
        }
    }

    @Test
    fun testPaymentMethodTypeFromValue() {
        // Test that fromValue works for all known types
        assertEquals(PaymentMethodType.CARD, PaymentMethodType.fromValue("card"))
        assertEquals(PaymentMethodType.CARD_PRESENT, PaymentMethodType.fromValue("card_present"))
        assertEquals(PaymentMethodType.FPX, PaymentMethodType.fromValue("fpx"))
        assertEquals(PaymentMethodType.IDEAL, PaymentMethodType.fromValue("ideal"))
        assertEquals(PaymentMethodType.SEPA_DEBIT, PaymentMethodType.fromValue("sepa_debit"))
        assertEquals(PaymentMethodType.AU_BECS_DEBIT, PaymentMethodType.fromValue("au_becs_debit"))
        assertEquals(PaymentMethodType.BACS_DEBIT, PaymentMethodType.fromValue("bacs_debit"))
        assertEquals(PaymentMethodType.BANCONTACT, PaymentMethodType.fromValue("bancontact"))
        assertEquals(PaymentMethodType.EPS, PaymentMethodType.fromValue("eps"))
        assertEquals(PaymentMethodType.GIROPAY, PaymentMethodType.fromValue("giropay"))
        assertEquals(PaymentMethodType.GRABPAY, PaymentMethodType.fromValue("grabpay"))
        assertEquals(PaymentMethodType.KLARNA, PaymentMethodType.fromValue("klarna"))
        assertEquals(PaymentMethodType.OXXO, PaymentMethodType.fromValue("oxxo"))
        assertEquals(PaymentMethodType.P24, PaymentMethodType.fromValue("p24"))
        assertEquals(PaymentMethodType.SOFORT, PaymentMethodType.fromValue("sofort"))
        assertEquals(PaymentMethodType.ALIPAY, PaymentMethodType.fromValue("alipay"))
        assertEquals(PaymentMethodType.WECHAT_PAY, PaymentMethodType.fromValue("wechat_pay"))
        assertEquals(PaymentMethodType.AFFIRM, PaymentMethodType.fromValue("affirm"))
        assertEquals(PaymentMethodType.AFTERPAY_CLEARPAY, PaymentMethodType.fromValue("afterpay_clearpay"))
        assertEquals(PaymentMethodType.AMAZON_PAY, PaymentMethodType.fromValue("amazon_pay"))
        assertEquals(PaymentMethodType.BLIK, PaymentMethodType.fromValue("blik"))
        assertEquals(PaymentMethodType.BOLETO, PaymentMethodType.fromValue("boleto"))
        assertEquals(PaymentMethodType.CASHAPP, PaymentMethodType.fromValue("cashapp"))
        assertEquals(PaymentMethodType.CUSTOMER_BALANCE, PaymentMethodType.fromValue("customer_balance"))
        assertEquals(PaymentMethodType.KONBINI, PaymentMethodType.fromValue("konbini"))
        assertEquals(PaymentMethodType.LINK, PaymentMethodType.fromValue("link"))
        assertEquals(PaymentMethodType.MOBILEPAY, PaymentMethodType.fromValue("mobilepay"))
        assertEquals(PaymentMethodType.PAYPAL, PaymentMethodType.fromValue("paypal"))
        assertEquals(PaymentMethodType.PROMPTPAY, PaymentMethodType.fromValue("promptpay"))
        assertEquals(PaymentMethodType.REVOLUT_PAY, PaymentMethodType.fromValue("revolut_pay"))
        assertEquals(PaymentMethodType.SWISH, PaymentMethodType.fromValue("swish"))
        assertEquals(PaymentMethodType.TWINT, PaymentMethodType.fromValue("twint"))
        assertEquals(PaymentMethodType.US_BANK_ACCOUNT, PaymentMethodType.fromValue("us_bank_account"))
        assertEquals(PaymentMethodType.ZIP, PaymentMethodType.fromValue("zip"))
        assertEquals(PaymentMethodType.UNKNOWN, PaymentMethodType.fromValue("unknown"))
        assertEquals(PaymentMethodType.UNKNOWN, PaymentMethodType.fromValue("nonexistent_type"))
    }

    @Test
    fun testCardBrandCompleteness() {
        // Verify all card brands exist
        val expectedBrands = listOf(
            CardBrand.VISA,
            CardBrand.MASTERCARD,
            CardBrand.AMERICAN_EXPRESS,
            CardBrand.DISCOVER,
            CardBrand.JCB,
            CardBrand.DINERS_CLUB,
            CardBrand.UNION_PAY,
            CardBrand.UNKNOWN
        )

        assertEquals(8, CardBrand.entries.size, "CardBrand should have 8 entries")

        expectedBrands.forEach { brand ->
            assertTrue(CardBrand.entries.contains(brand), "Missing CardBrand: $brand")
        }
    }

    @Test
    fun testCardBrandFromValue() {
        assertEquals(CardBrand.VISA, CardBrand.fromValue("visa"))
        assertEquals(CardBrand.MASTERCARD, CardBrand.fromValue("mastercard"))
        assertEquals(CardBrand.AMERICAN_EXPRESS, CardBrand.fromValue("amex"))
        assertEquals(CardBrand.DISCOVER, CardBrand.fromValue("discover"))
        assertEquals(CardBrand.JCB, CardBrand.fromValue("jcb"))
        assertEquals(CardBrand.DINERS_CLUB, CardBrand.fromValue("diners"))
        assertEquals(CardBrand.UNION_PAY, CardBrand.fromValue("unionpay"))
        assertEquals(CardBrand.UNKNOWN, CardBrand.fromValue("unknown"))
        assertEquals(CardBrand.UNKNOWN, CardBrand.fromValue("invalid_brand"))
    }

    @Test
    fun testCardFundingCompleteness() {
        val expectedFunding = listOf(
            CardFunding.CREDIT,
            CardFunding.DEBIT,
            CardFunding.PREPAID,
            CardFunding.UNKNOWN
        )

        assertEquals(4, CardFunding.entries.size, "CardFunding should have 4 entries")

        expectedFunding.forEach { funding ->
            assertTrue(CardFunding.entries.contains(funding), "Missing CardFunding: $funding")
        }
    }

    @Test
    fun testCardFundingFromValue() {
        assertEquals(CardFunding.CREDIT, CardFunding.fromValue("credit"))
        assertEquals(CardFunding.DEBIT, CardFunding.fromValue("debit"))
        assertEquals(CardFunding.PREPAID, CardFunding.fromValue("prepaid"))
        assertEquals(CardFunding.UNKNOWN, CardFunding.fromValue("unknown"))
        assertEquals(CardFunding.UNKNOWN, CardFunding.fromValue("invalid_funding"))
    }

    @Test
    fun testPaymentIntentStatusCompleteness() {
        val expectedStatuses = listOf(
            PaymentIntentStatus.REQUIRES_PAYMENT_METHOD,
            PaymentIntentStatus.REQUIRES_CONFIRMATION,
            PaymentIntentStatus.REQUIRES_ACTION,
            PaymentIntentStatus.PROCESSING,
            PaymentIntentStatus.REQUIRES_CAPTURE,
            PaymentIntentStatus.CANCELED,
            PaymentIntentStatus.SUCCEEDED
        )

        assertEquals(7, PaymentIntentStatus.entries.size, "PaymentIntentStatus should have 7 entries")

        expectedStatuses.forEach { status ->
            assertTrue(PaymentIntentStatus.entries.contains(status), "Missing PaymentIntentStatus: $status")
        }
    }

    @Test
    fun testPaymentIntentStatusFromValue() {
        assertEquals(PaymentIntentStatus.REQUIRES_PAYMENT_METHOD, PaymentIntentStatus.fromValue("requires_payment_method"))
        assertEquals(PaymentIntentStatus.REQUIRES_CONFIRMATION, PaymentIntentStatus.fromValue("requires_confirmation"))
        assertEquals(PaymentIntentStatus.REQUIRES_ACTION, PaymentIntentStatus.fromValue("requires_action"))
        assertEquals(PaymentIntentStatus.PROCESSING, PaymentIntentStatus.fromValue("processing"))
        assertEquals(PaymentIntentStatus.REQUIRES_CAPTURE, PaymentIntentStatus.fromValue("requires_capture"))
        assertEquals(PaymentIntentStatus.CANCELED, PaymentIntentStatus.fromValue("canceled"))
        assertEquals(PaymentIntentStatus.SUCCEEDED, PaymentIntentStatus.fromValue("succeeded"))
    }

    @Test
    fun testSetupIntentStatusCompleteness() {
        val expectedStatuses = listOf(
            SetupIntentStatus.REQUIRES_PAYMENT_METHOD,
            SetupIntentStatus.REQUIRES_CONFIRMATION,
            SetupIntentStatus.REQUIRES_ACTION,
            SetupIntentStatus.PROCESSING,
            SetupIntentStatus.CANCELED,
            SetupIntentStatus.SUCCEEDED
        )

        assertEquals(6, SetupIntentStatus.entries.size, "SetupIntentStatus should have 6 entries")

        expectedStatuses.forEach { status ->
            assertTrue(SetupIntentStatus.entries.contains(status), "Missing SetupIntentStatus: $status")
        }
    }

    @Test
    fun testSetupIntentStatusFromValue() {
        assertEquals(SetupIntentStatus.REQUIRES_PAYMENT_METHOD, SetupIntentStatus.fromValue("requires_payment_method"))
        assertEquals(SetupIntentStatus.REQUIRES_CONFIRMATION, SetupIntentStatus.fromValue("requires_confirmation"))
        assertEquals(SetupIntentStatus.REQUIRES_ACTION, SetupIntentStatus.fromValue("requires_action"))
        assertEquals(SetupIntentStatus.PROCESSING, SetupIntentStatus.fromValue("processing"))
        assertEquals(SetupIntentStatus.CANCELED, SetupIntentStatus.fromValue("canceled"))
        assertEquals(SetupIntentStatus.SUCCEEDED, SetupIntentStatus.fromValue("succeeded"))
    }

    @Test
    fun testConfirmationMethodCompleteness() {
        val expectedMethods = listOf(
            ConfirmationMethod.AUTOMATIC,
            ConfirmationMethod.MANUAL
        )

        assertEquals(2, ConfirmationMethod.entries.size, "ConfirmationMethod should have 2 entries")

        expectedMethods.forEach { method ->
            assertTrue(ConfirmationMethod.entries.contains(method), "Missing ConfirmationMethod: $method")
        }
    }

    @Test
    fun testConfirmationMethodFromValue() {
        assertEquals(ConfirmationMethod.AUTOMATIC, ConfirmationMethod.fromValue("automatic"))
        assertEquals(ConfirmationMethod.MANUAL, ConfirmationMethod.fromValue("manual"))
    }

    @Test
    fun testCaptureMethodCompleteness() {
        val expectedMethods = listOf(
            CaptureMethod.AUTOMATIC,
            CaptureMethod.MANUAL
        )

        assertEquals(2, CaptureMethod.entries.size, "CaptureMethod should have 2 entries")

        expectedMethods.forEach { method ->
            assertTrue(CaptureMethod.entries.contains(method), "Missing CaptureMethod: $method")
        }
    }

    @Test
    fun testCaptureMethodFromValue() {
        assertEquals(CaptureMethod.AUTOMATIC, CaptureMethod.fromValue("automatic"))
        assertEquals(CaptureMethod.MANUAL, CaptureMethod.fromValue("manual"))
    }

    @Test
    fun testSetupFutureUsageCompleteness() {
        val expectedUsages = listOf(
            SetupFutureUsage.ON_SESSION,
            SetupFutureUsage.OFF_SESSION
        )

        assertEquals(2, SetupFutureUsage.entries.size, "SetupFutureUsage should have 2 entries")

        expectedUsages.forEach { usage ->
            assertTrue(SetupFutureUsage.entries.contains(usage), "Missing SetupFutureUsage: $usage")
        }
    }

    @Test
    fun testSetupFutureUsageFromValue() {
        assertEquals(SetupFutureUsage.ON_SESSION, SetupFutureUsage.fromValue("on_session"))
        assertEquals(SetupFutureUsage.OFF_SESSION, SetupFutureUsage.fromValue("off_session"))
    }

    @Test
    fun testSetupIntentUsageCompleteness() {
        val expectedUsages = listOf(
            SetupIntentUsage.ON_SESSION,
            SetupIntentUsage.OFF_SESSION
        )

        assertEquals(2, SetupIntentUsage.entries.size, "SetupIntentUsage should have 2 entries")

        expectedUsages.forEach { usage ->
            assertTrue(SetupIntentUsage.entries.contains(usage), "Missing SetupIntentUsage: $usage")
        }
    }

    @Test
    fun testSetupIntentUsageFromValue() {
        assertEquals(SetupIntentUsage.ON_SESSION, SetupIntentUsage.fromValue("on_session"))
        assertEquals(SetupIntentUsage.OFF_SESSION, SetupIntentUsage.fromValue("off_session"))
    }

    @Test
    fun testNextActionTypeCompleteness() {
        val expectedTypes = listOf(
            NextActionType.REDIRECT_TO_URL,
            NextActionType.USE_STRIPE_SDK,
            NextActionType.DISPLAY_OXXO_DETAILS,
            NextActionType.DISPLAY_BOLETO_DETAILS,
            NextActionType.DISPLAY_KONBINI_DETAILS,
            NextActionType.VERIFY_WITH_MICRODEPOSITS,
            NextActionType.ALIPAY_HANDLE_REDIRECT,
            NextActionType.WECHAT_PAY_DISPLAY_QR_CODE
        )

        assertEquals(8, NextActionType.entries.size, "NextActionType should have 8 entries")

        expectedTypes.forEach { type ->
            assertTrue(NextActionType.entries.contains(type), "Missing NextActionType: $type")
        }
    }

    @Test
    fun testNextActionTypeFromValue() {
        assertEquals(NextActionType.REDIRECT_TO_URL, NextActionType.fromValue("redirect_to_url"))
        assertEquals(NextActionType.USE_STRIPE_SDK, NextActionType.fromValue("use_stripe_sdk"))
        assertEquals(NextActionType.DISPLAY_OXXO_DETAILS, NextActionType.fromValue("display_oxxo_details"))
        assertEquals(NextActionType.DISPLAY_BOLETO_DETAILS, NextActionType.fromValue("display_boleto_details"))
        assertEquals(NextActionType.DISPLAY_KONBINI_DETAILS, NextActionType.fromValue("display_konbini_details"))
        assertEquals(NextActionType.VERIFY_WITH_MICRODEPOSITS, NextActionType.fromValue("verify_with_microdeposits"))
        assertEquals(NextActionType.ALIPAY_HANDLE_REDIRECT, NextActionType.fromValue("alipay_handle_redirect"))
        assertEquals(NextActionType.WECHAT_PAY_DISPLAY_QR_CODE, NextActionType.fromValue("wechat_pay_display_qr_code"))
    }

    @Test
    fun testSetupNextActionTypeCompleteness() {
        val expectedTypes = listOf(
            SetupNextActionType.REDIRECT_TO_URL,
            SetupNextActionType.USE_STRIPE_SDK,
            SetupNextActionType.VERIFY_WITH_MICRODEPOSITS
        )

        assertEquals(3, SetupNextActionType.entries.size, "SetupNextActionType should have 3 entries")

        expectedTypes.forEach { type ->
            assertTrue(SetupNextActionType.entries.contains(type), "Missing SetupNextActionType: $type")
        }
    }

    @Test
    fun testSetupNextActionTypeFromValue() {
        assertEquals(SetupNextActionType.REDIRECT_TO_URL, SetupNextActionType.fromValue("redirect_to_url"))
        assertEquals(SetupNextActionType.USE_STRIPE_SDK, SetupNextActionType.fromValue("use_stripe_sdk"))
        assertEquals(SetupNextActionType.VERIFY_WITH_MICRODEPOSITS, SetupNextActionType.fromValue("verify_with_microdeposits"))
    }

    @Test
    fun testStripeErrorCodeCompleteness() {
        // Verify critical error codes exist
        assertNotNull(StripeErrorCode.CARD_DECLINED)
        assertNotNull(StripeErrorCode.EXPIRED_CARD)
        assertNotNull(StripeErrorCode.INCORRECT_CVC)
        assertNotNull(StripeErrorCode.INCORRECT_NUMBER)
        assertNotNull(StripeErrorCode.INCORRECT_ZIP)
        assertNotNull(StripeErrorCode.INVALID_CVC)
        assertNotNull(StripeErrorCode.INVALID_EXPIRY_MONTH)
        assertNotNull(StripeErrorCode.INVALID_EXPIRY_YEAR)
        assertNotNull(StripeErrorCode.INVALID_NUMBER)
        assertNotNull(StripeErrorCode.PROCESSING_ERROR)
        assertNotNull(StripeErrorCode.INSUFFICIENT_FUNDS)
        assertNotNull(StripeErrorCode.LOST_CARD)
        assertNotNull(StripeErrorCode.STOLEN_CARD)
        assertNotNull(StripeErrorCode.FRAUDULENT)
        assertNotNull(StripeErrorCode.DO_NOT_HONOR)
        assertNotNull(StripeErrorCode.DO_NOT_TRY_AGAIN)
        assertNotNull(StripeErrorCode.GENERIC_DECLINE)
        assertNotNull(StripeErrorCode.INVALID_REQUEST_ERROR)
        assertNotNull(StripeErrorCode.MISSING)
        assertNotNull(StripeErrorCode.PARAMETER_INVALID_EMPTY)
        assertNotNull(StripeErrorCode.PARAMETER_INVALID_INTEGER)
        assertNotNull(StripeErrorCode.PARAMETER_INVALID_STRING_BLANK)
        assertNotNull(StripeErrorCode.PARAMETER_INVALID_STRING_EMPTY)
        assertNotNull(StripeErrorCode.PARAMETER_MISSING)
        assertNotNull(StripeErrorCode.PARAMETER_UNKNOWN)
        assertNotNull(StripeErrorCode.API_KEY_EXPIRED)
        assertNotNull(StripeErrorCode.AUTHENTICATION_REQUIRED)
        assertNotNull(StripeErrorCode.RATE_LIMIT)
        assertNotNull(StripeErrorCode.AMOUNT_TOO_LARGE)
        assertNotNull(StripeErrorCode.AMOUNT_TOO_SMALL)
        assertNotNull(StripeErrorCode.PAYMENT_INTENT_AUTHENTICATION_FAILURE)
        assertNotNull(StripeErrorCode.SETUP_INTENT_AUTHENTICATION_FAILURE)
        assertNotNull(StripeErrorCode.UNKNOWN)

        // Verify count (should be at least 60+ error codes)
        assertTrue(StripeErrorCode.entries.size >= 60, "StripeErrorCode should have at least 60 entries, found ${StripeErrorCode.entries.size}")
    }

    @Test
    fun testStripeErrorCodeFromCode() {
        assertEquals(StripeErrorCode.CARD_DECLINED, StripeErrorCode.fromCode("card_declined"))
        assertEquals(StripeErrorCode.EXPIRED_CARD, StripeErrorCode.fromCode("expired_card"))
        assertEquals(StripeErrorCode.INCORRECT_CVC, StripeErrorCode.fromCode("incorrect_cvc"))
        assertEquals(StripeErrorCode.PROCESSING_ERROR, StripeErrorCode.fromCode("processing_error"))
        assertEquals(StripeErrorCode.INVALID_REQUEST_ERROR, StripeErrorCode.fromCode("invalid_request_error"))
        assertEquals(StripeErrorCode.UNKNOWN, StripeErrorCode.fromCode("nonexistent_code"))
    }

    @Test
    fun testAccountHolderTypeCompleteness() {
        val expectedTypes = listOf(
            BankAccountTokenParams.AccountHolderType.INDIVIDUAL,
            BankAccountTokenParams.AccountHolderType.COMPANY
        )

        assertEquals(2, BankAccountTokenParams.AccountHolderType.entries.size, "AccountHolderType should have 2 entries")

        expectedTypes.forEach { type ->
            assertTrue(BankAccountTokenParams.AccountHolderType.entries.contains(type), "Missing AccountHolderType: $type")
        }
    }

    @Test
    fun testAccountHolderTypeFromValue() {
        assertEquals(BankAccountTokenParams.AccountHolderType.INDIVIDUAL, BankAccountTokenParams.AccountHolderType.fromValue("individual"))
        assertEquals(BankAccountTokenParams.AccountHolderType.COMPANY, BankAccountTokenParams.AccountHolderType.fromValue("company"))
    }

    @Test
    fun testBusinessTypeCompleteness() {
        val expectedTypes = listOf(
            AccountParams.BusinessType.INDIVIDUAL,
            AccountParams.BusinessType.COMPANY
        )

        assertEquals(2, AccountParams.BusinessType.entries.size, "BusinessType should have 2 entries")

        expectedTypes.forEach { type ->
            assertTrue(AccountParams.BusinessType.entries.contains(type), "Missing BusinessType: $type")
        }
    }

    @Test
    fun testBusinessTypeFromValue() {
        assertEquals(AccountParams.BusinessType.INDIVIDUAL, AccountParams.BusinessType.fromValue("individual"))
        assertEquals(AccountParams.BusinessType.COMPANY, AccountParams.BusinessType.fromValue("company"))
    }

    @Test
    fun testThreeDSecureVersionCompleteness() {
        val expectedVersions = listOf(
            ThreeDSecureVersion.V1_0,
            ThreeDSecureVersion.V2_1,
            ThreeDSecureVersion.V2_2
        )

        assertEquals(3, ThreeDSecureVersion.entries.size, "ThreeDSecureVersion should have 3 entries")

        expectedVersions.forEach { version ->
            assertTrue(ThreeDSecureVersion.entries.contains(version), "Missing ThreeDSecureVersion: $version")
        }
    }

    @Test
    fun testAuthenticationStateCompleteness() {
        val expectedStates = listOf(
            AuthenticationState.SUCCEEDED,
            AuthenticationState.FAILED,
            AuthenticationState.CHALLENGED,
            AuthenticationState.REDIRECT_REQUIRED
        )

        assertEquals(4, AuthenticationState.entries.size, "AuthenticationState should have 4 entries")

        expectedStates.forEach { state ->
            assertTrue(AuthenticationState.entries.contains(state), "Missing AuthenticationState: $state")
        }
    }
}
