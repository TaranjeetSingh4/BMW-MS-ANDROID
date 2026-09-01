package com.appventurez.bmwms.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.appventurez.bmwms.R;

public class WebViewActivity extends AppCompatActivity {

    private static final String TAG = "WebViewActivity";
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        
        Toast.makeText(this, "Opening page...", Toast.LENGTH_SHORT).show();

        setupWebView();

        String url = getIntent().getStringExtra("url");
        Log.d(TAG, "Loading URL: " + url);
        
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        } else {
            Toast.makeText(this, "URL not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void setupWebView() {
        // Essential settings for modern sites
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        
        // Add a User-Agent string to help some servers
        String userAgent = settings.getUserAgentString();
        settings.setUserAgentString(userAgent + " MobileBMWMSApp");
        
        // Ensure webView is ready
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d(TAG, "onPageStarted: " + url);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "onPageFinished: " + url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.e(TAG, "Page Error: " + error.toString());
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                Log.w(TAG, "Bypassing SSL Error: " + error.toString());
                handler.proceed(); // Ignore SSL errors for this specific government site
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false; 
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                Log.d(TAG, "Progress: " + newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

}
