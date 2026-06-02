// Copyright 2024 Bootpay
// Classifies popup (window.open / target="_blank") URLs as ad pages, so the
// Android popup shows a manual close bar only for ads — payment popups stay
// clean (no bar). Payment popups navigate to dynamic PG gateway URLs that can
// not be enumerated, so the default is "no bar"; only popups whose host matches
// a known ad network get the bar.

package kr.co.bootpay.webviewflutter;

import android.net.Uri;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Holds the ad-network host list used to decide whether a popup needs a manual
 * close bar. Nothing is blocked: the popup (and the ad) is always shown — this
 * list only controls whether the close bar is displayed.
 *
 * <p>AdSense click-throughs always route through one of the default fragments
 * ({@code googleadservices.com} / {@code doubleclick.net} /
 * {@code googlesyndication.com}) before redirecting to the advertiser, so
 * matching the popup's URL host is reliable for the AdSense case. The list can
 * be extended from Dart via the {@code kr.co.bootpay/webview_popup} method
 * channel ({@code addAdHosts}), registered in {@link WebViewFlutterPlugin}.
 */
public final class BootpayPopupAdFilter {

  private static final BootpayPopupAdFilter INSTANCE = new BootpayPopupAdFilter();

  public static BootpayPopupAdFilter getInstance() {
    return INSTANCE;
  }

  /**
   * Known ad-network host fragments (case-insensitive substring match against
   * the URL host). Conservative on purpose: anything not matched is treated as
   * a payment/other popup and keeps the current bar-less behavior.
   */
  private final List<String> adHostFragments =
      new ArrayList<>(
          Arrays.asList(
              "doubleclick.net",
              "googleadservices.com",
              "googlesyndication.com",
              "adservice.google.",
              "adnxs.com",
              "amazon-adsystem.com",
              "taboola.com",
              "outbrain.com",
              "criteo.com",
              "media.net",
              "adsystem.com"));

  /**
   * Close-button visibility mode: {@code "auto"} (default — show ✕ only on ad
   * popups), {@code "always"} (every popup) or {@code "never"} (no ✕). Set from
   * Dart via {@code setCloseButtonMode}.
   */
  private String closeButtonMode = "auto";

  private BootpayPopupAdFilter() {}

  /**
   * Sets the close-button visibility mode ({@code "auto"} | {@code "always"} |
   * {@code "never"}). Unknown / null values fall back to {@code "auto"}.
   */
  public synchronized void setCloseButtonMode(@Nullable String mode) {
    String normalized = mode == null ? "auto" : mode.toLowerCase(Locale.ROOT);
    switch (normalized) {
      case "always":
      case "never":
      case "auto":
        closeButtonMode = normalized;
        break;
      default:
        closeButtonMode = "auto";
    }
  }

  /**
   * Whether the floating close (✕) button should be shown for a popup at
   * {@code url}, per the current mode:
   *
   * <ul>
   *   <li>{@code "never"}  -&gt; always false
   *   <li>{@code "always"} -&gt; always true
   *   <li>{@code "auto"}   -&gt; true only when the URL matches a known ad host.
   * </ul>
   */
  public synchronized boolean shouldShowCloseButton(@Nullable String url) {
    switch (closeButtonMode) {
      case "never":
        return false;
      case "always":
        return true;
      default:
        return isAdUrl(url);
    }
  }

  /**
   * Appends host fragments to the ad-host list. Empty / duplicate entries are
   * ignored. Defaults always remain present (additive only).
   */
  public synchronized void addAdHosts(@Nullable List<String> hosts) {
    if (hosts == null) {
      return;
    }
    for (String host : hosts) {
      if (host == null) {
        continue;
      }
      String normalized = host.toLowerCase(Locale.ROOT);
      if (!normalized.isEmpty() && !adHostFragments.contains(normalized)) {
        adHostFragments.add(normalized);
      }
    }
  }

  /** Returns true if the URL's host matches a known ad-network fragment. */
  public synchronized boolean isAdUrl(@Nullable String url) {
    if (url == null || url.isEmpty()) {
      return false;
    }
    String host;
    try {
      host = Uri.parse(url).getHost();
    } catch (Exception e) {
      return false;
    }
    if (host == null || host.isEmpty()) {
      return false;
    }
    host = host.toLowerCase(Locale.ROOT);
    for (String fragment : adHostFragments) {
      if (host.contains(fragment)) {
        return true;
      }
    }
    return false;
  }
}
