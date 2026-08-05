package com.appventurez.EcoGov.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

import com.appventurez.EcoGov.R;


public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true); // Enable JavaScript if required

      //  String pdfUrl = getIntent().
       // webView.loadUrl("https://docs.google.com/gview?embedded=true&url=" + pdfUrl);

    }
}