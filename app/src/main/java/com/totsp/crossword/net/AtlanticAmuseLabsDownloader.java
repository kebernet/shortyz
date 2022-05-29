package com.totsp.crossword.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;

public class AtlanticAmuseLabsDownloader extends AbstractAmuseLabsDownloader {

        protected static String al_atlantic_picker_url = "https://cdn3.amuselabs.com/atlantic/date-picker?set=atlantic";
        protected static String al_atlantic_base_url = "https://cdn3.amuselabs.com/atlantic/crossword?&set=atlantic";
        private static final String NAME = "The Atlantic";
        private final HashMap<String, String> headers = new HashMap<String, String>();
        NumberFormat nf = NumberFormat.getInstance();

        public AtlanticAmuseLabsDownloader() {
            super(al_atlantic_base_url, DOWNLOAD_DIR, NAME, al_atlantic_picker_url);
                nf.setMinimumIntegerDigits(2);
                nf.setMaximumFractionDigits(0);
                nf.setGroupingUsed(false);
        }

        public int[] getDownloadDates() {
                return DATE_DAILY;
        }

        public String getName() {
                return NAME;
        }

        public File download(Date date) {
                return download(date, this.createUrlSuffix(date), headers);
        }

        @Override
        protected String createUrlSuffix(Date date) {
            String val = "atlantic_";
            return val + nf.format((1900+date.getYear()))
                + nf.format(date.getMonth() + 1) + nf.format(date.getDate());
        }

}
