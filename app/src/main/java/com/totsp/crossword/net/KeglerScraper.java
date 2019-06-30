package com.totsp.crossword.net;


// http://www.lafn.org/~keglerron/Block_style/index.html
// replaced by https://kegler.gitlab.io/Block_style/
// because lafn.org is gone
public class KeglerScraper extends AbstractPageScraper {
    public KeglerScraper() {
        super("https://kegler.gitlab.io/Block_style/", "Kegler's Kryptics");
    }
}
