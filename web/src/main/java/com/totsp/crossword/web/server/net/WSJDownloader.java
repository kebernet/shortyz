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
public class WSJDownloader extends AbstractDownloader {
    private static final String NAME = "The Wall Street Journal";
    private static final String BASE_URL = "http://mazerlm.home.comcast.net/~mazerlm/";
    private NumberFormat nf = NumberFormat.getInstance();

    public WSJDownloader(){
        super(BASE_URL);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);

    }

    @Override
    protected String getFileName(Date date) {
        String name = "wsj" + nf.format(date.getYear() - 100) +
            nf.format(date.getMonth() + 1) + nf.format(date.getDate()) +
            ".puz";

        return name;
    }

    @Override
    public String getName() {
        return NAME;
    }

}
