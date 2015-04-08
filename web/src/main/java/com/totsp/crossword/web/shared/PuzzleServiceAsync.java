/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.shared;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.totsp.crossword.puz.Puzzle;

/**
 *
 * @author kebernet
 */
public interface PuzzleServiceAsync {
    
    public RequestBuilder findPuzzle(Long puzzleId, AsyncCallback<Puzzle> callback);
    public RequestBuilder listPuzzles(AsyncCallback<PuzzleDescriptor[]> callback);
    public RequestBuilder savePuzzle(Long listingId, Puzzle puzzle, AsyncCallback callback);
}
