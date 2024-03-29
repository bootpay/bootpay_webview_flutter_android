// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package kr.co.bootpay.webviewflutter;

import android.os.Handler;
import androidx.annotation.NonNull;
import kr.co.bootpay.webviewflutter.GeneratedAndroidWebView.JavaScriptChannelHostApi;

/**
 * Host api implementation for {@link JavaScriptChannel}.
 *
 * <p>Handles creating {@link JavaScriptChannel}s that intercommunicate with a paired Dart object.
 */
public class JavaScriptChannelHostApiImpl implements JavaScriptChannelHostApi {
  private final InstanceManager instanceManager;
  private final JavaScriptChannelCreator javaScriptChannelCreator;
  private final JavaScriptChannelFlutterApiImpl flutterApi;

  private Handler platformThreadHandler;

  /** Handles creating {@link JavaScriptChannel}s for a {@link JavaScriptChannelHostApiImpl}. */
  public static class JavaScriptChannelCreator {
    /**
     * Creates a {@link JavaScriptChannel}.
     *
     * @param flutterApi handles sending messages to Dart
     * @param channelName JavaScript channel the message should be sent through
     * @param platformThreadHandler handles making callbacks on the desired thread
     * @return the created {@link JavaScriptChannel}
     */
    @NonNull
    public JavaScriptChannel createJavaScriptChannel(
            @NonNull JavaScriptChannelFlutterApiImpl flutterApi,
            @NonNull String channelName,
            @NonNull Handler platformThreadHandler) {
      return new JavaScriptChannel(flutterApi, channelName, platformThreadHandler);
    }
  }

  /**
   * Creates a host API that handles creating {@link JavaScriptChannel}s.
   *
   * @param instanceManager maintains instances stored to communicate with Dart objects
   * @param javaScriptChannelCreator handles creating {@link JavaScriptChannel}s
   * @param flutterApi handles sending messages to Dart
   * @param platformThreadHandler handles making callbacks on the desired thread
   */
  public JavaScriptChannelHostApiImpl(
          @NonNull InstanceManager instanceManager,
          @NonNull JavaScriptChannelCreator javaScriptChannelCreator,
          @NonNull JavaScriptChannelFlutterApiImpl flutterApi,
          @NonNull Handler platformThreadHandler) {
    this.instanceManager = instanceManager;
    this.javaScriptChannelCreator = javaScriptChannelCreator;
    this.flutterApi = flutterApi;
    this.platformThreadHandler = platformThreadHandler;
  }

  /**
   * Sets the platformThreadHandler to make callbacks
   *
   * @param platformThreadHandler the new thread handler
   */
  public void setPlatformThreadHandler(@NonNull Handler platformThreadHandler) {
    this.platformThreadHandler = platformThreadHandler;
  }

  @Override
  public void create(@NonNull Long instanceId, @NonNull String channelName) {
    final JavaScriptChannel javaScriptChannel =
            javaScriptChannelCreator.createJavaScriptChannel(
                    flutterApi, channelName, platformThreadHandler);
    instanceManager.addDartCreatedInstance(javaScriptChannel, instanceId);
  }
}
