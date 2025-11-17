# webview_flutter_android → bootpay_webview_flutter_android 포크 작업 가이드

이 문서는 Flutter 공식 `webview_flutter_android` 패키지를 Bootpay용으로 포크하거나 업데이트할 때 필요한 작업을 설명합니다.

## 충돌 방지 처리 (필수!)

> ⚠️ **매우 중요**: 이 처리를 하지 않으면 `webview_flutter`와 `bootpay_flutter_webview`를 동시에 사용할 때 충돌이 발생합니다!

### 문제 상황

공식 `webview_flutter_android`와 플랫폼 뷰 타입 이름이 동일하면 Flutter 엔진에서 다음과 같은 에러가 발생합니다:

```
Error: The platform view type 'plugins.flutter.io/webview' is already registered.
```

### 해결 방법: Bootpay 전용 네임스페이스 사용

플랫폼 뷰 타입 이름을 `"plugins.flutter.io/webview"`에서 `"kr.co.bootpay/webview"`로 변경합니다.

## 필수 변경 사항

### 1. Java 네이티브 코드

**파일**: `android/src/main/java/kr/co/bootpay/webviewflutter/WebViewFlutterPlugin.java`

```java
// ❌ 잘못된 예 (webview_flutter와 충돌)
binding
    .getPlatformViewRegistry()
    .registerViewFactory(
        "plugins.flutter.io/webview",  // ← 충돌 발생
        new FlutterViewFactory(proxyApiRegistrar.getInstanceManager()));

// ✅ 올바른 예 (충돌 없음)
binding
    .getPlatformViewRegistry()
    .registerViewFactory(
        "kr.co.bootpay/webview",  // ← Bootpay 전용 네임스페이스
        new FlutterViewFactory(proxyApiRegistrar.getInstanceManager()));
```

**변경 위치**: 약 45번째 줄

### 2. Dart 코드

다음 파일들에서 모든 `viewType` 발생을 변경해야 합니다:

#### 파일 1: `lib/src/android_webview_controller.dart`

```dart
// ❌ 잘못된 예
viewType: 'plugins.flutter.io/webview',

// ✅ 올바른 예
viewType: 'kr.co.bootpay/webview',
```

**변경 개수**: 4군데

#### 파일 2: `lib/src/legacy/webview_android.dart`

```dart
// ❌ 잘못된 예
viewType: 'plugins.flutter.io/webview',

// ✅ 올바른 예
viewType: 'kr.co.bootpay/webview',
```

**변경 개수**: 1군데

#### 파일 3: `lib/src/legacy/webview_surface_android.dart`

```dart
// ❌ 잘못된 예
viewType: 'plugins.flutter.io/webview',

// ✅ 올바른 예
viewType: 'kr.co.bootpay/webview',
```

**변경 개수**: 2군데

## 일괄 변경 방법

### 수동 변경

```bash
# 1. Java 파일 변경
# WebViewFlutterPlugin.java 파일을 열어서 수동으로 변경

# 2. Dart 파일들 변경
# 각 파일을 열어서 viewType을 수동으로 변경
```

### 자동 스크립트 (macOS/Linux)

```bash
#!/bin/bash
# replace_android_view_types.sh

ANDROID_ROOT="$(pwd)"

# Dart 파일들 일괄 변경
find "$ANDROID_ROOT/lib" -name "*.dart" -type f -exec \
  sed -i '' "s/viewType: 'plugins\.flutter\.io\/webview'/viewType: 'kr.co.bootpay\/webview'/g" {} \;

# Java 파일 변경
sed -i '' 's/"plugins\.flutter\.io\/webview"/"kr.co.bootpay\/webview"/g' \
  "$ANDROID_ROOT/android/src/main/java/kr/co/bootpay/webviewflutter/WebViewFlutterPlugin.java"

echo "✅ Android 플랫폼 뷰 타입 이름 변경 완료"
```

## 변경 확인

```bash
cd bootpay_webview_flutter_android

# 올바르게 변경되었는지 확인
grep -r "kr.co.bootpay/webview" .
# 결과: Java 파일 1개, Dart 파일 7개에서 발견되어야 함

# 잘못된 값이 남아있는지 확인
grep -r "plugins.flutter.io/webview" . --include="*.dart" --include="*.java"
# 결과: 아무것도 나오지 않아야 함
```

## 충돌 방지 테스트

실제 환경에서 두 패키지를 동시에 사용하여 검증:

```yaml
# test_app/pubspec.yaml
dependencies:
  flutter:
    sdk: flutter
  webview_flutter: ^4.0.0  # 공식 패키지
  bootpay_webview_flutter: ^3.0.0  # Bootpay 패키지
```

```dart
// test_app/lib/main.dart
import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart' as official;
import 'package:bootpay_webview_flutter/bootpay_webview_flutter.dart' as bootpay;

class TestPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        // 공식 WebView
        Expanded(
          child: official.WebViewWidget(
            controller: official.WebViewController()
              ..loadRequest(Uri.parse('https://flutter.dev')),
          ),
        ),
        // Bootpay WebView
        Expanded(
          child: bootpay.WebViewWidget(
            controller: bootpay.WebViewController()
              ..loadRequest(Uri.parse('https://bootpay.co.kr')),
          ),
        ),
      ],
    );
  }
}
```

**성공 조건**:
- ✅ 두 WebView가 모두 정상적으로 표시됨
- ✅ 각각 다른 URL을 로드함
- ✅ 충돌이나 에러가 발생하지 않음
- ✅ 각 WebView가 독립적으로 작동함

**실패 시 나타나는 에러**:
```
Error: The platform view type 'plugins.flutter.io/webview' is already registered.
```

## 패키지명 및 구조 유지

새 버전을 fork할 때 다음 사항을 반드시 유지해야 합니다:

### 1. 패키지 구조

```
kr.co.bootpay.webviewflutter  (Java 패키지명)
```

**주의**: Pigeon 설정 파일에서는 다른 패키지명을 사용합니다!

### 2. Pigeon 설정

**파일**: `pigeons/android_webkit.dart`

```dart
@ConfigurePigeon(
  PigeonOptions(
    kotlinOut: 'android/src/main/java/io/flutter/plugins/webviewflutter/AndroidWebkitLibrary.g.kt',
    kotlinOptions: KotlinOptions(
      package: 'io.flutter.plugins.webviewflutter',  // ← 이 패키지명 유지
      errorClassName: 'AndroidWebKitError',
    ),
  ),
)
```

**이유**: Pigeon이 생성하는 코드는 `io.flutter.plugins.webviewflutter` 패키지를 사용하지만, 실제 플러그인 코드는 `kr.co.bootpay.webviewflutter` 패키지를 사용합니다. 이 구조를 변경하지 마세요!

### 3. pubspec.yaml

```yaml
name: bootpay_webview_flutter_android

dependencies:
  bootpay_webview_flutter_platform_interface:
    path: ../bootpay_webview_flutter_platform_interface

flutter:
  plugin:
    implements: bootpay_webview_flutter  # 메인 패키지 이름
    platforms:
      android:
        package: kr.co.bootpay.webviewflutter
        pluginClass: WebViewFlutterPlugin
```

## 체크리스트

새 버전 fork 시 다음 항목을 확인하세요:

- [ ] `pubspec.yaml` 패키지명 및 의존성 확인
- [ ] `WebViewFlutterPlugin.java` - 플랫폼 뷰 이름 변경
- [ ] `lib/src/android_webview_controller.dart` - viewType 변경 (4군데)
- [ ] `lib/src/legacy/webview_android.dart` - viewType 변경 (1군데)
- [ ] `lib/src/legacy/webview_surface_android.dart` - viewType 변경 (2군데)
- [ ] `grep`으로 변경 확인
- [ ] `flutter pub get` 실행
- [ ] `flutter build apk` 테스트
- [ ] 충돌 방지 테스트 (webview_flutter와 동시 사용)

## Pigeon 재생성

Pigeon 정의 파일을 수정한 경우:

```bash
cd bootpay_webview_flutter_android
dart run pigeon --input pigeons/android_webkit.dart
```

**주의**: Pigeon 재생성 후 반드시 충돌 방지 처리를 다시 확인하세요!

## 참고 자료

- [전체 프로젝트 Fork 업데이트 가이드](../FORK_UPDATE_GUIDE.md)
- [공식 webview_flutter_android](https://pub.dev/packages/webview_flutter_android)
- [Flutter Platform Views](https://docs.flutter.dev/platform-integration/android/platform-views)

---

**마지막 업데이트**: 2025-01-17
