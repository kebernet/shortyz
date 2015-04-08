/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.totsp.crossword.web.wave;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.gadgets.client.AnalyticsFeature;
import com.google.gwt.gadgets.client.DynamicHeightFeature;
import com.google.gwt.gadgets.client.Gadget.ModulePrefs;
import com.google.gwt.gadgets.client.IntrinsicFeature;
import com.google.gwt.gadgets.client.NeedsAnalytics;
import com.google.gwt.gadgets.client.NeedsDynamicHeight;
import com.google.gwt.gadgets.client.NeedsIntrinsics;
import com.google.gwt.gadgets.client.UserPreferences;
import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.totsp.crossword.puz.Playboard.Position;

import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.web.client.GadgetResponse;
import com.totsp.crossword.web.client.Game;
import com.totsp.crossword.web.client.Game.DisplayChangeListener;
import com.totsp.crossword.web.client.Game.PlayStateListener;
import com.totsp.crossword.web.client.PuzzleCodec;

import com.totsp.gwittir.serial.client.SerializationException;

import org.cobogw.gwt.waveapi.gadget.client.Mode;
import org.cobogw.gwt.waveapi.gadget.client.ModeChangeEvent;
import org.cobogw.gwt.waveapi.gadget.client.ModeChangeEventHandler;
import org.cobogw.gwt.waveapi.gadget.client.Participant;
import org.cobogw.gwt.waveapi.gadget.client.ParticipantUpdateEvent;
import org.cobogw.gwt.waveapi.gadget.client.ParticipantUpdateEventHandler;
import org.cobogw.gwt.waveapi.gadget.client.State;
import org.cobogw.gwt.waveapi.gadget.client.StateUpdateEvent;
import org.cobogw.gwt.waveapi.gadget.client.StateUpdateEventHandler;
import org.cobogw.gwt.waveapi.gadget.client.WaveGadget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author kebernet
 */
@ModulePrefs(title = "Shortyz", author = "Robert Cooper", author_quote = "If you only have two ducks, they are always in a row.", author_email = "kebernet@gmail.com", width = 1020, height = 600, scrolling = true)
public class ShortyzWave extends WaveGadget<UserPreferences>
    implements NeedsIntrinsics, NeedsDynamicHeight, NeedsAnalytics {
    private static final String INTIAL_PUZZLE_KEY = "intial";
    private static final String[] COLORS = new String[] {
            "blue", "green", "gray", "violet","maroon", "olive", "teal", "lime", "fuchsia"
        };
    private static final PuzzleCodec CODEC = GWT.create(PuzzleCodec.class);
    private static final PlayContainerCodec PLAY_CODEC = GWT.create(PlayContainerCodec.class);
    UserPreferences prefs;
    private DynamicHeightFeature height;
    private FlexTable userList = new FlexTable();
    private HandlerRegistration startupStateHandler;

    private NumberFormat format = NumberFormat.getFormat(
            "#######################");
    private PlayStateListener deltaStateListener = new PlayStateListener() {
            @Override
            public void onLetterPlayed(String responder, int across, int down,
                char response, boolean cheated) {
                Play play = new Play();
                play.setTime(getWave().getTime());
                play.setAcross(across);
                play.setDown(down);
                play.setResponse(response);
                play.setResponder(responder);
                play.setCheated(cheated);
                batch.getPlays().add(play);
            }

            @Override
            public void onPuzzleLoaded(Puzzle puz) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onCursorMoved(int across, int down) {
                if(getWave().getViewer() != null && getWave().getState() != null ){
                    getWave().getState().submitValue("cursor-"+getWave().getViewer().getId(), across+","+down);
                }
            }
        };

    private Set<String> stateKeysSeen = new HashSet<String>();
    private PlayContainer batch = new PlayContainer();

    public ShortyzWave() {
        super();
    }

    @Override
    public void initializeFeature(IntrinsicFeature feature) {
    }

    public static native void makePostRequest(String url, String postdata,
        RequestCallback callback) /*-{
    var response = function(obj) {
    @com.totsp.crossword.web.wave.ShortyzWave::onSuccessInternal(Lcom/totsp/crossword/web/client/GadgetResponse;Lcom/google/gwt/http/client/RequestCallback;)(obj, callback);
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

        final Game g = Injector.INSTANCE.game();
        g.setShowBackButton(false);
        g.setDisplayChangeListener(new DisplayChangeListener() {
                @Override
                public void onDisplayChange() {
                    height.adjustHeight();
                }
            });
        this.userList.setWidth("160px");
        g.getDisplay().add(userList);
        Frame ad = new Frame();
        ad.setWidth("120px");
        ad.setHeight("600px");
        ad.getElement().getStyle().setBorderStyle(BorderStyle.NONE);
        ad.getElement().getStyle().setOverflow(Overflow.HIDDEN);
        g.getDisplay().add(ad);
        ad.setUrl("http://shortyz.kebernet.net/static/wave-ad.html");

        final StateUpdateEventHandler externalPlayHandler = new StateUpdateEventHandler() {
                @Override
                public void onUpdate(final StateUpdateEvent event) {
                    final State state = event.getState();
                    
                    if(!g.isPlaying()){
                        try {
                            g.startPuzzle(0L, CODEC.deserialize(state.get(INTIAL_PUZZLE_KEY)));
                        } catch (SerializationException ex) {
                            Window.alert("Exception trying to override the list "+ex);
                        }

                        ArrayList<String> updateKeys = new ArrayList<String>();
                        JsArrayString keys = event.getState().getKeys();
                        ArrayList<String> allKeys = new ArrayList<String>();
                        for (int i = 0; i < keys.length(); i++) {
                            String key = keys.get(i);

                            if (!"undefined".equals("" + key) && key.startsWith("play-") &&
                                    !stateKeysSeen.contains(key)) {
                                updateKeys.add(key);
                                stateKeysSeen.add(key);
                                allKeys.add(key);
                            } else if (!"undefined".equals("" + key) && key.startsWith("play-")){
                                allKeys.add(key);
                            }
                        }

                        final String[] keysArray = updateKeys.toArray(new String[updateKeys.size()]);
                        final String[] allKeysArray = allKeys.toArray(new String[allKeys.size()]);
                        Arrays.sort(keysArray);
                        Arrays.sort(allKeysArray);

                        for (String key : keysArray) {
                            String data = event.getState().get(key);
                            if(!"undefined".equals(""+key)){
                                try {
                                    if (data != null) {
                                        PlayContainer container = PLAY_CODEC.deserialize(data);
                                        for(Play play : container.getPlays()){
                                            g.play(play.getResponder(),
                                                play.getAcross(),
                                                play.getDown(),
                                                play.getResponse(),
                                                play.isCheated());
                                        }
                                    }
                                } catch (SerializationException ex) {
                                    Window.alert(
                                        "Critical error getting state update " +
                                        ex + "\n" + key + "\n" + data);
                                }
                            }
                        }

                        if(allKeysArray.length > 10 ){

                            try{
                                state.submitValue(INTIAL_PUZZLE_KEY, CODEC.serialize(g.getPuzzle()));
                                for(int i=0; allKeysArray.length - i > 10; i++ ){
                                    state.submitValue(allKeysArray[i], null);
                                }
                            } catch(SerializationException se){
                                Window.alert("Update of wave state failed"+se);
                            }
                        }

                        g.render();
                    } else { // is playing


                        ArrayList<String> cursorKeys = new ArrayList<String>();
                        ArrayList<String> updateKeys = new ArrayList<String>();
                         ArrayList<String> allKeys = new ArrayList<String>();

                        JsArrayString keys = event.getState().getKeys();

                        for (int i = 0; i < keys.length(); i++) {
                            String key = keys.get(i);

                            if (!"undefined".equals("" + key) &&
                                    key.startsWith("play-") &&
                                    !stateKeysSeen.contains(key)) {
                                updateKeys.add(key);

                                if (!getWave().isPlayback()) {
                                    stateKeysSeen.add(key);
                                }
                            } else if (!"undefined".equals("" + key) && key.startsWith("play-")){
                                allKeys.add(key);
                            } else if(!"undefined".equals("" + key) &&
                                    key.startsWith("cursor-")){
                                    cursorKeys.add(key);
                            }

                        }
                    for(String key : cursorKeys){
                        if (!"undefined".equals("" + key) ){
                            String responder = key.substring(key.indexOf("-")+1);
                            if(!getWave().getViewer().getId().equals(responder)){
                                String[] value = state.get(key).split(",");
                                Injector.INSTANCE.renderer().updateCursor(responder, new Position(Integer.parseInt(value[0]), Integer.parseInt(value[1])));
                            }
                        }
                    }
                    final String[] keysArray = updateKeys.toArray(new String[updateKeys.size()]);
                    final String[] allKeysArray = allKeys.toArray(new String[allKeys.size()]);
                    Arrays.sort(keysArray);
                    Arrays.sort(allKeysArray);

                    if(allKeysArray.length > 10 ){

                        try{
                            state.submitValue(INTIAL_PUZZLE_KEY, CODEC.serialize(g.getPuzzle()));
                            for(int i=0; allKeysArray.length - i > 10; i++ ){
                                state.submitValue(allKeysArray[i], null);
                            }
                        } catch(SerializationException se){
                            Window.alert("Update of wave state failed"+se);
                        }
                    }

                    DeferredCommand.addCommand(new IncrementalCommand() {
                            int i = 0;

                            @Override
                            public boolean execute() {
                                String key = keysArray[i++];
                                String data = event.getState().get(key);
                                if(!"undefined".equals(""+key)){
                                    try {
                                        PlayContainer container = PLAY_CODEC.deserialize(data);
                                        for(Play play : container.getPlays()){
                                            g.play(play.getResponder(),
                                                play.getAcross(), play.getDown(),
                                                play.getResponse(),
                                                play.isCheated());
                                        }
                                        g.render();
                                    } catch (SerializationException ex) {
                                        Window.alert(
                                            "Critical error getting state update " +
                                            ex + "\n" + key + "\n" + data);
                                    }
                                }
                                return i < keysArray.length;
                            }
                        });
                    }
                }
            };

        this.getWave().addParticipantUpdateEventHandler(new ParticipantUpdateEventHandler() {
                @Override
                public void onUpdate(ParticipantUpdateEvent event) {
                    Injector.INSTANCE.renderer()
                                     .setColorMap(provisionColors(
                            event.getParticipants()));
                    g.render();
                }
            });
        this.getWave().addModeUpdateEventHandler(new ModeChangeEventHandler() {
                @Override
                public void onUpdate(ModeChangeEvent event) {
                    if (event.getMode() == Mode.PLAYBACK) {
                        stateKeysSeen.clear();
                    }
                }
            });

        this.startupStateHandler = this.getWave().addStateUpdateEventHandler(new StateUpdateEventHandler() {
                    @Override
                    public void onUpdate(StateUpdateEvent event) {
                        final State state = event.getState();
                        
                        if (state.get(INTIAL_PUZZLE_KEY) == null) {
                            g.loadList();
                            g.setPlayStateListener(new PlayStateListener() {

                                    @Override
                                    public void onCursorMoved(int across, int down) {
                                        //
                                    }
                                    @Override
                                    public void onLetterPlayed(
                                        String responder, int across, int down,
                                        char response, boolean cheated) {
                                        //
                                    }

                                    @Override
                                    public void onPuzzleLoaded(Puzzle puz) {
                                        try {
                                            startupStateHandler.removeHandler();

                                            HashMap<String, String> delta = new HashMap<String, String>();
                                            delta.put(INTIAL_PUZZLE_KEY,
                                                CODEC.serialize(puz));
                                            state.submitDelta(delta);
                                            g.setPlayStateListener(deltaStateListener);
                                            getWave()
                                                .addStateUpdateEventHandler(externalPlayHandler);
                                        } catch (SerializationException ex) {
                                            Window.alert("Critical error " +
                                                ex.toString());
                                        }
                                    }
                                });
                        } else {
                            try {
                                String puzSer = state.get(INTIAL_PUZZLE_KEY);
                                g.startPuzzle(0L,
                                    CODEC.deserialize(puzSer), false);
                                ArrayList<String> cursorKeys = new ArrayList<String>();
                                ArrayList<String> updateKeys = new ArrayList<String>();
                                JsArrayString keys = event.getState().getKeys();

                                for (int i = 0; i < keys.length(); i++) {
                                    String key = keys.get(i);

                                    if (!"undefined".equals("" + key) && key.startsWith("play-") &&
                                            !stateKeysSeen.contains(key)) {
                                        updateKeys.add(key);
                                        stateKeysSeen.add(key);
                                    } else if(!"undefined".equals("" + key) &&
                                    key.startsWith("cursor-")){
                                    cursorKeys.add(key);
                                        }

                                    }
                                for(String key : cursorKeys){
                                    if (!"undefined".equals("" + key) ){
                                        String responder = key.substring(key.indexOf("-")+1);
                                        String[] value = state.get(key).split(",");
                                        Injector.INSTANCE.renderer().updateCursor(responder, new Position(Integer.parseInt(value[0]), Integer.parseInt(value[1])));
                                    }
                                }

                                final String[] keysArray = updateKeys.toArray(new String[updateKeys.size()]);
                                Arrays.sort(keysArray);

                                for (String key : keysArray) {
                                    String data = event.getState().get(key);
                                    if(!"undefined".equals(""+key)){
                                        try {
                                            if (data != null) {
                                                PlayContainer container = PLAY_CODEC.deserialize(data);
                                                for(Play play : container.getPlays()){
                                                    g.play(play.getResponder(),
                                                        play.getAcross(),
                                                        play.getDown(),
                                                        play.getResponse(),
                                                        play.isCheated());
                                                }
                                            }
                                        } catch (SerializationException ex) {
                                            Window.alert(
                                                "Critical error getting state update " +
                                                ex + "\n" + key + "\n" + data);
                                        }
                                    }
                                }

                                g.render();
                                startupStateHandler.removeHandler();
                                g.setPlayStateListener(deltaStateListener);
                                getWave()
                                    .addStateUpdateEventHandler(externalPlayHandler);
                            } catch (Exception ex) {
                                Window.alert("Critical error: " +
                                    ex.toString());
                            }
                        }
                    }
                });
         Timer updateStateTimer = new Timer(){

            @Override
            public void run() {
                if(g.isPlaying() && batch.getPlays().size() > 0){
                    PlayContainer current = batch;
                    batch = new PlayContainer();
                    HashMap<String, String> delta = new HashMap<String, String>();
                    try {
                        delta.put("play-" + format.format(getWave().getTime()),
                            PLAY_CODEC.serialize(current));
                        getWave().getState().submitDelta(delta);
                    } catch (SerializationException ex) {
                        Window.alert("Failed to submit play" + ex.toString());
                    }
                }
            }

        };
        updateStateTimer.scheduleRepeating(500);

        SimplePanel below = g.getBelow();
        below.setWidth((1020 - 120 - 160)+"px");
        below.setWidget( new HTML(Injector.INSTANCE.resources().waveAbout().getText()));
    }

    static void onSuccessInternal(final GadgetResponse response,
        RequestCallback callback) {
        try {
            callback.onResponseReceived(null, new FakeResponse(response));
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }

    private Map<String, String> provisionColors(
        JsArray<Participant> participants) {
        Participant user = this.getWave().getViewer();
        Injector.INSTANCE.game()
                         .setResponder((user != null) ? user.getId() : null);

        HashMap<String, String> colors = new HashMap<String, String>();
        colors.put(user.getId(), "black");
        userList.removeAllRows();

        Label l = new Label("Players:");
        l.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        userList.setWidget(0, 0, l);

        int count = 1;
        int colorIndex = 0;
        l = new Label(user.getDisplayName());
        l.getElement().getStyle().setColor("black");
        userList.setWidget(1, 0, l);

        for (int i = 0; i < participants.length(); i++) {
            Participant p = participants.get(i);

            if (p.getId().equals(user.getId())) {
                continue;
            }

            colors.put(p.getId(), COLORS[colorIndex]);
            l = new Label(p.getDisplayName());
            l.getElement().getStyle().setColor(COLORS[colorIndex]);
            count++;
            userList.setWidget(count, 0, l);
            colorIndex++;

            if (colorIndex == COLORS.length) {
                colorIndex = 0;
            }
        }

        return colors;
    }

    @Override
    public void initializeFeature(AnalyticsFeature feature) {
        feature.recordPageView("UA-15907620-2", "/");
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
