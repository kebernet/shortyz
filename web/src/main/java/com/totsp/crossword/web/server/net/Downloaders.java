/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.server.net;

import com.totsp.crossword.puz.Puzzle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 * @author kebernet
 */
public class Downloaders {

    private static List<Downloader> DOWNLOADERS = Arrays.asList(new Downloader[]{
        new AVClubDownloader(),
        //new ChronDownloader(),
        new InkwellDownloader(),
        new LATimesDownloader(),
        new PhillyDownloader(),
        new ThinksDownloader(),
        new WSJDownloader(),
        new BostonGlobeDownloader()
    });

    public List<Puzzle> getPuzzles(Date date){
        ArrayList<Puzzle> result = new ArrayList<Puzzle>();
        for(Downloader d: DOWNLOADERS){
            Puzzle p = d.download(date);
            if(p != null){
                result.add(p);
            }
        }
        return result;
    }

}
