import Foundation
import StripePaymentSheet
import UIKit

@objc public class StripeBridge: NSObject {

    @objc public static let shared = StripeBridge()

    private var paymentSheet: PaymentSheet?
    private var presentationContext: UIViewController?

    private override init() {
        super.init()
    }

    // MARK: - Initialization

    @objc public func initialize(publishableKey: String) {
        StripeAPI.defaultPublishableKey = publishableKey
    }

    // MARK: - Token Creation

    @objc public func createCardToken(
        number: String,
        expMonth: Int,
        expYear: Int,
        cvc: String?,
        name: String?,
        addressLine1: String?,
        addressLine2: String?,
        addressCity: String?,
        addressState: String?,
        addressZip: String?,
        addressCountry: String?,
        completion: @escaping (String?, String?, String?, Int, Int, String?, String?, String?) -> Void
    ) {
        let cardParams = STPCardParams()
        cardParams.number = number
        cardParams.expMonth = UInt(expMonth)
        cardParams.expYear = UInt(expYear)
        cardParams.cvc = cvc
        cardParams.name = name
        cardParams.addressLine1 = addressLine1
        cardParams.addressLine2 = addressLine2
        cardParams.addressCity = addressCity
        cardParams.addressState = addressState
        cardParams.addressZip = addressZip
        cardParams.addressCountry = addressCountry

        STPAPIClient.shared.createToken(withCard: cardParams) { token, error in
            if let token = token, let card = token.card {
                completion(
                    token.tokenId,
                    nil,
                    card.last4,
                    Int(card.expMonth),
                    Int(card.expYear),
                    card.brand.stringValue,
                    card.funding.stringValue,
                    card.country
                )
            } else {
                completion(nil, error?.localizedDescription ?? "Unknown error", nil, 0, 0, nil, nil, nil)
            }
        }
    }

    @objc public func createBankAccountToken(
        country: String,
        currency: String,
        accountNumber: String,
        routingNumber: String?,
        accountHolderName: String?,
        accountHolderType: String?,
        completion: @escaping (String?, String?, String?, String?, String?) -> Void
    ) {
        let bankParams = STPBankAccountParams()
        bankParams.country = country
        bankParams.currency = currency
        bankParams.accountNumber = accountNumber
        bankParams.routingNumber = routingNumber
        bankParams.accountHolderName = accountHolderName

        if let holderType = accountHolderType {
            bankParams.accountHolderType = holderType == "individual" ? .individual : .company
        }

        STPAPIClient.shared.createToken(withBankAccount: bankParams) { token, error in
            if let token = token, let bankAccount = token.bankAccount {
                completion(
                    token.tokenId,
                    nil,
                    bankAccount.last4,
                    bankAccount.bankName,
                    bankAccount.routingNumber
                )
            } else {
                completion(nil, error?.localizedDescription ?? "Unknown error", nil, nil, nil)
            }
        }
    }

    // MARK: - PaymentMethod

    @objc public func createPaymentMethod(
        cardNumber: String,
        expMonth: Int,
        expYear: Int,
        cvc: String?,
        billingName: String?,
        billingEmail: String?,
        billingPhone: String?,
        billingAddressLine1: String?,
        billingAddressLine2: String?,
        billingAddressCity: String?,
        billingAddressState: String?,
        billingAddressZip: String?,
        billingAddressCountry: String?,
        completion: @escaping (String?, String?, String?, Int, Int, String?, String?) -> Void
    ) {
        let cardParams = STPPaymentMethodCardParams()
        cardParams.number = cardNumber
        cardParams.expMonth = NSNumber(value: expMonth)
        cardParams.expYear = NSNumber(value: expYear)
        cardParams.cvc = cvc

        let billingDetails = STPPaymentMethodBillingDetails()
        billingDetails.name = billingName
        billingDetails.email = billingEmail
        billingDetails.phone = billingPhone

        if billingAddressLine1 != nil || billingAddressCity != nil || billingAddressState != nil ||
           billingAddressZip != nil || billingAddressCountry != nil {
            let address = STPPaymentMethodAddress()
            address.line1 = billingAddressLine1
            address.line2 = billingAddressLine2
            address.city = billingAddressCity
            address.state = billingAddressState
            address.postalCode = billingAddressZip
            address.country = billingAddressCountry
            billingDetails.address = address
        }

        let paymentMethodParams = STPPaymentMethodParams(
            card: cardParams,
            billingDetails: billingDetails,
            metadata: nil
        )

        Task {
            do {
                let paymentMethod = try await STPAPIClient.shared.createPaymentMethod(with: paymentMethodParams, additionalPaymentUserAgentValues: [])
                if let card = paymentMethod.card {
                    completion(
                        paymentMethod.stripeId,
                        nil,
                        card.last4,
                        card.expMonth,
                        card.expYear,
                        card.brand.stringValue,
                        card.funding
                    )
                } else {
                    completion(nil, "No card data in payment method", nil, 0, 0, nil, nil)
                }
            } catch {
                completion(nil, error.localizedDescription, nil, 0, 0, nil, nil)
            }
        }
    }

    @objc public func retrievePaymentMethod(
        paymentMethodId: String,
        completion: @escaping (String?, String?, String?, Int, Int, String?, String?) -> Void
    ) {
        // Note: Client-side retrieval is not supported by Stripe for security reasons
        // This should be done server-side
        completion(nil, "Payment method retrieval requires server-side implementation", nil, 0, 0, nil, nil)
    }

    // MARK: - PaymentIntent

    @objc public func retrievePaymentIntent(
        clientSecret: String,
        completion: @escaping (String?, Int64, String?, String?) -> Void
    ) {
        STPAPIClient.shared.retrievePaymentIntent(withClientSecret: clientSecret) { paymentIntent, error in
            if let pi = paymentIntent {
                completion(
                    pi.stripeId,
                    Int64(pi.amount),
                    pi.status.stringValue,
                    nil
                )
            } else {
                completion(nil, 0, nil, error?.localizedDescription ?? "Unknown error")
            }
        }
    }

    @objc public func confirmPaymentIntent(
        clientSecret: String,
        paymentMethodId: String?,
        returnURL: String?,
        completion: @escaping (String?, Int64, String?, String?) -> Void
    ) {
        let paymentIntentParams = STPPaymentIntentParams(clientSecret: clientSecret)
        paymentIntentParams.paymentMethodId = paymentMethodId
        paymentIntentParams.returnURL = returnURL

        guard let viewController = self.presentationContext else {
            completion(nil, 0, nil, "No presentation context set. Call setPresentationContext first.")
            return
        }

        let paymentHandler = STPPaymentHandler.shared()
        paymentHandler.confirmPayment(paymentIntentParams, with: viewController) { status, paymentIntent, error in
            switch status {
            case .succeeded:
                if let pi = paymentIntent {
                    completion(pi.stripeId, Int64(pi.amount), pi.status.stringValue, nil)
                } else {
                    completion(nil, 0, "succeeded", nil)
                }
            case .canceled:
                completion(nil, 0, "canceled", nil)
            case .failed:
                completion(nil, 0, "failed", error?.localizedDescription ?? "Unknown error")
            @unknown default:
                completion(nil, 0, "unknown", nil)
            }
        }
    }

    @objc public func handleNextActionForPayment(
        clientSecret: String,
        returnURL: String?,
        completion: @escaping (String?, Int64, String?, String?) -> Void
    ) {
        guard let viewController = self.presentationContext else {
            completion(nil, 0, nil, "No presentation context set. Call setPresentationContext first.")
            return
        }

        let paymentHandler = STPPaymentHandler.shared()
        paymentHandler.handleNextAction(forPayment: clientSecret, with: viewController, returnURL: returnURL) { status, paymentIntent, error in
            switch status {
            case .succeeded:
                if let pi = paymentIntent {
                    completion(pi.stripeId, Int64(pi.amount), pi.status.stringValue, nil)
                } else {
                    completion(nil, 0, "succeeded", nil)
                }
            case .canceled:
                completion(nil, 0, "canceled", nil)
            case .failed:
                completion(nil, 0, "failed", error?.localizedDescription ?? "Unknown error")
            @unknown default:
                completion(nil, 0, "unknown", nil)
            }
        }
    }

    // MARK: - SetupIntent

    @objc public func retrieveSetupIntent(
        clientSecret: String,
        completion: @escaping (String?, String?, String?) -> Void
    ) {
        STPAPIClient.shared.retrieveSetupIntent(withClientSecret: clientSecret) { setupIntent, error in
            if let si = setupIntent {
                completion(
                    si.stripeID,
                    si.status.stringValue,
                    nil
                )
            } else {
                completion(nil, nil, error?.localizedDescription ?? "Unknown error")
            }
        }
    }

    @objc public func confirmSetupIntent(
        clientSecret: String,
        paymentMethodId: String?,
        returnURL: String?,
        completion: @escaping (String?, String?, String?) -> Void
    ) {
        let setupIntentParams = STPSetupIntentConfirmParams(clientSecret: clientSecret)
        setupIntentParams.paymentMethodID = paymentMethodId
        setupIntentParams.returnURL = returnURL

        guard let viewController = self.presentationContext else {
            completion(nil, nil, "No presentation context set. Call setPresentationContext first.")
            return
        }

        let paymentHandler = STPPaymentHandler.shared()
        paymentHandler.confirmSetupIntent(setupIntentParams, with: viewController) { status, setupIntent, error in
            switch status {
            case .succeeded:
                if let si = setupIntent {
                    completion(si.stripeID, si.status.stringValue, nil)
                } else {
                    completion(nil, "succeeded", nil)
                }
            case .canceled:
                completion(nil, "canceled", nil)
            case .failed:
                completion(nil, "failed", error?.localizedDescription ?? "Unknown error")
            @unknown default:
                completion(nil, "unknown", nil)
            }
        }
    }

    @objc public func handleNextActionForSetupIntent(
        clientSecret: String,
        returnURL: String?,
        completion: @escaping (String?, String?, String?) -> Void
    ) {
        guard let viewController = self.presentationContext else {
            completion(nil, nil, "No presentation context set. Call setPresentationContext first.")
            return
        }

        let paymentHandler = STPPaymentHandler.shared()
        paymentHandler.handleNextAction(forSetupIntent: clientSecret, with: viewController, returnURL: returnURL) { status, setupIntent, error in
            switch status {
            case .succeeded:
                if let si = setupIntent {
                    completion(si.stripeID, si.status.stringValue, nil)
                } else {
                    completion(nil, "succeeded", nil)
                }
            case .canceled:
                completion(nil, "canceled", nil)
            case .failed:
                completion(nil, "failed", error?.localizedDescription ?? "Unknown error")
            @unknown default:
                completion(nil, "unknown", nil)
            }
        }
    }

    // MARK: - PaymentSheet

    @objc public func presentPaymentSheet(
        clientSecret: String,
        merchantDisplayName: String,
        customerEphemeralKeySecret: String?,
        customerId: String?,
        completion: @escaping (String, String?) -> Void
    ) {
        var configuration = PaymentSheet.Configuration()
        configuration.merchantDisplayName = merchantDisplayName

        if let ephemeralKey = customerEphemeralKeySecret, let custId = customerId {
            configuration.customer = .init(id: custId, ephemeralKeySecret: ephemeralKey)
        }

        guard let viewController = self.presentationContext else {
            completion("failed", "No presentation context set. Call setPresentationContext first.")
            return
        }

        paymentSheet = PaymentSheet(
            paymentIntentClientSecret: clientSecret,
            configuration: configuration
        )

        paymentSheet?.present(from: viewController) { result in
            switch result {
            case .completed:
                completion("completed", nil)
            case .canceled:
                completion("canceled", nil)
            case .failed(let error):
                completion("failed", error.localizedDescription)
            }
        }
    }

    // MARK: - Source (Legacy)

    @objc public func createSource(
        type: String,
        amount: Int64,
        currency: String,
        completion: @escaping (String?, String?, String?, String?) -> Void
    ) {
        let sourceParams = STPSourceParams()
        sourceParams.type = STPSourceType.fromString(type)
        sourceParams.amount = NSNumber(value: amount)
        sourceParams.currency = currency

        STPAPIClient.shared.createSource(with: sourceParams) { source, error in
            if let source = source {
                completion(
                    source.stripeID,
                    source.type.stringValue,
                    source.status.stringValue,
                    nil
                )
            } else {
                completion(nil, nil, nil, error?.localizedDescription ?? "Unknown error")
            }
        }
    }

    @objc public func retrieveSource(
        sourceId: String,
        clientSecret: String,
        completion: @escaping (String?, String?, String?, String?) -> Void
    ) {
        STPAPIClient.shared.retrieveSource(withId: sourceId, clientSecret: clientSecret) { source, error in
            if let source = source {
                completion(
                    source.stripeID,
                    source.type.stringValue,
                    source.status.stringValue,
                    nil
                )
            } else {
                completion(nil, nil, nil, error?.localizedDescription ?? "Unknown error")
            }
        }
    }

    // MARK: - Presentation Context

    @objc public func setPresentationContext(_ viewController: UIViewController) {
        self.presentationContext = viewController
    }
}

// MARK: - Extensions for String Conversion

extension STPCardBrand {
    var stringValue: String {
        switch self {
        case .visa: return "visa"
        case .mastercard: return "mastercard"
        case .amex: return "amex"
        case .discover: return "discover"
        case .JCB: return "jcb"
        case .dinersClub: return "diners"
        case .unionPay: return "unionpay"
        case .cartesBancaires: return "cartes_bancaires"
        case .unknown: return "unknown"
        @unknown default: return "unknown"
        }
    }
}

extension STPCardFundingType {
    var stringValue: String {
        switch self {
        case .debit: return "debit"
        case .credit: return "credit"
        case .prepaid: return "prepaid"
        case .other: return "unknown"
        @unknown default: return "unknown"
        }
    }
}

extension STPPaymentIntentStatus {
    var stringValue: String {
        switch self {
        case .requiresPaymentMethod: return "requires_payment_method"
        case .requiresConfirmation: return "requires_confirmation"
        case .requiresAction: return "requires_action"
        case .processing: return "processing"
        case .succeeded: return "succeeded"
        case .requiresCapture: return "requires_capture"
        case .canceled: return "canceled"
        case .unknown: return "unknown"
        @unknown default: return "unknown"
        }
    }
}

extension STPSetupIntentStatus {
    var stringValue: String {
        switch self {
        case .requiresPaymentMethod: return "requires_payment_method"
        case .requiresConfirmation: return "requires_confirmation"
        case .requiresAction: return "requires_action"
        case .processing: return "processing"
        case .succeeded: return "succeeded"
        case .canceled: return "canceled"
        case .unknown: return "unknown"
        @unknown default: return "unknown"
        }
    }
}

extension STPSourceType {
    static func fromString(_ string: String) -> STPSourceType {
        switch string {
        case "bancontact": return .bancontact
        case "card": return .card
        case "giropay": return .giropay
        case "ideal": return .iDEAL
        case "sepa_debit": return .SEPADebit
        case "sofort": return .sofort
        case "three_d_secure": return .threeDSecure
        case "alipay": return .alipay
        case "eps": return .EPS
        case "multibanco": return .multibanco
        case "klarna": return .klarna
        default: return .unknown
        }
    }

    var stringValue: String {
        switch self {
        case .bancontact: return "bancontact"
        case .card: return "card"
        case .giropay: return "giropay"
        case .iDEAL: return "ideal"
        case .SEPADebit: return "sepa_debit"
        case .sofort: return "sofort"
        case .threeDSecure: return "three_d_secure"
        case .alipay: return "alipay"
        case .EPS: return "eps"
        case .multibanco: return "multibanco"
        case .klarna: return "klarna"
        case .unknown: return "unknown"
        @unknown default: return "unknown"
        }
    }
}

extension STPSourceStatus {
    var stringValue: String {
        switch self {
        case .pending: return "pending"
        case .chargeable: return "chargeable"
        case .consumed: return "consumed"
        case .canceled: return "canceled"
        case .failed: return "failed"
        case .unknown: return "unknown"
        @unknown default: return "unknown"
        }
    }
}

// MARK: - UIViewController STPAuthenticationContext Conformance

extension UIViewController: STPAuthenticationContext {
    public func authenticationPresentingViewController() -> UIViewController {
        return self
    }
}
