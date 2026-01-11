#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface StripeBridge : NSObject

+ (StripeBridge * _Nonnull)shared;

- (void)initializeWithPublishableKey:(NSString * _Nonnull)publishableKey;

- (void)createCardTokenWithNumber:(NSString * _Nonnull)number
                         expMonth:(NSInteger)expMonth
                          expYear:(NSInteger)expYear
                              cvc:(NSString * _Nullable)cvc
                             name:(NSString * _Nullable)name
                     addressLine1:(NSString * _Nullable)addressLine1
                     addressLine2:(NSString * _Nullable)addressLine2
                      addressCity:(NSString * _Nullable)addressCity
                     addressState:(NSString * _Nullable)addressState
                       addressZip:(NSString * _Nullable)addressZip
                   addressCountry:(NSString * _Nullable)addressCountry
                       completion:(void (^ _Nonnull)(NSString * _Nullable tokenId,
                                                     NSString * _Nullable error,
                                                     NSString * _Nullable last4,
                                                     NSInteger expMonth,
                                                     NSInteger expYear,
                                                     NSString * _Nullable brand,
                                                     NSString * _Nullable funding,
                                                     NSString * _Nullable country))completion;

- (void)createBankAccountTokenWithCountry:(NSString * _Nonnull)country
                                  currency:(NSString * _Nonnull)currency
                             accountNumber:(NSString * _Nonnull)accountNumber
                             routingNumber:(NSString * _Nullable)routingNumber
                         accountHolderName:(NSString * _Nullable)accountHolderName
                         accountHolderType:(NSString * _Nullable)accountHolderType
                                completion:(void (^ _Nonnull)(NSString * _Nullable tokenId,
                                                              NSString * _Nullable error,
                                                              NSString * _Nullable last4,
                                                              NSString * _Nullable bankName,
                                                              NSString * _Nullable routingNumber))completion;

- (void)createPaymentMethodWithCardNumber:(NSString * _Nonnull)cardNumber
                                  expMonth:(NSInteger)expMonth
                                   expYear:(NSInteger)expYear
                                       cvc:(NSString * _Nullable)cvc
                               billingName:(NSString * _Nullable)billingName
                              billingEmail:(NSString * _Nullable)billingEmail
                              billingPhone:(NSString * _Nullable)billingPhone
                       billingAddressLine1:(NSString * _Nullable)billingAddressLine1
                       billingAddressLine2:(NSString * _Nullable)billingAddressLine2
                        billingAddressCity:(NSString * _Nullable)billingAddressCity
                       billingAddressState:(NSString * _Nullable)billingAddressState
                         billingAddressZip:(NSString * _Nullable)billingAddressZip
                     billingAddressCountry:(NSString * _Nullable)billingAddressCountry
                                completion:(void (^ _Nonnull)(NSString * _Nullable paymentMethodId,
                                                              NSString * _Nullable error,
                                                              NSString * _Nullable last4,
                                                              NSInteger expMonth,
                                                              NSInteger expYear,
                                                              NSString * _Nullable brand,
                                                              NSString * _Nullable funding))completion;

- (void)retrievePaymentMethodWithPaymentMethodId:(NSString * _Nonnull)paymentMethodId
                                       completion:(void (^ _Nonnull)(NSString * _Nullable paymentMethodId,
                                                                     NSString * _Nullable error,
                                                                     NSString * _Nullable last4,
                                                                     NSInteger expMonth,
                                                                     NSInteger expYear,
                                                                     NSString * _Nullable brand,
                                                                     NSString * _Nullable funding))completion;

- (void)retrievePaymentIntentWithClientSecret:(NSString * _Nonnull)clientSecret
                                    completion:(void (^ _Nonnull)(NSString * _Nullable paymentIntentId,
                                                                  int64_t amount,
                                                                  NSString * _Nullable status,
                                                                  NSString * _Nullable error))completion;

- (void)confirmPaymentIntentWithClientSecret:(NSString * _Nonnull)clientSecret
                              paymentMethodId:(NSString * _Nullable)paymentMethodId
                                    returnURL:(NSString * _Nullable)returnURL
                                   completion:(void (^ _Nonnull)(NSString * _Nullable paymentIntentId,
                                                                 int64_t amount,
                                                                 NSString * _Nullable status,
                                                                 NSString * _Nullable error))completion;

- (void)handleNextActionForPaymentWithClientSecret:(NSString * _Nonnull)clientSecret
                                          returnURL:(NSString * _Nullable)returnURL
                                         completion:(void (^ _Nonnull)(NSString * _Nullable paymentIntentId,
                                                                       int64_t amount,
                                                                       NSString * _Nullable status,
                                                                       NSString * _Nullable error))completion;

- (void)retrieveSetupIntentWithClientSecret:(NSString * _Nonnull)clientSecret
                                  completion:(void (^ _Nonnull)(NSString * _Nullable setupIntentId,
                                                                NSString * _Nullable status,
                                                                NSString * _Nullable error))completion;

- (void)confirmSetupIntentWithClientSecret:(NSString * _Nonnull)clientSecret
                            paymentMethodId:(NSString * _Nullable)paymentMethodId
                                  returnURL:(NSString * _Nullable)returnURL
                                 completion:(void (^ _Nonnull)(NSString * _Nullable setupIntentId,
                                                               NSString * _Nullable status,
                                                               NSString * _Nullable error))completion;

- (void)handleNextActionForSetupIntentWithClientSecret:(NSString * _Nonnull)clientSecret
                                              returnURL:(NSString * _Nullable)returnURL
                                             completion:(void (^ _Nonnull)(NSString * _Nullable setupIntentId,
                                                                           NSString * _Nullable status,
                                                                           NSString * _Nullable error))completion;

- (void)presentPaymentSheetWithClientSecret:(NSString * _Nonnull)clientSecret
                         merchantDisplayName:(NSString * _Nonnull)merchantDisplayName
                      customerEphemeralKeySecret:(NSString * _Nullable)customerEphemeralKeySecret
                                      customerId:(NSString * _Nullable)customerId
                                      completion:(void (^ _Nonnull)(NSString * _Nonnull status,
                                                                    NSString * _Nullable error))completion;

- (void)createSourceWithType:(NSString * _Nonnull)type
                       amount:(int64_t)amount
                     currency:(NSString * _Nonnull)currency
                   completion:(void (^ _Nonnull)(NSString * _Nullable sourceId,
                                                 NSString * _Nullable sourceType,
                                                 NSString * _Nullable status,
                                                 NSString * _Nullable error))completion;

- (void)retrieveSourceWithSourceId:(NSString * _Nonnull)sourceId
                       clientSecret:(NSString * _Nonnull)clientSecret
                         completion:(void (^ _Nonnull)(NSString * _Nullable sourceId,
                                                       NSString * _Nullable sourceType,
                                                       NSString * _Nullable status,
                                                       NSString * _Nullable error))completion;

- (void)setPresentationContext:(UIViewController * _Nonnull)viewController;

@end
