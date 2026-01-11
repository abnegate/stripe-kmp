package com.jakebarnby.stripe

import com.jakebarnby.stripe.model.*

/**
 * iOS Financial Connections Sheet implementation.
 *
 * CRITICAL: iOS FinancialConnectionsSheet requires Swift bridge integration.
 *
 * The iOS Financial Connections Sheet uses the native Stripe iOS SDK via CocoaPods.
 * Due to KMP limitations with CocoaPods cinterop, you need to create a Swift bridge.
 *
 * ## Implementation Guide:
 *
 * 1. **Install CocoaPods dependencies:**
 *    ```bash
 *    ./gradlew podInstall
 *    ```
 *
 * 2. **Create a Swift bridge file (StripeFinancialConnectionsBridge.swift):**
 *    ```swift
 *    import StripeFinancialConnections
 *    import UIKit
 *
 *    @objc public class StripeFinancialConnectionsBridge: NSObject {
 *        @objc public static func presentFinancialConnections(
 *            clientSecret: String,
 *            publishableKey: String,
 *            completion: @escaping ([String: Any]?, String?, String?) -> Void
 *        ) {
 *            let configuration = FinancialConnectionsSheet.Configuration(
 *                apiKey: publishableKey
 *            )
 *
 *            let financialConnectionsSheet = FinancialConnectionsSheet(
 *                financialConnectionsSessionClientSecret: clientSecret,
 *                configuration: configuration
 *            )
 *
 *            guard let rootVC = UIApplication.shared.keyWindow?.rootViewController else {
 *                completion(nil, "failed", "no_view_controller")
 *                return
 *            }
 *
 *            financialConnectionsSheet.present(from: rootVC) { result in
 *                switch result {
 *                case .completed(let session):
 *                    let sessionData: [String: Any] = [
 *                        "id": session.id,
 *                        "clientSecret": session.clientSecret,
 *                        "livemode": session.livemode,
 *                        "accounts": session.accounts.map { account in
 *                            [
 *                                "id": account.id,
 *                                "institutionName": account.institutionName,
 *                                "displayName": account.displayName ?? "",
 *                                "last4": account.last4 ?? "",
 *                                "created": account.created.timeIntervalSince1970,
 *                                "livemode": account.livemode,
 *                                "category": account.category.rawValue,
 *                                "status": account.status.rawValue,
 *                                "supportedPaymentMethodTypes": account.supportedPaymentMethodTypes
 *                            ]
 *                        }
 *                    ]
 *                    completion(sessionData, "completed", nil)
 *
 *                case .canceled:
 *                    completion(nil, "canceled", nil)
 *
 *                case .failed(let error):
 *                    completion(nil, "failed", error.localizedDescription)
 *                }
 *            }
 *        }
 *    }
 *    ```
 *
 * 3. **Call the bridge from Kotlin** (you'll need to add cinterop definitions)
 *
 * This placeholder implementation returns an error with setup instructions.
 */
public actual class FinancialConnectionsSheet private constructor(
    private val configuration: FinancialConnectionsSheetConfiguration
) {
    public actual companion object {
        /**
         * Create a new Financial Connections Sheet instance.
         *
         * @param configuration The configuration for the sheet
         * @return A new FinancialConnectionsSheet instance
         */
        public actual fun create(
            configuration: FinancialConnectionsSheetConfiguration
        ): FinancialConnectionsSheet {
            return FinancialConnectionsSheet(configuration)
        }
    }

    /**
     * Present the Financial Connections Sheet to the user.
     *
     * On iOS, this requires a Swift bridge. See class documentation for setup instructions.
     *
     * @return The result of the Financial Connections flow
     */
    public actual suspend fun present(): FinancialConnectionsSheetResult {
        return FinancialConnectionsSheetResult.Failed(
            error = StripeException(
                message = "iOS FinancialConnectionsSheet requires Swift bridge integration. " +
                        "See FinancialConnectionsSheet class documentation for setup instructions. " +
                        "The CocoaPods pod 'StripeFinancialConnections' is configured and ready to use.",
                stripeError = null,
                statusCode = null,
                requestId = null,
                cause = null
            )
        )
    }

    /**
     * Present the Financial Connections Sheet and create a token.
     *
     * On iOS, this requires a Swift bridge. See class documentation for setup instructions.
     *
     * @return The result of the Financial Connections flow with token
     */
    public actual suspend fun presentForToken(): FinancialConnectionsSheetForTokenResult {
        return FinancialConnectionsSheetForTokenResult.Failed(
            error = StripeException(
                message = "iOS FinancialConnectionsSheet requires Swift bridge integration. " +
                        "See FinancialConnectionsSheet class documentation for setup instructions. " +
                        "The CocoaPods pod 'StripeFinancialConnections' is configured and ready to use.",
                stripeError = null,
                statusCode = null,
                requestId = null,
                cause = null
            )
        )
    }
}
