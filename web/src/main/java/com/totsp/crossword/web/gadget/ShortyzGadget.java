/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.totsp.crossword.web.gadget;

import com.totsp.crossword.web.client.GadgetResponse;
import com.google.gwt.gadgets.client.DynamicHeightFeature;
import com.google.gwt.gadgets.client.Gadget;
import com.google.gwt.gadgets.client.Gadget.ModulePrefs;
import com.google.gwt.gadgets.client.IntrinsicFeature;
import com.google.gwt.gadgets.client.NeedsDynamicHeight;
import com.google.gwt.gadgets.client.NeedsIntrinsics;
import com.google.gwt.gadgets.client.UserPreferences;
import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

import com.totsp.crossword.web.client.Game;
import com.totsp.crossword.web.client.Game.DisplayChangeListener;


/**
 *
 * @author kebernet
 */
@ModulePrefs(title = "Shortyz", author = "Robert Cooper", author_quote = "If you only have two ducks, they are always in a row.", author_email = "kebernet@gmail.com", width = 450, height = 500, scrolling = true)
public class ShortyzGadget extends Gadget<UserPreferences>
    implements NeedsIntrinsics, NeedsDynamicHeight {
    UserPreferences prefs;
    private DynamicHeightFeature height;

    @Override
    public void initializeFeature(IntrinsicFeature feature) {
    }

    public static native void makePostRequest(String url, String postdata,
        RequestCallback callback) /*-{
    var response = function(obj) { 
        @com.totsp.crossword.web.gadget.ShortyzGadget::onSuccessInternal(Lcom/totsp/crossword/web/client/GadgetResponse;Lcom/google/gwt/http/client/RequestCallback;)(obj, callback);
    };
    var params = {};
    params[$wnd.gadgets.io.RequestParameters.HEADERS] = {
    "Content-Type": "text/x-gwt-rpc"
    };
    params[$wnd.gadgets.io.RequestParameters.METHOD] = $wnd.gadgets.io.MethodType.POST; 
    params[$wnd.gadgets.io.RequestParameters.POST_DATA]= postdata; 
    $wnd.gadgets.io.makeRequest(url, response, params);


    }-*/;

    @Override
    public void initializeFeature(DynamicHeightFeature feature) {
        this.height = feature;
    }

    @Override
    protected void init(UserPreferences preferences) {
        this.prefs = preferences;

        Game g = Injector.INSTANCE.game();
        g.setDisplayChangeListener(new DisplayChangeListener() {
                @Override
                public void onDisplayChange() {
                    height.adjustHeight();
                }
            });
        g.setSmallView(true);
        g.loadList();
    }

    static void onSuccessInternal(final GadgetResponse response,
        RequestCallback callback) {
        try {
            callback.onResponseReceived(null, new FakeResponse(response));
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }

    public static class FakeRequest extends Request {
    }

    private static class FakeResponse extends Response {
        private GadgetResponse response;

        FakeResponse(GadgetResponse response) {
            this.response = response;
        }

        @Override
        public String getHeader(String header) {
            return null;
        }

        @Override
        public Header[] getHeaders() {
            return new Header[0];
        }

        @Override
        public String getHeadersAsString() {
            return null;
        }

        @Override
        public int getStatusCode() {
            return 200;
        }

        @Override
        public String getStatusText() {
            return "OK";
        }

        @Override
        public String getText() {
            return response.getText();
        }
    }
}
