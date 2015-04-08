/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.totsp.crossword.web.facebook;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.totsp.crossword.web.client.Game;
import com.totsp.crossword.web.client.WASDCodes;




/**
 *
 * @author kebernet
 */
public class BasicEntryPoint implements EntryPoint {
    static final WASDCodes CODES = GWT.create(WASDCodes.class);
    

    @Override
    public void onModuleLoad() {
        Element e = DOM.getElementById("loadingIndicator");

        if (e != null) {
            e.removeFromParent();
        }


        Game g = Injector.INSTANCE.game();
        g.loadList();
        
    }

    


   
}
