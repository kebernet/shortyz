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
import com.totsp.crossword.io.UclickXMLIO;

/**
 * Tests for UclickXMLIO.
 */
public class UclickXMLIOTest extends TestCase {
	private static final String TITLE = "12/15/09 LET'S BE HONEST";
	private static final String AUTHOR = "by Billie Truitt, edited by Stanley Newman";
	private static final Date DATE;
	private static final String COPYRIGHT = "Stanley Newman, distributed by Creators Syndicate, Inc.";

	static {
		Calendar c = Calendar.getInstance();
		c.set(2009, 11, 15);
		DATE = c.getTime();
	}

	private InputStream is;
	private DataOutputStream os;
	private File tmp;

	public UclickXMLIOTest(String testName) {
		super(testName);
	}

	public void setUp() throws Exception {
		tmp = File.createTempFile("uclick-test", ".puz");
		os = new DataOutputStream(new FileOutputStream(tmp));
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		is.close();
		os.close();
		tmp.delete();
	}

	public void testConvert1() throws IOException {
		is = UclickXMLIOTest.class.getResourceAsStream("/crnet091215-data.xml");
		assertNotNull(is);

		assertTrue(UclickXMLIO.convertUclickPuzzle(is, os, COPYRIGHT, DATE));
		Puzzle puz = IO.load(tmp);


		assertEquals(TITLE, puz.getTitle());
		assertEquals(AUTHOR, puz.getAuthor());
		assertEquals(COPYRIGHT, puz.getCopyright());

		assertEquals("Film legend Greta", puz.findAcrossClue(1));
		assertEquals("Ballerina's skirt", puz.findAcrossClue(49));
		assertEquals("Equips for combat", puz.findAcrossClue(60));
		assertEquals("Double curves", puz.findAcrossClue(65));

		assertEquals("Squash or pumpkin", puz.findDownClue(1));
		assertEquals("Toss in", puz.findDownClue(21));
		assertEquals("Bullfight shouts", puz.findDownClue(56));
	}

	public void testConvert2() throws IOException {
		is = UclickXMLIOTest.class.getResourceAsStream("/fcx170416-data.xml");
		assertNotNull(is);

		assertTrue(UclickXMLIO.convertUclickPuzzle(is, os, COPYRIGHT, DATE));
		Puzzle puz = IO.load(tmp);

		assertEquals("SAY IT, DON'T FILET IT", puz.getTitle());
		assertEquals("Hollywood's Brad", puz.findAcrossClue(1));
		assertEquals("Like a \"breaky heart\" of song", puz.findAcrossClue(13));
	}
}