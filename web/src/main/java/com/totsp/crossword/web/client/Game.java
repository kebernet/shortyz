/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.totsp.crossword.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.google.inject.Inject;

import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Playboard;
import com.totsp.crossword.puz.Playboard.Clue;
import com.totsp.crossword.puz.Playboard.Position;
import com.totsp.crossword.puz.Playboard.Word;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.web.client.Renderer.ClickListener;
import com.totsp.crossword.web.client.resources.Css;
import com.totsp.crossword.web.client.resources.Resources;
import com.totsp.crossword.web.shared.PuzzleDescriptor;

import com.totsp.gwittir.client.fx.ui.SoftScrollArea;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 *
 * @author kebernet
 */
public class Game {
    private static final WASDCodes CODES = GWT.create(WASDCodes.class);
    private static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private Css css;
    private DisplayChangeListener displayChangeListener = new DisplayChangeListener() {
            @Override
            public void onDisplayChange() {
                ; //noop
            }
        };

    private FlexTable t;
    private FocusPanel mainPanel = new FocusPanel();
    private HandlerRegistration closingRegistration = null;
    private HashMap<Clue, Widget> acrossClueViews = new HashMap<Clue, Widget>();
    private HashMap<Clue, Widget> downClueViews = new HashMap<Clue, Widget>();
    private HorizontalPanel display = new HorizontalPanel();
    private Label clueLine = new Label();
    private Playboard board;
    private KeyboardListener l = new KeyboardListener() {
            @Override
            public void onKeyPress(Widget sender, char keyCode, int modifiers) {
            }

            @Override
            public void onKeyDown(Widget sender, char keyCode, int modifiers) {
                if (board == null) {
                    return;
                }

                if ((keyCode == KeyCodes.KEY_DOWN) ||
                        (((modifiers & KeyboardListener.MODIFIER_CTRL) > 0) &&
                        (keyCode == CODES.s()))) {
                    Word w = board.moveDown();
                    render(w);
                    playStateListener.onCursorMoved(board.getHighlightLetter().across, board.getHighlightLetter().down);
                    
                    return;
                }

                if ((keyCode == KeyCodes.KEY_UP) ||
                        (((modifiers & KeyboardListener.MODIFIER_CTRL) > 0) &&
                        (keyCode == CODES.w()))) {
                    Word w = board.moveUp();
                    render(w);
                    playStateListener.onCursorMoved(board.getHighlightLetter().across, board.getHighlightLetter().down);

                    return;
                }

                if ((keyCode == KeyCodes.KEY_LEFT) ||
                        (((modifiers & KeyboardListener.MODIFIER_CTRL) > 0) &&
                        (keyCode == CODES.a()))) {
                    Word w = board.moveLeft();
                    render(w);
                    playStateListener.onCursorMoved(board.getHighlightLetter().across, board.getHighlightLetter().down);
                    return;
                }

                if ((keyCode == KeyCodes.KEY_RIGHT) ||
                        (((modifiers & KeyboardListener.MODIFIER_CTRL) > 0) &&
                        (keyCode == CODES.d()))) {
                    Word w = board.moveRight();
                    render(w);
                    playStateListener.onCursorMoved(board.getHighlightLetter().across, board.getHighlightLetter().down);
                    return;
                }

                if ((keyCode == ' ') || (keyCode == KeyCodes.KEY_ENTER)) {
                    Word w = board.setHighlightLetter(board.getHighlightLetter());
                    render(w);

                    return;
                }

                if ((keyCode == KeyCodes.KEY_BACKSPACE) ||
                        (keyCode == KeyCodes.KEY_DELETE)) {
                    Position p = board.getHighlightLetter();
                    Word w = board.deleteLetter();
                    render(w);
                    playStateListener.onCursorMoved(board.getHighlightLetter().across, board.getHighlightLetter().down);
                    playStateListener.onLetterPlayed(responder, p.across,
                        p.down, ' ',
                        board.getBoxes()[p.across][p.down].isCheated());
                    dirty = true;
                    return;
                }

                if (((modifiers & KeyboardListener.MODIFIER_CTRL) == 0) &&
                        (ALPHA.indexOf(Character.toUpperCase(keyCode)) != -1)) {
                    Position p = board.getHighlightLetter();
                    Word w = board.playLetter(Character.toUpperCase(keyCode));
                    if(board.getHighlightLetter().across == board.getBoxes().length -1 ||
                            board.getHighlightLetter().down == board.getBoxes()[board.getHighlightLetter().across].length -1 ){
                        w= null;
                    }
                    render(w);
                    playStateListener.onLetterPlayed(responder, p.across,
                        p.down, Character.toUpperCase(keyCode),
                        board.getBoxes()[p.across][p.down].isCheated());
                    playStateListener.onCursorMoved(board.getHighlightLetter().across, board.getHighlightLetter().down);
                    dirty = true;

                    return;
                }
            }

            @Override
            public void onKeyUp(Widget sender, char keyCode, int modifiers) {
            }
        };

    private Label status = new Label();
    private PuzzleListView plv;
    private PuzzleServiceProxy service;
    private Renderer renderer;
    private Request request = null;
    private ScrollPanel acrossScroll = new ScrollPanel();
    private ScrollPanel downScroll = new ScrollPanel();
    private SimplePanel below = new SimplePanel();
    private SoftScrollArea ssa = new SoftScrollArea();
    private String responder;
    private PlayStateListener playStateListener = new PlayStateListener() {
            @Override
            public void onPuzzleLoaded(Puzzle puz) {
                //noop
            }

            @Override
            public void onLetterPlayed(String responder, int across, int down,
                char response, boolean cheated) {
                // noop
            }

            @Override
            public void onCursorMoved(int across, int down) {
                GWT.log("Move to "+across+", "+down, null);
            }
        };

    private TextArea keyboardIntercept = new TextArea();
    private Timer autoSaveTimer = null;
    private VerticalPanel verticalPanel = new VerticalPanel();
    private Widget lastClueWidget;
    private Clue[] acrossClues;
    private Clue[] downClues;
    private boolean dirty;
    private boolean playing;
    private boolean showBackButton = true;
    private boolean smallView;

    @Inject
    public Game(RootPanel rootPanel, PuzzleServiceProxy service,
        Resources resources, final PuzzleListView plv, Renderer renderer) {
        this.service = service;
        this.plv = plv;
        this.renderer = renderer;
        this.css = resources.css();

        History.newItem("list", false);
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    if (event.getValue().equals("list")) {
                        if (closingRegistration != null) {
                            closingRegistration.removeHandler();
                            closingRegistration = null;
                        }

                        if (autoSaveTimer != null) {
                            autoSaveTimer.cancel();
                            autoSaveTimer.run();
                            autoSaveTimer = null;
                        }

                        mainPanel.setWidget(plv);
                        keyboardIntercept.removeKeyboardListener(l);

                        getDisplayChangeListener().onDisplayChange();
                    } else if (event.getValue().startsWith("play=")) {
                        Long id = Long.parseLong(event.getValue().split("=")[1]);
                        loadPuzzle(id);
                    }
                }
            });
        verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        verticalPanel.setWidth("100%");
        StyleInjector.inject(resources.css().getText());
        keyboardIntercept.setWidth("1px");
        keyboardIntercept.setHeight("1px");
        keyboardIntercept.setStyleName(css.keyboardIntercept());
        rootPanel.add(keyboardIntercept);

        verticalPanel.add(status);
        verticalPanel.setCellHorizontalAlignment(status,
            HasHorizontalAlignment.ALIGN_CENTER);
        verticalPanel.add(mainPanel);
        verticalPanel.setCellHorizontalAlignment(mainPanel,
            HasHorizontalAlignment.ALIGN_CENTER);
        verticalPanel.add(below);
        display.add(verticalPanel);
        display.setWidth("97%");
        rootPanel.add(display);
    }

    public SimplePanel getBelow() {
        return this.below;
    }

    public HorizontalPanel getDisplay() {
        return this.display;
    }

    /**
     * @param displayChangeListener the displayChangeListener to set
     */
    public void setDisplayChangeListener(
        DisplayChangeListener displayChangeListener) {
        this.displayChangeListener = displayChangeListener;
    }

    /**
     * @return the displayChangeListener
     */
    public DisplayChangeListener getDisplayChangeListener() {
        return displayChangeListener;
    }

    /**
     * Set the value of playStateListener
     *
     * @param newplayStateListener new value of playStateListener
     */
    public void setPlayStateListener(PlayStateListener newplayStateListener) {
        this.playStateListener = newplayStateListener;
    }

    /**
     * Get the value of playStateListener
     *
     * @return the value of playStateListener
     */
    public PlayStateListener getPlayStateListener() {
        return this.playStateListener;
    }

    public boolean isPlaying() {
        return this.playing;
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
     * Set the value of showBackButton
     *
     * @param newshowBackButton new value of showBackButton
     */
    public void setShowBackButton(boolean newshowBackButton) {
        this.showBackButton = newshowBackButton;
    }

    /**
     * Get the value of showBackButton
     *
     * @return the value of showBackButton
     */
    public boolean isShowBackButton() {
        return this.showBackButton;
    }

    /**
     * @param smallView the smallView to set
     */
    public void setSmallView(boolean smallView) {
        this.smallView = smallView;
    }

    /**
     * @return the smallView
     */
    public boolean isSmallView() {
        return smallView;
    }

    public Label getStatus() {
        return this.status;
    }

    public void loadList() {
        status.setText("Loading puzzles...");
        status.setStyleName(css.statusInfo());
        service.listPuzzles(new AsyncCallback<PuzzleDescriptor[]>() {
                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Failed to load puzzles. \n" +
                        caught.toString());
                }

                @Override
                public void onSuccess(PuzzleDescriptor[] result) {
                    Arrays.sort(result);
                    mainPanel.setWidget(plv);
                    plv.setValue(Arrays.asList(result));
                    setFBSize();
                    displayChangeListener.onDisplayChange();
                    status.setStyleName(css.statusHidden());
                    status.setText(" ");
                }
            });
    }

    public void loadPuzzle(final Long id) {
        status.setText("Loading puzzle...");
        status.setStyleName(css.statusInfo());

        if (request == null) {
            request = service.findPuzzle(id,
                    new AsyncCallback<Puzzle>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert(caught.toString());
                        }

                        @Override
                        public void onSuccess(Puzzle result) {
                            History.newItem("play=" + id, false);
                            startPuzzle(id, result);
                            request = null;
                            status.setStyleName(css.statusHidden());
                            status.setText(" ");
                        }
                    });
        } else {
            request.cancel();
            request = null;
            loadPuzzle(id);
        }
    }

    public void play(String responder, int across, int down, char response,
        boolean cheated) {
        if (this.board != null) {
            Box b = this.board.getBoxes()[across][down];

            if (b != null) {
                b.setResponse(response);
                b.setResponder(responder);
                b.setCheated(cheated);
            }
        }
    }

    public void render() {
        render(null);
    }

    public void showWait() {
        this.status.setText("Please wait while the creator selects a puzzle...");
        this.mainPanel.clear();
    }

    public void startPuzzle(final long listingId, final Puzzle puzzle) {
        this.startPuzzle(listingId, puzzle, true);
    }

    public Puzzle getPuzzle(){
        return board.getPuzzle();
    }

    public void startPuzzle(final long listingId, final Puzzle puzzle,
        boolean fireEvent) {
        this.playing = true;

        if (fireEvent) {
            this.playStateListener.onPuzzleLoaded(puzzle);
        }

        VerticalPanel outer = new VerticalPanel();
        board = new Playboard(puzzle);
        board.setResponder(this.getResponder());
        board.setHighlightLetter(new Position(0, 0));

        if (board.getBoxes()[0][0] == null) {
            board.moveRight();
        }

        if (this.isSmallView()) {
            outer.add(this.clueLine);
            outer.setCellHorizontalAlignment(this.clueLine,
                HasHorizontalAlignment.ALIGN_CENTER);
            this.clueLine.setStyleName(css.clueLine());
        }

        ssa.setWidth("421px");
        ssa.setHeight("421px");
        //        ssa.setWidth("300px");
        //        ssa.setHeight("300px");
        ssa.setStyleName(css.ssa());
        ssa.addMouseListener(ssa.MOUSE_MOVE_SCROLL_LISTENER);
        t = renderer.initialize(board);

        HorizontalPanel hp = new HorizontalPanel();
        ssa.setWidget(t);
        hp.add(ssa);

        acrossScroll.setWidth("155px");
        downScroll.setWidth("155px");

        VerticalPanel list = new VerticalPanel();

        int index = 0;

        for (Clue c : acrossClues = board.getAcrossClues()) {
            final int cIndex = index;
            Grid g = new Grid(1, 2);
            g.setWidget(0, 0, new Label(c.number + ""));
            g.getCellFormatter().setStyleName(0, 0, css.clueNumber());
            g.getRowFormatter()
             .setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
            g.setWidget(0, 1, new Label(c.hint));
            g.setStyleName(css.clueBox());

            list.add(g);
            acrossClueViews.put(c, g);
            g.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        board.jumpTo(cIndex, true);
                        mainPanel.setFocus(true);
                        render();
                    }
                });
            index++;
        }

        Widget acrossList = list;

        list = new VerticalPanel();

        Widget downList = list;

        index = 0;

        for (Clue c : downClues = board.getDownClues()) {
            final int cIndex = index;
            Grid g = new Grid(1, 2);
            g.setWidget(0, 0, new Label(c.number + ""));
            g.getCellFormatter().setStyleName(0, 0, css.clueNumber());
            g.getRowFormatter()
             .setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
            g.setWidget(0, 1, new Label(c.hint));
            g.setStyleName(css.clueBox());

            list.add(g);
            downClueViews.put(c, g);
            g.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        board.jumpTo(cIndex, false);
                        mainPanel.setFocus(true);
                        render();
                    }
                });
            index++;
        }

        if (!this.isSmallView()) {
            hp.add(acrossScroll);
            hp.add(downScroll);
        }

        render(board.getCurrentWord());
        outer.add(hp);

        outer.setCellHorizontalAlignment(hp, HasHorizontalAlignment.ALIGN_CENTER);

        HorizontalPanel controls = new HorizontalPanel();

        if (this.isShowBackButton()) {
            Button back = new Button("Return to List",
                    new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            playing = false;
                            History.newItem("list");
                        }
                    });

            if (!this.isSmallView()) {
                back.getElement().getStyle().setMarginRight(30, Unit.PX);
            }

            controls.add(back);
        }

        controls.add(new Button("Show Errors",
                new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    board.toggleShowErrors();
                    ((Button) event.getSource()).setText(board.isShowErrors()
                            ? "Hide Errors" : "Show Errors");
                    render();
                }
            }));
        controls.add(new Button("Reveal Letter",
                new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    Position p = board.getHighlightLetter();
                    Position change = board.revealLetter();
                    char solution = board.getBoxes()[p.across][p.down].getSolution();
                    dirty = true;

                    if (change != null) {
                        playStateListener.onLetterPlayed(responder, p.across,
                                p.down, solution, true);
                    }

                    render();
                }
            }));
        controls.add(new Button("Reveal Word",
                new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    Word word = board.getCurrentWord();
                    List<Position> changes = board.revealWord();

                    for (Position p : changes) {
                        playStateListener.onLetterPlayed(responder, p.across,
                                p.down,
                                board.getBoxes()[p.across][p.down].getSolution(),
                                true);
                    }

                    dirty = true;

                    render();
                }
            }));
        controls.add(new Button("Reveal Puzzle",
                new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    List<Position> changes = board.revealPuzzle();

                    for (Position p : changes) {
                        playStateListener.onLetterPlayed(responder, p.across,
                                p.down,
                                board.getBoxes()[p.across][p.down].getSolution(),
                                true);
                    }

                    dirty = true;
                    render();
                }
            }));
        controls.getElement().getStyle().setMarginTop(10, Unit.PX);
        outer.add(controls);

        mainPanel.setWidget(outer);

        acrossScroll.setHeight(ssa.getOffsetHeight() + "px ");
        downScroll.setHeight(ssa.getOffsetHeight() + "px ");
        setFBSize();

        autoSaveTimer = new Timer() {
                    @Override
                    public void run() {
                        if (!dirty) {
                            return;
                        }

                        dirty = false;
                        status.setStyleName(css.statusInfo());
                        status.setText("Autosaving...");
                        service.savePuzzle(listingId, puzzle,
                            new AsyncCallback() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    GWT.log("Save failed", caught);
                                    status.setStyleName(css.statusError());
                                    status.setText("Failed to save puzzle.");
                                }

                                @Override
                                public void onSuccess(Object result) {
                                    status.setStyleName(css.statusHidden());
                                    status.setText(" ");
                                }
                            });
                    }
                };
        autoSaveTimer.scheduleRepeating(2 * 60 * 1000);
        closingRegistration = Window.addWindowClosingHandler(new ClosingHandler() {
                    @Override
                    public void onWindowClosing(ClosingEvent event) {
                        if (dirty) {
                            event.setMessage("Abandon unsaved changes?");
                        }
                    }
                });
        getDisplayChangeListener().onDisplayChange();
        acrossScroll.setWidget(acrossList);
        downScroll.setWidget(downList);
        keyboardIntercept.addKeyboardListener(l);
        renderer.setClickListener(new ClickListener() {
                @Override
                public void onClick(int across, int down) {
                    Word w = board.setHighlightLetter(new Position(across, down));
                    render(w);
                    playStateListener.onCursorMoved(board.getHighlightLetter().across, board.getHighlightLetter().down);
                    keyboardIntercept.setFocus(true);
                }
            });
        keyboardIntercept.setFocus(true);
    }

    private static native void setFBSize() /*-{
    if($wnd.FB && $wnd.FB.CanvasClient.setSizeToContent){
    $wnd.FB.CanvasClient.setSizeToContent();
    }
    }-*/;

    private void ensureVisible(int x, int y) {
        int maxScrollX = ssa.getMaxHorizontalScrollPosition();
        int maxScrollY = ssa.getMaxScrollPosition();

        int currentMinX = ssa.getHorizontalScrollPosition();
        int currentMaxX = ssa.getOffsetWidth() + currentMinX;
        int currentMinY = ssa.getScrollPosition();
        int currentMaxY = ssa.getOffsetHeight() + currentMinY;

        GWT.log("X range " + currentMinX + " to " + currentMaxX, null);
        GWT.log("Desired X:" + x, null);
        GWT.log("Y range " + currentMinY + " to " + currentMaxY, null);
        GWT.log("Desired Y:" + y, null);

        int newScrollX = ssa.getHorizontalScrollPosition();
        int newScrollY = ssa.getScrollPosition();

        if ((x < currentMinX) || (x > currentMaxX)) {
            newScrollX = (x > maxScrollX) ? maxScrollX : x;
        }

        if ((y < currentMinY) || (y > currentMaxY)) {
            newScrollY = (y > maxScrollY) ? maxScrollY : y;
        }

        ssa.setHorizontalScrollPosition(newScrollX);
        ssa.setScrollPosition(newScrollY);
    }

    private void render(Word w) {
        renderer.render(w);

        Position highlight = board.getHighlightLetter();
        this.ensureVisible((highlight.across + 1) * 26,
            (highlight.down + 1) * 26);
        this.ensureVisible(highlight.across * 26, highlight.down * 26);

        if (lastClueWidget != null) {
            lastClueWidget.removeStyleName(css.highlightClue());
        }

        Clue clue = board.getClue();

        if (board.isAcross() && (board.getClue() != null)) {
            lastClueWidget = acrossClueViews.get(clue);
            acrossScroll.ensureVisible(lastClueWidget);
            clueLine.setText("(across) " + clue.number + ". " + clue.hint);
        } else if (board.getClue() != null) {
            lastClueWidget = downClueViews.get(board.getClue());
            downScroll.ensureVisible(lastClueWidget);
            clueLine.setText("(down) " + clue.number + ". " + clue.hint);
        }

        lastClueWidget.addStyleName(css.highlightClue());
        keyboardIntercept.setFocus(true);
    }

    public static interface DisplayChangeListener {
        public void onDisplayChange();
    }

    public static interface PlayStateListener {
        public void onCursorMoved(int across, int down);

        public void onLetterPlayed(String responder, int across, int down,
            char response, boolean cheated);

        public void onPuzzleLoaded(Puzzle puz);
    }
}
