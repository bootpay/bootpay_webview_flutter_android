// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:bootpay_webview_flutter_platform_interface/bootpay_webview_flutter_platform_interface.dart';

import 'android_webview_controller.dart';
import 'android_webview_cookie_manager.dart';

/// Implementation of [WebViewPlatform] using the WebKit API.
class BootpayAndroidWebViewPlatform extends WebViewPlatform {
  /// Registers this class as the default instance of [WebViewPlatform].
  static void registerWith() {
    WebViewPlatform.instance = BootpayAndroidWebViewPlatform();
  }

  @override
  AndroidWebViewController createPlatformWebViewController(
    PlatformWebViewControllerCreationParams params,
  ) {
    return AndroidWebViewController(params);
  }

  @override
  AndroidNavigationDelegate createPlatformNavigationDelegate(
    PlatformNavigationDelegateCreationParams params,
  ) {
    return AndroidNavigationDelegate(params);
  }

  @override
  AndroidWebViewWidget createPlatformWebViewWidget(
    PlatformWebViewWidgetCreationParams params,
  ) {
    return AndroidWebViewWidget(params);
  }

  @override
  BootpayAndroidWebViewCookieManager createPlatformCookieManager(
    PlatformWebViewCookieManagerCreationParams params,
  ) {
    return BootpayAndroidWebViewCookieManager(params);
  }
}
