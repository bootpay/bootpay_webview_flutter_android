// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package kr.co.bootpay.webviewflutter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
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
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import java.util.List;
import java.util.Objects;

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

    private boolean returnValueForOnShowFileChooser = false;
    private boolean returnValueForOnConsoleMessage = false;

    private boolean returnValueForOnJsAlert = false;
    private boolean returnValueForOnJsConfirm = false;
    private boolean returnValueForOnJsPrompt = false;

    /** Creates a {@link WebChromeClient} that passes arguments of callbacks methods to Dart. */
    public WebChromeClientImpl(@NonNull WebChromeClientProxyApi api) {
      super(api);
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
   *
   * Based on official webview_flutter approach: redirect popup URLs to main WebView.
   * Added bootpay-specific deep link handling for payment apps.
   */
  public static class SecureWebChromeClient extends WebChromeClient {
    @Nullable WebViewClient webViewClient;
    WebChromeClientProxyApi api;
    @Nullable WebView mainWebView;
    @Nullable ViewGroup popupParentView;
    @Nullable View popupContainer;

    /**
     * The most recently created popup container / web view, so Dart can dismiss
     * the active popup via {@code Bootpay.closePopupWebView()} (e.g. on an
     * ad-finished event). Cleared on dismissal so a closed popup is not retained.
     */
    @Nullable private static View sActivePopupContainer;
    @Nullable private static WebView sActivePopupWebView;

    public SecureWebChromeClient() {}
    public SecureWebChromeClient(WebChromeClientProxyApi api) {
      this.api = api;
    }

    /**
     * Dismisses the currently displayed popup, if any. No-op when none is open.
     * Invoked on the platform main thread from the {@code closePopup} method
     * channel, so it is safe to touch views here.
     */
    static void closeActivePopup() {
      final View container = sActivePopupContainer;
      final WebView webView = sActivePopupWebView;
      if (container != null && webView != null) {
        dismissPopupContainer(container, webView);
      }
    }

    /** Helper to get Activity from Context */
    @Nullable
    private static Activity getActivity(Context context) {
      if (context == null) return null;
      if (context instanceof Activity) return (Activity) context;
      if (context instanceof ContextWrapper) {
        return getActivity(((ContextWrapper) context).getBaseContext());
      }
      return null;
    }

    @Override
    public void onCloseWindow(WebView window) {
      super.onCloseWindow(window);
      // The popup WebView is wrapped in a container (floating ✕ + WebView);
      // remove the whole container so the button disappears together with the
      // popup. Fall back to removing the bare WebView for any unwrapped popup.
      if (popupContainer != null) {
        ViewParent parent = popupContainer.getParent();
        if (parent instanceof ViewGroup) {
          ((ViewGroup) parent).removeView(popupContainer);
        } else if (popupParentView != null) {
          popupParentView.removeView(popupContainer);
        }
      } else if (popupParentView != null) {
        popupParentView.removeView(window);
      } else if (mainWebView != null) {
        mainWebView.removeView(window);
      }
      window.setVisibility(View.GONE);
      window.destroy();
      // Drop the static reference so a closed popup is not retained / re-closed.
      if (sActivePopupWebView == window || sActivePopupContainer == popupContainer) {
        sActivePopupContainer = null;
        sActivePopupWebView = null;
      }
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
        @Nullable WebView onCreateWindowWebView) {
      // WebChromeClient requires a WebViewClient because of a bug fix that makes
      // calls to WebViewClient.requestLoading/WebViewClient.urlLoading when a new
      // window is opened. This is to make sure a url opened by `Window.open` has
      // a secure url.
      if (webViewClient == null) {
        return false;
      }

      // Store main WebView reference for onCloseWindow to call goBack()
      this.mainWebView = view;

      final WebViewClient windowWebViewClient =
          new WebViewClient() {
            BootpayUrlHelper bootpayUrlHelper = new BootpayUrlHelper();

            @Override
            public void onPageStarted(
                WebView windowWebView, String url, android.graphics.Bitmap favicon) {
              // AdSense click-throughs route through an ad-network domain on each
              // navigation; reveal the close button as soon as one is seen.
              revealCloseButtonIfNeeded(windowWebView, url);
              super.onPageStarted(windowWebView, url, favicon);
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(
                @NonNull WebView windowWebView, @NonNull WebResourceRequest request) {
              String url = request.getUrl().toString();

              // Bootpay: Handle payment app deep links (intent://, market://, etc.)
              if (!url.startsWith("http://") && !url.startsWith("https://")) {
                if (bootpayUrlHelper.doDeepLinkIfPayUrl(view, url)) {
                  return true;
                }
              }

              // Reveal the close button if this navigation should show it.
              revealCloseButtonIfNeeded(windowWebView, url);

              // Let popup WebView handle HTTP/HTTPS URLs normally
              return false;
            }

            // Legacy codepath for < N.
            @Override
            @SuppressWarnings({"deprecation", "RedundantSuppression"})
            public boolean shouldOverrideUrlLoading(WebView windowWebView, String url) {
              // Bootpay: Handle payment app deep links
              if (!url.startsWith("http://") && !url.startsWith("https://")) {
                if (bootpayUrlHelper.doDeepLinkIfPayUrl(view, url)) {
                  return true;
                }
              }

              // Reveal the close button if this navigation should show it.
              revealCloseButtonIfNeeded(windowWebView, url);

              // Let popup WebView handle HTTP/HTTPS URLs normally
              return false;
            }
          };

      if (onCreateWindowWebView == null) {
        onCreateWindowWebView = new WebView(view.getContext());
      }

      // Inherit settings from parent WebView for proper payment processing
      onCreateWindowWebView.getSettings().setJavaScriptEnabled(view.getSettings().getJavaScriptEnabled());
      onCreateWindowWebView.getSettings().setDomStorageEnabled(view.getSettings().getDomStorageEnabled());
      onCreateWindowWebView.getSettings().setMediaPlaybackRequiresUserGesture(view.getSettings().getMediaPlaybackRequiresUserGesture());
      onCreateWindowWebView.getSettings().setBuiltInZoomControls(view.getSettings().getBuiltInZoomControls());
      onCreateWindowWebView.getSettings().setDisplayZoomControls(view.getSettings().getDisplayZoomControls());
      onCreateWindowWebView.getSettings().setAllowFileAccess(view.getSettings().getAllowFileAccess());
      onCreateWindowWebView.getSettings().setAllowContentAccess(view.getSettings().getAllowContentAccess());
      onCreateWindowWebView.getSettings().setLoadWithOverviewMode(view.getSettings().getLoadWithOverviewMode());
      onCreateWindowWebView.getSettings().setUseWideViewPort(view.getSettings().getUseWideViewPort());
      onCreateWindowWebView.getSettings().setSupportMultipleWindows(true);
      onCreateWindowWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(view.getSettings().getJavaScriptCanOpenWindowsAutomatically());
      onCreateWindowWebView.getSettings().setUserAgentString(view.getSettings().getUserAgentString());
      onCreateWindowWebView.getSettings().setCacheMode(view.getSettings().getCacheMode());
      onCreateWindowWebView.getSettings().setTextZoom(view.getSettings().getTextZoom());
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        onCreateWindowWebView.getSettings().setMixedContentMode(view.getSettings().getMixedContentMode());
      }

      onCreateWindowWebView.setWebViewClient(windowWebViewClient);

      // Set WebChromeClient on popup to handle window.close()
      SecureWebChromeClient popupChromeClient = new SecureWebChromeClient(this.api);
      popupChromeClient.mainWebView = view;  // Pass main WebView reference for removeView()
      onCreateWindowWebView.setWebChromeClient(popupChromeClient);

      // Set background to ensure immediate rendering (avoid transparent initial state)
      onCreateWindowWebView.setBackgroundColor(android.graphics.Color.WHITE);

      // Wrap the popup WebView in a container that overlays a single floating,
      // semi-transparent close (✕) button on top of it. The button is hidden by
      // default, so payment popups render full-bleed exactly as before and
      // auto-dismiss via `window.close()` (-> onCloseWindow). It is shown per the
      // configured mode (auto: ad popups only — the default; always; never) — see
      // revealCloseButtonIfNeeded / BootpayPopupAdFilter — giving users a manual
      // escape hatch from ad pages opened via window.open / target="_blank"
      // (e.g. AdSense) while still displaying the ad in-app.
      final FrameLayout popupContainer = buildPopupContainer(view.getContext(), onCreateWindowWebView);
      popupChromeClient.popupContainer = popupContainer;
      // Track the active popup so Dart can dismiss it via closePopupWebView().
      sActivePopupContainer = popupContainer;
      sActivePopupWebView = onCreateWindowWebView;

      // Add popup to Activity's DecorView (outside Flutter PlatformView hierarchy)
      // This ensures touch events work properly
      Activity activity = getActivity(view.getContext());
      ViewGroup decorView = null;
      int statusBarHeight = 0;
      if (activity != null) {
        decorView = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        // Get status bar height for SafeArea
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
          statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
        }
      }

      FrameLayout.LayoutParams popupParams = new FrameLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT);
      // Apply status bar height as top margin (SafeArea)
      popupParams.topMargin = statusBarHeight;

      if (decorView != null) {
        decorView.addView(popupContainer, popupParams);
        popupContainer.bringToFront();
        popupChromeClient.popupParentView = decorView;
      } else {
        // Fallback: add to WebView's parent
        ViewGroup parentView = (view.getParent() instanceof ViewGroup)
                ? (ViewGroup) view.getParent()
                : null;
        if (parentView != null) {
          parentView.addView(popupContainer, popupParams);
          popupContainer.bringToFront();
          popupChromeClient.popupParentView = parentView;
        } else {
          // Last fallback: add to WebView itself
          view.addView(popupContainer, popupParams);
          popupContainer.bringToFront();
        }
      }

      final WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
      transport.setWebView(onCreateWindowWebView);
      resultMsg.sendToTarget();

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

    /**
     * Builds a {@link FrameLayout} that overlays a single floating, semi-
     * transparent close (✕) button on top of the popup {@link WebView}.
     *
     * <p>The button's initial visibility follows
     * {@link BootpayPopupAdFilter#shouldShowCloseButton} for a null URL: shown
     * immediately in "always" mode, hidden in "auto" (revealed later once the
     * popup navigates to a known ad network) and "never". This keeps payment
     * popups chrome-less while giving users a manual escape hatch from ad pages
     * (e.g. AdSense window.open / target="_blank") without altering the payment
     * success path.
     *
     * <p>The container exposes its close button via {@link View#setTag(Object)}
     * so the popup's {@link WebViewClient} can reveal it from a navigation
     * callback (see {@link #revealCloseButtonIfNeeded}).
     */
    @NonNull
    private static FrameLayout buildPopupContainer(
        @NonNull Context context, @NonNull final WebView popupWebView) {
      final float density = context.getResources().getDisplayMetrics().density;
      final int buttonSize = (int) (18 * density);
      final int margin = (int) (12 * density);

      final FrameLayout container = new FrameLayout(context);
      container.setBackgroundColor(android.graphics.Color.WHITE);

      // Popup web view fills the whole container.
      container.addView(popupWebView, new FrameLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

      // Circular, semi-transparent ✕ floated at the top-right corner.
      final TextView closeButton = new TextView(context);
      closeButton.setText("✕");
      closeButton.setTextColor(android.graphics.Color.WHITE);
      closeButton.setTextSize(11);
      closeButton.setGravity(Gravity.CENTER);
      final GradientDrawable buttonBackground = new GradientDrawable();
      buttonBackground.setShape(GradientDrawable.OVAL);
      buttonBackground.setColor(android.graphics.Color.argb(115, 0, 0, 0)); // ~0.45 alpha
      closeButton.setBackground(buttonBackground);
      closeButton.setClickable(true);
      // Hidden by default; "always" mode shows it immediately.
      closeButton.setVisibility(
          BootpayPopupAdFilter.getInstance().shouldShowCloseButton(null)
              ? View.VISIBLE
              : View.GONE);

      final FrameLayout.LayoutParams buttonParams =
          new FrameLayout.LayoutParams(buttonSize, buttonSize);
      buttonParams.gravity = Gravity.TOP | Gravity.END;
      buttonParams.setMargins(0, margin, margin, 0);
      container.addView(closeButton, buttonParams);

      closeButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          dismissPopupContainer(container, popupWebView);
        }
      });

      // Expose the close button so the popup's WebViewClient can reveal it.
      container.setTag(closeButton);

      return container;
    }

    /**
     * Reveals the popup's floating ✕ button when the popup navigates to a URL
     * that should show it (per the configured mode). Reveal-only: once shown it
     * stays. In "auto" mode payment popups never match and stay chrome-less. The
     * button is located via the container tag set in {@link #buildPopupContainer}.
     */
    private static void revealCloseButtonIfNeeded(
        @NonNull WebView popupWebView, @Nullable String url) {
      if (!BootpayPopupAdFilter.getInstance().shouldShowCloseButton(url)) {
        return;
      }
      ViewParent parent = popupWebView.getParent();
      if (parent instanceof View) {
        Object tag = ((View) parent).getTag();
        if (tag instanceof View) {
          ((View) tag).setVisibility(View.VISIBLE);
        }
      }
    }

    /** Removes the popup container from its parent and destroys its WebView. */
    private static void dismissPopupContainer(
        @NonNull View container, @NonNull WebView popupWebView) {
      popupWebView.stopLoading();
      ViewParent parent = container.getParent();
      if (parent instanceof ViewGroup) {
        ((ViewGroup) parent).removeView(container);
      }
      popupWebView.setVisibility(View.GONE);
      popupWebView.destroy();
      // Drop the static reference so a closed popup is not retained / re-closed.
      if (sActivePopupContainer == container) {
        sActivePopupContainer = null;
        sActivePopupWebView = null;
      }
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
