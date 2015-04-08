/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.puz;

import java.io.DataInputStream;

import junit.framework.TestCase;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Playboard.Position;

/**
 *
 * @author kebernet
 */
public class PlayboardTest extends TestCase {
    
    public PlayboardTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMoveUp() throws Exception {
         Puzzle puz = IO.loadNative(new DataInputStream(IOTest.class.getResourceAsStream("/test.puz")));

         Playboard board = new Playboard(puz);
         board.setHighlightLetter(new Position(5, 5));
         System.out.println("ON: "+board.getBoxes()[board.getHighlightLetter().across][board.getHighlightLetter().down].getSolution());
         board.moveUp(false);


         System.out.println("ON: "+board.getBoxes()[board.getHighlightLetter().across][board.getHighlightLetter().down].getSolution());
         assertEquals(4, board.getHighlightLetter().down);
         board.moveUp(false);


         System.out.println("ON: "+board.getBoxes()[board.getHighlightLetter().across][board.getHighlightLetter().down].getSolution());
         assertEquals(3, board.getHighlightLetter().down);
         board.moveUp(false);
         
         System.out.println("ON: "+board.getBoxes()[board.getHighlightLetter().across][board.getHighlightLetter().down].getSolution());
         assertEquals(2, board.getHighlightLetter().down);
         board.moveUp(false);

         System.out.println("ON: "+board.getBoxes()[board.getHighlightLetter().across][board.getHighlightLetter().down].getSolution());
         assertEquals(2, board.getHighlightLetter().down);
         board.moveUp(false);

         System.out.println("ON: "+board.getBoxes()[board.getHighlightLetter().across][board.getHighlightLetter().down].getSolution());
         assertEquals(2, board.getHighlightLetter().down);
         board.moveUp(false);


         System.out.println("----------");
         board.setHighlightLetter(new Position(4,4));

         System.out.println("ON: "+board.getBoxes()[board.getHighlightLetter().across][board.getHighlightLetter().down].getSolution());
         assertEquals(4, board.getHighlightLetter().down);
         board.moveUp(false);

         System.out.println("ON: "+board.getBoxes()[board.getHighlightLetter().across][board.getHighlightLetter().down].getSolution());
         assertEquals(2, board.getHighlightLetter().down);
         board.moveUp(false);

         System.out.println("ON: "+board.getBoxes()[board.getHighlightLetter().across][board.getHighlightLetter().down].getSolution());
         assertEquals(1, board.getHighlightLetter().down);
         board.moveUp(false);

         System.out.println("ON: "+board.getBoxes()[board.getHighlightLetter().across][board.getHighlightLetter().down].getSolution());
         assertEquals(0, board.getHighlightLetter().down);
         board.moveUp(false);

         System.out.println("ON: "+board.getBoxes()[board.getHighlightLetter().across][board.getHighlightLetter().down].getSolution());
         assertEquals(0, board.getHighlightLetter().down);
         board.moveUp(false);

         System.out.println("ON: "+board.getBoxes()[board.getHighlightLetter().across][board.getHighlightLetter().down].getSolution());
         assertEquals(0, board.getHighlightLetter().down);
         board.moveUp(false);

        

    }

}
