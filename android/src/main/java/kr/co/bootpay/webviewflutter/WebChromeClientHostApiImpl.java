// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package kr.co.bootpay.webviewflutter;

import android.os.Build;
import android.os.Message;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import kr.co.bootpay.webviewflutter.GeneratedAndroidWebView.WebChromeClientHostApi;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.view.Gravity;
import android.app.AlertDialog;

/**
 * Host api implementation for {@link WebChromeClient}.
 *
 * <p>Handles creating {@link WebChromeClient}s that intercommunicate with a paired Dart object.
 */
public class WebChromeClientHostApiImpl implements WebChromeClientHostApi {
  private final InstanceManager instanceManager;
  private final WebChromeClientCreator webChromeClientCreator;
  private final WebChromeClientFlutterApiImpl flutterApi;

  /**
   * Implementation of {@link WebChromeClient} that passes arguments of callback methods to Dart.
   */
  public static class WebChromeClientImpl extends WebChromeClient implements Releasable {
    @Nullable private WebChromeClientFlutterApiImpl flutterApi;
    private WebViewClient webViewClient;

    /**
     * Creates a {@link WebChromeClient} that passes arguments of callbacks methods to Dart.
     *
     * @param flutterApi handles sending messages to Dart
     * @param webViewClient receives forwarded calls from {@link WebChromeClient#onCreateWindow}
     */
    public WebChromeClientImpl(
        @NonNull WebChromeClientFlutterApiImpl flutterApi, WebViewClient webViewClient) {
      this.flutterApi = flutterApi;
      this.webViewClient = webViewClient;
    }

    @Override
    public boolean onCreateWindow(
        final WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
      return onCreateWindow(view, resultMsg, new WebView(view.getContext()));
    }



    /**
     * Verifies that a url opened by `Window.open` has a secure url.
     *
     * @param view the WebView from which the request for a new window originated.
     * @param resultMsg the message to send when once a new WebView has been created. resultMsg.obj
     *     is a {@link WebView.WebViewTransport} object. This should be used to transport the new
     *     WebView, by calling WebView.WebViewTransport.setWebView(WebView)
     * @param onCreateWindowWebView the temporary WebView used to verify the url is secure
     * @return this method should return true if the host application will create a new window, in
     *     which case resultMsg should be sent to its target. Otherwise, this method should return
     *     false. Returning false from this method but also sending resultMsg will result in
     *     undefined behavior
     */
    @VisibleForTesting
    boolean onCreateWindow(
        final WebView view, Message resultMsg, @Nullable WebView onCreateWindowWebView) {

      final WebViewClient windowWebViewClient =
          new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(
                @NonNull WebView windowWebView, @NonNull WebResourceRequest request) {

//              Log.d("bootpay", "shouldOverrideUrlLoading: " + request.getUrl().toString());
//
              if(BootpayUrlHelper.shouldOverrideUrlLoading(view, request)) {
                return true;
              }

              if (!webViewClient.shouldOverrideUrlLoading(view, request)) {
                view.loadUrl(request.getUrl().toString());
              }
//              return true;
              return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView windowWebView, String url) {
//              Log.d("bootpay", "shouldOverrideUrlLoading: " + url);

              if(BootpayUrlHelper.shouldOverrideUrlLoading(view, url)) {
                return true;
              }

              if (!webViewClient.shouldOverrideUrlLoading(view, url)) {
                view.loadUrl(url);
              }
//              return true;
              return true;
            }

          };

      if (onCreateWindowWebView == null) {
        onCreateWindowWebView = new WebView(view.getContext());
      }

      onCreateWindowWebView.setWebViewClient(windowWebViewClient);


//      view.addView(onCreateWindowWebView,
//              new FrameLayout.LayoutParams(
//                      ViewGroup.LayoutParams.MATCH_PARENT,
//                      ViewGroup.LayoutParams.MATCH_PARENT,
//                      Gravity.NO_GRAVITY)
//      );

//      onCreateWindowWebView.loadUrl("https://www.naver.com");

      final WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
      transport.setWebView(onCreateWindowWebView);
      resultMsg.sendToTarget();

      return true;
    }

//    private void setting(Context context) {
//      setWebChromeClient(new Client());
////        if(javascriptInterfaceObject == null) addJavascriptInterface(new AndroidBridge(), "Android");
//      addJavascriptInterface(new AndroidBridge(), "Android");
//      CookieManager.getInstance().setAcceptCookie(true);
//      WebSettings s = getSettings();
//      if (Build.VERSION.SDK_INT >= 21) {
////            s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
//        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//        CookieManager.getInstance().setAcceptCookie(true);
//        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
//      }
//      s.setJavaScriptEnabled(true);
//      s.setJavaScriptCanOpenWindowsAutomatically(true);
//      s.setDomStorageEnabled(true);
//      s.setSupportMultipleWindows(true);
//      s.setLoadsImagesAutomatically(true);
//      s.setUseWideViewPort(true);
//      s.setLoadWithOverviewMode(true);
//      s.setAppCacheEnabled(true);
//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//        context.getApplicationInfo().flags &=  context.getApplicationInfo().FLAG_DEBUGGABLE;
//        if (0 != context.getApplicationInfo().flags)
//          WebView.setWebContentsDebuggingEnabled(true);
//      }
//    }

    @Override
    public void onProgressChanged(WebView view, int progress) {
      if (flutterApi != null) {
        flutterApi.onProgressChanged(this, view, (long) progress, reply -> {});
      }
    }

    /**
     * Set the {@link WebViewClient} that calls to {@link WebChromeClient#onCreateWindow} are passed
     * to.
     *
     * @param webViewClient the forwarding {@link WebViewClient}
     */
    public void setWebViewClient(WebViewClient webViewClient) {
      this.webViewClient = webViewClient;
    }

    @Override
    public void release() {
      if (flutterApi != null) {
        flutterApi.dispose(this, reply -> {});
      }
      flutterApi = null;
    }
  }

  /** Handles creating {@link WebChromeClient}s for a {@link WebChromeClientHostApiImpl}. */
  public static class WebChromeClientCreator {
    /**
     * Creates a {@link DownloadListenerHostApiImpl.DownloadListenerImpl}.
     *
     * @param flutterApi handles sending messages to Dart
     * @param webViewClient receives forwarded calls from {@link WebChromeClient#onCreateWindow}
     * @return the created {@link DownloadListenerHostApiImpl.DownloadListenerImpl}
     */
    public WebChromeClientImpl createWebChromeClient(
        WebChromeClientFlutterApiImpl flutterApi, WebViewClient webViewClient) {
      return new WebChromeClientImpl(flutterApi, webViewClient);
    }
  }

  /**
   * Creates a host API that handles creating {@link WebChromeClient}s.
   *
   * @param instanceManager maintains instances stored to communicate with Dart objects
   * @param webChromeClientCreator handles creating {@link WebChromeClient}s
   * @param flutterApi handles sending messages to Dart
   */
  public WebChromeClientHostApiImpl(
      InstanceManager instanceManager,
      WebChromeClientCreator webChromeClientCreator,
      WebChromeClientFlutterApiImpl flutterApi) {
    this.instanceManager = instanceManager;
    this.webChromeClientCreator = webChromeClientCreator;
    this.flutterApi = flutterApi;
  }

  @Override
  public void create(Long instanceId, Long webViewClientInstanceId) {
    final WebViewClient webViewClient =
        (WebViewClient) instanceManager.getInstance(webViewClientInstanceId);
    final WebChromeClient webChromeClient =
        webChromeClientCreator.createWebChromeClient(flutterApi, webViewClient);
    instanceManager.addInstance(webChromeClient, instanceId);
  }
}
