package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*

/**
 * Android SDK mapper stubs.
 *
 * IMPLEMENTATION NOTE:
 * The Android Stripe SDK has platform-specific API signatures that require detailed mapping.
 * This file contains simplified stubs that document the required conversions.
 *
 * To fully implement these mappers, you'll need to:
 * 1. Match the exact API signatures of the Android Stripe SDK version you're using
 * 2. Map between SDK enums/classes and the KMP model classes
 * 3. Handle async callbacks and convert them to suspend functions
 * 4. Properly handle nullable fields and error cases
 *
 * The mapping logic depends on the specific version of the Stripe Android SDK.
 * Refer to the Stripe Android SDK documentation for the exact API surface.
 */

// Placeholder mapper functions

internal fun com.stripe.android.model.Token.toKmpToken(): Token {
    // Simplified stub - implement based on Android SDK version
    return Token(
        id = id,
        type = "card", // Map from SDK type
        created = created.time / 1000,
        livemode = livemode,
        used = used
    )
}

internal fun SourceParams.toAndroidSourceParams(): com.stripe.android.model.SourceParams {
    return when (type) {
        SourceType.CARD -> {
            val cardData = extraParams?.get("card") as? Map<*, *>
            val number = cardData?.get("number") as? String
            val expMonth = (cardData?.get("exp_month") as? Number)?.toInt()
            val expYear = (cardData?.get("exp_year") as? Number)?.toInt()
            val cvc = cardData?.get("cvc") as? String

            if (number != null && expMonth != null && expYear != null) {
                com.stripe.android.model.SourceParams.createCardParams(
                    com.stripe.android.model.CardParams(
                        number = number,
                        expMonth = expMonth,
                        expYear = expYear,
                        cvc = cvc
                    )
                )
            } else {
                throw IllegalArgumentException("Card params require number, expMonth, and expYear")
            }
        }
        else -> throw UnsupportedOperationException("Source type $type not yet fully implemented. Please use the Android SDK directly for this source type.")
    }
}

internal fun com.stripe.android.model.Source.toKmpSource(): Source {
    // Note: This is a simplified mapping. The Android SDK's Source model may have
    // different property names or structure. Adjust based on actual SDK version.
    throw UnsupportedOperationException(
        "Source mapping from Android SDK requires manual implementation based on your specific Stripe Android SDK version. " +
        "Please implement this mapper according to your SDK's Source model structure."
    )
}

internal fun PaymentMethodCreateParams.toAndroidPaymentMethodCreateParams(): com.stripe.android.model.PaymentMethodCreateParams {
    return when (type) {
        PaymentMethodType.CARD -> {
            val cardParams = card ?: throw IllegalStateException("Card params required for CARD type")
            val androidCard = if (cardParams.token != null) {
                com.stripe.android.model.PaymentMethodCreateParams.Card.create(cardParams.token!!)
            } else {
                com.stripe.android.model.PaymentMethodCreateParams.Card(
                    number = cardParams.number,
                    expiryMonth = cardParams.expMonth,
                    expiryYear = cardParams.expYear,
                    cvc = cardParams.cvc
                )
            }

            com.stripe.android.model.PaymentMethodCreateParams.create(
                card = androidCard,
                billingDetails = billingDetails?.toAndroidBillingDetails()
            )
        }
        else -> throw UnsupportedOperationException("PaymentMethod type $type not yet fully implemented. Please use the Android SDK directly for this payment method type.")
    }
}

private fun BillingDetails.toAndroidBillingDetails(): com.stripe.android.model.PaymentMethod.BillingDetails {
    return com.stripe.android.model.PaymentMethod.BillingDetails(
        name = name,
        email = email,
        phone = phone,
        address = address?.let {
            com.stripe.android.model.Address(
                line1 = it.line1,
                line2 = it.line2,
                city = it.city,
                state = it.state,
                postalCode = it.postalCode,
                country = it.country
            )
        }
    )
}

internal fun com.stripe.android.model.PaymentMethod.toKmpPaymentMethod(): PaymentMethod {
    return PaymentMethod(
        id = id ?: "",
        type = PaymentMethodType.fromValue(type?.code ?: "unknown"),
        created = created ?: 0L,
        livemode = liveMode,
        billingDetails = billingDetails?.let {
            BillingDetails(
                name = it.name,
                email = it.email,
                phone = it.phone,
                address = it.address?.let { addr ->
                    Address(
                        line1 = addr.line1,
                        line2 = addr.line2,
                        city = addr.city,
                        state = addr.state,
                        postalCode = addr.postalCode,
                        country = addr.country
                    )
                }
            )
        },
        card = card?.let {
            Card(
                brand = CardBrand.fromValue(it.brand?.code ?: "unknown"),
                last4 = it.last4 ?: "",
                expMonth = it.expiryMonth ?: 0,
                expYear = it.expiryYear ?: 0,
                funding = CardFunding.fromValue(it.funding?.toString()?.lowercase() ?: "unknown"),
                country = it.country,
                fingerprint = it.fingerprint,
                checks = it.checks?.let { checks ->
                    CardChecks(
                        addressLine1Check = checks.addressLine1Check,
                        addressPostalCodeCheck = checks.addressPostalCodeCheck,
                        cvcCheck = checks.cvcCheck
                    )
                },
                wallet = it.wallet?.let { wallet ->
                    // Map wallet type dynamically using toString as a fallback
                    val walletType = wallet.javaClass.simpleName.lowercase().replace("wallet", "")
                    CardWallet(type = walletType)
                },
                threeDSecureUsage = it.threeDSecureUsage?.let { usage ->
                    ThreeDSecureUsage(supported = usage.isSupported)
                },
                networks = it.networks?.let { networks ->
                    CardNetworks(
                        available = networks.available.toList(),
                        preferred = networks.preferred
                    )
                }
            )
        },
        customer = customerId
    )
}

internal fun ConfirmPaymentIntentParams.toAndroidConfirmPaymentIntentParams(): com.stripe.android.model.ConfirmPaymentIntentParams {
    return when {
        paymentMethodId != null -> {
            com.stripe.android.model.ConfirmPaymentIntentParams.createWithPaymentMethodId(
                paymentMethodId = paymentMethodId!!,
                clientSecret = clientSecret,
                shipping = shipping?.toAndroidShipping()
            )
        }
        paymentMethodCreateParams != null -> {
            com.stripe.android.model.ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(
                paymentMethodCreateParams = paymentMethodCreateParams!!.toAndroidPaymentMethodCreateParams(),
                clientSecret = clientSecret,
                shipping = shipping?.toAndroidShipping()
            )
        }
        else -> {
            com.stripe.android.model.ConfirmPaymentIntentParams.create(
                clientSecret = clientSecret
            )
        }
    }
}

private fun ShippingDetails.toAndroidShipping(): com.stripe.android.model.ConfirmPaymentIntentParams.Shipping {
    return com.stripe.android.model.ConfirmPaymentIntentParams.Shipping(
        name = name,
        address = com.stripe.android.model.Address(
            line1 = address.line1,
            line2 = address.line2,
            city = address.city,
            state = address.state,
            postalCode = address.postalCode,
            country = address.country
        ),
        carrier = carrier,
        phone = phone,
        trackingNumber = trackingNumber
    )
}

internal fun com.stripe.android.model.PaymentIntent.toKmpPaymentIntent(): PaymentIntent {
    return PaymentIntent(
        id = id ?: "",
        clientSecret = clientSecret ?: "",
        amount = amount ?: 0L,
        currency = currency ?: "",
        status = when (status) {
            com.stripe.android.model.StripeIntent.Status.RequiresPaymentMethod -> PaymentIntentStatus.REQUIRES_PAYMENT_METHOD
            com.stripe.android.model.StripeIntent.Status.RequiresConfirmation -> PaymentIntentStatus.REQUIRES_CONFIRMATION
            com.stripe.android.model.StripeIntent.Status.RequiresAction -> PaymentIntentStatus.REQUIRES_ACTION
            com.stripe.android.model.StripeIntent.Status.Processing -> PaymentIntentStatus.PROCESSING
            com.stripe.android.model.StripeIntent.Status.RequiresCapture -> PaymentIntentStatus.REQUIRES_CAPTURE
            com.stripe.android.model.StripeIntent.Status.Canceled -> PaymentIntentStatus.CANCELED
            com.stripe.android.model.StripeIntent.Status.Succeeded -> PaymentIntentStatus.SUCCEEDED
            else -> PaymentIntentStatus.REQUIRES_PAYMENT_METHOD
        },
        created = created ?: 0L,
        livemode = isLiveMode,
        paymentMethodId = paymentMethodId,
        paymentMethodTypes = paymentMethodTypes,
        confirmationMethod = when (confirmationMethod) {
            com.stripe.android.model.PaymentIntent.ConfirmationMethod.Automatic -> ConfirmationMethod.AUTOMATIC
            com.stripe.android.model.PaymentIntent.ConfirmationMethod.Manual -> ConfirmationMethod.MANUAL
            else -> ConfirmationMethod.AUTOMATIC
        },
        captureMethod = when (captureMethod) {
            com.stripe.android.model.PaymentIntent.CaptureMethod.Automatic -> CaptureMethod.AUTOMATIC
            com.stripe.android.model.PaymentIntent.CaptureMethod.Manual -> CaptureMethod.MANUAL
            else -> CaptureMethod.AUTOMATIC
        },
        description = description,
        receiptEmail = receiptEmail,
        setupFutureUsage = setupFutureUsage?.let {
            when (it) {
                com.stripe.android.model.StripeIntent.Usage.OnSession -> SetupFutureUsage.ON_SESSION
                com.stripe.android.model.StripeIntent.Usage.OffSession -> SetupFutureUsage.OFF_SESSION
                else -> null
            }
        },
        lastPaymentError = lastPaymentError?.let {
            PaymentIntentError(
                type = it.type?.code ?: "unknown",
                code = it.code,
                declineCode = it.declineCode,
                message = it.message ?: "",
                paymentMethod = it.paymentMethod?.toKmpPaymentMethod()
            )
        },
        nextAction = nextActionData?.let { nextAction ->
            val type = when (nextAction) {
                is com.stripe.android.model.StripeIntent.NextActionData.RedirectToUrl -> NextActionType.REDIRECT_TO_URL
                is com.stripe.android.model.StripeIntent.NextActionData.DisplayOxxoDetails -> NextActionType.DISPLAY_OXXO_DETAILS
                else -> NextActionType.USE_STRIPE_SDK
            }

            NextAction(
                type = type,
                redirectToUrl = (nextAction as? com.stripe.android.model.StripeIntent.NextActionData.RedirectToUrl)?.let {
                    RedirectToUrl(
                        url = it.url.toString(),
                        returnUrl = it.returnUrl
                    )
                }
            )
        },
        canceledAt = canceledAt,
        cancellationReason = cancellationReason?.toString(),
        metadata = null
    )
}

internal fun ConfirmSetupIntentParams.toAndroidConfirmSetupIntentParams(): com.stripe.android.model.ConfirmSetupIntentParams {
    return when {
        paymentMethodId != null -> {
            com.stripe.android.model.ConfirmSetupIntentParams.create(
                clientSecret = clientSecret,
                paymentMethodId = paymentMethodId
            )
        }
        paymentMethodCreateParams != null -> {
            com.stripe.android.model.ConfirmSetupIntentParams.create(
                clientSecret = clientSecret,
                paymentMethodCreateParams = paymentMethodCreateParams.toAndroidPaymentMethodCreateParams()
            )
        }
        else -> {
            throw IllegalArgumentException("Either paymentMethodId or paymentMethodCreateParams must be provided")
        }
    }
}

internal fun com.stripe.android.model.SetupIntent.toKmpSetupIntent(): SetupIntent {
    return SetupIntent(
        id = id ?: "",
        clientSecret = clientSecret ?: "",
        created = created ?: 0L,
        livemode = isLiveMode,
        status = when (status) {
            com.stripe.android.model.StripeIntent.Status.RequiresPaymentMethod -> SetupIntentStatus.REQUIRES_PAYMENT_METHOD
            com.stripe.android.model.StripeIntent.Status.RequiresConfirmation -> SetupIntentStatus.REQUIRES_CONFIRMATION
            com.stripe.android.model.StripeIntent.Status.RequiresAction -> SetupIntentStatus.REQUIRES_ACTION
            com.stripe.android.model.StripeIntent.Status.Processing -> SetupIntentStatus.PROCESSING
            com.stripe.android.model.StripeIntent.Status.Canceled -> SetupIntentStatus.CANCELED
            com.stripe.android.model.StripeIntent.Status.Succeeded -> SetupIntentStatus.SUCCEEDED
            else -> SetupIntentStatus.REQUIRES_PAYMENT_METHOD
        },
        paymentMethodId = paymentMethodId,
        paymentMethodTypes = paymentMethodTypes,
        description = description,
        usage = when (usage) {
            com.stripe.android.model.StripeIntent.Usage.OnSession -> SetupIntentUsage.ON_SESSION
            com.stripe.android.model.StripeIntent.Usage.OffSession -> SetupIntentUsage.OFF_SESSION
            else -> SetupIntentUsage.OFF_SESSION
        },
        customerId = null,
        lastSetupError = lastSetupError?.let {
            SetupIntentError(
                type = it.type?.code ?: "unknown",
                code = it.code,
                declineCode = it.declineCode,
                message = it.message ?: "",
                paymentMethod = it.paymentMethod?.toKmpPaymentMethod()
            )
        },
        nextAction = nextActionData?.let { nextAction ->
            val type = when (nextAction) {
                is com.stripe.android.model.StripeIntent.NextActionData.RedirectToUrl -> SetupNextActionType.REDIRECT_TO_URL
                else -> SetupNextActionType.USE_STRIPE_SDK
            }

            SetupNextAction(
                type = type,
                redirectToUrl = (nextAction as? com.stripe.android.model.StripeIntent.NextActionData.RedirectToUrl)?.let {
                    RedirectToUrl(
                        url = it.url.toString(),
                        returnUrl = it.returnUrl
                    )
                }
            )
        },
        cancellationReason = cancellationReason?.toString(),
        metadata = null
    )
}
