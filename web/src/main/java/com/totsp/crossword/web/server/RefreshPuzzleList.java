/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.totsp.crossword.web.server;

import com.google.appengine.api.datastore.Blob;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.web.server.model.PuzzleListing;
import com.totsp.crossword.web.server.net.Downloaders;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 *
 * @author kebernet
 */
public class RefreshPuzzleList extends HttpServlet {

    

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.add(Calendar.DATE, -10);

        Downloaders downloaders = new Downloaders();
        DataService data = new DataService();

        for (int i = 0; i < 10; i++) {
            List<Puzzle> puzzles = downloaders.getPuzzles(cal.getTime());
            System.out.println("Got " + puzzles.size() + " puzzles for " +
                cal.getTime());

            for (Puzzle puz : puzzles) {
                PuzzleListing listing = data.findPuzzleListingBySourceAndDate(puz.getSource(),
                        cal.getTime());

                if (listing != null) {
                    System.out.println("Puzzle from " + puz.getSource() +
                        " already in database.");
                } else {
                    System.out.println("Persisting from " + puz.getSource() +
                        ".");
                    listing = new PuzzleListing();
                    listing.setDate(cal.getTime());
                    listing.setSource(puz.getSource());
                    listing.setTitle(puz.getTitle());

                    ByteArrayOutputStream puzData = new ByteArrayOutputStream();
                    ByteArrayOutputStream meta = new ByteArrayOutputStream();
                    IO.save(puz, new DataOutputStream(puzData), new DataOutputStream(meta));
                    listing.setPuzzleSerial(new Blob(puzData.toByteArray()));
                    listing.setMetaSerial(new Blob(meta.toByteArray()));
                    data.store(listing);
                }
            }

            cal.add(Calendar.DATE, 1);
        }

        data.close();
        PuzzleServlet.CACHE.put("puzzle-list", null);
    }
}
