package com.totsp.crossword.shortyz.test;

import android.test.AndroidTestCase;

import com.totsp.crossword.net.AbstractPageScraper;

public class PageScraperTest extends AndroidTestCase{

	public void testParse() throws Exception {

		String testString = "<a href=\"http://code.google.com/some/test/puzzle.puz\"> test puzzle</a>\n<a href=\"http://code.google.com/some/test/puzzle2.puz\"> test2 puzzle</a>";
		System.out.println("Running...");
		System.out.println(AbstractPageScraper.puzzleURLs(testString));

	}
	
	public void testBEQ() throws Exception {
		
		AbstractPageScraper scraper = new TestScraper();
		
		String payload = scraper.getContent();
		System.out.println(payload);
		
		System.out.println(scraper.puzzleURLs(payload));
		
		
		
	}
	
	
	private static class TestScraper extends AbstractPageScraper {
		
		TestScraper(){
			super("http://www.fleetwoodwack.typepad.com/", "BEQ");
		}
	}

}
