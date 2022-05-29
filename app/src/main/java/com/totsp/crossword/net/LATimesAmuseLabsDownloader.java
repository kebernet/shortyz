package com.totsp.crossword.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;

public class LATimesAmuseLabsDownloader extends AbstractAmuseLabsDownloader {

        protected static String al_lat_picker_url = "https://cdn4.amuselabs.com/lat/date-picker?set=latimes";
        protected static String al_lat_base_url = "https://cdn4.amuselabs.com/lat/crossword?&set=latimes";
        private static final String NAME = "Los Angeles Times";
        private final HashMap<String, String> headers = new HashMap<String, String>();
        NumberFormat nf = NumberFormat.getInstance();

        public LATimesAmuseLabsDownloader() {
            super(al_lat_base_url, DOWNLOAD_DIR, NAME, al_lat_picker_url);
                nf.setMinimumIntegerDigits(2);
                nf.setMaximumFractionDigits(0);
        }

        public int[] getDownloadDates() {
                return LATimesDownloader.DATE_DAILY;

        }

        public String getName() {
                return NAME;
        }

        public File download(Date date) {
                return download(date, this.createUrlSuffix(date), headers);
        }

        @Override
        protected String createUrlSuffix(Date date) {
            String val = "tca";
            return val + nf.format((date.getYear() - 100))
                + nf.format(date.getMonth() + 1) + nf.format(date.getDate());
        }

}
