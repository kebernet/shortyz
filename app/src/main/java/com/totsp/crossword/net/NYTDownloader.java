package com.totsp.crossword.net;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.nyt.ErrorActivity;
import com.totsp.crossword.shortyz.ShortyzApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * New York Times URL: http://select.nytimes.com/premium/xword/[Mon]DDYY.puz
 * Date = Daily
 */
public class NYTDownloader extends AbstractDownloader {
    private static final long lastDailyPathTime = new Date(2017, 04, 20, 00, 00, 00).getTime();
    private static final String[] MONTHS = new String[]{"Jan", "Feb", "Mar",
            "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    public static final String NAME = "New York Times";
    private static final String PUZZLES_PAGE_URL = "https://www.nytimes.com/crosswords/index.html?page=home&_r=0";
    NumberFormat nf = NumberFormat.getInstance();
    private Context context;
    private HashMap<String, String> params = new HashMap<String, String>();

    public NYTDownloader(Context context) {
        super("https://www.nytimes.com/svc/crosswords/v2/puzzle/", DOWNLOAD_DIR, NAME);
        this.context = context;
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates() {
        return DATE_DAILY;
    }

    public String getName() {
        return NYTDownloader.NAME;
    }

    public File download(Date date) {
        return this.download(date, this.createUrlSuffix(date));
    }

    @Override
    protected String createUrlSuffix(Date date) {
//		if(date.getTime() < lastDailyPathTime) {
        return "daily-" + (date.getYear() + 1900) + "-"
                + this.nf.format(date.getMonth() + 1) + "-"
                + this.nf.format(date.getDate()) + ".puz";
//		} else {
//			return
//		}
    }

    @Override
    protected File download(Date date, String urlSuffix) {
        try {
            URL url = new URL(this.baseUrl + urlSuffix);
            OkHttpClient client = this.createClient();


            Request request = new Request.Builder()
                    .url(url)
                    .header("Referer", PUZZLES_PAGE_URL)
                    .build();


            Response response = client.newCall(request).execute();

            if(response.code() == 401){
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putBoolean("didNYTLogin", false)
                        .apply();
                Intent i = new Intent(context, ErrorActivity.class);
                context.startActivity(i);
                return null;
            }
            if (response.code() == 200) {
                File f = new File(downloadDirectory, this.createFileName(date));
                FileOutputStream fos = new FileOutputStream(f);
                IO.copyStream(
                        response.body().byteStream(), fos);
                fos.close();

                IO.copyStream(
                        new FileInputStream(f),
                        new FileOutputStream(downloadDirectory
                                .getAbsolutePath() + "/debug/debug.puz"));

                return f;
            } else {
                return null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public OkHttpClient createClient() throws IOException {
        LOG.info(ShortyzApplication.getInstance().getCookieJar().loadForRequest(HttpUrl.parse("https://www.nytimes.com/svc/crosswords/v2/puzzle/")).toString());
        OkHttpClient httpclient = new OkHttpClient.Builder()
                .cookieJar(ShortyzApplication.getInstance().getCookieJar())
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request originalRequest = chain.request();
                        Request requestWithUserAgent = originalRequest.newBuilder()
                                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36")
                                .build();
                        return chain.proceed(requestWithUserAgent);
                    }
                })
                .build();

        return httpclient;
    }
}
