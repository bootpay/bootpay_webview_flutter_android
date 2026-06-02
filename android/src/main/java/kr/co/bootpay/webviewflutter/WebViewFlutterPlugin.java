// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package kr.co.bootpay.webviewflutter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

import java.util.List;
import java.util.Map;

/**
 * Java platform implementation of the webview_flutter plugin.
 *
 * <p>Register this in an add to app scenario to gracefully handle activity and context changes.
 */
public class WebViewFlutterPlugin implements FlutterPlugin, ActivityAware {
  private FlutterPluginBinding pluginBinding;
  private ProxyApiRegistrar proxyApiRegistrar;

  /** Lets Dart extend the popup ad-host list at runtime. */
  @Nullable private MethodChannel popupConfigChannel;

  /**
   * Add an instance of this to {@link io.flutter.embedding.engine.plugins.PluginRegistry} to
   * register it.
   *
   * <p>Registration should eventually be handled automatically by v2 of the
   * GeneratedPluginRegistrant. https://github.com/flutter/flutter/issues/42694
   */
  public WebViewFlutterPlugin() {}

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    pluginBinding = binding;

    proxyApiRegistrar =
        new ProxyApiRegistrar(
            binding.getBinaryMessenger(),
            binding.getApplicationContext(),
            new FlutterAssetManager.PluginBindingFlutterAssetManager(
                binding.getApplicationContext().getAssets(), binding.getFlutterAssets()));

    binding
        .getPlatformViewRegistry()
        .registerViewFactory(
            "kr.co.bootpay/webview",
            new FlutterViewFactory(proxyApiRegistrar.getInstanceManager()));

    proxyApiRegistrar.setUp();

    // Popup config channel: lets Dart configure the popup close button
    // (addAdHosts / setCloseButtonMode) and dismiss the active popup (closePopup).
    popupConfigChannel = new MethodChannel(binding.getBinaryMessenger(), "kr.co.bootpay/webview_popup");
    popupConfigChannel.setMethodCallHandler(
        (@NonNull MethodCall call, @NonNull MethodChannel.Result result) -> {
          if ("addAdHosts".equals(call.method)) {
            BootpayPopupAdFilter.getInstance().addAdHosts(extractHosts(call.arguments));
            result.success(true);
          } else if ("setCloseButtonMode".equals(call.method)) {
            BootpayPopupAdFilter.getInstance().setCloseButtonMode(extractMode(call.arguments));
            result.success(true);
          } else if ("closePopup".equals(call.method)) {
            WebChromeClientProxyApi.SecureWebChromeClient.closeActivePopup();
            result.success(true);
          } else {
            result.notImplemented();
          }
        });
  }

  /** Reads the close-button mode from either a raw String arg or a {"mode": "..."} map arg. */
  @Nullable
  private static String extractMode(@Nullable Object args) {
    if (args instanceof String) {
      return (String) args;
    }
    if (args instanceof Map) {
      Object value = ((Map<?, ?>) args).get("mode");
      if (value instanceof String) {
        return (String) value;
      }
    }
    return null;
  }

  /** Reads the host list from either a raw List arg or a {"hosts": [...]} map arg. */
  @Nullable
  @SuppressWarnings("unchecked")
  private static List<String> extractHosts(@Nullable Object args) {
    if (args instanceof List) {
      return (List<String>) args;
    }
    if (args instanceof Map) {
      Object value = ((Map<?, ?>) args).get("hosts");
      if (value instanceof List) {
        return (List<String>) value;
      }
    }
    return null;
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    if (proxyApiRegistrar != null) {
      proxyApiRegistrar.tearDown();
      proxyApiRegistrar.getInstanceManager().stopFinalizationListener();
      proxyApiRegistrar = null;
    }
    if (popupConfigChannel != null) {
      popupConfigChannel.setMethodCallHandler(null);
      popupConfigChannel = null;
    }
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding activityPluginBinding) {
    if (proxyApiRegistrar != null) {
      proxyApiRegistrar.setContext(activityPluginBinding.getActivity());
    }
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    proxyApiRegistrar.setContext(pluginBinding.getApplicationContext());
  }

  @Override
  public void onReattachedToActivityForConfigChanges(
      @NonNull ActivityPluginBinding activityPluginBinding) {
    proxyApiRegistrar.setContext(activityPluginBinding.getActivity());
  }

  @Override
  public void onDetachedFromActivity() {
    proxyApiRegistrar.setContext(pluginBinding.getApplicationContext());
  }

  /** Maintains instances used to communicate with the corresponding objects in Dart. */
  @Nullable
  public AndroidWebkitLibraryPigeonInstanceManager getInstanceManager() {
    return proxyApiRegistrar.getInstanceManager();
  }
}
