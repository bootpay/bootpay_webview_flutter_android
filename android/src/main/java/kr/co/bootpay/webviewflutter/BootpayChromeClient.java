package kr.co.bootpay.webviewflutter;

import android.webkit.WebChromeClient;
import android.webkit.WebView;

//popup webview을 위한 처리
public class BootpayChromeClient extends WebChromeClient {

    @Override
    public void onCloseWindow(WebView window) {
        super.onCloseWindow(window);
    }


}
