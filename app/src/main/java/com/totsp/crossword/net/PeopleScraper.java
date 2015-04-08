package com.totsp.crossword.net;

public class PeopleScraper extends AbstractPageScraper {

	
	public PeopleScraper(){
		super("http://www.people.com/people/puzzler/", "People Magazine");
		this.updateable = true;
	}
}
