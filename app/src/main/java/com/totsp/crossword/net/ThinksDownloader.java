package com.totsp.crossword.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;


/**
 * Thinks.com
 * URL: http://thinks.com/daily-crossword/puzzles/YYYY-MM/dc1-YYYY-MM-DD.puz
 * Date = Fridays
 */
public class ThinksDownloader extends AbstractDownloader {
    private static final String NAME = "Thinks.com";
    NumberFormat nf = NumberFormat.getInstance();

    public ThinksDownloader() {
        super("http://thinks.com/daily-crossword/puzzles/", DOWNLOAD_DIR, NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates() {
        return DATE_DAILY;
    }

    public String getName() {
        return NAME;
    }

    public File download(Date date) {
        return super.download(date, this.createUrlSuffix(date));
    }

    @Override
    protected String createUrlSuffix(Date date) {
        return (date.getYear() + 1900) + "-" + nf.format(date.getMonth() + 1) + "/" + "dc1-" + (date.getYear() + 1900) +
        "-" + nf.format(date.getMonth() + 1) + "-" + nf.format(date.getDate()) + ".puz";
    }
}
