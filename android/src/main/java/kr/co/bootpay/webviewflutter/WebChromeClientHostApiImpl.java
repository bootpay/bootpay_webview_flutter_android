// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package kr.co.bootpay.webviewflutter;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import kr.co.bootpay.webviewflutter.GeneratedAndroidWebView.WebChromeClientHostApi;
import java.util.Objects;

import android.widget.FrameLayout;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


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
  public static class WebChromeClientImpl extends SecureWebChromeClient {
    private final WebChromeClientFlutterApiImpl flutterApi;
    private boolean returnValueForOnShowFileChooser = false;

    /**
     * Creates a {@link WebChromeClient} that passes arguments of callbacks methods to Dart.
     *
     * @param flutterApi handles sending messages to Dart
     */
    public WebChromeClientImpl(WebChromeClientFlutterApiImpl flutterApi) {
      this.flutterApi = flutterApi;
    }

    @Override
    public void onProgressChanged(WebView view, int progress) {
      if (flutterApi != null)
        flutterApi.onProgressChanged(this, view, (long) progress, reply -> {});
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onShowFileChooser(
            WebView webView,
            ValueCallback<Uri[]> filePathCallback,
            FileChooserParams fileChooserParams) {
      final boolean currentReturnValueForOnShowFileChooser = returnValueForOnShowFileChooser;
      if (flutterApi != null)
        flutterApi.onShowFileChooser(
              this,
              webView,
              fileChooserParams,
              reply -> {
                // The returned list of file paths can only be passed to `filePathCallback` if the
                // `onShowFileChooser` method returned true.
                if (currentReturnValueForOnShowFileChooser) {
                  final Uri[] filePaths = new Uri[reply.size()];
                  for (int i = 0; i < reply.size(); i++) {
                    filePaths[i] = Uri.parse(reply.get(i));
                  }
                  filePathCallback.onReceiveValue(filePaths);
                }
              });
      return currentReturnValueForOnShowFileChooser;
    }

    /** Sets return value for {@link #onShowFileChooser}. */
    public void setReturnValueForOnShowFileChooser(boolean value) {
      returnValueForOnShowFileChooser = value;
    }
  }

  /**
   * Implementation of {@link WebChromeClient} that only allows secure urls when opening a new
   * window.
   */
  public static class SecureWebChromeClient extends WebChromeClient {
    @Nullable private WebViewClient webViewClient;
    private WebChromeClientFlutterApiImpl flutterApi;
    WebView mainView;

    public SecureWebChromeClient() {}
    public SecureWebChromeClient(WebChromeClientFlutterApiImpl flutterApi) {
      this.flutterApi = flutterApi;
    }
    public void setFlutterApi(WebChromeClientFlutterApiImpl flutterApi) {
      this.flutterApi = flutterApi;
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
      // WebChromeClient requires a WebViewClient because of a bug fix that makes
      // calls to WebViewClient.requestLoading/WebViewClient.urlLoading when a new
      // window is opened. This is to make sure a url opened by `Window.open` has
      // a secure url.
      if (webViewClient == null) {
        return false;
      }

      final WebViewClient windowWebViewClient =
              new WebViewClient() {



                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public boolean shouldOverrideUrlLoading(
                        @NonNull WebView windowWebView, @NonNull WebResourceRequest request) {

                  String url = request.getUrl().toString();

                  if(BootpayUrlHelper.doDeepLinkIfPayUrl(view, url)) {
                    //do deep link by doDeepLinkIfPayUrl function
                    return true;
                  }

                  return false;
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView windowWebView, String url) {
                  if(BootpayUrlHelper.doDeepLinkIfPayUrl(view, url)) {
                    return true;
                  }

                  return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                  if(url.contains("nid.naver.com"))
                    view.evaluateJavascript("document.getElementById('back').remove()", null);
                  
                  super.onPageFinished(view, url);
                }
              };


      if (newWebView == null) {
        newWebView = new WebView(view.getContext());
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        newWebView.getSettings().setMediaPlaybackRequiresUserGesture( view.getSettings().getMediaPlaybackRequiresUserGesture() );
      }
      newWebView.setWebViewClient(windowWebViewClient);
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

      newWebView.setWebChromeClient(new WebChromeClientImpl(null));
//      newWebView.setWebChromeClient(new WebChromeClient());

      view.addView(newWebView,
              new FrameLayout.LayoutParams(
                      ViewGroup.LayoutParams.MATCH_PARENT,
                      ViewGroup.LayoutParams.MATCH_PARENT,
                      Gravity.NO_GRAVITY)
      );

      final WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
      transport.setWebView(newWebView);
      resultMsg.sendToTarget();

//      newWebView.loadUrl("https://www.google.com");

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

  /** Handles creating {@link WebChromeClient}s for a {@link WebChromeClientHostApiImpl}. */
  public static class WebChromeClientCreator {
    /**
     * Creates a {@link DownloadListenerHostApiImpl.DownloadListenerImpl}.
     *
     * @param flutterApi handles sending messages to Dart
     * @return the created {@link WebChromeClientHostApiImpl.WebChromeClientImpl}
     */
    public WebChromeClientImpl createWebChromeClient(WebChromeClientFlutterApiImpl flutterApi) {
      return new WebChromeClientImpl(flutterApi);
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
  public void create(Long instanceId) {
    final WebChromeClient webChromeClient =
            webChromeClientCreator.createWebChromeClient(flutterApi);
    instanceManager.addDartCreatedInstance(webChromeClient, instanceId);
  }

  @Override
  public void setSynchronousReturnValueForOnShowFileChooser(
          @NonNull Long instanceId, @NonNull Boolean value) {
    final WebChromeClientImpl webChromeClient =
            Objects.requireNonNull(instanceManager.getInstance(instanceId));
    webChromeClient.setReturnValueForOnShowFileChooser(value);
  }
}