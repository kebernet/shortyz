/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.server.net;

import com.totsp.crossword.puz.Puzzle;
import java.util.Date;

/**
 *
 * @author kebernet
 */
public interface Downloader {


    Puzzle download(Date date);
    String getName();

}
