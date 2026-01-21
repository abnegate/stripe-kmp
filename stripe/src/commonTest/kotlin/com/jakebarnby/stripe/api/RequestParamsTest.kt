package com.jakebarnby.stripe.api

import com.jakebarnby.stripe.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

/**
 * Tests for request parameter building and validation.
 * Ensures that all request params are correctly constructed.
 */
class RequestParamsTest {


    @Test
    fun testCardParams_creation_withRequiredFields() {
        val params = CardParams(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123"
        )

        assertEquals("4242424242424242", params.number)
        assertEquals(12, params.expMonth)
        assertEquals(2025, params.expYear)
        assertEquals("123", params.cvc)
    }

    @Test
    fun testCardParams_creation_withAllOptionalFields() {
        val params = CardParams(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123",
            name = "John Doe",
            addressLine1 = "123 Main St",
            addressLine2 = "Apt 4",
            addressCity = "San Francisco",
            addressState = "CA",
            addressZip = "94111",
            addressCountry = "US"
        )

        assertEquals("John Doe", params.name)
        assertEquals("123 Main St", params.addressLine1)
        assertEquals("Apt 4", params.addressLine2)
        assertEquals("San Francisco", params.addressCity)
        assertEquals("CA", params.addressState)
        assertEquals("94111", params.addressZip)
        assertEquals("US", params.addressCountry)
    }

    @Test
    fun testCardParams_sanitizesCardNumber_withSpaces() {
        val params = CardParams(
            number = "4242 4242 4242 4242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123"
        )
        assertEquals("4242424242424242", params.getSanitizedNumber())
    }

    @Test
    fun testCardParams_sanitizesCardNumber_withDashes() {
        val params = CardParams(
            number = "4242-4242-4242-4242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123"
        )
        assertEquals("4242424242424242", params.getSanitizedNumber())
    }

    @Test
    fun testCardParams_builder_createsValidParams() {
        val params = CardParams.builder()
            .number("4242424242424242")
            .expMonth(12)
            .expYear(2025)
            .cvc("123")
            .name("Jane Doe")
            .addressLine1("456 Oak Ave")
            .addressCity("Los Angeles")
            .addressState("CA")
            .addressZip("90001")
            .addressCountry("US")
            .build()

        assertEquals("4242424242424242", params.number)
        assertEquals("Jane Doe", params.name)
        assertEquals("456 Oak Ave", params.addressLine1)
    }

    @Test
    fun testCardParams_validation_rejectsInvalidExpMonth_tooLow() {
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "4242424242424242",
                expMonth = 0,
                expYear = 2025,
                cvc = "123"
            )
        }
    }

    @Test
    fun testCardParams_validation_rejectsInvalidExpMonth_tooHigh() {
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "4242424242424242",
                expMonth = 13,
                expYear = 2025,
                cvc = "123"
            )
        }
    }

    @Test
    fun testCardParams_validation_rejectsShortCvc() {
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "4242424242424242",
                expMonth = 12,
                expYear = 2025,
                cvc = "12"
            )
        }
    }

    @Test
    fun testCardParams_validation_rejectsLongCvc() {
        assertFailsWith<IllegalArgumentException> {
            CardParams(
                number = "4242424242424242",
                expMonth = 12,
                expYear = 2025,
                cvc = "12345"
            )
        }
    }

    @Test
    fun testCardParams_acceptsAmexCvc() {
        // AMEX cards have 4-digit CVCs
        val params = CardParams(
            number = "378282246310005", // AMEX test card
            expMonth = 12,
            expYear = 2025,
            cvc = "1234"
        )
        assertEquals("1234", params.cvc)
    }


    @Test
    fun testBankAccountTokenParams_creation_withRequiredFields() {
        val params = BankAccountTokenParams(
            country = "US",
            currency = "usd",
            accountNumber = "000123456789"
        )

        assertEquals("US", params.country)
        assertEquals("usd", params.currency)
        assertEquals("000123456789", params.accountNumber)
    }

    @Test
    fun testBankAccountTokenParams_creation_withAllFields() {
        val params = BankAccountTokenParams(
            country = "US",
            currency = "usd",
            accountNumber = "000123456789",
            routingNumber = "110000000",
            accountHolderName = "John Doe",
            accountHolderType = BankAccountTokenParams.AccountHolderType.INDIVIDUAL
        )

        assertEquals("110000000", params.routingNumber)
        assertEquals("John Doe", params.accountHolderName)
        assertEquals(BankAccountTokenParams.AccountHolderType.INDIVIDUAL, params.accountHolderType)
    }

    @Test
    fun testBankAccountTokenParams_validation_rejectsInvalidCountryCode() {
        assertFailsWith<IllegalArgumentException> {
            BankAccountTokenParams(
                country = "USA", // Should be 2-letter
                currency = "usd",
                accountNumber = "000123456789"
            )
        }
    }

    @Test
    fun testBankAccountTokenParams_validation_rejectsInvalidCurrencyCode() {
        assertFailsWith<IllegalArgumentException> {
            BankAccountTokenParams(
                country = "US",
                currency = "dollar", // Should be 3-letter
                accountNumber = "000123456789"
            )
        }
    }

    @Test
    fun testAccountHolderType_values() {
        assertEquals("individual", BankAccountTokenParams.AccountHolderType.INDIVIDUAL.value)
        assertEquals("company", BankAccountTokenParams.AccountHolderType.COMPANY.value)
    }


    @Test
    fun testPiiTokenParams_creation() {
        val params = PiiTokenParams(personalIdNumber = "000000000")
        assertEquals("000000000", params.personalIdNumber)
    }


    @Test
    fun testPaymentMethodCreateParams_createCard_withAllFields() {
        val params = PaymentMethodCreateParams.createCard(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123",
            billingDetails = BillingDetails(
                name = "John Doe",
                email = "john@example.com"
            )
        )

        assertEquals(PaymentMethodType.CARD, params.type)
        assertNotNull(params.card)
        assertEquals("4242424242424242", params.card?.number)
        assertEquals(12, params.card?.expMonth)
        assertEquals(2025, params.card?.expYear)
        assertEquals("123", params.card?.cvc)
        assertNotNull(params.billingDetails)
        assertEquals("John Doe", params.billingDetails?.name)
    }

    @Test
    fun testPaymentMethodCreateParams_createCardFromToken() {
        val params = PaymentMethodCreateParams.createCardFromToken(
            token = "tok_visa",
            billingDetails = BillingDetails(name = "Jane Doe")
        )

        assertEquals(PaymentMethodType.CARD, params.type)
        assertNotNull(params.card)
        assertEquals("tok_visa", params.card?.token)
        assertNull(params.card?.number)
    }

    @Test
    fun testPaymentMethodCreateParams_createIdeal() {
        val params = PaymentMethodCreateParams.createIdeal(
            bank = "abn_amro",
            billingDetails = BillingDetails(name = "Dutch Customer")
        )

        assertEquals(PaymentMethodType.IDEAL, params.type)
        assertNotNull(params.billingDetails)
    }

    @Test
    fun testPaymentMethodCreateParams_createSepaDebit() {
        val params = PaymentMethodCreateParams.createSepaDebit(
            iban = "DE89370400440532013000",
            billingDetails = BillingDetails(
                name = "German Customer",
                email = "customer@example.de"
            )
        )

        assertEquals(PaymentMethodType.SEPA_DEBIT, params.type)
        assertNotNull(params.billingDetails)
    }


    @Test
    fun testSourceParams_createCardParams() {
        val cardParams = CardParams(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123"
        )
        val params = SourceParams.createCardParams(cardParams)

        assertEquals(SourceType.CARD, params.type)
    }

    @Test
    fun testSourceParams_createBancontactParams() {
        val params = SourceParams.createBancontactParams(
            amount = 1000,
            name = "Belgian Customer",
            returnUrl = "https://example.com/return"
        )

        assertEquals(SourceType.BANCONTACT, params.type)
        assertEquals(1000L, params.amount)
        assertEquals("eur", params.currency)
        assertEquals("Belgian Customer", params.owner?.name)
    }

    @Test
    fun testSourceParams_createIdealParams() {
        val params = SourceParams.createIdealParams(
            amount = 2000,
            name = "Dutch Customer",
            returnUrl = "https://example.com/return",
            bank = "abn_amro"
        )

        assertEquals(SourceType.IDEAL, params.type)
        assertEquals(2000L, params.amount)
        assertEquals("eur", params.currency)
    }

    @Test
    fun testSourceParams_createGiropayParams() {
        val params = SourceParams.createGiropayParams(
            amount = 3000,
            name = "German Customer",
            returnUrl = "https://example.com/return"
        )

        assertEquals(SourceType.GIROPAY, params.type)
        assertEquals(3000L, params.amount)
        assertEquals("eur", params.currency)
    }

    @Test
    fun testSourceParams_createSofortParams() {
        val params = SourceParams.createSofortParams(
            amount = 4000,
            returnUrl = "https://example.com/return",
            country = "DE"
        )

        assertEquals(SourceType.SOFORT, params.type)
        assertEquals(4000L, params.amount)
        assertEquals("eur", params.currency)
    }

    @Test
    fun testSourceParams_createSepaDebitParams() {
        val params = SourceParams.createSepaDebitParams(
            name = "European Customer",
            iban = "DE89370400440532013000",
            email = "customer@example.eu"
        )

        assertEquals(SourceType.SEPA_DEBIT, params.type)
        assertEquals("European Customer", params.owner?.name)
        assertEquals("customer@example.eu", params.owner?.email)
    }


    @Test
    fun testConfirmPaymentIntentParams_createWithPaymentMethodId() {
        val params = ConfirmPaymentIntentParams.createWithPaymentMethodId(
            paymentMethodId = "pm_test_123",
            clientSecret = "pi_test_secret_456"
        )

        assertEquals("pm_test_123", params.paymentMethodId)
        assertEquals("pi_test_secret_456", params.clientSecret)
        assertNull(params.paymentMethodCreateParams)
    }

    @Test
    fun testConfirmPaymentIntentParams_createWithPaymentMethodCreateParams() {
        val createParams = PaymentMethodCreateParams.createCard(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123"
        )

        val params = ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(
            paymentMethodCreateParams = createParams,
            clientSecret = "pi_test_secret_789"
        )

        assertEquals("pi_test_secret_789", params.clientSecret)
        assertNotNull(params.paymentMethodCreateParams)
        assertNull(params.paymentMethodId)
    }

    @Test
    fun testConfirmPaymentIntentParams_withReturnUrl() {
        val params = ConfirmPaymentIntentParams.createWithPaymentMethodId(
            paymentMethodId = "pm_test_123",
            clientSecret = "pi_test_secret_456",
            returnUrl = "https://example.com/return"
        )

        assertEquals("https://example.com/return", params.returnUrl)
    }

    @Test
    fun testConfirmPaymentIntentParams_withShipping() {
        val shipping = ShippingDetails(
            name = "John Doe",
            address = Address(
                line1 = "123 Main St",
                city = "San Francisco",
                state = "CA",
                postalCode = "94111",
                country = "US"
            ),
            carrier = "USPS",
            phone = "+1234567890",
            trackingNumber = "1Z999AA10123456784"
        )

        val params = ConfirmPaymentIntentParams.createWithPaymentMethodId(
            paymentMethodId = "pm_test_123",
            clientSecret = "pi_test_secret_456",
            shipping = shipping
        )

        assertNotNull(params.shipping)
        assertEquals("John Doe", params.shipping?.name)
        assertEquals("USPS", params.shipping?.carrier)
    }


    @Test
    fun testConfirmSetupIntentParams_createWithPaymentMethodId() {
        val params = ConfirmSetupIntentParams.createWithPaymentMethodId(
            paymentMethodId = "pm_test_123",
            clientSecret = "seti_test_secret_456"
        )

        assertEquals("pm_test_123", params.paymentMethodId)
        assertEquals("seti_test_secret_456", params.clientSecret)
    }

    @Test
    fun testConfirmSetupIntentParams_createWithPaymentMethodCreateParams() {
        val createParams = PaymentMethodCreateParams.createCard(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123"
        )

        val params = ConfirmSetupIntentParams.createWithPaymentMethodCreateParams(
            paymentMethodCreateParams = createParams,
            clientSecret = "seti_test_secret_789"
        )

        assertNotNull(params.paymentMethodCreateParams)
        assertEquals("seti_test_secret_789", params.clientSecret)
    }


    @Test
    fun testBillingDetails_validation_acceptsValidEmail() {
        val billingDetails = BillingDetails(
            name = "Test User",
            email = "test@example.com"
        )
        assertEquals("test@example.com", billingDetails.email)
    }

    @Test
    fun testBillingDetails_validation_rejectsInvalidEmail() {
        assertFailsWith<IllegalArgumentException> {
            BillingDetails(email = "not-an-email")
        }
    }

    @Test
    fun testBillingDetails_validation_acceptsNullEmail() {
        val billingDetails = BillingDetails(name = "Test User", email = null)
        assertNull(billingDetails.email)
    }

    @Test
    fun testBillingDetails_builder_createsValidDetails() {
        val billingDetails = BillingDetails.builder()
            .name("Jane Doe")
            .email("jane@example.com")
            .phone("+1234567890")
            .address(
                Address.builder()
                    .line1("123 Main St")
                    .city("San Francisco")
                    .state("CA")
                    .postalCode("94111")
                    .country("US")
                    .build()
            )
            .build()

        assertEquals("Jane Doe", billingDetails.name)
        assertEquals("jane@example.com", billingDetails.email)
        assertNotNull(billingDetails.address)
    }


    @Test
    fun testAddress_validation_acceptsValidCountryCode() {
        val address = Address(
            line1 = "123 Main St",
            country = "US"
        )
        assertEquals("US", address.country)
    }

    @Test
    fun testAddress_validation_rejectsInvalidCountryCode() {
        assertFailsWith<IllegalArgumentException> {
            Address(
                line1 = "123 Main St",
                country = "USA" // Should be 2-letter
            )
        }
    }

    @Test
    fun testAddress_builder_createsValidAddress() {
        val address = Address.builder()
            .line1("456 Oak Ave")
            .line2("Apt 4B")
            .city("Los Angeles")
            .state("CA")
            .postalCode("90001")
            .country("US")
            .build()

        assertEquals("456 Oak Ave", address.line1)
        assertEquals("Apt 4B", address.line2)
        assertEquals("Los Angeles", address.city)
    }
}
