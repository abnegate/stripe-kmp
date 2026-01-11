package com.jakebarnby.stripe

/**
 * Configuration for PaymentSheet UI styling (JS platform).
 *
 * MEDIUM-03: Configurable styles with accessibility support.
 * LOW-05: Magic numbers extracted to named constants.
 */
public data class PaymentSheetStyles(
    val overlayBackgroundColor: String = "rgba(0, 0, 0, 0.5)",
    val modalBackgroundColor: String = "#ffffff",
    val modalPadding: String = "2rem",
    val modalBorderRadius: String = "8px",
    val modalMaxWidth: String = "500px",
    val modalWidth: String = "90%",
    val modalBoxShadow: String = "0 4px 6px rgba(0, 0, 0, 0.1)",
    val submitButtonBackgroundColor: String = "#635BFF",
    val submitButtonTextColor: String = "#ffffff",
    val submitButtonPadding: String = "0.75rem",
    val submitButtonBorderRadius: String = "4px",
    val submitButtonFontSize: String = "1rem",
    val cancelButtonBackgroundColor: String = "transparent",
    val cancelButtonTextColor: String = "#635BFF",
    val cancelButtonBorderColor: String = "#635BFF",
    val overlayZIndex: Int = 9999
) {
    public companion object {
        /**
         * Default Stripe-branded styles.
         */
        public val Default: PaymentSheetStyles = PaymentSheetStyles()

        /**
         * High contrast theme for better accessibility.
         */
        public val HighContrast: PaymentSheetStyles = PaymentSheetStyles(
            overlayBackgroundColor = "rgba(0, 0, 0, 0.8)",
            submitButtonBackgroundColor = "#000000",
            submitButtonTextColor = "#ffffff",
            cancelButtonTextColor = "#000000",
            cancelButtonBorderColor = "#000000"
        )
    }
}
