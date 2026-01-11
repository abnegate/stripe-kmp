package com.jakebarnby.stripe

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import kotlin.coroutines.resume
import kotlin.js.Promise

/**
 * External declarations for Stripe.js Payment Element API
 */
public external interface StripeElements {
    public fun create(type: String, options: dynamic = definedExternally): StripePaymentElement
}

public external interface StripePaymentElement {
    public fun mount(domElement: dynamic)
    public fun unmount()
    public fun destroy()
    public fun on(event: String, handler: (dynamic) -> Unit)
}

public external interface StripeConfirmPaymentData {
    public var elements: StripeElements
    public var confirmParams: dynamic
    public var redirect: String?
}

public external interface StripeConfirmSetupData {
    public var elements: StripeElements
    public var confirmParams: dynamic
    public var redirect: String?
}

public external interface StripePaymentResult {
    public val error: StripeJsError?
    public val paymentIntent: dynamic
}

public external interface StripeSetupResult {
    public val error: StripeJsError?
    public val setupIntent: dynamic
}

public external interface StripeJsError {
    public val message: String
    public val code: String?
}

public external interface StripeInstanceExtended : StripeInstance {
    public fun elements(options: dynamic = definedExternally): StripeElements
    public fun confirmPayment(data: StripeConfirmPaymentData): Promise<StripePaymentResult>
    public fun confirmSetup(data: StripeConfirmSetupData): Promise<StripeSetupResult>
}

/**
 * JS PaymentSheet implementation using Stripe.js Payment Element.
 *
 * This creates a modal overlay with the Stripe Payment Element embedded.
 * Styles are configurable via PaymentSheetStyles.
 */
public actual class PaymentSheet(
    private val styles: PaymentSheetStyles = PaymentSheetStyles.Default
) {
    // MEDIUM-04: Track event listeners for proper cleanup
    private var containerElement: HTMLElement? = null
    private var overlayElement: HTMLElement? = null
    private var paymentElement: StripePaymentElement? = null
    private var elements: StripeElements? = null
    private var submitListener: EventListener? = null
    private var cancelListener: EventListener? = null

    /**
     * Convenience constructor for default styles.
     */
    public constructor() : this(PaymentSheetStyles.Default)

    /**
     * Present the Payment Sheet with a PaymentIntent.
     *
     * @param configuration The PaymentIntent configuration
     * @param onResult Callback invoked with the payment result
     */
    public actual suspend fun presentWithPaymentIntent(
        configuration: PaymentIntentConfiguration,
        onResult: (PaymentSheetResult) -> Unit
    ) {
        // MEDIUM-06: Use suspendCancellableCoroutine for proper cancellation support
        suspendCancellableCoroutine { continuation ->
            val stripe = Stripe.getInstance()

            // MEDIUM-05: Handle unsafe cast with proper null check
            val stripeJs = stripe.stripeInstance?.unsafeCast<StripeInstanceExtended>()
            if (stripeJs == null) {
                onResult(PaymentSheetResult.Failed(
                    StripeError(
                        message = "Stripe.js not loaded. Call Stripe.initialize() and wait for load to complete.",
                        code = "stripe_not_loaded"
                    )
                ))
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
                return@suspendCancellableCoroutine
            }

            // HIGH-03: Create cancel handler that properly resumes coroutine
            val cancelHandler: () -> Unit = {
                cleanup()
                onResult(PaymentSheetResult.Canceled)
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }

            // Create container for payment element
            val (container, overlay) = createPaymentContainer(
                config = configuration.paymentSheetConfiguration,
                onCancel = cancelHandler
            )
            containerElement = container
            overlayElement = overlay

            // Create elements instance
            val elementsInstance = stripeJs.elements(object {
                val clientSecret = configuration.clientSecret
                val appearance = object {
                    val theme = "stripe"
                }
            })
            elements = elementsInstance

            // Create and mount payment element
            val paymentElement = elementsInstance.create("payment", object {
                val layout = "tabs"
            })
            this.paymentElement = paymentElement
            paymentElement.mount(container)

            // Handle form submission
            val form = container.parentElement?.asDynamic()
            if (form != null) {
                submitListener = EventListener { event ->
                    event.preventDefault()

                    stripeJs.confirmPayment(object : StripeConfirmPaymentData {
                        override var elements = elementsInstance
                        override var confirmParams = object {
                            val return_url = window.location.href
                        }
                        override var redirect: String? = "if_required"
                    }).then { result ->
                        cleanup()

                        val mappedResult = if (result.error != null) {
                            PaymentSheetResult.Failed(
                                StripeError(
                                    message = result.error!!.message,
                                    code = result.error!!.code
                                )
                            )
                        } else {
                            PaymentSheetResult.Completed
                        }

                        onResult(mappedResult)
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }.catch { error ->
                        cleanup()
                        onResult(PaymentSheetResult.Failed(
                            StripeError(
                                message = "Payment failed: $error",
                                code = "payment_error"
                            )
                        ))
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }
                }
                form.addEventListener("submit", submitListener)
            }

            // Handle coroutine cancellation
            continuation.invokeOnCancellation {
                cleanup()
            }
        }
    }

    /**
     * Present the Payment Sheet with a SetupIntent.
     *
     * @param configuration The SetupIntent configuration
     * @param onResult Callback invoked with the result
     */
    public actual suspend fun presentWithSetupIntent(
        configuration: SetupIntentConfiguration,
        onResult: (PaymentSheetResult) -> Unit
    ) {
        // MEDIUM-06: Use suspendCancellableCoroutine for proper cancellation support
        suspendCancellableCoroutine { continuation ->
            val stripe = Stripe.getInstance()

            // MEDIUM-05: Handle unsafe cast with proper null check
            val stripeJs = stripe.stripeInstance?.unsafeCast<StripeInstanceExtended>()
            if (stripeJs == null) {
                onResult(PaymentSheetResult.Failed(
                    StripeError(
                        message = "Stripe.js not loaded. Call Stripe.initialize() and wait for load to complete.",
                        code = "stripe_not_loaded"
                    )
                ))
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
                return@suspendCancellableCoroutine
            }

            // HIGH-03: Create cancel handler that properly resumes coroutine
            val cancelHandler: () -> Unit = {
                cleanup()
                onResult(PaymentSheetResult.Canceled)
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }

            // Create container for payment element
            val (container, overlay) = createPaymentContainer(
                config = configuration.paymentSheetConfiguration,
                onCancel = cancelHandler
            )
            containerElement = container
            overlayElement = overlay

            // Create elements instance
            val elementsInstance = stripeJs.elements(object {
                val clientSecret = configuration.clientSecret
            })
            elements = elementsInstance

            // Create and mount payment element
            val paymentElement = elementsInstance.create("payment")
            this.paymentElement = paymentElement
            paymentElement.mount(container)

            // Handle form submission
            val form = container.parentElement?.asDynamic()
            if (form != null) {
                submitListener = EventListener { event ->
                    event.preventDefault()

                    stripeJs.confirmSetup(object : StripeConfirmSetupData {
                        override var elements = elementsInstance
                        override var confirmParams = object {
                            val return_url = window.location.href
                        }
                        override var redirect: String? = "if_required"
                    }).then { result ->
                        cleanup()

                        val mappedResult = if (result.error != null) {
                            PaymentSheetResult.Failed(
                                StripeError(
                                    message = result.error!!.message,
                                    code = result.error!!.code
                                )
                            )
                        } else {
                            PaymentSheetResult.Completed
                        }

                        onResult(mappedResult)
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }.catch { error ->
                        cleanup()
                        onResult(PaymentSheetResult.Failed(
                            StripeError(
                                message = "Setup failed: $error",
                                code = "setup_error"
                            )
                        ))
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }
                }
                form.addEventListener("submit", submitListener)
            }

            // Handle coroutine cancellation
            continuation.invokeOnCancellation {
                cleanup()
            }
        }
    }

    /**
     * MEDIUM-03: Create payment container with configurable styles and accessibility.
     * LOW-05: Magic numbers extracted to PaymentSheetStyles.
     */
    private fun createPaymentContainer(
        config: PaymentSheetConfiguration,
        onCancel: () -> Unit
    ): Pair<HTMLElement, HTMLElement> {
        // Create a modal overlay with accessibility
        val overlay = document.createElement("div") as HTMLElement
        overlay.setAttribute("role", "dialog")
        overlay.setAttribute("aria-modal", "true")
        overlay.setAttribute("aria-label", "Payment")
        overlay.setAttribute("style", """
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: ${styles.overlayBackgroundColor};
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: ${styles.overlayZIndex};
        """.trimIndent())

        // Create modal content
        val modal = document.createElement("div") as HTMLElement
        modal.setAttribute("style", """
            background: ${styles.modalBackgroundColor};
            padding: ${styles.modalPadding};
            border-radius: ${styles.modalBorderRadius};
            max-width: ${styles.modalMaxWidth};
            width: ${styles.modalWidth};
            box-shadow: ${styles.modalBoxShadow};
        """.trimIndent())

        // Add merchant name
        val header = document.createElement("h2") as HTMLElement
        header.textContent = config.merchantDisplayName
        header.setAttribute("style", "margin-top: 0;")
        header.id = "payment-sheet-title"
        modal.appendChild(header)

        // Set aria-labelledby
        overlay.setAttribute("aria-labelledby", "payment-sheet-title")

        // Create form
        val form = document.createElement("form") as HTMLElement

        // Create payment element container
        val container = document.createElement("div") as HTMLElement
        container.id = "payment-element"
        container.setAttribute("style", "margin: 1rem 0;")
        form.appendChild(container)

        // Create submit button with accessibility
        val submitButton = document.createElement("button") as HTMLElement
        submitButton.textContent = "Pay"
        submitButton.setAttribute("type", "submit")
        submitButton.setAttribute("aria-label", "Submit payment")
        submitButton.setAttribute("style", """
            width: 100%;
            padding: ${styles.submitButtonPadding};
            background: ${styles.submitButtonBackgroundColor};
            color: ${styles.submitButtonTextColor};
            border: none;
            border-radius: ${styles.submitButtonBorderRadius};
            font-size: ${styles.submitButtonFontSize};
            cursor: pointer;
            margin-top: 1rem;
        """.trimIndent())
        form.appendChild(submitButton)

        // Create cancel button with accessibility - HIGH-03: properly calls onCancel
        val cancelButton = document.createElement("button") as HTMLElement
        cancelButton.textContent = "Cancel"
        cancelButton.setAttribute("type", "button")
        cancelButton.setAttribute("aria-label", "Cancel payment")
        cancelButton.setAttribute("style", """
            width: 100%;
            padding: ${styles.submitButtonPadding};
            background: ${styles.cancelButtonBackgroundColor};
            color: ${styles.cancelButtonTextColor};
            border: 1px solid ${styles.cancelButtonBorderColor};
            border-radius: ${styles.submitButtonBorderRadius};
            font-size: ${styles.submitButtonFontSize};
            cursor: pointer;
            margin-top: 0.5rem;
        """.trimIndent())

        // MEDIUM-04: Store cancel listener for cleanup
        cancelListener = EventListener {
            onCancel()
        }
        cancelButton.addEventListener("click", cancelListener)
        form.appendChild(cancelButton)

        modal.appendChild(form)
        overlay.appendChild(modal)
        document.body?.appendChild(overlay)

        // Focus management for accessibility
        submitButton.asDynamic().focus()

        return Pair(container, overlay)
    }

    /**
     * MEDIUM-04: Cleanup with proper event listener removal.
     */
    private fun cleanup() {
        // Remove event listeners before destroying elements
        if (submitListener != null) {
            containerElement?.parentElement?.asDynamic()?.removeEventListener("submit", submitListener)
            submitListener = null
        }

        if (cancelListener != null) {
            // Find cancel button and remove listener
            overlayElement?.querySelector("button[aria-label='Cancel payment']")?.let { cancelBtn ->
                cancelBtn.removeEventListener("click", cancelListener)
            }
            cancelListener = null
        }

        // Destroy Stripe elements
        paymentElement?.unmount()
        paymentElement?.destroy()

        // Remove DOM elements
        overlayElement?.remove()

        // Clear references
        containerElement = null
        overlayElement = null
        paymentElement = null
        elements = null
    }
}
