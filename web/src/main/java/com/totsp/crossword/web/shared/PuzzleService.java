/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.shared;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.totsp.crossword.puz.Puzzle;
import java.util.ArrayList;

/**
 *
 * @author kebernet
 */
@RemoteServiceRelativePath("puzzle.rpc")
public interface PuzzleService extends RemoteService{


    public Puzzle findPuzzle(Long puzzleId);
    public PuzzleDescriptor[] listPuzzles();
    public void savePuzzle(Long listingId, Puzzle puzzle) throws NoUserException;

}
