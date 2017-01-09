package com.totsp.crossword.view.recycler;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.totsp.crossword.shortyz.R;

public class ShowHideOnScroll extends ScrollDetector implements Animation.AnimationListener {
    private final View view;
    private final int showAnimationId;
    private final int hideAnimationId;

    public ShowHideOnScroll(View view) {
        this(view, R.anim.float_show, R.anim.float_hide);
    }

    public ShowHideOnScroll(View view, int animShow, int animHide) {
        super(view.getContext());
        this.view = view;
        this.showAnimationId = animShow;
        this.hideAnimationId = animHide;
    }

    public void onScrollDown() {
        if(this.view.getVisibility() != View.VISIBLE) {
            this.view.setVisibility(View.VISIBLE);
            this.animate(this.showAnimationId);
        }

    }

    public void onScrollUp() {
        if(this.view.getVisibility() == View.VISIBLE) {
            this.view.setVisibility(View.GONE);
            this.animate(this.hideAnimationId);
        }

    }

    private void animate(int anim) {
        if(anim != 0) {
            Animation a = AnimationUtils.loadAnimation(this.view.getContext(), anim);
            a.setAnimationListener(this);
            this.view.startAnimation(a);
            this.setIgnore(true);
        }

    }

    public void onAnimationStart(Animation animation) {
    }

    public void onAnimationEnd(Animation animation) {
        this.setIgnore(false);
    }

    public void onAnimationRepeat(Animation animation) {
    }
}
