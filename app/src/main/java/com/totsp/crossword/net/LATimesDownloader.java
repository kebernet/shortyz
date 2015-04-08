package com.totsp.crossword.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;

public class LATimesDownloader extends AbstractJPZDownloader {

	private static final String NAME = "Los Angeles Times";
	private final HashMap<String, String> headers = new HashMap<String, String>();
	NumberFormat nf = NumberFormat.getInstance();

	public LATimesDownloader() {
		super(
				"http://cdn.games.arkadiumhosted.com/latimes/assets/DailyCrossword/",
				DOWNLOAD_DIR, NAME);
		nf.setMinimumIntegerDigits(2);
		nf.setMaximumFractionDigits(0);
		headers.put("Accept","*/*");
		headers.put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
		headers.put("Accept-Language", "en-US,en;q=0.8");
		headers.put("Connection", "keep-alive");
		headers.put("Host", "cdn.games.arkadiumhosted.com");
		headers.put(
				"Referer",
				"http://cdn.games.arkadiumhosted.com/latimes/games/daily-crossword/game/crossword-expert.swf");
		headers.put("Content-Length", "0");
		
	}

	public int[] getDownloadDates() {
		return LATimesDownloader.DATE_DAILY;

	}

	public String getName() {
		return NAME;
	}

	public File download(Date date) {
		return download(date, this.createUrlSuffix(date), headers);
	}

	@Override
	protected String createUrlSuffix(Date date) {
        String val = "la";
        if(date.before(new Date(114, 0, 27))){
            val = "puzzle_";
        }
		return val + (date.getYear() - 100)
				+ nf.format(date.getMonth() + 1) + nf.format(date.getDate())
				+ ".xml";
	}

}
