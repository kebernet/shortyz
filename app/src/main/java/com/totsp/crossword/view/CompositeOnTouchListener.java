package com.totsp.crossword.view;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by rcooper on 11/24/14.
 */
public class CompositeOnTouchListener implements View.OnTouchListener {

    private final View.OnTouchListener first;
    private final View.OnTouchListener second;

    public CompositeOnTouchListener(View.OnTouchListener first, View.OnTouchListener second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return first.onTouch(v, event) | second.onTouch(v, event);
    }
}
