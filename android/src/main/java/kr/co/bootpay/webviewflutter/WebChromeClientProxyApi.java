// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package kr.co.bootpay.webviewflutter;

import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import java.util.List;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.widget.FrameLayout;

/**
 * Host api implementation for {@link WebChromeClient}.
 *
 * <p>Handles creating {@link WebChromeClient}s that intercommunicate with a paired Dart object.
 */
public class WebChromeClientProxyApi extends PigeonApiWebChromeClient {
  /**
   * Implementation of {@link WebChromeClient} that passes arguments of callback methods to Dart.
   */
  public static class WebChromeClientImpl extends SecureWebChromeClient {
    private static final String TAG = "WebChromeClientImpl";

//    private final WebChromeClientProxyApi api;
    private boolean returnValueForOnShowFileChooser = false;
    private boolean returnValueForOnConsoleMessage = false;

    private boolean returnValueForOnJsAlert = false;
    private boolean returnValueForOnJsConfirm = false;
    private boolean returnValueForOnJsPrompt = false;

    /** Creates a {@link WebChromeClient} that passes arguments of callbacks methods to Dart. */
    public WebChromeClientImpl(@NonNull WebChromeClientProxyApi api) {
      this.api = api;
    }

    @Override
    public void onProgressChanged(@NonNull WebView view, int progress) {
      if (api != null) api.onProgressChanged(this, view, (long) progress, reply -> null);
    }

    @Override
    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
      if (api != null) api.onShowCustomView(this, view, callback, reply -> null);
    }

    @Override
    public void onHideCustomView() {
      if (api != null) api.onHideCustomView(this, reply -> null);
    }

    public void onGeolocationPermissionsShowPrompt(
        @NonNull String origin, @NonNull GeolocationPermissions.Callback callback) {
      if (api != null) api.onGeolocationPermissionsShowPrompt(this, origin, callback, reply -> null);
    }

    @Override
    public void onGeolocationPermissionsHidePrompt() {
      if (api != null) api.onGeolocationPermissionsHidePrompt(this, reply -> null);
    }

    @SuppressWarnings("LambdaLast")
    @Override
    public boolean onShowFileChooser(
        @NonNull WebView webView,
        @NonNull ValueCallback<Uri[]> filePathCallback,
        @NonNull FileChooserParams fileChooserParams) {
      final boolean currentReturnValueForOnShowFileChooser = returnValueForOnShowFileChooser;
      if (api != null) api.onShowFileChooser(
          this,
          webView,
          fileChooserParams,
          ResultCompat.asCompatCallback(
              reply -> {
                if (reply.isFailure()) {
                  api.getPigeonRegistrar()
                      .logError(TAG, Objects.requireNonNull(reply.exceptionOrNull()));
                  return null;
                }

                final List<String> value = Objects.requireNonNull(reply.getOrNull());

                // The returned list of file paths can only be passed to `filePathCallback` if the
                // `onShowFileChooser` method returned true.
                if (currentReturnValueForOnShowFileChooser) {
                  final Uri[] filePaths = new Uri[value.size()];
                  for (int i = 0; i < value.size(); i++) {
                    filePaths[i] = Uri.parse(value.get(i));
                  }
                  filePathCallback.onReceiveValue(filePaths);
                }

                return null;
              }));
      return currentReturnValueForOnShowFileChooser;
    }

    @Override
    public void onPermissionRequest(@NonNull PermissionRequest request) {
      if (api != null) api.onPermissionRequest(this, request, reply -> null);
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
      if (api != null) api.onConsoleMessage(this, consoleMessage, reply -> null);
      return returnValueForOnConsoleMessage;
    }

    /** Sets return value for {@link #onShowFileChooser}. */
    public void setReturnValueForOnShowFileChooser(boolean value) {
      returnValueForOnShowFileChooser = value;
    }

    /** Sets return value for {@link #onConsoleMessage}. */
    public void setReturnValueForOnConsoleMessage(boolean value) {
      returnValueForOnConsoleMessage = value;
    }

    public void setReturnValueForOnJsAlert(boolean value) {
      returnValueForOnJsAlert = value;
    }

    public void setReturnValueForOnJsConfirm(boolean value) {
      returnValueForOnJsConfirm = value;
    }

    public void setReturnValueForOnJsPrompt(boolean value) {
      returnValueForOnJsPrompt = value;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
      if (returnValueForOnJsAlert) {
        if (api != null) api.onJsAlert(
            this,
            view,
            url,
            message,
            ResultCompat.asCompatCallback(
                reply -> {
                  if (reply.isFailure()) {
                    api.getPigeonRegistrar()
                        .logError(TAG, Objects.requireNonNull(reply.exceptionOrNull()));
                    return null;
                  }

                  result.confirm();
                  return null;
                }));
        return true;
      } else {
        return false;
      }
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
      if (returnValueForOnJsConfirm) {
        if (api != null) api.onJsConfirm(
            this,
            view,
            url,
            message,
            ResultCompat.asCompatCallback(
                reply -> {
                  if (reply.isFailure()) {
                    api.getPigeonRegistrar()
                        .logError(TAG, Objects.requireNonNull(reply.exceptionOrNull()));
                    return null;
                  }

                  if (Boolean.TRUE.equals(reply.getOrNull())) {
                    result.confirm();
                  } else {
                    result.cancel();
                  }

                  return null;
                }));
        return true;
      } else {
        return false;
      }
    }

    @Override
    public boolean onJsPrompt(
        WebView view, String url, String message, String defaultValue, JsPromptResult result) {
      if (returnValueForOnJsPrompt) {
        if (api != null) api.onJsPrompt(
            this,
            view,
            url,
            message,
            defaultValue,
            ResultCompat.asCompatCallback(
                reply -> {
                  if (reply.isFailure()) {
                    api.getPigeonRegistrar()
                        .logError(TAG, Objects.requireNonNull(reply.exceptionOrNull()));
                    return null;
                  }

                  @Nullable final String inputMessage = reply.getOrNull();

                  if (inputMessage != null) {
                    result.confirm(inputMessage);
                  } else {
                    result.cancel();
                  }

                  return null;
                }));
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * Implementation of {@link WebChromeClient} that only allows secure urls when opening a new
   * window.
   */
  public static class SecureWebChromeClient extends WebChromeClient {
    @Nullable WebViewClient webViewClient;
    WebChromeClientProxyApi api;
    WebView mainView;

    public SecureWebChromeClient() {}
    public SecureWebChromeClient(WebChromeClientProxyApi api) {
      this.api = api;
    }
//    public void setFlutterApi(WebChromeClientProxyApi flutterApi) {
//      this.flutterApi = flutterApi;
//    }

    @Override
    public void onCloseWindow(WebView window) {
      super.onCloseWindow(window);
      if(mainView != null) mainView.removeView(window);
      window.setVisibility(View.GONE);
      window.destroy();
    }

    @Override
    public boolean onCreateWindow(
        @NonNull final WebView view,
        boolean isDialog,
        boolean isUserGesture,
        @NonNull Message resultMsg) {

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
        @NonNull final WebView view,
        @NonNull Message resultMsg,
        @Nullable WebView newWebView) {
      // WebChromeClient requires a WebViewClient because of a bug fix that makes
      // calls to WebViewClient.requestLoading/WebViewClient.urlLoading when a new
      // window is opened. This is to make sure a url opened by `Window.open` has
      // a secure url.

      if (webViewClient == null) {
        return false;
      }
      this.mainView = view;

      final WebViewClient windowWebViewClient =
          new WebViewClient() {
            BootpayUrlHelper bootpayUrlHelper = new BootpayUrlHelper();

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(
                @NonNull WebView windowWebView, @NonNull WebResourceRequest request) {
//              if (!webViewClient.shouldOverrideUrlLoading(view, request)) {
//                view.loadUrl(request.getUrl().toString());
//              }
//              return true;
              String url = request.getUrl().toString();

              if(bootpayUrlHelper.doDeepLinkIfPayUrl(view, url)) {
                return true;
              }

              return false;
            }

            // Legacy codepath for < N.
            @Override
            @SuppressWarnings({"deprecation", "RedundantSuppression"})
            public boolean shouldOverrideUrlLoading(WebView windowWebView, String url) {
//              if (!webViewClient.shouldOverrideUrlLoading(view, url)) {
//                view.loadUrl(url);
//              }
//              return true;
              if(bootpayUrlHelper.doDeepLinkIfPayUrl(view, url)) {
                return true;
              }

              return false;
            }
          };

      if (newWebView == null) {
        newWebView = new WebView(view.getContext());
      }

      newWebView.getSettings().setMediaPlaybackRequiresUserGesture( view.getSettings().getMediaPlaybackRequiresUserGesture() );
      newWebView.setWebViewClient(windowWebViewClient);
      newWebView.setFocusable(true);
      newWebView.setFocusableInTouchMode(true);


      newWebView.getSettings().setBuiltInZoomControls(view.getSettings().getBuiltInZoomControls());
      newWebView.getSettings().setDisplayZoomControls(view.getSettings().getDisplayZoomControls());
      newWebView.getSettings().setAllowFileAccess(view.getSettings().getAllowFileAccess());
      newWebView.getSettings().setAllowContentAccess(view.getSettings().getAllowContentAccess());
      newWebView.getSettings().setLoadWithOverviewMode(view.getSettings().getLoadWithOverviewMode());
//      newWebView.getSettings().setEnableSmoothTransition(view.getSettings().);
//      newWebView.getSettings().setSaveFormData(view.getSettings().getSaveFormData());
//      newWebView.getSettings().setSavePassword(view.getSettings().getSavePassword());
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
//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//        newWebView.getSettings().setForceDark(view.getSettings().getForceDark());
//      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        newWebView.getSettings().setDisabledActionModeMenuItems(view.getSettings().getDisabledActionModeMenuItems());
      }
//      newWebView.requestFocus(View.FOCUS_DOWN);
      newWebView.setOnTouchListener((v, event) -> {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
          case MotionEvent.ACTION_UP:
            if (!v.hasFocus()) {
              v.requestFocus();
            }
            break;
        }
        return false;
      });

//      newWebView.setWebViewClient(windowWebViewClient);
      newWebView.setWebChromeClient(new WebChromeClientImpl(this.api));

//      view.scrollTo(0, 0);

      //기존 웹뷰가 스크롤 되어있을 경우 최신 웹킷 빌드환경에서는 팝업 웹뷰가 안보이는 버그현상이 있어서 처리
//      try {
//        view.postDelayed(new Runnable() {
//          @Override
//          public void run() {
//            view.scrollTo(0, 0);
//          }
//        }, 50);
//      }catch (Exception ignored){}

      view.addView(newWebView,
              new FrameLayout.LayoutParams(
                      ViewGroup.LayoutParams.MATCH_PARENT,
                      ViewGroup.LayoutParams.MATCH_PARENT,
                      Gravity.NO_GRAVITY)
      );

//      newWebView.requestFocus();

      final WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
      transport.setWebView(newWebView);
      resultMsg.sendToTarget();
//
//      view.pageUp(true);

//      if (onCreateWindowWebView == null) {
//        onCreateWindowWebView = new WebView(view.getContext());
//      }
//      onCreateWindowWebView.setWebViewClient(windowWebViewClient);
//
//      final WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
//      transport.setWebView(onCreateWindowWebView);
//      resultMsg.sendToTarget();

      return true;
    }

    /**
     * Set the {@link WebViewClient} that calls to {@link WebChromeClient#onCreateWindow} are passed
     * to.
     *
     * @param webViewClient the forwarding {@link WebViewClient}
     */
    public void setWebViewClient(@NonNull WebViewClient webViewClient) {
      this.webViewClient = webViewClient;
    }
  }

  /** Creates a host API that handles creating {@link WebChromeClient}s. */
  public WebChromeClientProxyApi(@NonNull ProxyApiRegistrar pigeonRegistrar) {
    super(pigeonRegistrar);
  }

  @NonNull
  @Override
  public WebChromeClientImpl pigeon_defaultConstructor() {
    return new WebChromeClientImpl(this);
  }

  @Override
  public void setSynchronousReturnValueForOnShowFileChooser(
      @NonNull WebChromeClientImpl pigeon_instance, boolean value) {
    pigeon_instance.setReturnValueForOnShowFileChooser(value);
  }

  @Override
  public void setSynchronousReturnValueForOnConsoleMessage(
      @NonNull WebChromeClientImpl pigeon_instance, boolean value) {
    pigeon_instance.setReturnValueForOnConsoleMessage(value);
  }

  @Override
  public void setSynchronousReturnValueForOnJsAlert(
      @NonNull WebChromeClientImpl pigeon_instance, boolean value) {
    pigeon_instance.setReturnValueForOnJsAlert(value);
  }

  @Override
  public void setSynchronousReturnValueForOnJsConfirm(
      @NonNull WebChromeClientImpl pigeon_instance, boolean value) {
    pigeon_instance.setReturnValueForOnJsConfirm(value);
  }

  @Override
  public void setSynchronousReturnValueForOnJsPrompt(
      @NonNull WebChromeClientImpl pigeon_instance, boolean value) {
    pigeon_instance.setReturnValueForOnJsPrompt(value);
  }

  @NonNull
  @Override
  public ProxyApiRegistrar getPigeonRegistrar() {
    return (ProxyApiRegistrar) super.getPigeonRegistrar();
  }
}
