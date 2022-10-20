package kr.co.bootpay.webviewflutter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.flutter.plugin.common.MethodChannel;

public class BootpayUrlHelper {

    public static boolean doDeepLinkIfPayUrl(WebView view, String url) {
        Intent intent = getIntentWithPackage(url);
        Context context = view.getContext();

        if(isIntent(url)) {
            if(isInstallApp(intent, context)) return startApp(intent, context);
            else return startGooglePlay(intent, context);
        } else if(isMarket(url)) {
            if(isInstallApp(intent, context)) return startApp(intent, context);
            else return startGooglePlay(intent, context);
        } else if(isSpecialCase(url)) {
            if(isInstallApp(intent, context)) return startApp(intent, context);
            else return startGooglePlay(intent, context);
        }
        return false;
    }

    public static boolean isPreventUrl(String url) {
//        List<String> ignoreUrls = Arrays.asList(
//                "pay.naver.com",
//                "nicepay.co.kr",
//                "payapp.kr",
//                "bootpay.co.kr",
//                "kcp.co.kr"
//        );
//        boolean isPreventUrl = false;
//        for(String ignoreUrl : ignoreUrls) {
//            if(url.contains(ignoreUrl)) {
//                isPreventUrl = true;
//                break;
//            }
//        }
//        return isPreventUrl;
        return false;
    }

    private static Boolean isSpecialCase(String url) {
        return url.matches("^shinhan\\S+$")
                || url.startsWith("kftc-bankpay://")
                || url.startsWith("v3mobileplusweb://")
                || url.startsWith("hdcardappcardansimclick://")
                || url.startsWith("nidlogin://")
                || url.startsWith("mpocket.online.ansimclick://")
                || url.startsWith("wooripay://")
                || url.startsWith("kakaotalk://");
    }

    private static Boolean isIntent(String url) {
        return url.startsWith("intent:");
    }
    private static Boolean isMarket(String url) {
        return url.startsWith("market://");
    }


    private static Intent getIntentWithPackage(String url) {
        try {
            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            if(intent.getPackage() == null) {
                if (url == null) return intent;
                if (url.startsWith("shinhan-sr-ansimclick")) intent.setPackage("com.shcard.smartpay");
                else if (url.startsWith("kftc-bankpay")) intent.setPackage("com.kftc.bankpay.android");
                else if (url.startsWith("ispmobile")) intent.setPackage("kvp.jjy.MispAndroid320");
                else if (url.startsWith("hdcardappcardansimclick")) intent.setPackage("com.hyundaicard.appcard");
                else if (url.startsWith("kb-acp")) intent.setPackage("com.kbcard.kbkookmincard");
                else if (url.startsWith("mpocket.online.ansimclick")) intent.setPackage("kr.co.samsungcard.mpocket");
                else if (url.startsWith("lotteappcard")) intent.setPackage("com.lcacApp");
                else if (url.startsWith("cloudpay")) intent.setPackage("com.hanaskcard.paycla");
                else if (url.startsWith("nhappvardansimclick")) intent.setPackage("nh.smart.nhallonepay");
                else if (url.startsWith("citispay")) intent.setPackage("kr.co.citibank.citimobile");
                else if (url.startsWith("kakaotalk")) intent.setPackage("com.kakao.talk");
            }
            return intent;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean isInstallApp(Intent intent, Context context) {
        return isExistPackageInfo(intent, context) || isExistLaunchedIntent(intent, context);
    }


    private static boolean isExistPackageInfo(Intent intent, Context context) {
        try {
            return intent != null && context.getPackageManager().getPackageInfo(intent.getPackage(), PackageManager.GET_ACTIVITIES) != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isExistLaunchedIntent(Intent intent, Context context) {
        return intent != null &&  intent.getPackage() != null && context.getPackageManager().getLaunchIntentForPackage(intent.getPackage()) != null;
    }

    private static boolean startApp(Intent intent, Context context) {
//        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        return true;
    }

    private static boolean startGooglePlay(Intent intent, Context context) {
        final String appPackageName = intent.getPackage();

        if(appPackageName == null) {
            Uri dataUri = intent.getData();

            try {
                Intent addIntent = new Intent(Intent.ACTION_VIEW, intent.getData());
//                addIntent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK);
                addIntent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(addIntent);
            } catch (Exception e) {
                String packageName = "com.nhn.android.search"; //appPackageName이 비어있으면 네이버로 보내기(네이버 로그인)
                if(dataUri != null && dataUri.toString().startsWith("wooripay://")) packageName = "com.wooricard.wpay"; //우리카드 예외처리

                Intent addIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
//                addIntent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK);
                addIntent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(addIntent);
//                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
            }
            return true;
        }
        try {
            Intent addIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
//            addIntent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK);
            addIntent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(addIntent);
        } catch (android.content.ActivityNotFoundException anfe) {
            Intent addIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
//            addIntent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK);
            addIntent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(addIntent);
        }
        return true;
    }
}
