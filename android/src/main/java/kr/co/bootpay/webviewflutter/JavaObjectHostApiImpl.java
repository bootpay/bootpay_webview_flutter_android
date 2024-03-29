// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package kr.co.bootpay.webviewflutter;

import androidx.annotation.NonNull;

/**
 * A pigeon Host API implementation that handles creating {@link Object}s and invoking its static
 * and instance methods.
 *
 * <p>{@link Object} instances created by {@link JavaObjectHostApiImpl} are used to intercommunicate
 * with a paired Dart object.
 */
public class JavaObjectHostApiImpl implements GeneratedAndroidWebView.JavaObjectHostApi {
  private final InstanceManager instanceManager;

  /**
   * Constructs a {@link JavaObjectHostApiImpl}.
   *
   * @param instanceManager maintains instances stored to communicate with Dart objects
   */
  public JavaObjectHostApiImpl(@NonNull InstanceManager instanceManager) {
    this.instanceManager = instanceManager;
  }

  @Override
  public void dispose(@NonNull Long identifier) {
    final Object instance = instanceManager.getInstance(identifier);
    if (instance instanceof WebViewHostApiImpl.WebViewPlatformView) {
      ((WebViewHostApiImpl.WebViewPlatformView) instance).destroy();
    }
    instanceManager.remove(identifier);
  }
}
