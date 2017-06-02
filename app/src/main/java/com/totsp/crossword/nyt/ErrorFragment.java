package com.totsp.crossword.nyt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.totsp.crossword.firstrun.SlideFragment;
import com.totsp.crossword.shortyz.R;

/**
 * Created by rcooper on 6/2/17.
 */

public class ErrorFragment extends SlideFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(thisView != null){
            return thisView;
        }
        View v = inflater.inflate(R.layout.slide_nytimes_error, container, false);
        initView(v);
        return v;
    }
}
