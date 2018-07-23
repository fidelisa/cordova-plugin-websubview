package com.websubview;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.text.TextUtils;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class WebSubViewPlugin extends CordovaPlugin{

    private  CallbackContext callbackContext;

    HashMap<String, WebView> mywebViews = new HashMap<String, WebView>();
    int tagMax;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        tagMax = 0;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        switch (action) {
            case "isAvailable":
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
                return true;

            case "show": {
                String tagName = args.getString(0);
                this.show(tagName);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
                return true;
            }

            case "hide": {
                String tagName = args.getString(0);
                this.hide(tagName);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
                return true;
            }

            case "remove": {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
                return true;
            }

            case "moveHorizontal": {
                String tagName = args.getString(0);
                JSONObject options = args.getJSONObject(1);
                int pixels = dpToPixels(options.optInt("pixels"));
                int duration = options.optInt("duration");
                this.moveHorizontal(tagName, pixels, duration);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
                return true;
            }

            case "back": {
                String tagName = args.getString(0);
                this.back(tagName);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
                return true;
            }

            case "load": {
                final JSONObject options = args.getJSONObject(0);
                final String url = options.optString("url");
                if(TextUtils.isEmpty(url)){
                    JSONObject result = new JSONObject();
                    result.put("error", "expected argument 'url' to be non empty string.");
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, result);
                    callbackContext.sendPluginResult(pluginResult);
                    return true;
                }

                int topBar = dpToPixels(options.optInt("top"));
                int bottomBar = dpToPixels(options.optInt("bottom"));
                final String css = options.optString("css");


                PluginResult pluginResult;
                JSONObject result = new JSONObject();
                try {
                    this.load(url, topBar, bottomBar, css);
                    result.put("event", "opened");
                    result.put("tag", String.valueOf(tagMax));

                    pluginResult = new PluginResult(PluginResult.Status.OK, result);
                    pluginResult.setKeepCallback(true);
                    this.callbackContext = callbackContext;
                } catch (Exception ex) {
                    result.put("error", ex.getMessage());
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, result);
                }

                callbackContext.sendPluginResult(pluginResult);
                return true;
            }
        }
        return false;
    }


    private int dpToPixels(int dipValue) {
        int value = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP,
                (float) dipValue,
                cordova.getActivity().getResources().getDisplayMetrics()
        );

        return value;
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = cordova.getActivity().getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private void load(final String url, final int topBar, final int bottomBar, final String injectCss)  {
        final String keyValue = String.valueOf(++tagMax);

        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                WebView myWebView = new WebView(cordova.getActivity());
                mywebViews.put(keyValue, myWebView);

                myWebView.getSettings().setJavaScriptEnabled(true);
                myWebView.setLayoutParams(new LinearLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));

                WebSettings settings = myWebView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setJavaScriptCanOpenWindowsAutomatically(true);
                //TODO ?? settings.setBuiltInZoomControls(showZoomControls);
                settings.setPluginState(android.webkit.WebSettings.PluginState.ON);

                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    //TODO ??     settings.setMediaPlaybackRequiresUserGesture(mediaPlaybackRequiresUserGesture);
                }

                String overrideUserAgent = preferences.getString("OverrideUserAgent", null);
                String appendUserAgent = preferences.getString("AppendUserAgent", null);

                if (overrideUserAgent != null) {
                    settings.setUserAgentString(overrideUserAgent);
                }
                if (appendUserAgent != null) {
                    settings.setUserAgentString(settings.getUserAgentString() + appendUserAgent);
                }

                settings.setSupportMultipleWindows(true);
                myWebView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, android.os.Message resultMsg)
                    {
                        WebView.HitTestResult result = view.getHitTestResult();
                        String data = result.getExtra();
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
                        cordova.getActivity().startActivity(browserIntent);
                        return false;
                    }
                });

                myWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onLoadResource(WebView view, String url) {
                        if (injectCss != "" && url.contains(".css")) {

                            view.loadUrl("javascript:(function() {" +
                                    "var parent = document.getElementsByTagName('head').item(0);" +
                                    "var style = document.createElement('style');" +
                                    "style.type = 'text/css';" +

                                    "style.innerHTML = '" + injectCss + "';" +
                                    "parent.appendChild(style);" +
                                    "})()");
                        }
                        super.onLoadResource(view, url);

                    }


                    public void onPageFinished(WebView view, String url) {

                        JSONObject result = new JSONObject();
                        try {
                            result.put("event","loaded");
                            result.put("url", url) ;
                            result.put("canGoBack",view.canGoBack());

                            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
                            pluginResult.setKeepCallback(true);
                            callbackContext.sendPluginResult(pluginResult);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        super.onPageFinished(view, url);
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

                        return super.shouldOverrideUrlLoading(view, request);
                    }
                });

                myWebView.loadUrl(url);
                myWebView.getSettings().setLoadWithOverviewMode(true);
                myWebView.getSettings().setUseWideViewPort(true);
                myWebView.requestFocus();
                myWebView.requestFocusFromTouch();


                View rootView = webView.getView();
                FrameLayout layout = (FrameLayout) rootView.getParent();
                int rootHeight = rootView.getHeight();
                int rootWidth= rootView.getWidth();

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(rootWidth, rootHeight - topBar - bottomBar);
                params.setMargins(0, topBar, 0, bottomBar);
                myWebView.setLayoutParams(params);
                layout.addView(myWebView);

            }
        });

    }


    private void moveHorizontal(final String tagName, final int pixels, final int duration) {

        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                WebView viewTag = mywebViews.get(tagName);
                if (viewTag != null) {
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) viewTag.getLayoutParams();
                    layoutParams.leftMargin = pixels;
                    viewTag.setLayoutParams(layoutParams);
                }
            }
        });

    }

    private void show(final String tagName) {

        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                WebView viewTag = mywebViews.get(tagName);
                if (viewTag != null) {
                    viewTag.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void back(final String tagName) {

        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                WebView viewTag = mywebViews.get(tagName);
                if (viewTag != null) {
                    viewTag.goBack();
                }
            }
        });

    }

    private void hide(final String tagName) {

        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                WebView viewTag = mywebViews.get(tagName);
                if (viewTag != null) {
                    viewTag.setVisibility(View.GONE);
                }
            }
        });

    }

    private int getIdentifier(String name, String type) {
        final Activity activity = cordova.getActivity();
        return activity.getResources().getIdentifier(name, type, activity.getPackageName());
    }

}
