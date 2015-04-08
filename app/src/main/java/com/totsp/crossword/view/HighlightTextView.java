package com.totsp.crossword.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.TextView;

import com.totsp.crossword.shortyz.R;


public class HighlightTextView extends TextView {
    Paint blackPaint = new Paint();
    Paint highlight = new Paint();
    private DisplayMetrics metrics;

    public HighlightTextView(Context context, AttributeSet as) {
        super(context, as);
        metrics = new DisplayMetrics();

        WindowManager manager = (WindowManager) this.getContext()
                                                    .getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay()
               .getMetrics(metrics);
        blackPaint.setColor(context.getResources().getColor(android.support.v7.appcompat.R.color.primary_material_light));
        blackPaint.setAntiAlias(true);
        blackPaint.setStyle(Style.FILL_AND_STROKE);
        highlight.setColor(context.getResources().getColor(R.color.primary_dark));
        highlight.setStyle(Style.FILL_AND_STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int height = this.getHeight();
        int width = this.getWidth();
//        int center = height / 2;
//        int topBreak = center - (int) (metrics.density * 10F);
//        int bottomBreak = center + (int) (metrics.density * 10F);
//        Path p = new Path();
//        p.moveTo(width, 0F);
//        p.lineTo(width, topBreak);
//        p.lineTo(width - (metrics.density * 10), center);
//        p.lineTo(width, bottomBreak);
//        p.lineTo(width, height);
//        p.lineTo(width + 1, height);
//        p.lineTo(width + 1, -1);
//        p.lineTo(width, 0);

        canvas.drawRect(0, 0, width, height, highlight);
        //canvas.drawPath(p, blackPaint);
        super.onDraw(canvas);
    }
}
