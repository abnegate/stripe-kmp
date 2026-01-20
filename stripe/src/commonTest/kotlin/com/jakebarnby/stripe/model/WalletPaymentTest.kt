package com.jakebarnby.stripe.model

import kotlin.test.*

class WalletPaymentTest {


    @Test
    fun testGooglePayConfiguration_validConfiguration() {
        val config = GooglePayConfiguration(
            environment = GooglePayEnvironment.TEST,
            merchantName = "Test Merchant",
            merchantCountryCode = "US"
        )

        assertEquals(GooglePayEnvironment.TEST, config.environment)
        assertEquals("Test Merchant", config.merchantName)
        assertEquals("US", config.merchantCountryCode)
        assertTrue(config.allowedCardNetworks.isNotEmpty())
        assertTrue(config.allowedCardAuthMethods.isNotEmpty())
        assertFalse(config.billingAddressRequired)
    }

    @Test
    fun testGooglePayConfiguration_customNetworks() {
        val config = GooglePayConfiguration(
            environment = GooglePayEnvironment.PRODUCTION,
            merchantName = "Test Merchant",
            merchantCountryCode = "US",
            allowedCardNetworks = listOf(CardBrand.VISA, CardBrand.MASTERCARD),
            allowedCardAuthMethods = listOf(CardAuthMethod.CRYPTOGRAM_3DS)
        )

        assertEquals(2, config.allowedCardNetworks.size)
        assertTrue(config.allowedCardNetworks.contains(CardBrand.VISA))
        assertTrue(config.allowedCardNetworks.contains(CardBrand.MASTERCARD))
        assertEquals(1, config.allowedCardAuthMethods.size)
        assertEquals(CardAuthMethod.CRYPTOGRAM_3DS, config.allowedCardAuthMethods.first())
    }

    @Test
    fun testGooglePayConfiguration_withRequirements() {
        val config = GooglePayConfiguration(
            environment = GooglePayEnvironment.TEST,
            merchantName = "Test Merchant",
            merchantCountryCode = "US",
            billingAddressRequired = true,
            shippingAddressRequired = true,
            emailRequired = true
        )

        assertTrue(config.billingAddressRequired)
        assertTrue(config.shippingAddressRequired)
        assertTrue(config.emailRequired)
    }

    @Test
    fun testGooglePayConfiguration_blankMerchantName_throwsException() {
        assertFailsWith<IllegalArgumentException> {
            GooglePayConfiguration(
                environment = GooglePayEnvironment.TEST,
                merchantName = "",
                merchantCountryCode = "US"
            )
        }
    }

    @Test
    fun testGooglePayConfiguration_invalidCountryCode_throwsException() {
        assertFailsWith<IllegalArgumentException> {
            GooglePayConfiguration(
                environment = GooglePayEnvironment.TEST,
                merchantName = "Test",
                merchantCountryCode = "USA"
            )
        }
    }

    @Test
    fun testGooglePayConfiguration_emptyNetworks_throwsException() {
        assertFailsWith<IllegalArgumentException> {
            GooglePayConfiguration(
                environment = GooglePayEnvironment.TEST,
                merchantName = "Test",
                merchantCountryCode = "US",
                allowedCardNetworks = emptyList()
            )
        }
    }

    @Test
    fun testGooglePayConfiguration_builder() {
        val config = GooglePayConfiguration.builder()
            .environment(GooglePayEnvironment.PRODUCTION)
            .merchantName("Builder Merchant")
            .merchantCountryCode("GB")
            .billingAddressRequired(true)
            .emailRequired(true)
            .build()

        assertEquals(GooglePayEnvironment.PRODUCTION, config.environment)
        assertEquals("Builder Merchant", config.merchantName)
        assertEquals("GB", config.merchantCountryCode)
        assertTrue(config.billingAddressRequired)
        assertTrue(config.emailRequired)
    }


    @Test
    fun testApplePayConfiguration_validConfiguration() {
        val config = ApplePayConfiguration(
            merchantIdentifier = "merchant.com.example",
            merchantCountryCode = "US",
            currencyCode = "USD"
        )

        assertEquals("merchant.com.example", config.merchantIdentifier)
        assertEquals("US", config.merchantCountryCode)
        assertEquals("USD", config.currencyCode)
        assertTrue(config.supportedNetworks.isNotEmpty())
        assertTrue(config.merchantCapabilities.isNotEmpty())
    }

    @Test
    fun testApplePayConfiguration_customNetworksAndCapabilities() {
        val config = ApplePayConfiguration(
            merchantIdentifier = "merchant.com.example",
            merchantCountryCode = "US",
            currencyCode = "USD",
            supportedNetworks = listOf(CardBrand.VISA, CardBrand.AMERICAN_EXPRESS),
            merchantCapabilities = listOf(
                ApplePayMerchantCapability.CAPABILITY_3DS,
                ApplePayMerchantCapability.CAPABILITY_CREDIT
            )
        )

        assertEquals(2, config.supportedNetworks.size)
        assertTrue(config.supportedNetworks.contains(CardBrand.VISA))
        assertTrue(config.supportedNetworks.contains(CardBrand.AMERICAN_EXPRESS))
        assertEquals(2, config.merchantCapabilities.size)
    }

    @Test
    fun testApplePayConfiguration_withContactFields() {
        val config = ApplePayConfiguration(
            merchantIdentifier = "merchant.com.example",
            merchantCountryCode = "US",
            currencyCode = "USD",
            requiredBillingContactFields = listOf(ApplePayContactField.NAME, ApplePayContactField.EMAIL),
            requiredShippingContactFields = listOf(ApplePayContactField.POSTAL_ADDRESS)
        )

        assertEquals(2, config.requiredBillingContactFields.size)
        assertTrue(config.requiredBillingContactFields.contains(ApplePayContactField.NAME))
        assertTrue(config.requiredBillingContactFields.contains(ApplePayContactField.EMAIL))
        assertEquals(1, config.requiredShippingContactFields.size)
        assertTrue(config.requiredShippingContactFields.contains(ApplePayContactField.POSTAL_ADDRESS))
    }

    @Test
    fun testApplePayConfiguration_blankMerchantId_throwsException() {
        assertFailsWith<IllegalArgumentException> {
            ApplePayConfiguration(
                merchantIdentifier = "",
                merchantCountryCode = "US",
                currencyCode = "USD"
            )
        }
    }

    @Test
    fun testApplePayConfiguration_invalidCountryCode_throwsException() {
        assertFailsWith<IllegalArgumentException> {
            ApplePayConfiguration(
                merchantIdentifier = "merchant.com.example",
                merchantCountryCode = "USA",
                currencyCode = "USD"
            )
        }
    }

    @Test
    fun testApplePayConfiguration_invalidCurrencyCode_throwsException() {
        assertFailsWith<IllegalArgumentException> {
            ApplePayConfiguration(
                merchantIdentifier = "merchant.com.example",
                merchantCountryCode = "US",
                currencyCode = "US"
            )
        }
    }

    @Test
    fun testApplePayConfiguration_emptyNetworks_throwsException() {
        assertFailsWith<IllegalArgumentException> {
            ApplePayConfiguration(
                merchantIdentifier = "merchant.com.example",
                merchantCountryCode = "US",
                currencyCode = "USD",
                supportedNetworks = emptyList()
            )
        }
    }

    @Test
    fun testApplePayConfiguration_builder() {
        val config = ApplePayConfiguration.builder()
            .merchantIdentifier("merchant.com.builder")
            .merchantCountryCode("CA")
            .currencyCode("CAD")
            .supportedNetworks(listOf(CardBrand.MASTERCARD))
            .requiredBillingContactFields(listOf(ApplePayContactField.EMAIL))
            .build()

        assertEquals("merchant.com.builder", config.merchantIdentifier)
        assertEquals("CA", config.merchantCountryCode)
        assertEquals("CAD", config.currencyCode)
        assertEquals(1, config.supportedNetworks.size)
        assertEquals(1, config.requiredBillingContactFields.size)
    }


    @Test
    fun testWalletPaymentRequest_validRequest() {
        val request = WalletPaymentRequest(
            amount = 1000,
            currencyCode = "USD",
            label = "Test Purchase",
            countryCode = "US"
        )

        assertEquals(1000, request.amount)
        assertEquals("USD", request.currencyCode)
        assertEquals("Test Purchase", request.label)
        assertEquals("US", request.countryCode)
    }

    @Test
    fun testWalletPaymentRequest_zeroAmount() {
        val request = WalletPaymentRequest(
            amount = 0,
            currencyCode = "USD",
            label = "Free Item",
            countryCode = "US"
        )

        assertEquals(0, request.amount)
    }

    @Test
    fun testWalletPaymentRequest_negativeAmount_throwsException() {
        assertFailsWith<IllegalArgumentException> {
            WalletPaymentRequest(
                amount = -100,
                currencyCode = "USD",
                label = "Test",
                countryCode = "US"
            )
        }
    }

    @Test
    fun testWalletPaymentRequest_invalidCurrency_throwsException() {
        assertFailsWith<IllegalArgumentException> {
            WalletPaymentRequest(
                amount = 1000,
                currencyCode = "US",
                label = "Test",
                countryCode = "US"
            )
        }
    }

    @Test
    fun testWalletPaymentRequest_blankLabel_throwsException() {
        assertFailsWith<IllegalArgumentException> {
            WalletPaymentRequest(
                amount = 1000,
                currencyCode = "USD",
                label = "",
                countryCode = "US"
            )
        }
    }

    @Test
    fun testWalletPaymentRequest_invalidCountryCode_throwsException() {
        assertFailsWith<IllegalArgumentException> {
            WalletPaymentRequest(
                amount = 1000,
                currencyCode = "USD",
                label = "Test",
                countryCode = "USA"
            )
        }
    }

    @Test
    fun testWalletPaymentRequest_builder() {
        val request = WalletPaymentRequest.builder()
            .amount(5000)
            .currencyCode("EUR")
            .label("Builder Purchase")
            .countryCode("DE")
            .build()

        assertEquals(5000, request.amount)
        assertEquals("EUR", request.currencyCode)
        assertEquals("Builder Purchase", request.label)
        assertEquals("DE", request.countryCode)
    }


    @Test
    fun testWalletPaymentResult_success() {
        val result = WalletPaymentResult.Success(
            paymentMethodId = "pm_test_123",
            token = null
        )

        assertTrue(result is WalletPaymentResult.Success)
        assertEquals("pm_test_123", result.paymentMethodId)
        assertNull(result.token)
    }

    @Test
    fun testWalletPaymentResult_successWithToken() {
        val token = Token(
            id = "tok_test_123",
            type = "card",
            created = 1234567890,
            livemode = false,
            used = false
        )

        val result = WalletPaymentResult.Success(
            paymentMethodId = "pm_test_123",
            token = token
        )

        assertTrue(result is WalletPaymentResult.Success)
        assertEquals("pm_test_123", result.paymentMethodId)
        assertNotNull(result.token)
        assertEquals("tok_test_123", result.token?.id)
    }

    @Test
    fun testWalletPaymentResult_success_blankPaymentMethodId_throwsException() {
        assertFailsWith<IllegalArgumentException> {
            WalletPaymentResult.Success(
                paymentMethodId = "",
                token = null
            )
        }
    }

    @Test
    fun testWalletPaymentResult_canceled() {
        val result = WalletPaymentResult.Canceled

        assertTrue(result is WalletPaymentResult.Canceled)
    }

    @Test
    fun testWalletPaymentResult_failed() {
        val error = StripeException("Payment failed")
        val result = WalletPaymentResult.Failed(error)

        assertTrue(result is WalletPaymentResult.Failed)
        assertEquals("Payment failed", result.error.message)
    }


    @Test
    fun testGooglePayEnvironment_values() {
        val environments = GooglePayEnvironment.entries
        assertEquals(2, environments.size)
        assertTrue(environments.contains(GooglePayEnvironment.TEST))
        assertTrue(environments.contains(GooglePayEnvironment.PRODUCTION))
    }

    @Test
    fun testCardAuthMethod_values() {
        val methods = CardAuthMethod.entries
        assertEquals(2, methods.size)
        assertTrue(methods.contains(CardAuthMethod.PAN_ONLY))
        assertTrue(methods.contains(CardAuthMethod.CRYPTOGRAM_3DS))
    }

    @Test
    fun testApplePayMerchantCapability_values() {
        val capabilities = ApplePayMerchantCapability.entries
        assertEquals(4, capabilities.size)
        assertTrue(capabilities.contains(ApplePayMerchantCapability.CAPABILITY_3DS))
        assertTrue(capabilities.contains(ApplePayMerchantCapability.CAPABILITY_CREDIT))
        assertTrue(capabilities.contains(ApplePayMerchantCapability.CAPABILITY_DEBIT))
        assertTrue(capabilities.contains(ApplePayMerchantCapability.CAPABILITY_EMV))
    }

    @Test
    fun testApplePayContactField_values() {
        val fields = ApplePayContactField.entries
        assertEquals(4, fields.size)
        assertTrue(fields.contains(ApplePayContactField.NAME))
        assertTrue(fields.contains(ApplePayContactField.EMAIL))
        assertTrue(fields.contains(ApplePayContactField.PHONE))
        assertTrue(fields.contains(ApplePayContactField.POSTAL_ADDRESS))
    }
}
