/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.wave;

import com.totsp.gwittir.client.beans.annotations.Introspectable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kebernet
 */
@Introspectable
public class PlayContainer {


    private List<Play> plays = new ArrayList<Play>();

    /**
     * Get the value of plays
     *
     * @return the value of plays
     */
    public List<Play> getPlays() {
        return this.plays;
    }

    /**
     * Set the value of plays
     *
     * @param newplays new value of plays
     */
    public void setPlays(List<Play> newplays) {
        this.plays = newplays;
    }


}
