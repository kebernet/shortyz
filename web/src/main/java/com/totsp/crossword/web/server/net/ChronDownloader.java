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
public class ChronDownloader extends AbstractDownloader {

    private static final String NAME = "The Houston Chronicle";
    private static final String BASE_URL = "http://www.chron.com/apps/games/xword/puzzles/";
    private NumberFormat nf = NumberFormat.getInstance();
    public ChronDownloader(){
        super(BASE_URL);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);

    }

    @Override
    protected String getFileName(Date date) {
        String name = "cs" + (date.getYear() - 100) +
            nf.format(date.getMonth() + 1) + nf.format(date.getDate()) +
            ".puz";
        return name;
    }

    @Override
    public String getName() {
        return NAME;
    }



}
