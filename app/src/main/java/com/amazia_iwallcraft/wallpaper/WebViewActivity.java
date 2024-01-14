package com.amazia_iwallcraft.wallpaper;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import com.amazia_iwallcraft.items.ItemPage;
import com.amazia_iwallcraft.utils.Methods;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class WebViewActivity extends AppCompatActivity {

    Toolbar toolbar;
    Methods methods;
    WebView webView;
    ItemPage itemPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        methods = new Methods(this);
        methods.forceRTLIfSupported(getWindow());
        methods.setStatusColor(getWindow());

        itemPage = (ItemPage) getIntent().getSerializableExtra("item");

        toolbar = this.findViewById(R.id.toolbar_pages);
        toolbar.setTitle(itemPage.getTitle());
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        webView = findViewById(R.id.webView_pages);
        webView.getSettings().setJavaScriptEnabled(true);

        String mimeType = "text/html;charset=UTF-8";
        String encoding = "utf-8";

        String text;
        if (methods.isDarkMode()) {
            text = "<html><head>"
                    + "<style> body{color: #fff !important;text-align:left}"
                    + "</style></head>"
                    + "<body>"
                    + itemPage.getContent()
                    + "</body></html>";
        } else {
            text = "<html><head>"
                    + "<style> body{color: #000 !important;text-align:left}"
                    + "</style></head>"
                    + "<body>"
                    + itemPage.getContent()
                    + "</body></html>";
        }

        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.loadDataWithBaseURL("blarg://ignored", text, mimeType, encoding, "");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }
}