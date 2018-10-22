package com.totsp.crossword.net;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

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

    private static final String NOTIFICATION_TAG = "NYTDownloader";
    private static final int LOGIN_NOTIFICATION_ID = 1;

    NumberFormat nf = NumberFormat.getInstance();
    private Context context;
    private HashMap<String, String> params = new HashMap<String, String>();
    private NotificationManager nm;

    public NYTDownloader(Context context, NotificationManager nm) {
        super("https://www.nytimes.com/svc/crosswords/v2/puzzle/", DOWNLOAD_DIR, NAME);
        this.context = context;
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
        this.nm = nm;
    }

    public NYTDownloader(Context context) {
        this(context, null);
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
        // When we attempt to download a puzzle and our NYT credentials are out of date, if nm is null,
        // immediately enter the login workflow.  If nm is not null, instead of launching the login
        // workflow, post a notification to the user which can be used to log in at their convenience.

        // At the moment, clearing NYT credentials just sets the didNYTLogin preference to false,
        // and doesn't actually clear the credentials.  This means that if we don't gate the
        // download with a request for credentials, we could successfully download (using the old
        // credentials) when this is not intended by the user.
        //
        // For non-background downloads, we do this request when creating the set of downloaders by
        // not providing a NotificationManager.  During non-interactive downloads (e.g. automatic
        // background downloads), we don't, so add the check here in order to avoid a confusing
        // state where we notify both that we need a login but successfully download a puzzle.
        //
        // Likely, we want to consolidate the behavior for both cases, as there's a similar problem
        // when we launch the login workflow (if you skip the credential screen, the puzzle still
        // downloads successfully).
        if ((nm != null) && requestCredentialsIfNeeded()) {
            return null;
        }

        try {
            URL url = new URL(this.baseUrl + urlSuffix);
            OkHttpClient client = this.createClient();

            Request request = new Request.Builder()
                    .url(url)
                    .header("Referer", PUZZLES_PAGE_URL)
                    .build();

            Response response = client.newCall(request).execute();

            if(response.code() == 401){
                if(response.body().string().contains("[\"Not Found\"]")){
                    Toast.makeText(context, "The NYT Puzzle was not found. Maybe wait 24 hours?", Toast.LENGTH_LONG).show();
                } else {
                    PreferenceManager.getDefaultSharedPreferences(context).edit()
                            .putBoolean("didNYTLogin", false)
                            .apply();
                    requestCredentialsIfNeeded();
                }
                return null;
            }
            if (response.code() == 200) {
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putBoolean("didNYTLogin", true)
                        .apply();
                File f = new File(downloadDirectory, this.createFileName(date));
                FileOutputStream fos = new FileOutputStream(f);
                IO.copyStream(
                        response.body().byteStream(), fos);
                fos.close();

                File debug = new File(downloadDirectory,"debug/debug.puz");

                debug.getParentFile().mkdirs();

                IO.copyStream(
                        new FileInputStream(f),
                        new FileOutputStream(debug));

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

    // Returns true if credentials are needed and a request was made, false if credentials were
    // not needed.
    public boolean requestCredentialsIfNeeded() {
        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                "didNYTLogin", false)) {
            return false;
        }

        Intent loginIntent = new Intent(context, ErrorActivity.class);

        if (nm == null) {
            context.startActivity(loginIntent);
        } else {
            PendingIntent contentIntent = PendingIntent.getActivity(
                    context, 0, loginIntent, 0);
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this.context)
                            .setSmallIcon(android.R.drawable.stat_notify_error)
                            .setContentTitle("Unable to download New York Times puzzles")
                            .setContentText("Click to log in again")
                            .setAutoCancel(true)
                            .setContentIntent(contentIntent)
                            .setWhen(System.currentTimeMillis());
            nm.notify(NOTIFICATION_TAG, LOGIN_NOTIFICATION_ID, builder.build());
        }

        return true;
    }
}
