/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.totsp.crossword.web.server;

import com.google.gwt.user.server.rpc.RPCServletUtils;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.web.server.model.PuzzleListing;
import com.totsp.crossword.web.shared.NoUserException;
import com.totsp.crossword.web.shared.PuzzleDescriptor;
import com.totsp.crossword.web.shared.PuzzleService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


/**
 *
 * @author kebernet
 */
public class PuzzleServlet extends RemoteServiceServlet implements PuzzleService {
    public static final Cache CACHE;

    static {
        try {
            CacheFactory cacheFactory = CacheManager.getInstance()
                                                    .getCacheFactory();
            CACHE = cacheFactory.createCache(Collections.emptyMap());
        } catch (CacheException e) {
            throw new Error(e);
        }
    }

    @Override
    public Puzzle findPuzzle(Long puzzleId) {
        DataService service = new DataService();

        try {
            String userUri = (String) this.getThreadLocalRequest().getSession()
                                          .getAttribute("user.id");

            if (userUri != null) {
                System.out.println("User: " + userUri + " Puzzle:" + puzzleId);

                Puzzle p = service.loadSavedPuzzle(userUri, puzzleId);

                if (p != null) {
                    System.out.println("Found saved.");

                    return p;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            String key = "puzzle:" + puzzleId;

            Puzzle p = (Puzzle) CACHE.get(key);

            if (p != null) {
                return p;
            }

            PuzzleListing l = service.findListingById(PuzzleListing.class,
                    puzzleId);

            p = IO.load(new DataInputStream(new ByteArrayInputStream(l.getPuzzleSerial().getBytes())),
                    new DataInputStream(new ByteArrayInputStream(l.getMetaSerial().getBytes())));
            System.out.println("Returning " + p);
            //CACHE.put(key, p);

            return p;
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(PuzzleServlet.class.getName())
                  .log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } finally {
            service.close();
        }
    }

    @Override
    public PuzzleDescriptor[] listPuzzles() {
        PuzzleDescriptor[] result = (PuzzleDescriptor[]) CACHE.get(
                "puzzle-list");

        if (result != null) {
            return result;
        }

        result = new PuzzleDescriptor[0];

        DataService service = new DataService();

        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -10);

            List<PuzzleListing> listings = service.findAfterDateNoPuzzle(cal.getTime());
            int i = 0;
            result = new PuzzleDescriptor[listings.size()];

            for (PuzzleListing listing : listings) {
                PuzzleDescriptor desc = new PuzzleDescriptor();
                desc.setId(listing.getId());
                desc.setSource(listing.getSource());
                desc.setTitle(listing.getTitle());
                desc.setDate(listing.getDate());
                System.out.println("\t" + desc.getTitle() + " " +
                    desc.getDate());
                result[i++] = desc;
            }
        } catch (Exception ex) {
            Logger.getLogger(PuzzleServlet.class.getName())
                  .log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } finally {
            service.close();
        }

        CACHE.put("puzzle-list", result);
        System.out.println("Returing " + result.length);

        return result;
    }

    @Override
    public void savePuzzle(Long listingId, Puzzle puzzle)
        throws NoUserException {
        DataService data = new DataService();
        String userUri = (String) this.getThreadLocalRequest().getSession()
                                      .getAttribute("user.id");

        if (userUri == null) {
            throw new NoUserException();
        }

        data.savePuzzle(userUri, listingId, puzzle);
        System.out.println("Save done.");
    }

    @Override
    protected String readContent(HttpServletRequest request)
        throws ServletException, IOException {
        return RPCServletUtils.readContentAsUtf8(new FakeRequest(request), false);
    }

    private static class FakeRequest extends HttpServletRequestWrapper {
        byte[] bytes;

        public FakeRequest(HttpServletRequest request)
            throws IOException {
            super(request);

            InputStream is = request.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            copyStream(is, baos);
            System.out.println(new String(baos.toByteArray()));
            this.bytes = baos.toByteArray();
        }

        @Override
        public int getContentLength() {
            return bytes.length;
        }

        @Override
        public ServletInputStream getInputStream() {
            final InputStream is = new ByteArrayInputStream(bytes);

            return new ServletInputStream() {
                    @Override
                    public int read() throws IOException {
                        return is.read();
                    }
                };
        }

        public static int copyStream(InputStream sourceStream,
            OutputStream destinationStream) throws IOException {
            int bytesRead = 0;
            int totalBytes = 0;
            byte[] buffer = new byte[1024];

            while (bytesRead >= 0) {
                bytesRead = sourceStream.read(buffer, 0, buffer.length);

                if (bytesRead > 0) {
                    destinationStream.write(buffer, 0, bytesRead);
                }

                totalBytes += bytesRead;
            }

            destinationStream.flush();
            destinationStream.close();

            return totalBytes;
        }
    }
}
