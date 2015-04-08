/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.totsp.crossword.web.server.model;

import com.google.appengine.api.datastore.Blob;

import java.io.Serializable;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 *
 * @author kebernet
 */
@Entity
@NamedQueries({@NamedQuery(name = "SavedPuzzle.findUserUriAndListingIds", query = "SELECT sp FROM com.totsp.crossword.web.server.model.SavedPuzzle sp " +
    "WHERE sp.userUri = :userUri and sp.listingId IN (:listingIds) ORDER BY sp.listingId")
    , @NamedQuery(name = "SavedPuzzle.findUserUriAndListingId", query = "SELECT sp FROM com.totsp.crossword.web.server.model.SavedPuzzle sp " +
    "WHERE sp.userUri = :userUri and sp.listingId = :listingId")
    , @NamedQuery(name = "SavedPuzzle.findUserUriAndListingIdAfterDate", query = "SELECT sp FROM com.totsp.crossword.web.server.model.SavedPuzzle sp " +
    "WHERE sp.userUri = :userUri and sp.listingId IN (:listingIds) AND  ORDER BY sp.listingId")
})
public class SavedPuzzle implements Serializable {
    private Blob metaSerial;
    private Blob puzzleSerial;
    @Temporal(TemporalType.DATE)
    private Date puzzleDate;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long listingId;
    private String userUri;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the value of listingId
     *
     * @param newlistingId new value of listingId
     */
    public void setListingID(Long newlistingId) {
        this.listingId = newlistingId;
    }

    /**
     * Get the value of listingId
     *
     * @return the value of listingId
     */
    public Long getListingId() {
        return this.listingId;
    }

    /**
     * Set the value of metaSerial
     *
     * @param newmetaSerial new value of metaSerial
     */
    public void setMetaSerial(Blob newmetaSerial) {
        this.metaSerial = newmetaSerial;
    }

    /**
     * Get the value of metaSerial
     *
     * @return the value of metaSerial
     */
    public Blob getMetaSerial() {
        return this.metaSerial;
    }

    /**
     * @param puzzleDate the puzzleDate to set
     */
    public void setPuzzleDate(Date puzzleDate) {
        this.puzzleDate = puzzleDate;
    }

    /**
     * @return the puzzleDate
     */
    public Date getPuzzleDate() {
        return puzzleDate;
    }

    /**
     * Set the value of puzzleSerial
     *
     * @param newpuzzleSerial new value of puzzleSerial
     */
    public void setPuzzleSerial(Blob newpuzzleSerial) {
        this.puzzleSerial = newpuzzleSerial;
    }

    /**
     * Get the value of puzzleSerial
     *
     * @return the value of puzzleSerial
     */
    public Blob getPuzzleSerial() {
        return this.puzzleSerial;
    }

    /**
     * Set the value of userURI
     *
     * @param newuserURI new value of userURI
     */
    public void setUserUri(String newuserURI) {
        this.userUri = newuserURI;
    }

    /**
     * Get the value of userURI
     *
     * @return the value of userURI
     */
    public String getUserUri() {
        return this.userUri;
    }
}
