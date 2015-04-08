/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.server.net;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Puzzle;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kebernet
 */
public abstract class AbstractDownloader implements Downloader {

    private String baseUrl;

    protected AbstractDownloader(String baseUrl){
        this.baseUrl = baseUrl;
    }


    @Override
    public Puzzle download(Date d){
        InputStream is = this.connect(d);
        if(is == null){
            return null;
        } else {
            try {
                Puzzle p = IO.loadNative(new DataInputStream(is));
                p.setSource(this.getName());
                p.setDate(d);
                return p;
            } catch (IOException ex) {
                Logger.getLogger(AbstractDownloader.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
    }

    protected InputStream connect(Date d){
        try{
            String fileName = this.getFileName(d);
            if(fileName == null){
                return null;
            }
            URL u = new URL(baseUrl + fileName);
            return u.openStream();
        } catch(IOException ioe){
            ioe.printStackTrace();
        }
        return null;
    }

    protected abstract String getFileName(Date date);

}
