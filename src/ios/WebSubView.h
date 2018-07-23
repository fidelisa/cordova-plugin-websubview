#import <Cordova/CDVPlugin.h>
#import <SafariServices/SafariServices.h>
#import <WebKit/WebKit.h>

@protocol ActivityItemProvider

- (NSArray<UIActivity *> *)safariViewController:(SFSafariViewController *)controller
                            activityItemsForURL:(NSURL *)URL
                                          title:(nullable NSString *)title;

@end

@interface WebSubView : CDVPlugin<WKNavigationDelegate>

@property (nonatomic, copy) NSString* callbackId;
@property (nonatomic) bool animated;
@property (nonatomic) id<ActivityItemProvider> activityItemProvider;

- (void) isAvailable:(CDVInvokedUrlCommand*)command;
- (void) load:(CDVInvokedUrlCommand*)command;
- (void) show:(CDVInvokedUrlCommand*)command;
- (void) hide:(CDVInvokedUrlCommand*)command;
- (void) remove:(CDVInvokedUrlCommand*)command;

@end
