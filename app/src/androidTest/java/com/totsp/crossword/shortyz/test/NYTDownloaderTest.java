package com.totsp.crossword.shortyz.test;

import android.test.AndroidTestCase;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.totsp.crossword.net.NYTDownloader;

public class NYTDownloaderTest extends AndroidTestCase {

    private static Calendar makeCalendar(
            String tz, int year, int month, int day, int h, int m, int s) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(tz));
        calendar.set(year, month, day, h, m, s);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    // When checking if a puzzle if available for download, com.totsp.crossword.net.Downloaders
    // will use a Date set to midnight in the local time zone on the device.
    private static long comparisonTimeForPuz(String tz, int year, int month, int day) {
        return makeCalendar(tz, year, month, day, 0, 0, 0).getTime().getTime();
    }

    private class MockTimeNYTDownloader extends NYTDownloader {
        public MockTimeNYTDownloader() {
            super(getContext());
        }

        public void setTime(Date time) {
            now = time;
        }

        public void setTimeZone(TimeZone tz) {
            injectedCalendar = Calendar.getInstance(tz);
        }
    }

    MockTimeNYTDownloader downloader = new MockTimeNYTDownloader();

    public void testAvailableAtMidnightInNY() {
        Calendar estMidnight =
                makeCalendar("America/New_York", 2017, Calendar.JANUARY, 15, 0, 0, 0);

        downloader.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        downloader.setTime(estMidnight.getTime());

        assertTrue(comparisonTimeForPuz("America/New_York", 2017, Calendar.JANUARY, 15) <=
                downloader.getGoodThrough().getTime());
    }

    public void testNotAvailableEarlyInNY() {
        Calendar estEarly =
                makeCalendar("America/New_York", 2017, Calendar.JANUARY, 14, 23, 59, 59);

        downloader.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        downloader.setTime(estEarly.getTime());

        assertFalse(comparisonTimeForPuz("America/New_York", 2017, Calendar.JANUARY, 15) <=
                downloader.getGoodThrough().getTime());
    }

    public void testAvailableEarlyInCA() {
        Calendar caEarly =
                makeCalendar("America/Los_Angeles", 2017, Calendar.JANUARY, 14, 23, 59, 59);

        downloader.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        downloader.setTime(caEarly.getTime());

        assertTrue(comparisonTimeForPuz("America/Los_Angeles", 2017, Calendar.JANUARY, 15) <=
                downloader.getGoodThrough().getTime());
    }


    public void testNotAvailableBeforeNinePMInCA() {
        Calendar caUnavailable =
                makeCalendar("America/Los_Angeles", 2017, Calendar.JANUARY, 14, 20, 59, 59);

        downloader.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        downloader.setTime(caUnavailable.getTime());

        assertFalse(comparisonTimeForPuz("America/Los_Angeles", 2017, Calendar.JANUARY, 15) <=
                downloader.getGoodThrough().getTime());
    }

    // To prevent regressions for users ahead of Eastern time, we assume the puzzle is available
    // at midnight local time.
    public void testAvailableAtMidnightInSpain() {
        Calendar spainMidnight =
                makeCalendar("Europe/Madrid", 2017, Calendar.JANUARY, 15, 00, 00, 00);

        downloader.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
        downloader.setTime(spainMidnight.getTime());

        assertTrue(comparisonTimeForPuz("Europe/Madrid", 2017, Calendar.JANUARY, 15) <=
                downloader.getGoodThrough().getTime());
    }
}
