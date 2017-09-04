package com.totsp.crossword.gmail;

import com.google.api.services.gmail.GmailScopes;

/**
 * Created by rcooper on 6/28/15.
 */
public interface GMConstants {

    int REQUEST_ACCOUNT_PICKER = 1000;
    int REQUEST_AUTHORIZATION = 1001;
    int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    String PREF_ACCOUNT_NAME = "gmail.accountName";
    String[] SCOPES = { GmailScopes.GMAIL_READONLY };
}
