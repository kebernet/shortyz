package com.totsp.crossword.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;


/**
 * Wall Street Journal
 * URL: http://mazerlm.home.comcast.net/~mazerlm/wsjYYMMDD.puz
 * Date = Fridays
 */
public class WSJDownloader extends AbstractDownloader {
    private static final String NAME = "Wall Street Journal";
    NumberFormat nf = NumberFormat.getInstance();

    public WSJDownloader() {
        super("http://herbach.dnsalias.com/wsj/", DOWNLOAD_DIR, NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates() {
        return DATE_FRIDAY;
    }

    public String getName() {
        return NAME;
    }

    public File download(Date date) {
        return super.download(date, this.createUrlSuffix(date));
    }

    @Override
    protected String createUrlSuffix(Date date) {
        return "wsj" + nf.format(date.getYear() - 100) + nf.format(date.getMonth() + 1) + nf.format(date.getDate()) +
        ".puz";
    }
}
