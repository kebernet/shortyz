/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.web.client.PuzzleCodec;
import com.totsp.crossword.web.client.PuzzleServiceProxy;
import com.totsp.crossword.web.shared.PuzzleServiceAsync;
import com.totsp.gwittir.client.util.WindowContext;
import com.totsp.gwittir.client.util.WindowContext.WindowContextCallback;
import com.totsp.gwittir.serial.client.SerializationException;

/**
 *
 * @author kebernet
 */
public class LocalStorageServiceProxy extends PuzzleServiceProxy {

    private static final PuzzleCodec CODEC = GWT.create(PuzzleCodec.class);


    public LocalStorageServiceProxy(PuzzleServiceAsync service, CallStrategy strat){
        super(service, strat);
    }

    @Override
    public Request savePuzzle(final Long listingId, final Puzzle puzzle, final AsyncCallback callback) {
        if(!WindowContext.INSTANCE.isInitialized()){
            WindowContext.INSTANCE.initialize( new WindowContextCallback(){

                @Override
                public void onInitialized() {
                    saveInternal(listingId, puzzle, callback);
                }

            });
        } else {

            saveInternal(listingId, puzzle, callback);
        }
        return new FakeRequest();
    }
    
    private void saveInternal(Long listingId, Puzzle puzzle, final AsyncCallback callback){
        try{
            WindowContext.INSTANCE.put(listingId.toString(), CODEC.serialize(puzzle));
            WindowContext.INSTANCE.flush();
        } catch(final Exception e){
            callback.onFailure(e);
        }
       
        callback.onSuccess(null);
    }

    @Override
    public Request findPuzzle(final Long puzzleId, final AsyncCallback<Puzzle> callback) {
        GWT.log(WindowContext.INSTANCE.isInitialized() +" init", null);
        if(!WindowContext.INSTANCE.isInitialized()){
            WindowContext.INSTANCE.initialize(new WindowContextCallback(){

                @Override
                public void onInitialized() {
                    GWT.log("Init callback", null);
                    findPuzzle(puzzleId, callback);
                }

            });
            return new FakeRequest();
        }

        if(WindowContext.INSTANCE.get(puzzleId.toString()) != null){
            GWT.log("Found local data.", null);
            DeferredCommand.addCommand(new Command(){

                @Override
                public void execute() {
                    loadInternal(puzzleId, callback);
                }

            });
            return new FakeRequest();
        }
        return super.findPuzzle(puzzleId, callback);
    }



    private void loadInternal(Long listingId, AsyncCallback<Puzzle> callback){
        try {
            GWT.log("Load internal", null);
            String data = WindowContext.INSTANCE.get(listingId.toString());
            GWT.log(data, null);
            callback.onSuccess(CODEC.deserialize(data));
        } catch (SerializationException ex) {
            callback.onFailure(ex);
        }
    }
    
    public static class FakeRequest extends Request{
        
    }




}
