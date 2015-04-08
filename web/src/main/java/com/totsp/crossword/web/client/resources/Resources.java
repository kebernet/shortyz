/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

/**
 *
 * @author kebernet
 */
public interface Resources extends ClientBundle  {


    @Source("css.css")
    Css css();

    @Source("wave-about.txt")
    TextResource waveAbout();

}
