package com.totsp.crossword.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;


/**
 * Washington Post Puzzler
 * URL: http://crosswords.washingtonpost.com/wp-srv/style/crosswords/util/csserve2.cgi?t=puz&z=puzzler&f=csYYMMDD.puz
 * Date = Sundays
 */
public class WaPoPuzzlerDownloader extends AbstractDownloader {
    private static final String NAME = "Washington Post Puzzler";
    NumberFormat nf = NumberFormat.getInstance();

    public WaPoPuzzlerDownloader() {
        super("http://crosswords.washingtonpost.com/wp-srv/style/crosswords/util/csserve2.cgi?t=puz&z=puzzler&f=",
            DOWNLOAD_DIR, NAME);
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
        return "cs" + nf.format(date.getYear() - 100) + nf.format(date.getMonth() + 1) + nf.format(date.getDate()) +
        ".puz";
    }
}
