package com.totsp.crossword.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Date;


/**
 * The Onion AV Club
 * URL: http://herbach.dnsalias.com/Tausig/avYYMMDD.puz
 * Date = Wednesdays
 */
public class AVClubDownloader extends AbstractDownloader {
    public static final String NAME = "The Onion AV Club";
    NumberFormat nf = NumberFormat.getInstance();

    protected AVClubDownloader() {
        super("http://herbach.dnsalias.com/Tausig/", DOWNLOAD_DIR, NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates() {
        return DATE_WEDNESDAY;
    }

    public String getName() {
        return NAME;
    }

    public File download(Date date) {
        return this.download(date, this.createUrlSuffix(date));
    }

    @Override
    protected String createUrlSuffix(Date date) {
        return "av" + this.nf.format(date.getYear() - 100) + this.nf.format(date.getMonth() + 1) +
        this.nf.format(date.getDate()) + ".puz";
    }

    @Override
    protected File download(Date date, String urlSuffix) {
        try {
            URL url = new URL(this.baseUrl + urlSuffix);
            System.out.println(url);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Referer", this.baseUrl);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                File f = new File(downloadDirectory, this.createFileName(date));
                FileOutputStream fos = new FileOutputStream(f);
                AbstractDownloader.copyStream(connection.getInputStream(), fos);
                fos.close();

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
}
