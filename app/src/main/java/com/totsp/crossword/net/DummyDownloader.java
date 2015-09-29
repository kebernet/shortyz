package com.totsp.crossword.net;

import java.io.File;
import java.util.Date;

import android.content.Context;


/**
 * Does not actually download any puzzles; just adds an "All Available" option to the dropdown.
 */
public class DummyDownloader implements Downloader {
    @Override
    public void setContext(Context context) {}

    @Override
    public int[] getDownloadDates() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String createFileName(Date date) {
        return null;
    }

    @Override
    public File download(Date date) {
        return null;
    }

    @Override
    public String sourceUrl(Date date) {
        return null;
    }

    @Override
    public String toString() {
        return "All available";
    }

    @Override
    public boolean alwaysRun(){
        return false;
    }

    @Override
    public Date getGoodThrough() {
        return new Date();
    }

    @Override
    public Date getGoodFrom() {
        return new Date(0L);
    }
}
