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
@NamedQueries({@NamedQuery(name = "PuzzleListing.findByDateAndSource", query = "SELECT pl FROM com.totsp.crossword.web.server.model.PuzzleListing pl " +
    "WHERE pl.pubDate = :pubDate AND pl.source = :source")
    , @NamedQuery(name = "PuzzleListing.findAfterDate", query = "SELECT pl FROM com.totsp.crossword.web.server.model.PuzzleListing pl " +
    "WHERE pl.pubDate > :startDate")
})
public class PuzzleListing implements Serializable {
    private static final long serialVersionUID = 1L;
    private Blob boxesSerial;
    private Blob metaSerial;
    @Temporal(TemporalType.DATE)
    private Date pubDate;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String source;
    private String title;

    /**
     * Set the value of date
     *
     * @param newdate new value of date
     */
    public void setDate(Date newdate) {
        this.pubDate = newdate;
    }

    /**
     * Get the value of date
     *
     * @return the value of date
     */
    public Date getDate() {
        return this.pubDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
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
     * Set the value of boxesSerial
     *
     * @param newboxesSerial new value of boxesSerial
     */
    public void setPuzzleSerial(Blob newboxesSerial) {
        this.boxesSerial = newboxesSerial;
    }

    /**
     * Get the value of boxesSerial
     *
     * @return the value of boxesSerial
     */
    public Blob getPuzzleSerial() {
        return this.boxesSerial;
    }

    /**
     * Set the value of source
     *
     * @param newsource new value of source
     */
    public void setSource(String newsource) {
        this.source = newsource;
    }

    /**
     * Get the value of source
     *
     * @return the value of source
     */
    public String getSource() {
        return this.source;
    }

    /**
     * Set the value of title
     *
     * @param newtitle new value of title
     */
    public void setTitle(String newtitle) {
        this.title = newtitle;
    }

    /**
     * Get the value of title
     *
     * @return the value of title
     */
    public String getTitle() {
        return this.title;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PuzzleListing)) {
            return false;
        }

        PuzzleListing other = (PuzzleListing) object;

        if (((this.id == null) && (other.id != null)) ||
                ((this.id != null) && !this.id.equals(other.id))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += ((id != null) ? id.hashCode() : 0);

        return hash;
    }

    @Override
    public String toString() {
        return "com.totsp.crossword.web.server.model.PuzzleListing[id=" + id +
        "]";
    }
}
