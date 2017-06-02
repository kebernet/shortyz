package com.totsp.crossword.nyt;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.totsp.crossword.firstrun.AppIntro;
import com.totsp.crossword.firstrun.NYTimes;

/**
 * Created by rcooper on 6/2/17.
 */

public class ErrorActivity extends AppIntro {

    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        addSlide(new ErrorFragment(), this);
        addSlide(new NYTimes(), this);
        setBarColor(Color.argb(100, 0, 0, 0));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("didNYTLogin", false)){
            finish();
        }
    }

    @Override
    public void onSkipPressed() {
        finish();
    }

    @Override
    public void onDonePressed() {
        finish();
    }
}
