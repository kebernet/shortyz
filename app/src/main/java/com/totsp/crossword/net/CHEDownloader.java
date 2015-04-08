package com.totsp.crossword.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;


/**
 * Chronicle of Higher Education
 * URL: http://chronicle.com/items/biz/puzzles/YYYYMMDD.puz
 * Date = Fridays
 */
public class CHEDownloader extends AbstractDownloader {
    private static final String NAME = "Chronicle of Higher Education";
    NumberFormat nf = NumberFormat.getInstance();

    public CHEDownloader() {
        super("http://chronicle.com/items/biz/puzzles/", DOWNLOAD_DIR, NAME);
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
        return (date.getYear() + 1900) + nf.format(date.getMonth() + 1) + nf.format(date.getDate()) + ".puz";
    }
}
