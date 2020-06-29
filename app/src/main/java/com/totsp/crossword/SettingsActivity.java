package com.totsp.crossword;

import android.os.Bundle;

import com.totsp.crossword.shortyz.R;


public class SettingsActivity extends ShortyzActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        utils.holographic(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        utils.finishOnHomeButton(this);
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        }
        else {
            getSupportFragmentManager().popBackStack();
        }
    }


}
