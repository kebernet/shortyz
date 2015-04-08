/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.totsp.crossword.web.wave;

import com.totsp.gwittir.client.beans.annotations.Introspectable;


/**
 *
 * @author kebernet
 */
@Introspectable
public class Play {
    private String responder;
    private boolean cheated;
    private char response;
    private int across;
    private int down;
    private long time;

    /**
     * Set the value of across
     *
     * @param newacross new value of across
     */
    public void setAcross(int newacross) {
        this.across = newacross;
    }

    /**
     * Get the value of across
     *
     * @return the value of across
     */
    public int getAcross() {
        return this.across;
    }

    /**
     * Set the value of cheated
     *
     * @param newcheated new value of cheated
     */
    public void setCheated(boolean newcheated) {
        this.cheated = newcheated;
    }

    /**
     * Get the value of cheated
     *
     * @return the value of cheated
     */
    public boolean isCheated() {
        return this.cheated;
    }

    /**
     * Set the value of down
     *
     * @param newdown new value of down
     */
    public void setDown(int newdown) {
        this.down = newdown;
    }

    /**
     * Get the value of down
     *
     * @return the value of down
     */
    public int getDown() {
        return this.down;
    }

    /**
     * Set the value of responder
     *
     * @param newresponder new value of responder
     */
    public void setResponder(String newresponder) {
        this.responder = newresponder;
    }

    /**
     * Get the value of responder
     *
     * @return the value of responder
     */
    public String getResponder() {
        return this.responder;
    }

    /**
     * Set the value of response
     *
     * @param newresponse new value of response
     */
    public void setResponse(char newresponse) {
        this.response = newresponse;
    }

    /**
     * Get the value of response
     *
     * @return the value of response
     */
    public char getResponse() {
        return this.response;
    }

    /**
     * Set the value of time
     *
     * @param newtime new value of time
     */
    public void setTime(long newtime) {
        this.time = newtime;
    }

    /**
     * Get the value of time
     *
     * @return the value of time
     */
    public long getTime() {
        return this.time;
    }
}
