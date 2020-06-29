package com.totsp.crossword.shortyz.test;
;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.totsp.crossword.net.AbstractPageScraper;

import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class PageScraperTest  {

	@Test
	public void testParse() throws Exception {

		String testString = "<a href=\"http://code.google.com/some/test/puzzle.puz\"> test puzzle</a>\n<a href=\"http://code.google.com/some/test/puzzle2.puz\"> test2 puzzle</a>";
		System.out.println("Running...");
		System.out.println(AbstractPageScraper.puzzleURLs(testString));

	}

	@Test
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
