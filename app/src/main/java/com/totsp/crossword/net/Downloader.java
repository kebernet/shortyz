package com.totsp.crossword.net;

import java.io.File;
import java.util.Date;

import android.content.Context;


public interface Downloader {
    // These lists must be sorted for binary search.
    int[] DATE_SUNDAY = new int[] { 0 };
    int[] DATE_MONDAY = new int[] { 1 };
    int[] DATE_TUESDAY = new int[] { 2 };
    int[] DATE_WEDNESDAY = new int[] { 3 };
    int[] DATE_THURSDAY = new int[] { 4 };
    int[] DATE_FRIDAY = new int[] { 5 };
    int[] DATE_SATURDAY = new int[] { 6 };
    int[] DATE_DAILY = new int[] { 0, 1, 2, 3, 4, 5, 6 };
    int[] DATE_NO_SUNDAY = new int[] { 1, 2, 3, 4, 5, 6 };
    File DEFERRED_FILE = new File(".");

    void setContext(Context context);

    int[] getDownloadDates();

    String getName();

    String createFileName(Date date);

    File download(Date date);

    String sourceUrl(Date date);

    boolean alwaysRun();

    // Returns a Date representing 12AM or later in LOCAL TIME for the most recent publication
    // available.
    Date getGoodThrough();

    Date getGoodFrom();
}
