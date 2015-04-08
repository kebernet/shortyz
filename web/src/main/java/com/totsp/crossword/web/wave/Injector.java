/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.wave;

import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.totsp.crossword.web.client.BoxView;
import com.totsp.crossword.web.client.Game;
import com.totsp.crossword.web.client.PuzzleDescriptorView;
import com.totsp.crossword.web.client.PuzzleListView;
import com.totsp.crossword.web.client.Renderer;
import com.totsp.crossword.web.client.resources.Resources;
import com.totsp.crossword.web.shared.PuzzleServiceAsync;

/**
 *
 * @author kebernet
 */
@GinModules(Module.class)
public interface Injector extends Ginjector {

    public static final Injector INSTANCE = GWT.create(Injector.class);
    Resources resources();
    Renderer renderer();
    PuzzleServiceAsync service();
    BoxView boxView();
    PuzzleDescriptorView puzzleDescriptorView();
    PuzzleListView puzzleListView();

    Game game();
}