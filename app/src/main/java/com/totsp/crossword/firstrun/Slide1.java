package com.totsp.crossword.firstrun;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.totsp.crossword.shortyz.R;

/**
 *
 * Created by rcooper on 6/27/15.
 */
public class Slide1 extends SlideFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        System.out.println("onCreateView");
        if(thisView != null){
            return thisView;
        }
        View v = inflater.inflate(R.layout.slide1, container, false);
        initView(v);
        return v;
    }
}
