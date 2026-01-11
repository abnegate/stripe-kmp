package com.jakebarnby.stripe.model

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

/**
 * Tests that verify all model properties exist.
 * Will fail if properties are removed from the SDK.
 *
 * These tests ensure backward compatibility when upgrading Stripe SDK versions.
 */
class ModelCompletenessTest {

    @Test
    fun testPaymentIntentAllProperties() {
        val pi = PaymentIntent(
            id = "pi_123",
            clientSecret = "secret",
            amount = 1000,
            currency = "usd",
            status = PaymentIntentStatus.SUCCEEDED,
            paymentMethodId = "pm_123",
            paymentMethodTypes = listOf("card", "ideal"),
            lastPaymentError = null,
            nextAction = null,
            created = 123456789,
            description = "Test",
            receiptEmail = "test@test.com",
            confirmationMethod = ConfirmationMethod.AUTOMATIC,
            captureMethod = CaptureMethod.AUTOMATIC,
            setupFutureUsage = SetupFutureUsage.ON_SESSION,
            livemode = false,
            canceledAt = null,
            cancellationReason = null,
            metadata = mapOf("key" to "value")
        )

        // Verify ALL properties are accessible
        assertNotNull(pi.id)
        assertEquals("pi_123", pi.id)
        assertNotNull(pi.clientSecret)
        assertEquals("secret", pi.clientSecret)
        assertNotNull(pi.amount)
        assertEquals(1000L, pi.amount)
        assertNotNull(pi.currency)
        assertEquals("usd", pi.currency)
        assertNotNull(pi.status)
        assertEquals(PaymentIntentStatus.SUCCEEDED, pi.status)
        assertNotNull(pi.created)
        assertEquals(123456789L, pi.created)
        assertNotNull(pi.confirmationMethod)
        assertEquals(ConfirmationMethod.AUTOMATIC, pi.confirmationMethod)
        assertNotNull(pi.captureMethod)
        assertEquals(CaptureMethod.AUTOMATIC, pi.captureMethod)
        assertNotNull(pi.livemode)
        assertEquals(false, pi.livemode)
        assertNotNull(pi.metadata)
        assertEquals(mapOf("key" to "value"), pi.metadata)
        assertEquals("pm_123", pi.paymentMethodId)
        assertEquals(listOf("card", "ideal"), pi.paymentMethodTypes)
        assertEquals("Test", pi.description)
        assertEquals("test@test.com", pi.receiptEmail)
        assertEquals(SetupFutureUsage.ON_SESSION, pi.setupFutureUsage)
    }

    @Test
    fun testPaymentMethodAllProperties() {
        val card = Card(
            brand = CardBrand.VISA,
            last4 = "4242",
            expMonth = 12,
            expYear = 2025,
            funding = CardFunding.CREDIT,
            country = "US",
            fingerprint = "fingerprint_123",
            checks = CardChecks(
                addressLine1Check = "pass",
                addressPostalCodeCheck = "pass",
                cvcCheck = "pass"
            ),
            wallet = CardWallet(type = "apple_pay"),
            threeDSecureUsage = ThreeDSecureUsage(supported = true),
            networks = CardNetworks(
                available = listOf("visa", "mastercard"),
                preferred = "visa"
            )
        )

        val billingDetails = BillingDetails(
            name = "John Doe",
            email = "john@example.com",
            phone = "+1234567890",
            address = Address(
                line1 = "123 Main St",
                line2 = "Apt 4",
                city = "San Francisco",
                state = "CA",
                postalCode = "94102",
                country = "US"
            )
        )

        val pm = PaymentMethod(
            id = "pm_123",
            type = PaymentMethodType.CARD,
            created = 1234567890,
            livemode = false,
            billingDetails = billingDetails,
            card = card,
            customer = "cus_123",
            metadata = mapOf("key" to "value")
        )

        // Verify all PaymentMethod properties
        assertNotNull(pm.id)
        assertEquals("pm_123", pm.id)
        assertNotNull(pm.type)
        assertEquals(PaymentMethodType.CARD, pm.type)
        assertNotNull(pm.created)
        assertEquals(1234567890L, pm.created)
        assertNotNull(pm.livemode)
        assertEquals(false, pm.livemode)
        assertNotNull(pm.billingDetails)
        assertNotNull(pm.card)
        assertEquals("cus_123", pm.customer)
        assertEquals(mapOf("key" to "value"), pm.metadata)

        // Verify all Card properties
        assertNotNull(card.brand)
        assertEquals(CardBrand.VISA, card.brand)
        assertNotNull(card.last4)
        assertEquals("4242", card.last4)
        assertNotNull(card.expMonth)
        assertEquals(12, card.expMonth)
        assertNotNull(card.expYear)
        assertEquals(2025, card.expYear)
        assertNotNull(card.funding)
        assertEquals(CardFunding.CREDIT, card.funding)
        assertEquals("US", card.country)
        assertEquals("fingerprint_123", card.fingerprint)
        assertNotNull(card.checks)
        assertNotNull(card.wallet)
        assertNotNull(card.threeDSecureUsage)
        assertNotNull(card.networks)

        // Verify CardChecks properties
        assertEquals("pass", card.checks?.addressLine1Check)
        assertEquals("pass", card.checks?.addressPostalCodeCheck)
        assertEquals("pass", card.checks?.cvcCheck)

        // Verify CardWallet properties
        assertEquals("apple_pay", card.wallet?.type)

        // Verify ThreeDSecureUsage properties
        assertEquals(true, card.threeDSecureUsage?.supported)

        // Verify CardNetworks properties
        assertEquals(listOf("visa", "mastercard"), card.networks?.available)
        assertEquals("visa", card.networks?.preferred)
    }

    @Test
    fun testSetupIntentAllProperties() {
        val si = SetupIntent(
            id = "seti_123",
            clientSecret = "seti_secret",
            created = 1234567890,
            livemode = false,
            status = SetupIntentStatus.SUCCEEDED,
            paymentMethodId = "pm_123",
            paymentMethodTypes = listOf("card"),
            description = "Setup for future payments",
            usage = SetupIntentUsage.OFF_SESSION,
            customerId = "cus_123",
            lastSetupError = null,
            nextAction = null,
            cancellationReason = "requested_by_customer",
            metadata = mapOf("key" to "value")
        )

        // Verify all properties
        assertNotNull(si.id)
        assertEquals("seti_123", si.id)
        assertNotNull(si.clientSecret)
        assertEquals("seti_secret", si.clientSecret)
        assertNotNull(si.created)
        assertEquals(1234567890L, si.created)
        assertNotNull(si.livemode)
        assertEquals(false, si.livemode)
        assertNotNull(si.status)
        assertEquals(SetupIntentStatus.SUCCEEDED, si.status)
        assertEquals("pm_123", si.paymentMethodId)
        assertEquals(listOf("card"), si.paymentMethodTypes)
        assertEquals("Setup for future payments", si.description)
        assertNotNull(si.usage)
        assertEquals(SetupIntentUsage.OFF_SESSION, si.usage)
        assertEquals("cus_123", si.customerId)
        assertEquals("requested_by_customer", si.cancellationReason)
        assertEquals(mapOf("key" to "value"), si.metadata)
    }

    @Test
    fun testTokenAllProperties() {
        val cardToken = CardToken(
            id = "card_123",
            brand = "visa",
            last4 = "4242",
            expMonth = 12,
            expYear = 2025,
            funding = "credit",
            country = "US",
            name = "John Doe",
            addressLine1 = "123 Main St",
            addressLine2 = "Apt 4",
            addressCity = "San Francisco",
            addressState = "CA",
            addressZip = "94102",
            addressCountry = "US",
            cvcCheck = "pass",
            addressLine1Check = "pass",
            addressZipCheck = "pass"
        )

        val token = Token(
            id = "tok_123",
            type = "card",
            created = 1234567890,
            livemode = false,
            used = false,
            card = cardToken,
            bankAccount = null
        )

        // Verify Token properties
        assertNotNull(token.id)
        assertEquals("tok_123", token.id)
        assertNotNull(token.type)
        assertEquals("card", token.type)
        assertNotNull(token.created)
        assertEquals(1234567890L, token.created)
        assertNotNull(token.livemode)
        assertEquals(false, token.livemode)
        assertNotNull(token.used)
        assertEquals(false, token.used)
        assertNotNull(token.card)

        // Verify CardToken properties
        assertNotNull(cardToken.id)
        assertEquals("card_123", cardToken.id)
        assertNotNull(cardToken.brand)
        assertEquals("visa", cardToken.brand)
        assertNotNull(cardToken.last4)
        assertEquals("4242", cardToken.last4)
        assertNotNull(cardToken.expMonth)
        assertEquals(12, cardToken.expMonth)
        assertNotNull(cardToken.expYear)
        assertEquals(2025, cardToken.expYear)
        assertEquals("credit", cardToken.funding)
        assertEquals("US", cardToken.country)
        assertEquals("John Doe", cardToken.name)
        assertEquals("123 Main St", cardToken.addressLine1)
        assertEquals("Apt 4", cardToken.addressLine2)
        assertEquals("San Francisco", cardToken.addressCity)
        assertEquals("CA", cardToken.addressState)
        assertEquals("94102", cardToken.addressZip)
        assertEquals("US", cardToken.addressCountry)
        assertEquals("pass", cardToken.cvcCheck)
        assertEquals("pass", cardToken.addressLine1Check)
        assertEquals("pass", cardToken.addressZipCheck)
    }

    @Test
    fun testBankAccountTokenAllProperties() {
        val bankAccount = BankAccountToken(
            id = "ba_123",
            country = "US",
            currency = "usd",
            last4 = "6789",
            bankName = "Chase",
            accountHolderName = "Jane Doe",
            accountHolderType = "individual",
            routingNumber = "110000000",
            status = "verified"
        )

        // Verify all properties
        assertNotNull(bankAccount.id)
        assertEquals("ba_123", bankAccount.id)
        assertNotNull(bankAccount.country)
        assertEquals("US", bankAccount.country)
        assertNotNull(bankAccount.currency)
        assertEquals("usd", bankAccount.currency)
        assertNotNull(bankAccount.last4)
        assertEquals("6789", bankAccount.last4)
        assertEquals("Chase", bankAccount.bankName)
        assertEquals("Jane Doe", bankAccount.accountHolderName)
        assertEquals("individual", bankAccount.accountHolderType)
        assertEquals("110000000", bankAccount.routingNumber)
        assertEquals("verified", bankAccount.status)
    }

    @Test
    fun testCardParamsAllProperties() {
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
            addressZip = "94102",
            addressCountry = "US",
            currency = "usd"
        )

        // Verify all properties
        assertNotNull(params.number)
        assertEquals("4242424242424242", params.number)
        assertNotNull(params.expMonth)
        assertEquals(12, params.expMonth)
        assertNotNull(params.expYear)
        assertEquals(2025, params.expYear)
        assertEquals("123", params.cvc)
        assertEquals("John Doe", params.name)
        assertEquals("123 Main St", params.addressLine1)
        assertEquals("Apt 4", params.addressLine2)
        assertEquals("San Francisco", params.addressCity)
        assertEquals("CA", params.addressState)
        assertEquals("94102", params.addressZip)
        assertEquals("US", params.addressCountry)
        assertEquals("usd", params.currency)
    }

    @Test
    fun testBankAccountTokenParamsAllProperties() {
        val params = BankAccountTokenParams(
            country = "US",
            currency = "usd",
            accountNumber = "000123456789",
            routingNumber = "110000000",
            accountHolderName = "Jane Doe",
            accountHolderType = BankAccountTokenParams.AccountHolderType.INDIVIDUAL
        )

        // Verify all properties
        assertNotNull(params.country)
        assertEquals("US", params.country)
        assertNotNull(params.currency)
        assertEquals("usd", params.currency)
        assertNotNull(params.accountNumber)
        assertEquals("000123456789", params.accountNumber)
        assertEquals("110000000", params.routingNumber)
        assertEquals("Jane Doe", params.accountHolderName)
        assertEquals(BankAccountTokenParams.AccountHolderType.INDIVIDUAL, params.accountHolderType)
    }

    @Test
    fun testPaymentMethodCreateParamsAllProperties() {
        val billingDetails = BillingDetails(
            name = "John Doe",
            email = "john@example.com",
            phone = "+1234567890",
            address = Address(
                line1 = "123 Main St",
                city = "San Francisco",
                state = "CA",
                postalCode = "94102",
                country = "US"
            )
        )

        val cardParams = CardPaymentMethodCreateParams(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2025,
            cvc = "123"
        )

        val params = PaymentMethodCreateParams(
            type = PaymentMethodType.CARD,
            billingDetails = billingDetails,
            card = cardParams,
            metadata = mapOf("key" to "value")
        )

        // Verify all properties
        assertNotNull(params.type)
        assertEquals(PaymentMethodType.CARD, params.type)
        assertNotNull(params.billingDetails)
        assertNotNull(params.card)
        assertEquals(mapOf("key" to "value"), params.metadata)
    }

    @Test
    fun testConfirmPaymentIntentParamsAllProperties() {
        val shippingDetails = ShippingDetails(
            name = "John Doe",
            address = Address(
                line1 = "123 Main St",
                city = "San Francisco",
                state = "CA",
                postalCode = "94102",
                country = "US"
            ),
            carrier = "UPS",
            phone = "+1234567890",
            trackingNumber = "1Z999AA10123456784"
        )

        val mandateData = MandateData(
            customerAcceptance = CustomerAcceptance(
                type = "online",
                acceptedAt = 1234567890,
                online = OnlineAcceptance(
                    ipAddress = "192.168.1.1",
                    userAgent = "Mozilla/5.0"
                )
            )
        )

        val params = ConfirmPaymentIntentParams(
            clientSecret = "pi_secret_123",
            paymentMethodId = "pm_123",
            paymentMethodCreateParams = null,
            returnUrl = "https://example.com/return",
            shipping = shippingDetails,
            receiptEmail = "receipt@example.com",
            setupFutureUsage = SetupFutureUsage.OFF_SESSION,
            mandate = "mandate_123",
            mandateData = mandateData,
            savePaymentMethod = true
        )

        // Verify all properties
        assertNotNull(params.clientSecret)
        assertEquals("pi_secret_123", params.clientSecret)
        assertEquals("pm_123", params.paymentMethodId)
        assertEquals("https://example.com/return", params.returnUrl)
        assertNotNull(params.shipping)
        assertEquals("receipt@example.com", params.receiptEmail)
        assertEquals(SetupFutureUsage.OFF_SESSION, params.setupFutureUsage)
        assertEquals("mandate_123", params.mandate)
        assertNotNull(params.mandateData)
        assertEquals(true, params.savePaymentMethod)
    }

    @Test
    fun testConfirmSetupIntentParamsAllProperties() {
        val mandateData = MandateData(
            customerAcceptance = CustomerAcceptance(
                type = "online",
                acceptedAt = 1234567890,
                online = OnlineAcceptance(
                    ipAddress = "192.168.1.1",
                    userAgent = "Mozilla/5.0"
                )
            )
        )

        val params = ConfirmSetupIntentParams(
            clientSecret = "seti_secret_123",
            paymentMethodId = "pm_123",
            paymentMethodCreateParams = null,
            returnUrl = "https://example.com/return",
            mandate = "mandate_123",
            mandateData = mandateData
        )

        // Verify all properties
        assertNotNull(params.clientSecret)
        assertEquals("seti_secret_123", params.clientSecret)
        assertEquals("pm_123", params.paymentMethodId)
        assertEquals("https://example.com/return", params.returnUrl)
        assertEquals("mandate_123", params.mandate)
        assertNotNull(params.mandateData)
    }

    @Test
    fun testPaymentIntentErrorAllProperties() {
        val paymentMethod = PaymentMethod(
            id = "pm_123",
            type = PaymentMethodType.CARD,
            created = 1234567890,
            livemode = false
        )

        val error = PaymentIntentError(
            type = "card_error",
            code = "card_declined",
            declineCode = "insufficient_funds",
            message = "Your card has insufficient funds.",
            paymentMethod = paymentMethod
        )

        // Verify all properties
        assertNotNull(error.type)
        assertEquals("card_error", error.type)
        assertEquals("card_declined", error.code)
        assertEquals("insufficient_funds", error.declineCode)
        assertNotNull(error.message)
        assertEquals("Your card has insufficient funds.", error.message)
        assertNotNull(error.paymentMethod)
    }

    @Test
    fun testNextActionAllProperties() {
        val redirectToUrl = RedirectToUrl(
            url = "https://authenticate.stripe.com",
            returnUrl = "https://example.com/return"
        )

        val nextAction = NextAction(
            type = NextActionType.REDIRECT_TO_URL,
            redirectToUrl = redirectToUrl,
            useStripeSdk = null
        )

        // Verify all properties
        assertNotNull(nextAction.type)
        assertEquals(NextActionType.REDIRECT_TO_URL, nextAction.type)
        assertNotNull(nextAction.redirectToUrl)
        assertEquals("https://authenticate.stripe.com", nextAction.redirectToUrl?.url)
        assertEquals("https://example.com/return", nextAction.redirectToUrl?.returnUrl)
    }

    @Test
    fun testBillingDetailsAllProperties() {
        val billingDetails = BillingDetails(
            name = "John Doe",
            email = "john@example.com",
            phone = "+1234567890",
            address = Address(
                line1 = "123 Main St",
                line2 = "Apt 4",
                city = "San Francisco",
                state = "CA",
                postalCode = "94102",
                country = "US"
            )
        )

        // Verify all properties
        assertEquals("John Doe", billingDetails.name)
        assertEquals("john@example.com", billingDetails.email)
        assertEquals("+1234567890", billingDetails.phone)
        assertNotNull(billingDetails.address)
    }

    @Test
    fun testAddressAllProperties() {
        val address = Address(
            line1 = "123 Main St",
            line2 = "Apt 4",
            city = "San Francisco",
            state = "CA",
            postalCode = "94102",
            country = "US"
        )

        // Verify all properties
        assertNotNull(address.line1)
        assertEquals("123 Main St", address.line1)
        assertEquals("Apt 4", address.line2)
        assertNotNull(address.city)
        assertEquals("San Francisco", address.city)
        assertEquals("CA", address.state)
        assertNotNull(address.postalCode)
        assertEquals("94102", address.postalCode)
        assertNotNull(address.country)
        assertEquals("US", address.country)
    }

    @Test
    fun testStripeExceptionAllProperties() {
        val stripeError = StripeError(
            type = "card_error",
            code = "card_declined",
            message = "Your card was declined.",
            param = "card_number",
            declineCode = "insufficient_funds",
            charge = "ch_123"
        )

        val exception = StripeException(
            message = "Payment failed",
            stripeError = stripeError,
            statusCode = 402,
            requestId = "req_123"
        )

        // Verify all properties
        assertNotNull(exception.message)
        assertEquals("Payment failed", exception.message)
        assertNotNull(exception.stripeError)
        assertEquals(402, exception.statusCode)
        assertEquals("req_123", exception.requestId)

        // Verify StripeError properties
        assertNotNull(stripeError.type)
        assertEquals("card_error", stripeError.type)
        assertEquals("card_declined", stripeError.code)
        assertNotNull(stripeError.message)
        assertEquals("Your card was declined.", stripeError.message)
        assertEquals("card_number", stripeError.param)
        assertEquals("insufficient_funds", stripeError.declineCode)
        assertEquals("ch_123", stripeError.charge)
    }
}
