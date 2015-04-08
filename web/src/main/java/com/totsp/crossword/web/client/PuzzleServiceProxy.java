/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.web.shared.PuzzleDescriptor;
import com.totsp.crossword.web.shared.PuzzleServiceAsync;

/**
 *
 * @author kebernet
 */
public class PuzzleServiceProxy {

    PuzzleServiceAsync service;
    CallStrategy strat;

    public PuzzleServiceProxy(PuzzleServiceAsync service, CallStrategy strat){
        this.service = service;
        this.strat = strat == null ? new DefaultCallStrategy() : strat;
    }

    public Request findPuzzle(Long puzzleId, AsyncCallback<Puzzle> callback){
        RequestBuilder builder = service.findPuzzle(puzzleId, callback);
        return strat.makeRequest(builder);
    }
    public Request listPuzzles(AsyncCallback<PuzzleDescriptor[]> callback){
        RequestBuilder builder = service.listPuzzles(callback);
        return strat.makeRequest(builder);
    }
    public Request savePuzzle(Long listingId, Puzzle puzzle, AsyncCallback callback){
        RequestBuilder builder = service.savePuzzle(listingId, puzzle, callback);
        return strat.makeRequest(builder);
    }


    public static interface CallStrategy {


        public Request makeRequest(RequestBuilder builder);

    }

    public static class DefaultCallStrategy implements CallStrategy {

        @Override
        public Request makeRequest(RequestBuilder builder) {
            try{
                return builder.send();
            } catch(Exception e){
                throw new RuntimeException(e.toString(), e);
            }
        }

    }


}
