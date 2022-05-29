package com.totsp.crossword.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;

public class WaPoAmuseLabsDownloader extends AbstractAmuseLabsDownloader {

        protected static String al_wapo_picker_url = "https://cdn1.amuselabs.com/wapo/wp-picker?set=wapo-eb";
        protected static String al_wapo_base_url = "https://cdn1.amuselabs.com/wapo/crossword?set=wapo-eb";

        private static final String NAME = "Washington Post";
        private final HashMap<String, String> headers = new HashMap<String, String>();
        NumberFormat nf = NumberFormat.getInstance();
                
        public WaPoAmuseLabsDownloader() {
            super(al_wapo_base_url, DOWNLOAD_DIR, NAME, al_wapo_picker_url);
                nf.setMinimumIntegerDigits(2);
                nf.setMaximumFractionDigits(0);
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
            String val = "ebirnholz_";
            return val + nf.format((date.getYear() - 100))
                + nf.format(date.getMonth() + 1) + nf.format(date.getDate());
        }

}
