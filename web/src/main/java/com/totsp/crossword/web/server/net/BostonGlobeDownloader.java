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
public class BostonGlobeDownloader extends AbstractDownloader{

    private static final String NAME = "The Boston Globe";
    private static final String BASE_URL = "http://standalone.com/dl/puz/boston/";
    private NumberFormat nf = NumberFormat.getInstance();
    public BostonGlobeDownloader(){
        super(BASE_URL);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);

    }

    @Override
    protected String getFileName(Date date) {
        if (date.getDay() != 0) {
            return null;
        }
        String name = "boston_" + nf.format(date.getMonth() + 1) +
            nf.format(date.getDate()) + (date.getYear() + 1900) + ".puz";
        return name;
    }

    @Override
    public String getName() {
        return NAME;
    }

}
