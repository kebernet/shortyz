/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.totsp.crossword.web.server;

import com.google.appengine.api.datastore.Blob;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.web.server.model.PuzzleListing;
import com.totsp.crossword.web.server.model.SavedPuzzle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;


/**
 *
 * @author kebernet
 */
public class DataService {
    private static final EntityManagerFactory emfInstance = Persistence.createEntityManagerFactory(
            "transactions-optional");
    private EntityManager entityManager = emfInstance.createEntityManager();

    public DataService() {
    }

    public void close() {
        this.entityManager.close();
    }

    public List<PuzzleListing> findAfterDateNoPuzzle(Date date) {
        EntityTransaction t = entityManager.getTransaction();
        t.begin();

        Query q = entityManager.createNamedQuery("PuzzleListing.findAfterDate");
        q.setParameter("startDate", date);

        try {
            List<PuzzleListing> result = q.getResultList();
            t.commit();

            return result;
        } catch (NoResultException nre) {
            nre.printStackTrace();
            t.rollback();

            return new ArrayList<PuzzleListing>();
        }
    }

    public Object findById(Class clazz, Long id) {
        EntityTransaction t = this.entityManager.getTransaction();
        t.begin();

        try {
            Object result = this.entityManager.getReference(clazz, id);
            t.commit();

            return result;
        } catch (NoResultException nre) {
            t.rollback();

            return null;
        }
    }

    public PuzzleListing findPuzzleListingBySourceAndDate(String source, Date date) {
        EntityTransaction t = entityManager.getTransaction();
        t.begin();

        Query q = entityManager.createNamedQuery("PuzzleListing.findByDateAndSource");
        q.setParameter("source", source);
        q.setParameter("pubDate", date);

        try {
            PuzzleListing result = (PuzzleListing) q.getSingleResult();
            t.commit();

            return result;
        } catch (NoResultException nre) {
            System.out.println("No result for " + source + " " + date);
            t.rollback();

            return null;
        }
    }

    public Map<Long, Puzzle> findSavedPuzzlesByUserAndListingIds(String userUri, List<Long> listingIds) {
        HashMap<Long, Puzzle> results = new HashMap<Long, Puzzle>();
        EntityTransaction t = entityManager.getTransaction();
        t.begin();

        try {
            Query q = entityManager.createNamedQuery("SavedPuzzle.findUserUriAndListingIds");
            q.setParameter("userUri", userUri);
            q.setParameter("listinIds", listingIds);

            List<SavedPuzzle> saves = q.getResultList();

            for (SavedPuzzle save : saves) {
                try {
                    Puzzle p = IO.load(new DataInputStream(new ByteArrayInputStream(save.getPuzzleSerial().getBytes())),
                            new DataInputStream(new ByteArrayInputStream(save.getMetaSerial().getBytes())));

                    results.put(save.getId(), p);
                } catch (IOException ex) {
                    Logger.getLogger(DataService.class.getName())
                          .log(Level.SEVERE, null, ex);
                }
            }
        } catch (NoResultException nre) {
            nre.printStackTrace();
            t.rollback();
        }

        return results;
    }

    public Puzzle loadSavedPuzzle(String userUri, Long listingId) {
        EntityTransaction t = entityManager.getTransaction();
        t.begin();

        try {
            Query q = entityManager.createNamedQuery("SavedPuzzle.findUserUriAndListingId");
            q.setParameter("userUri", userUri);
            q.setParameter("listingId", listingId);

            SavedPuzzle sp = (SavedPuzzle) q.getSingleResult();
            Puzzle p = IO.load(new DataInputStream(new ByteArrayInputStream(sp.getPuzzleSerial().getBytes())),
                    new DataInputStream(new ByteArrayInputStream(sp.getMetaSerial().getBytes())));
            t.commit();

            return p;
        } catch (IOException ex) {
            Logger.getLogger(DataService.class.getName())
                  .log(Level.SEVERE, null, ex);
            t.rollback();

            return null;
        } catch (NoResultException nre) {
            t.rollback();

            return null;
        }
    }

    public void savePuzzle(String userUri, Long listingId, Puzzle puzzle) {
        EntityTransaction t = entityManager.getTransaction();
        t.begin();

        try {
            Query q = entityManager.createNamedQuery("SavedPuzzle.findUserUriAndListingId");
            q.setParameter("userUri", userUri);
            q.setParameter("listingId", listingId);

            SavedPuzzle sp = (SavedPuzzle) q.getSingleResult();

            try {
                //sp.setPuzzleSerial(new Blob(this.serialize(puzzle)));
                ByteArrayOutputStream puz = new ByteArrayOutputStream();
                ByteArrayOutputStream meta = new ByteArrayOutputStream();
                IO.save(puzzle, new DataOutputStream(puz), new DataOutputStream(meta));
                sp.setPuzzleSerial(new Blob(puz.toByteArray()));
                sp.setMetaSerial(new Blob(meta.toByteArray()));
            } catch (IOException ioe) {
                t.rollback();
                throw new RuntimeException(ioe);
            }

            entityManager.merge(sp);
            t.commit();
            System.out.println("Savd puzzle UserId:" + userUri + " PuzzleId:" + listingId);
        } catch (NoResultException nre) {
            SavedPuzzle sp = new SavedPuzzle();
            sp.setPuzzleDate(puzzle.getDate());

            try {
                ByteArrayOutputStream puz = new ByteArrayOutputStream();
                ByteArrayOutputStream meta = new ByteArrayOutputStream();
                IO.save(puzzle, new DataOutputStream(puz), new DataOutputStream(meta));
                sp.setPuzzleSerial(new Blob(puz.toByteArray()));
                sp.setMetaSerial(new Blob(meta.toByteArray()));
            } catch (IOException ioe) {
                t.rollback();
                throw new RuntimeException(ioe);
            }

            sp.setListingID(listingId);
            sp.setUserUri(userUri);
            entityManager.persist(sp);
            System.out.println("Created save for UserId:" + userUri + " PuzzleId:" + listingId);
            t.commit();
        }
    }

    public PuzzleListing store(PuzzleListing listing) {
        EntityTransaction t = entityManager.getTransaction();
        t.begin();
        entityManager.persist(listing);
        t.commit();

        return listing;
    }

    PuzzleListing findListingById(Class<PuzzleListing> aClass, Long puzzleId) {
        PuzzleListing result = (PuzzleListing) this.findById(PuzzleListing.class, puzzleId);

        return result;
    }
}
