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
public class AVClubDownloader extends AbstractDownloader {

    public static final String NAME = "The Onion AV Club";
    public static final String BASE_URL = "http://herbach.dnsalias.com/Tausig/";
    NumberFormat nf = NumberFormat.getInstance();

    public AVClubDownloader() {
        super(BASE_URL);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected String getFileName(Date date) {
        if (date.getDay() != 3) {
            return null;
        }
        return "av" + this.nf.format(date.getYear() - 100)
                + this.nf.format(date.getMonth() + 1) + this.nf.format(date.getDate())
                + ".puz";
    }
}
