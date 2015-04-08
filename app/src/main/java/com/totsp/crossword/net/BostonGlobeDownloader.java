package com.totsp.crossword.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;


/**
 * Boston Globe
 * URL: http://standalone.com/dl/puz/boston/boston_MMDDYY.puz
 * Date = Sundays
 */
public class BostonGlobeDownloader extends AbstractDownloader {
    private static final String NAME = "Boston Globe";
    NumberFormat nf = NumberFormat.getInstance();

    public BostonGlobeDownloader() {
        super("http://standalone.com/dl/puz/boston/", DOWNLOAD_DIR, NAME);
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
        return "boston_" + nf.format(date.getMonth() + 1) + nf.format(date.getDate()) + (date.getYear() + 1900) +
        ".puz";
    }
}
