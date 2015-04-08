/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.server.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Date;

/**
 *
 * @author kebernet
 */
public class LATimesDownloader extends AbstractDownloader {
    private static final String NAME = "Los Angeles Times";
    private static final String BASE_URL = "http://www.cruciverb.com/puzzles/lat/";
    private NumberFormat nf = NumberFormat.getInstance();

    public LATimesDownloader(){
        super(BASE_URL);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);

    }

    @Override
    protected String getFileName(Date date) {
        String name = "lat" + this.nf.format(date.getYear() - 100) +
            this.nf.format(date.getMonth() + 1) +
            this.nf.format(date.getDate()) + ".puz";

        return name;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected InputStream connect(Date d){
        try{
            String fileName = this.getFileName(d);
            if(fileName == null){
                return null;
            }
            URL url = new URL(BASE_URL + fileName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Referer", BASE_URL);
            return connection.getInputStream();
        } catch(IOException ioe){
            ioe.printStackTrace();
        }
        return null;

    }

}