package com.totsp.crossword.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.util.logging.Logger;

/**
 * Created by rcooper on 1/19/17.
 */

public class IOService extends IntentService {
    private static final Logger LOGGER = Logger.getLogger(IOService.class.getSimpleName());
    public static final String EXTRA_OP = "operation";
    public static final String EXTRA_META = "metadata";
    public static final String EXTRA_PUZ = "puzzle";
    public static final String EXTRA_FILENAME = "filename";
    public static final int OP_PERSIST = 0;

    public IOService() {
        super(IOService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        int op = intent.getIntExtra(EXTRA_OP, -1);
        switch(op){
            case -1:
                LOGGER.warning("Received missing op code");
                return;
            case OP_PERSIST:

        }
    }
}
