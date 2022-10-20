package kr.co.bootpay.webviewflutter;

import android.annotation.TargetApi;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

public class BootpayWebViewClient extends WebViewClient {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(
            @NonNull WebView view, @NonNull WebResourceRequest request) {
        String url = request.getUrl().toString();


        if(BootpayUrlHelper.doDeepLinkIfPayUrl(view, url)) {
            //do deep link by doDeepLinkIfPayUrl function
            return true;
        }
        return false;
    }


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if(BootpayUrlHelper.doDeepLinkIfPayUrl(view, url)) {
            //do deep link by doDeepLinkIfPayUrl function
            return true;
        }
        return false;
    }
}
