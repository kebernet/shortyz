package com.totsp.crossword.net;

import java.util.Date;

/**
 * Created by rcooper on 9/28/15.
 */
public class WSJSaturdayDownloader extends WSJFridayDownloader {

    @Override
    public int[] getDownloadDates() {
        return DATE_SATURDAY;
    }

    public Date getGoodFrom(){
        return new Date(115, 8, 19, 0, 0, 0);
    }
}
