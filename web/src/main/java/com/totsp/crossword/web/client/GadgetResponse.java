/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 *
 * @author kebernet
 */
public class GadgetResponse  extends JavaScriptObject {
      protected GadgetResponse() {
      }

      public final native String getText() /*-{ return this.text; }-*/;
      public final native String[] getErrors()  /*-{ return this.errors;  }-*/;
      public final native String getData() /*-{ return this.data; }-*/;
}
