package com.totsp.crossword.nyt;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.totsp.crossword.ShortyzActivity;
import com.totsp.crossword.shortyz.R;
import com.totsp.crossword.shortyz.ShortyzApplication;
import com.totsp.crossword.versions.AndroidVersionUtils;

import java.net.HttpCookie;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.internal.http.HttpDate;

import static android.view.View.GONE;

/**
 * Created by rcooper on 6/1/17.
 */

public class LoginActivity extends ShortyzActivity {
    private static final Logger LOG = Logger.getLogger(LoginActivity.class.getCanonicalName());
    private static final String PUZZLES_URL = "https://www.nytimes.com/crosswords";
    private static final String LOGIN_URL = "https://myaccount.nytimes.com/auth/login?URI=https%3A%2F%2Fwww.nytimes.com%2Fcrosswords%2Findex.html&OQ=page%3Dhome%26_r%3D1%26page%3Dhome%26";
    protected AndroidVersionUtils utils = AndroidVersionUtils.Factory.getInstance();


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login to Your NYT Account.");
        utils.holographic(this);
        utils.finishOnHomeButton(this);
        this.setContentView(R.layout.html_view);

        WebView webview = (WebView) this.findViewById(R.id.webkit);
        webview.loadUrl(LOGIN_URL);
        webview.getSettings().setJavaScriptEnabled(true);
        FloatingActionButton download = (FloatingActionButton) this.findViewById(R.id.button_floating_action);
        if(download != null) {
            download.setVisibility(GONE);
        }


        webview.setWebViewClient(new WebViewClient(){

            ProgressDialog dialog;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                dialog = ProgressDialog.show(LoginActivity.this, "Please wait...", null, true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                dialog.dismiss();
                if(url.startsWith(PUZZLES_URL)){
                    String string = CookieManager.getInstance().getCookie(url);
                    LOG.info("Got cookie string: "+string);
                    HttpUrl httpUrl = HttpUrl.parse(url);
                    List<Cookie> cookies =  parseCookies(httpUrl, string);
                    CookieJar cookieJar = ShortyzApplication.getInstance().getCookieJar();
                    cookieJar.saveFromResponse(httpUrl, cookies);
                    prefs.edit().putBoolean("didNYTLogin", true).apply();
                    finish();
                }
            }
        });
    }

    @Nonnull
    private List<Cookie> parseCookies(@Nonnull final HttpUrl url, @Nonnull String string) {
        List<String> cookieStrings = Splitter.on(";").trimResults().splitToList(string);
        List<HttpCookie> httpCookies = Lists.transform(cookieStrings, new Function<String, HttpCookie>() {
            @Nullable
            @Override
            public HttpCookie apply(@Nullable String input) {
                return input != null ? HttpCookie.parse(input).get(0) : null;
            }
        });
        return Lists.transform(httpCookies, new Function<HttpCookie, Cookie>() {
            @Nullable
            @Override
            public Cookie apply(@Nullable HttpCookie input) {
                if(input == null){
                    return null;
                }
                Cookie.Builder builder = new Cookie.Builder()
                        .name(input.getName())
                        .value(input.getValue());
                if(input.getMaxAge() != -1){
                     builder = builder.expiresAt(System.currentTimeMillis() + input.getMaxAge());
                } else {
                    builder.expiresAt(HttpDate.MAX_DATE);
                }
                if(input.getSecure()){
                    builder = builder.secure();
                }
                if(input.getPath() != null) {
                    builder = builder.path(input.getPath());
                }
                if(input.getDomain() != null){
                    builder = builder.domain(input.getDomain());
                } else {
                    builder= builder.domain(url.host());
                }
                Cookie result = builder.build();
                LOG.info("Parsed Cookie "+result);
                return builder.build();
            }
        });
    }
}
