package com.totsp.crossword.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;


/**
 * Washington Post downloader
 * URL: http://www.washingtonpost.com/r/WashingtonPost/Content/Puzzles/Daily/
 * Date = Daily
 */
public class WaPoDownloader extends AbstractJPZDownloader {
    private static final String NAME = "Washington Post";
    NumberFormat nf = NumberFormat.getInstance();

    public WaPoDownloader() {
        super("http://cdn.games.arkadiumhosted.com/washingtonpost/crossynergy/", DOWNLOAD_DIR, NAME);
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
        return download(date, this.createUrlSuffix(date), EMPTY_MAP);
    }

    @Override
    protected String createUrlSuffix(Date date) {
        return "cs" + (date.getYear() - 100) + nf.format(date.getMonth() + 1) + nf.format(date.getDate()) + ".jpz";
    }
}
