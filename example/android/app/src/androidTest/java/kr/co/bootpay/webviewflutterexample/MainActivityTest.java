// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package example.android.app.src.androidTest.java.kr.co.bootpay.webviewflutterexample;

import androidx.test.rule.ActivityTestRule;
import dev.flutter.plugins.integration_test.FlutterTestRunner;
import io.flutter.embedding.android.FlutterActivity;
import kr.co.bootpay.DartIntegrationTest;
import org.junit.Rule;
import org.junit.runner.RunWith;

@DartIntegrationTest
@RunWith(FlutterTestRunner.class)
public class MainActivityTest {
  @Rule
  public ActivityTestRule<FlutterActivity> rule = new ActivityTestRule<>(FlutterActivity.class);
}
