package com.totsp.crossword.net;

import java.io.File;
import java.util.Date;

import android.content.Context;


public interface Downloader {
    // These lists must be sorted for binary search.
    public static final int[] DATE_SUNDAY = new int[] { 0 };
    public static final int[] DATE_MONDAY = new int[] { 1 };
    public static final int[] DATE_TUESDAY = new int[] { 2 };
    public static final int[] DATE_WEDNESDAY = new int[] { 3 };
    public static final int[] DATE_THURSDAY = new int[] { 4 };
    public static final int[] DATE_FRIDAY = new int[] { 5 };
    public static final int[] DATE_SATURDAY = new int[] { 6 };
    public static final int[] DATE_DAILY = new int[] { 0, 1, 2, 3, 4, 5, 6 };
    public static final int[] DATE_NO_SUNDAY = new int[] { 1, 2, 3, 4, 5, 6 };
    public static final File DEFERRED_FILE = new File(".");

    public void setContext(Context context);

    public int[] getDownloadDates();

    public String getName();

    public String createFileName(Date date);

    public File download(Date date);

    public String sourceUrl(Date date);
}
