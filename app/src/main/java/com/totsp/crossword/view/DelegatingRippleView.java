package com.totsp.crossword.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.andexert.library.RippleView;

/**
 * Created by rcooper on 11/24/14.
 */
public class DelegatingRippleView extends RippleView {


    public DelegatingRippleView(Context context) {
        super(context);

    }

    public DelegatingRippleView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public DelegatingRippleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event){
        return true;
    }
}
