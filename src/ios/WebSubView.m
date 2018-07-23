#import "WebSubView.h"

@implementation WebSubView
{
    NSMutableDictionary *mywebViews;
    int tagMax;
}


- (void) isAvailable:(CDVInvokedUrlCommand*)command {
    bool avail = NSClassFromString(@"WKWebView") != nil;
    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:avail];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) load:(CDVInvokedUrlCommand*)command {
    NSDictionary* options = [command.arguments objectAtIndex:0];
    NSString* urlString = options[@"url"];
    if (urlString == nil) {
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"url can't be empty"] callbackId:command.callbackId];
        return;
    }
    if (![[urlString lowercaseString] hasPrefix:@"http"]) {
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"url must start with http or https"] callbackId:command.callbackId];
        return;
    }
    NSURL *url = [NSURL URLWithString:urlString];
    if (url == nil) {
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"bad url"] callbackId:command.callbackId];
        return;
    }
    
    NSString *cssInject = options[@"css"];

    self.animated = [options[@"animated"] isEqual:[NSNumber numberWithBool:YES]];
    self.callbackId = command.callbackId;
    
    WKWebViewConfiguration *theConfiguration = [[WKWebViewConfiguration alloc] init];
    
    if (cssInject != nil) {
        NSString *codeScript = @"var parent = document.getElementsByTagName('head').item(0); var style = document.createElement('style'); style.type = 'text/css'; style.innerHTML = '%@'; parent.appendChild(style);";
        NSString *userScript = [NSString stringWithFormat:codeScript, cssInject ];
//        NSString *myScriptSource = @"alert('Hello, World!')";
        
        WKUserScript *s = [[WKUserScript alloc] initWithSource:userScript injectionTime:WKUserScriptInjectionTimeAtDocumentEnd forMainFrameOnly:YES];
        WKUserContentController *c = [[WKUserContentController alloc] init];
        [c addUserScript:s];
        
        theConfiguration.userContentController = c;
//

//        [webView evaluateJavaScript:userScript completionHandler:nil];
    }
    

    
    WKWebView *mywebView = [[WKWebView alloc] initWithFrame:self.viewController.view.frame configuration:theConfiguration];
    [mywebView setNavigationDelegate:self];
    
    NSString *tagName = [NSString stringWithFormat:@"%d", ++tagMax ];
    if (mywebViews == nil) {
        mywebViews = [NSMutableDictionary dictionary];
    }
    [mywebViews setObject:mywebView forKey:tagName];
    
    long topBar = options[@"top"]  ? [options[@"top"] integerValue]: 0;
    long bottomBar = options[@"bottom"]  ? [options[@"bottom"] integerValue]: 0;
    
    
    
    mywebView.frame = CGRectMake(0, topBar, self.viewController.view.frame.size.width, self.viewController.view.frame.size.height - topBar - bottomBar);
    NSURL *urlbis = [NSURL URLWithString:urlString];
    NSMutableURLRequest *urlReq=[NSMutableURLRequest requestWithURL:urlbis];
    
    [mywebView loadRequest:urlReq];
    
    [self.viewController.view addSubview:mywebView];
    
    
    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{@"event":@"opened", @"tag":tagName}];
    [pluginResult setKeepCallback:[NSNumber numberWithBool:YES]];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
}

- (void) show:(CDVInvokedUrlCommand*)command {
    NSString *tagName = [command argumentAtIndex:0];
    WKWebView *viewTag = mywebViews[tagName];
    if (viewTag != nil) {
        viewTag.hidden=NO;
    }
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

- (void) hide:(CDVInvokedUrlCommand*)command {
    NSString *tagName = [command argumentAtIndex:0];
    WKWebView *viewTag = mywebViews[tagName];
    if (viewTag != nil) {
        viewTag.hidden=YES;
    }
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}


- (void) back:(CDVInvokedUrlCommand*)command {
    NSString *tagName = [command argumentAtIndex:0];
    WKWebView *viewTag = mywebViews[tagName];
    if (viewTag != nil) {
        [viewTag goBack];
    }
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

- (void) remove:(CDVInvokedUrlCommand*)command {
    NSString *tagName = [command argumentAtIndex:0];
    WKWebView *viewTag = mywebViews[tagName];
    if (viewTag != nil) {
        [viewTag removeFromSuperview];
    }
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

- (void) moveHorizontal:(CDVInvokedUrlCommand*)command {
    NSString *tagName = [command argumentAtIndex:0];
    NSDictionary* options = [command.arguments objectAtIndex:1];
    NSString *pixels = options[@"pixels"];
    NSString *duration = options[@"duration"];
    
    WKWebView *viewTag = mywebViews[tagName];
    if (viewTag != nil) {
        long pixelsValue = pixels  ? [pixels integerValue]: 0;
        float durationValue = duration  ? [duration floatValue]: 0;
        CGRect newFrame = viewTag.frame;
        newFrame.origin.x += pixelsValue;
        
        [UIView animateWithDuration:durationValue
                         animations:^{
                             viewTag.frame = newFrame;
                         }];
        
        
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
    }
}

# pragma mark - WKNavigationDelegate
- (void)webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation {
    NSString *url = [webView.URL absoluteString];
    
       CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{@"event":@"loaded", @"url":url, @"canGoBack":[NSNumber numberWithBool:webView.canGoBack]}];
    [pluginResult setKeepCallback:[NSNumber numberWithBool:YES]];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
}

- (void)webView:(WKWebView *)webView decidePolicyForNavigationAction:(WKNavigationAction *)navigationAction decisionHandler:(void (^)(WKNavigationActionPolicy))decisionHandler
{
    //this is a 'new window action' (aka target="_blank") > open this URL externally. If we´re doing nothing here, WKWebView will also just do nothing. Maybe this will change in a later stage of the iOS 8 Beta
    if (!navigationAction.targetFrame) {
        NSURL *url = navigationAction.request.URL;
        UIApplication *app = [UIApplication sharedApplication];
        if ([app canOpenURL:url]) {
            [app openURL:url];
        }
    }
    decisionHandler(WKNavigationActionPolicyAllow);
}
//- (void)webView:(WKWebView *)webView didStartProvisionalNavigation:(WKNavigation *)navigation {
//
//    NSString *url = [webView.URL absoluteString];
//
//    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{@"event":@"loading", @"url":url, @"canGoBack":[NSNumber numberWithBool:webView.canGoBack]}];
//    [pluginResult setKeepCallback:[NSNumber numberWithBool:YES]];
//    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
//    
//}

@end
