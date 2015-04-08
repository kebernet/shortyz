package com.totsp.crossword.net;

import java.io.File;
import java.util.Calendar;
import java.util.Date;


/**
 * New York Times Classic
 * URL: http://www.nytimes.com/specials/puzzles/classic.puz
 * Date = Mondays, but no archive is available.
 */
public class NYTClassicDownloader extends AbstractDownloader {
    private static final String NAME = "New York Times Classic";

    public NYTClassicDownloader() {
        super("http://www.nytimes.com/specials/puzzles/", DOWNLOAD_DIR, NAME);
    }

    public int[] getDownloadDates() {
        return DATE_MONDAY;
    }

    public String getName() {
        return NAME;
    }

    public File download(Date date) {
        Calendar currentCal = Calendar.getInstance();
        Calendar downloadCal = Calendar.getInstance();
        downloadCal.setTime(date);

        // Only download if requested week is same as current week, because there is no archive.
        if ((currentCal.get(Calendar.YEAR) != downloadCal.get(Calendar.YEAR)) ||
                (currentCal.get(Calendar.WEEK_OF_YEAR) != downloadCal.get(Calendar.WEEK_OF_YEAR))) {
            return null;
        }

        return super.download(date, this.createUrlSuffix(date));
    }

    @Override
    protected String createUrlSuffix(Date date) {
        return "classic.puz";
    }
}
