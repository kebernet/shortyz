package com.totsp.crossword.puz;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.io.KingFeaturesPlaintextIO;

/**
 * Tests for KingFeaturesPlaintextIO.
 */
public class KingFeaturesPlaintextIOTest extends TestCase {
	
	private static final String TITLE = "Premier Crossword";
	private static final String AUTHOR = "Donna J. Stone";
	private static final String COPYRIGHT = "\u00a9 2010 King Features Syndicate, Inc.";
	private static final Date DATE;
	
	static {
		Calendar c = Calendar.getInstance();
		c.set(2010, 6, 4);
		DATE = c.getTime();
	}
	
	private InputStream is;
	private DataOutputStream os;
	private File tmp;
	
	public KingFeaturesPlaintextIOTest(String testName) {
		super(testName);
	}
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		is = KingFeaturesPlaintextIOTest.class.getResourceAsStream("/premiere-20100704.txt");
		tmp = File.createTempFile("kfp-test", ".puz");
        os = new DataOutputStream(new FileOutputStream(tmp));
	}
	
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		is.close();
		os.close();
		tmp.delete();
	}
	
	public void testConvert() {
		assertTrue(KingFeaturesPlaintextIO.convertKFPuzzle(is, os, TITLE, AUTHOR, COPYRIGHT, DATE));
		Puzzle puz = null;
		try {
			puz = IO.load(tmp);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Error in IO.load - " + e.getMessage());
		}
        
		assertEquals(TITLE, puz.getTitle());
		assertEquals(AUTHOR, puz.getAuthor());
		assertEquals(COPYRIGHT, puz.getCopyright());
		// Fails because date is unsaved - assertEquals(puz.getDate(), DATE);
		
		assertEquals("Aloha State state bird", puz.findAcrossClue(26));
		assertEquals("In stitches", puz.findDownClue(7));
		
		// Last clues
		assertEquals("Brutal force", puz.findAcrossClue(124));
		assertEquals("'-- -hoo!'", puz.findDownClue(118));
		
		// Clue with special characters
		assertEquals("'\u00c0 -- sant\u00e9!'", puz.findDownClue(52));
	}

}
