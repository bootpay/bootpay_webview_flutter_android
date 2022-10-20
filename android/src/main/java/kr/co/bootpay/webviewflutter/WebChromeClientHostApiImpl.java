// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package kr.co.bootpay.webviewflutter;

import android.os.Build;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import kr.co.bootpay.webviewflutter.GeneratedAndroidWebView.WebChromeClientHostApi;

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
    WebView mainView;


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
    public void onCloseWindow(WebView window) {
      super.onCloseWindow(window);
      if(mainView != null) mainView.removeView(window);
      window.setVisibility(View.GONE);
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
        final WebView view, Message resultMsg, @Nullable WebView newWebView) {
      mainView = view;

      final WebViewClient windowWebViewClient =
          new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(
                @NonNull WebView windowWebView, @NonNull WebResourceRequest request) {
              String url = request.getUrl().toString();

              if(BootpayUrlHelper.doDeepLinkIfPayUrl(windowWebView, url)) {
                //do deep link by doDeepLinkIfPayUrl function
                return true;
//              } else if (!webViewClient.shouldOverrideUrlLoading(windowWebView, request)) {
//              } else {
//                windowWebView.loadUrl(url);
//                view.loadUrl("https://www.naver.com");
//              } else {
//                windowWebView.loadUrl("https://www.naver.com");
              }
//              if(windowWebView != null) {
//                windowWebView.loadUrl("https://www.naver.com");
//              }
//              if (!webViewClient.shouldOverrideUrlLoading(windowWebView, request)) {
//
//                windowWebView.loadUrl(url);
//              }
//              windowWebView.loadUrl(url);

              return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView windowWebView, String url) {
              if(BootpayUrlHelper.doDeepLinkIfPayUrl(windowWebView, url)) {
                //do deep link by doDeepLinkIfPayUrl function
                return false;
              }
//              else if(BootpayUrlHelper.isPreventUrl(url)) {
//                //do nothing
//              } else if (!webViewClient.shouldOverrideUrlLoading(view, url)) {
////              } else {
//                view.loadUrl(url);
//              } else {
//                view.loadUrl("https://www.naver.com");
//              }
//              windowWebView.loadUrl("https://www.naver.com");
              return false;
            }

//            @Override
//            public void onPageFinished(WebView view, String url) {
//
//              if(url.startsWith("https://nid.naver.com/nidlogin.login")) {
//                view.evaluateJavascript("document.getElementById('back').style.display='none';", null);
//              }
//
////              Map<String, Object> args = new HashMap<>();
////              args.put("url", url);
////              methodChannel.invokeMethod("onPageFinished", args);
//            }
          };
//
      if (newWebView == null) {
        newWebView = new WebView(view.getContext());
      }
      newWebView.setWebViewClient(windowWebViewClient);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        newWebView.getSettings().setMediaPlaybackRequiresUserGesture( view.getSettings().getMediaPlaybackRequiresUserGesture() );
      }
//      newWebView.setFocusable(true);
//      newWebView.setFocusableInTouchMode(true);

      newWebView.getSettings().setBuiltInZoomControls(view.getSettings().getBuiltInZoomControls());
      newWebView.getSettings().setDisplayZoomControls(view.getSettings().getDisplayZoomControls());
      newWebView.getSettings().setAllowFileAccess(view.getSettings().getAllowFileAccess());
      newWebView.getSettings().setAllowContentAccess(view.getSettings().getAllowContentAccess());
      newWebView.getSettings().setLoadWithOverviewMode(view.getSettings().getLoadWithOverviewMode());
//      newWebView.getSettings().setEnableSmoothTransition(view.getSettings().);
      newWebView.getSettings().setSaveFormData(view.getSettings().getSaveFormData());
      newWebView.getSettings().setSavePassword(view.getSettings().getSavePassword());
      newWebView.getSettings().setTextZoom(view.getSettings().getTextZoom());


      newWebView.getSettings().setUseWideViewPort(view.getSettings().getUseWideViewPort());
      newWebView.getSettings().setSupportMultipleWindows(true);
      newWebView.getSettings().setLayoutAlgorithm(view.getSettings().getLayoutAlgorithm());
      newWebView.getSettings().setStandardFontFamily(view.getSettings().getStandardFontFamily());
      newWebView.getSettings().setFixedFontFamily(view.getSettings().getFixedFontFamily());
      newWebView.getSettings().setSansSerifFontFamily(view.getSettings().getSansSerifFontFamily());
      newWebView.getSettings().setSerifFontFamily(view.getSettings().getSerifFontFamily());
      newWebView.getSettings().setCursiveFontFamily(view.getSettings().getCursiveFontFamily());
      newWebView.getSettings().setFantasyFontFamily(view.getSettings().getFantasyFontFamily());
      newWebView.getSettings().setMinimumFontSize(view.getSettings().getMinimumFontSize());
      newWebView.getSettings().setMinimumLogicalFontSize(view.getSettings().getMinimumLogicalFontSize());
      newWebView.getSettings().setDefaultFontSize(view.getSettings().getDefaultFontSize());
      newWebView.getSettings().setDefaultFixedFontSize(view.getSettings().getDefaultFixedFontSize());
      newWebView.getSettings().setLoadsImagesAutomatically(view.getSettings().getLoadsImagesAutomatically());
      newWebView.getSettings().setBlockNetworkImage(view.getSettings().getBlockNetworkImage());
      newWebView.getSettings().setJavaScriptEnabled(view.getSettings().getJavaScriptEnabled());
      newWebView.getSettings().setDatabaseEnabled(view.getSettings().getDatabaseEnabled());
      newWebView.getSettings().setDomStorageEnabled(view.getSettings().getDomStorageEnabled());

      newWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(view.getSettings().getJavaScriptCanOpenWindowsAutomatically());
      newWebView.getSettings().setDefaultTextEncodingName(view.getSettings().getDefaultTextEncodingName());
      newWebView.getSettings().setUserAgentString(view.getSettings().getUserAgentString());
      newWebView.getSettings().setCacheMode(view.getSettings().getCacheMode());

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        newWebView.getSettings().setMixedContentMode(view.getSettings().getMixedContentMode());
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        newWebView.getSettings().setSafeBrowsingEnabled(view.getSettings().getSafeBrowsingEnabled());
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        newWebView.getSettings().setForceDark(view.getSettings().getForceDark());
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        newWebView.getSettings().setDisabledActionModeMenuItems(view.getSettings().getDisabledActionModeMenuItems());
      }
      newWebView.requestFocus(View.FOCUS_DOWN);
      newWebView.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
              if (!v.hasFocus()) {
                v.requestFocus();
              }
              break;
          }
          return false;
        }
      });


      newWebView.setWebChromeClient(new WebChromeClientImpl(null, windowWebViewClient));
      view.addView(newWebView,
              new FrameLayout.LayoutParams(
                      ViewGroup.LayoutParams.MATCH_PARENT,
                      ViewGroup.LayoutParams.MATCH_PARENT,
                      Gravity.NO_GRAVITY)
      );

      final WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
      transport.setWebView(newWebView);
      resultMsg.sendToTarget();
//      newWebView.loadUrl("https://www.naver.com");

      return true;
    }

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
    instanceManager.addDartCreatedInstance(webChromeClient, instanceId);
  }
}
