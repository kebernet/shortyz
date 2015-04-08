package com.totsp.crossword.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;


/**
 * Philadelphia Inquirer
 * URL: http://mazerlm.home.comcast.net/~mazerlm/piYYMMDD.puz
 * Date = Sundays
 */
public class PhillyDownloader extends AbstractDownloader {
    private static final String NAME = "Phil Inquirer";
    NumberFormat nf = NumberFormat.getInstance();

    public PhillyDownloader() {
        super("http://mazerlm.home.comcast.net/~mazerlm/", DOWNLOAD_DIR, NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates() {
        return DATE_SUNDAY;
    }

    public String getName() {
        return NAME;
    }

    public File download(Date date) {
        return super.download(date, this.createUrlSuffix(date));
    }

    @Override
    protected String createUrlSuffix(Date date) {
        return "pi" + nf.format(date.getYear() - 100) + nf.format(date.getMonth() + 1) + nf.format(date.getDate()) +
        ".puz";
    }
}
