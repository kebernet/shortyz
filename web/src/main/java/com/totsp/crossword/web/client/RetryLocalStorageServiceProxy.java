/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.web.client.LocalStorageServiceProxy;
import com.totsp.crossword.web.shared.PuzzleDescriptor;
import com.totsp.crossword.web.shared.PuzzleServiceAsync;

/**
 *
 * @author kebernet
 */
public class RetryLocalStorageServiceProxy extends LocalStorageServiceProxy {

    public RetryLocalStorageServiceProxy(PuzzleServiceAsync service, CallStrategy strat){
        super(service, strat);
    }

    @Override
    public Request listPuzzles(final AsyncCallback<PuzzleDescriptor[]> callback) {
        return super.listPuzzles( new AsyncCallback<PuzzleDescriptor[]>(){

            @Override
            public void onFailure(Throwable caught) {
                if(caught instanceof InvocationException){
                    RetryLocalStorageServiceProxy.super.listPuzzles(callback);
                } else {
                    callback.onFailure(caught);
                }
            }

            @Override
            public void onSuccess(PuzzleDescriptor[] result) {
                callback.onSuccess(result);
            }

        });
    }

    @Override
    public Request findPuzzle(final Long puzzleId, final AsyncCallback<Puzzle> callback) {
        return super.findPuzzle(puzzleId, new AsyncCallback<Puzzle>(){

            @Override
            public void onFailure(Throwable caught) {
                if(caught instanceof InvocationException ){
                    RetryLocalStorageServiceProxy.super.findPuzzle(puzzleId, callback);
                } else {
                    callback.onFailure(caught);
                }
            }

            @Override
            public void onSuccess(Puzzle result) {
                callback.onSuccess(result);
            }

        });
    }





}
