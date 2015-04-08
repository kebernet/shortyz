package com.totsp.crossword.net;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by keber_000 on 2/11/14.
 */
public class LATSundayDownloader extends AbstractJPZDownloader {

    private static final String NAME = "LAT Sunday Calendar";
    private final SimpleDateFormat df = new SimpleDateFormat("yyMMdd");


    public LATSundayDownloader() {
        super("http://cdn.games.arkadiumhosted.com/latimes/assets/SundayCrossword/mreagle_", DOWNLOAD_DIR, NAME);
    }

    @Override
    protected String createUrlSuffix(Date date) {
        return df.format(date) +".xml";
    }

    public int[] getDownloadDates() {
        return DATE_SUNDAY;
    }

    public String getName() {
        return NAME;
    }

    public File download(Date date) {
        return download(date, this.createUrlSuffix(date), EMPTY_MAP);
    }
}
