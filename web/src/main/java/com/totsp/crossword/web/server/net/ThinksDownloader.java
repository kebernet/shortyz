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
public class ThinksDownloader extends AbstractDownloader {
    private static final String NAME = "Thinks.com";
    private static final String BASE_URL = "http://thinks.com/daily-crossword/puzzles/";
    private NumberFormat nf = NumberFormat.getInstance();

    public ThinksDownloader(){
        super(BASE_URL);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);

    }

    @Override
    protected String getFileName(Date date) {
        String name =(date.getYear() + 1900) + "-" +
            nf.format(date.getMonth() + 1) + "/" + "dc1-" +
            (date.getYear() + 1900) + "-" + nf.format(date.getMonth() + 1) +
            "-" + nf.format(date.getDate()) + ".puz";

        return name;
    }

    @Override
    public String getName() {
        return NAME;
    }

}
