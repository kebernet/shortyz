/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.server.net;

import java.text.NumberFormat;
import java.util.Date;

/**
 *
 * @author kebernet
 */
public class InkwellDownloader extends AbstractDownloader {
    private static final String NAME = "InkwellXWords.com";
    private static final String BASE_URL = "http://herbach.dnsalias.com/Tausig/";
    private NumberFormat nf = NumberFormat.getInstance();
    
    public InkwellDownloader(){
        super(BASE_URL);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);

    }

    @Override
    protected String getFileName(Date date) {
        if (date.getDay() != 5) {
            return null;
        }
        String name = "vv" + (date.getYear() - 100) +
            nf.format(date.getMonth() + 1) + nf.format(date.getDate()) +
            ".puz";

        return name;
    }

    @Override
    public String getName() {
        return NAME;
    }

}
