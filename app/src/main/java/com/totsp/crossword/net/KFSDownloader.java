package com.totsp.crossword.net;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * King Features Syndicate Puzzles
 * URL: http://puzzles.kingdigital.com/jpz/YYYYMMDD.jpz
 * premier = Sunday
 * joseph = Monday-Saturday
 * sheffer = Monday-Saturday
 */
public class KFSDownloader extends AbstractJPZDownloader {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    private String fullName;
    private int[] days;

    public KFSDownloader(String shortName, String fullName, int[] days) {
        super("http://puzzles.kingdigital.com/jpz/"+shortName+"/", DOWNLOAD_DIR, fullName);
        this.fullName = fullName;
        this.days = days;
    }

    public int[] getDownloadDates() {
        return days;
    }

    public String getName() {
        return fullName;
    }

    public File download(Date date) {
        return download(date, this.createUrlSuffix(date), EMPTY_MAP);
    }

    @Override
    protected String createUrlSuffix(Date date) {
        return df.format(date) + ".jpz";
    }
}
