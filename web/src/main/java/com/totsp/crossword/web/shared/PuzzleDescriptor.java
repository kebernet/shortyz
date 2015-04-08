/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.totsp.crossword.web.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import com.totsp.gwittir.client.beans.AbstractModelBean;
import com.totsp.gwittir.client.beans.annotations.Introspectable;

import java.io.Serializable;

import java.util.Date;


/**
 *
 * @author kebernet
 */
@Introspectable
public class PuzzleDescriptor extends AbstractModelBean
    implements IsSerializable, Comparable, Serializable {
    private Date date;
    private Long id;
    private String source;
    private String title;
    private int percentComplete;

    /**
     * Set the value of date
     *
     * @param newdate new value of date
     */
    public void setDate(Date newdate) {
        this.changeSupport.firePropertyChange("date", this.date,
            this.date = newdate);
    }

    /**
     * Get the value of date
     *
     * @return the value of date
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * Set the value of id
     *
     * @param newid new value of id
     */
    public void setId(Long newid) {
        this.changeSupport.firePropertyChange("id", this.id, this.id = newid);
    }

    /**
     * Get the value of id
     *
     * @return the value of id
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Set the value of percentComplete
     *
     * @param newpercentComplete new value of percentComplete
     */
    public void setPercentComplete(int newpercentComplete) {
        this.percentComplete = newpercentComplete;
    }

    /**
     * Get the value of percentComplete
     *
     * @return the value of percentComplete
     */
    public int getPercentComplete() {
        return this.percentComplete;
    }

    /**
     * Set the value of source
     *
     * @param newsource new value of source
     */
    public void setSource(String newsource) {
        this.changeSupport.firePropertyChange("source", this.source,
            this.source = newsource);
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
        this.changeSupport.firePropertyChange("title", this.title,
            this.title = newtitle);
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
    public int compareTo(Object o) {
        PuzzleDescriptor d = (PuzzleDescriptor) o;

        return d.getDate().compareTo(this.getDate());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final PuzzleDescriptor other = (PuzzleDescriptor) obj;

        if ((this.id != other.id) &&
                ((this.id == null) || !this.id.equals(other.id))) {
            return false;
        }

        if ((this.title == null) ? (other.title != null)
                                     : (!this.title.equals(other.title))) {
            return false;
        }

        if ((this.source == null) ? (other.source != null)
                                      : (!this.source.equals(other.source))) {
            return false;
        }

        if ((this.date != other.date) &&
                ((this.date == null) || !this.date.equals(other.date))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (id == null) ? 0 : id.hashCode();
    }
}
