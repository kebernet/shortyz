package com.totsp.crossword.io;

import com.totsp.crossword.puz.Playboard;
import com.totsp.crossword.puz.Puzzle;
import junit.framework.TestCase;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created with IntelliJ IDEA.
 * User: keber_000
 * Date: 2/9/14
 * Time: 6:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class BrainsOnlyIOTest  extends TestCase {
    public void testParse() throws Exception {

        ClassLoader cl =BrainsOnlyIOTest.class.getClassLoader();

        URL[] urls = ((URLClassLoader)cl).getURLs();

        for(URL url: urls){
            System.out.println(url.getFile());
        }
        Puzzle puz = BrainsOnlyIO.parse(BrainsOnlyIOTest.class.getResourceAsStream("/brainsonly.txt"));
        assertEquals("SODA SPEAK", puz.getTitle());
        assertEquals("S.N. & Robert Francis, edited by Stanley Newman", puz.getAuthor());
        assertEquals(15, puz.getBoxes().length);
        assertEquals(15, puz.getBoxes()[0].length);
        assertEquals(1, puz.getBoxes()[0][0].getClueNumber());
        assertEquals(true, puz.getBoxes()[0][0].isAcross());
        assertEquals(true, puz.getBoxes()[0][0].isDown());
        assertEquals(false, puz.getBoxes()[0][3].isAcross());
        assertEquals("Toss out", puz.getAcrossClues()[0]);
        assertEquals("Sancho Panza's mount", puz.getDownClues()[0]);
        assertEquals("Straighten out", puz.findAcrossClue(41));

    }

    public void testParse2() throws Exception {

        Puzzle puz = BrainsOnlyIO.parse(BrainsOnlyIOTest.class.getResourceAsStream("/brainsonly2.txt"));
        assertEquals("OCCUPIED NATIONS: Surrounding the long answers", puz.getTitle());
        System.out.println("Across clue 15 "+ puz.findAcrossClue(15) );
        assertEquals("Elevator guy", puz.findAcrossClue(15));
        System.out.println("5 across "+puz.findAcrossClue(5));
        assertEquals("Company with a duck mascot", puz.findAcrossClue(5));

    }
}
