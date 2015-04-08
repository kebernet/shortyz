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
public class PhillyDownloader extends AbstractDownloader {
    private static final String NAME = "Philadelphia Inquirer";
    private static final String BASE_URL = "http://mazerlm.home.comcast.net/~mazerlm/";
    private NumberFormat nf = NumberFormat.getInstance();

    public PhillyDownloader(){
        super(BASE_URL);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);

    }

    @Override
    protected String getFileName(Date date) {
        if (date.getDay() != 0) {
            return null;
        }
        String name = "pi" + nf.format(date.getYear() - 100) +
            nf.format(date.getMonth() + 1) + nf.format(date.getDate()) +
            ".puz";

        return name;
    }

    @Override
    public String getName() {
        return NAME;
    }

}
