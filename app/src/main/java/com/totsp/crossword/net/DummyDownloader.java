package com.totsp.crossword.net;

import java.io.File;
import java.util.Date;

import android.content.Context;


/**
 * Does not actually download any puzzles; just adds an "All Available" option to the dropdown.
 */
public class DummyDownloader implements Downloader {
    public void setContext(Context context) {
        // TODO Auto-generated method stub
    }

    public int[] getDownloadDates() {
        return null;
    }

    public String getName() {
        return null;
    }

    public String createFileName(Date date) {
        return null;
    }

    public File download(Date date) {
        return null;
    }

    public String sourceUrl(Date date) {
        return null;
    }

    public String toString() {
        return "All available";
    }
}
