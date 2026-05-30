package com.muammercerdin.hesapasistaniv3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity {
    private WebView webView;

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.parseColor("#070d16"));
        getWindow().setNavigationBarColor(Color.parseColor("#070d16"));

        webView = new WebView(this);

        int topPad = getStatusBarHeight();
        if (topPad > 0) {
            webView.setPadding(0, topPad, 0, 0);
            webView.setClipToPadding(false);
        }

        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setSupportMultipleWindows(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return openExternalIfNeeded(request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return openExternalIfNeeded(url);
            }
        });

        webView.loadUrl("file:///android_asset/hesap_v3.html");
    }

    private int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private boolean openExternalIfNeeded(String url) {
        if (url == null) return false;

        if (url.startsWith("file:///android_asset/")) {
            return false;
        }

        if (url.startsWith("https://wa.me/")
                || url.startsWith("whatsapp://")
                || url.startsWith("mailto:")
                || url.startsWith("tel:")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    public class AndroidBridge {
        @JavascriptInterface
        public void shareCurrentScreen(String fileName) {
            runOnUiThread(() -> {
                try {
                    if (webView == null || webView.getWidth() <= 0 || webView.getHeight() <= 0) {
                        Toast.makeText(MainActivity.this, "Ekran hazır değil", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Bitmap bitmap = Bitmap.createBitmap(
                            webView.getWidth(),
                            webView.getHeight(),
                            Bitmap.Config.ARGB_8888
                    );

                    Canvas canvas = new Canvas(bitmap);
                    webView.draw(canvas);

                    File dir = new File(getCacheDir(), "share");
                    if (!dir.exists()) dir.mkdirs();

                    String cleanName = "hesap-pusulasi.png";
                    if (fileName != null && fileName.toLowerCase().endsWith(".png")) {
                        cleanName = fileName.replaceAll("[^a-zA-Z0-9._-]", "-");
                    }

                    File outFile = new File(dir, cleanName);
                    FileOutputStream fos = new FileOutputStream(outFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();

                    Uri uri = FileProvider.getUriForFile(
                            MainActivity.this,
                            getPackageName() + ".fileprovider",
                            outFile
                    );

                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("image/png");
                    sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Hesap Pusulası");
                    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    Intent chooser = Intent.createChooser(sendIntent, "Hesap pusulasını paylaş");
                    startActivity(chooser);

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Paylaşım hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }
}
