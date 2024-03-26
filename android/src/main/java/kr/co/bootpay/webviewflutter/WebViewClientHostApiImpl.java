// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package kr.co.bootpay.webviewflutter;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.view.KeyEvent;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebViewClientCompat;
import java.util.Objects;

/**
 * Host api implementation for {@link WebViewClient}.
 *
 * <p>Handles creating {@link WebViewClient}s that intercommunicate with a paired Dart object.
 */
public class WebViewClientHostApiImpl implements GeneratedAndroidWebView.WebViewClientHostApi {
  private final InstanceManager instanceManager;
  private final WebViewClientCreator webViewClientCreator;
  private final WebViewClientFlutterApiImpl flutterApi;

  /** Implementation of {@link WebViewClient} that passes arguments of callback methods to Dart. */
  @RequiresApi(Build.VERSION_CODES.N)
  public static class WebViewClientImpl extends WebViewClient {
    private final WebViewClientFlutterApiImpl flutterApi;
    private boolean returnValueForShouldOverrideUrlLoading = false;

    /**
     * Creates a {@link WebViewClient} that passes arguments of callbacks methods to Dart.
     *
     * @param flutterApi handles sending messages to Dart
     */
    public WebViewClientImpl(@NonNull WebViewClientFlutterApiImpl flutterApi) {
      this.flutterApi = flutterApi;
    }

    @Override
    public void onPageStarted(@NonNull WebView view, @NonNull String url, @NonNull Bitmap favicon) {
      flutterApi.onPageStarted(this, view, url, reply -> {});
    }

    @Override
    public void onPageFinished(@NonNull WebView view, @NonNull String url) {
      flutterApi.onPageFinished(this, view, url, reply -> {});
    }

    @Override
    public void onReceivedError(
            @NonNull WebView view,
            @NonNull WebResourceRequest request,
            @NonNull WebResourceError error) {
      flutterApi.onReceivedRequestError(this, view, request, error, reply -> {});

//      flutterApi.onReceivedError(
//              this, view, (long) error.getErrorCode(), error.getDescription().toString(), request.getUrl().toString(), reply -> {});
    }
    @SuppressLint("WebViewClientOnReceivedSslError")
    @Override
    public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
      flutterApi.onReceivedError(WebViewClientImpl.this, view, (long) error.getPrimaryError(), "sslerror:" + error.toString(), view.getUrl(), reply -> {});

      // for SSLErrorHandler
      AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
      builder.setTitle("SSL Connection Error");
      builder.setMessage("Your device's Android version is outdated and may not securely connect to our service. To continue using the app securely, please update your device's operating system. If you choose to proceed without updating, it may expose you to security vulnerabilities.");
      builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          // Redirect the user to the system update settings


          Intent intent = new Intent("android.settings.SYSTEM_UPDATE_SETTINGS");
          if (intent.resolveActivity(view.getContext().getPackageManager()) != null) {
            view.getContext().startActivity(intent);
          } else {
            // If the device does not support system update settings intent
            Toast.makeText(view.getContext(), "System update option not available. Please check your device settings manually.", Toast.LENGTH_LONG).show();
          }
        }
      });
      builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          handler.cancel();
        }
      });
      AlertDialog dialog = builder.create();
      dialog.show();
    }

    // Legacy codepath for < 23; newer versions use the variant above.
    @SuppressWarnings("deprecation")
    @Override
    public void onReceivedError(
            @NonNull WebView view,
            int errorCode,
            @NonNull String description,
            @NonNull String failingUrl) {
      flutterApi.onReceivedError(
              this, view, (long) errorCode, description, failingUrl, reply -> {});
    }

    @Override
    public boolean shouldOverrideUrlLoading(
            @NonNull WebView view, @NonNull WebResourceRequest request) {
      String url = request.getUrl().toString();
      BootpayUrlHelper bootpayUrlHelper = new BootpayUrlHelper();

      if(bootpayUrlHelper.doDeepLinkIfPayUrl(view, url)) {
        //do deep link by doDeepLinkIfPayUrl function
      } else {
        if (flutterApi != null)
          flutterApi.requestLoading(this, view, request, reply -> {});
      }

      return returnValueForShouldOverrideUrlLoading;
    }

    // Legacy codepath for < 24; newer versions use the variant above.
    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull String url) {
      BootpayUrlHelper bootpayUrlHelper = new BootpayUrlHelper();
      if(bootpayUrlHelper.doDeepLinkIfPayUrl(view, url)) {
        //do deep link by doDeepLinkIfPayUrl function
      } else {
        if (flutterApi != null)
          flutterApi.urlLoading(this, view, url, reply -> {});
      }

      return returnValueForShouldOverrideUrlLoading;
    }

    @Override
    public void doUpdateVisitedHistory(
            @NonNull WebView view, @NonNull String url, boolean isReload) {
      flutterApi.doUpdateVisitedHistory(this, view, url, isReload, reply -> {});
    }

    @Override
    public void onUnhandledKeyEvent(@NonNull WebView view, @NonNull KeyEvent event) {
      // Deliberately empty. Occasionally the webview will mark events as having failed to be
      // handled even though they were handled. We don't want to propagate those as they're not
      // truly lost.
    }

    /** Sets return value for {@link #shouldOverrideUrlLoading}. */
    public void setReturnValueForShouldOverrideUrlLoading(boolean value) {
      returnValueForShouldOverrideUrlLoading = value;
    }
  }

  /**
   * Implementation of {@link WebViewClientCompat} that passes arguments of callback methods to
   * Dart.
   */
  public static class WebViewClientCompatImpl extends WebViewClientCompat {
    private final WebViewClientFlutterApiImpl flutterApi;
    private boolean returnValueForShouldOverrideUrlLoading = false;

    public WebViewClientCompatImpl(@NonNull WebViewClientFlutterApiImpl flutterApi) {
      this.flutterApi = flutterApi;
    }

    @Override
    public void onPageStarted(@NonNull WebView view, @NonNull String url, @NonNull Bitmap favicon) {
      flutterApi.onPageStarted(this, view, url, reply -> {});
    }

    @Override
    public void onPageFinished(@NonNull WebView view, @NonNull String url) {
      flutterApi.onPageFinished(this, view, url, reply -> {});
    }

    // This method is only called when the WebViewFeature.RECEIVE_WEB_RESOURCE_ERROR feature is
    // enabled. The deprecated method is called when a device doesn't support this.
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("RequiresFeature")
    @Override
    public void onReceivedError(
            @NonNull WebView view,
            @NonNull WebResourceRequest request,
            @NonNull WebResourceErrorCompat error) {
      flutterApi.onReceivedRequestError(this, view, request, error, reply -> {});
    }

    // Legacy codepath for versions that don't support the variant above.
    @SuppressWarnings("deprecation")
    @Override
    public void onReceivedError(
            @NonNull WebView view,
            int errorCode,
            @NonNull String description,
            @NonNull String failingUrl) {
      flutterApi.onReceivedError(
              this, view, (long) errorCode, description, failingUrl, reply -> {});
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(
            @NonNull WebView view, @NonNull WebResourceRequest request) {
      String url = request.getUrl().toString();
      BootpayUrlHelper bootpayUrlHelper = new BootpayUrlHelper();

      if(bootpayUrlHelper.doDeepLinkIfPayUrl(view, url)) {
        //do deep link by doDeepLinkIfPayUrl function
      } else {
        if (flutterApi != null)
          flutterApi.requestLoading(this, view, request, reply -> {});
      }

      return returnValueForShouldOverrideUrlLoading;
    }

    // Legacy codepath for < Lollipop; newer versions use the variant above.
    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull String url) {
      flutterApi.urlLoading(this, view, url, reply -> {});
      return returnValueForShouldOverrideUrlLoading;
    }

    @Override
    public void doUpdateVisitedHistory(
            @NonNull WebView view, @NonNull String url, boolean isReload) {
      flutterApi.doUpdateVisitedHistory(this, view, url, isReload, reply -> {});
    }

    @Override
    public void onUnhandledKeyEvent(@NonNull WebView view, @NonNull KeyEvent event) {
      // Deliberately empty. Occasionally the webview will mark events as having failed to be
      // handled even though they were handled. We don't want to propagate those as they're not
      // truly lost.
    }

    /** Sets return value for {@link #shouldOverrideUrlLoading}. */
    public void setReturnValueForShouldOverrideUrlLoading(boolean value) {
      returnValueForShouldOverrideUrlLoading = value;
    }
  }

  /** Handles creating {@link WebViewClient}s for a {@link WebViewClientHostApiImpl}. */
  public static class WebViewClientCreator {
    /**
     * Creates a {@link WebViewClient}.
     *
     * @param flutterApi handles sending messages to Dart
     * @return the created {@link WebViewClient}
     */
    @NonNull
    public WebViewClient createWebViewClient(@NonNull WebViewClientFlutterApiImpl flutterApi) {
      // WebViewClientCompat is used to get
      // shouldOverrideUrlLoading(WebView view, WebResourceRequest request)
      // invoked by the webview on older Android devices, without it pages that use iframes will
      // be broken when a navigationDelegate is set on Android version earlier than N.
      //
      // However, this if statement attempts to avoid using WebViewClientCompat on versions >= N due
      // to bug https://bugs.chromium.org/p/chromium/issues/detail?id=925887. Also, see
      // https://github.com/flutter/flutter/issues/29446.
      if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return new WebViewClientImpl(flutterApi);
      } else {
        return new WebViewClientCompatImpl(flutterApi);
      }
    }
  }

  /**
   * Creates a host API that handles creating {@link WebViewClient}s.
   *
   * @param instanceManager maintains instances stored to communicate with Dart objects
   * @param webViewClientCreator handles creating {@link WebViewClient}s
   * @param flutterApi handles sending messages to Dart
   */
  public WebViewClientHostApiImpl(
          @NonNull InstanceManager instanceManager,
          @NonNull WebViewClientCreator webViewClientCreator,
          @NonNull WebViewClientFlutterApiImpl flutterApi) {
    this.instanceManager = instanceManager;
    this.webViewClientCreator = webViewClientCreator;
    this.flutterApi = flutterApi;
  }

  @Override
  public void create(@NonNull Long instanceId) {
    final WebViewClient webViewClient = webViewClientCreator.createWebViewClient(flutterApi);
    instanceManager.addDartCreatedInstance(webViewClient, instanceId);
  }

  @Override
  public void setSynchronousReturnValueForShouldOverrideUrlLoading(
          @NonNull Long instanceId, @NonNull Boolean value) {
    final WebViewClient webViewClient =
            Objects.requireNonNull(instanceManager.getInstance(instanceId));
    if (webViewClient instanceof WebViewClientCompatImpl) {
      ((WebViewClientCompatImpl) webViewClient).setReturnValueForShouldOverrideUrlLoading(value);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            && webViewClient instanceof WebViewClientImpl) {
      ((WebViewClientImpl) webViewClient).setReturnValueForShouldOverrideUrlLoading(value);
    } else {
      throw new IllegalStateException(
              "This WebViewClient doesn't support setting the returnValueForShouldOverrideUrlLoading.");
    }
  }
}
