package com.totsp.crossword.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by rcooper on 10/26/14.
 */
public class CircleProgressBar extends View {
    private static Typeface icons1;
    private static Typeface icons4;
    private static final int GRAY = Color.rgb(180, 180, 180);
    private static final int ORANGE = Color.rgb(213, 165, 24);
    private static final int GREEN = Color.rgb(49, 145, 90);
    private static final int RED = Color.rgb(255, 74, 77);
    private int height;
    private int width;
    private int percentComplete;
    private DisplayMetrics metrics;
    private float circleStroke;
    private float circleFine;


    public CircleProgressBar(Context context) {
        super(context);
        initMetrics(context);

    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMetrics(context);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initMetrics(context);
    }

    private final void initMetrics(Context context){
        metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        circleStroke = metrics.density * 6F;
        circleFine = metrics.density * 2f;
        if(icons1 == null) {
            icons1 = Typeface.createFromAsset(context.getAssets(), "icons1.ttf");
            icons4 = Typeface.createFromAsset(context.getAssets(), "icons4.ttf");
        }
    }

    public void setPercentComplete(int percentComplete) {
        this.percentComplete = percentComplete;
        this.invalidate();
    }

    public int getPercentComplete() {
        return percentComplete;
    }

    @Override
    protected void onMeasure(int widthSpecId, int heightSpecId) {
        this.height = View.MeasureSpec.getSize(heightSpecId);
        this.width = View.MeasureSpec.getSize(widthSpecId);
        setMeasuredDimension(this.width, this.height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float halfWidth = width / 2;
        float halfHeight = height / 2;
        float halfStroke = circleStroke / 2;
        float textSize = halfWidth * 0.75f;


        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(GRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTypeface(Typeface.SANS_SERIF);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(textSize);

        //System.out.println("Draw "+this.width + " " +this.height);
        if (this.percentComplete < 0) {
            paint.setColor(RED);
            paint.setStrokeWidth(circleStroke);
            canvas.drawCircle(halfWidth, halfWidth, halfWidth - halfStroke - metrics.density * 2f, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText("?", halfWidth, halfWidth + textSize / 3f, paint);
        } else if (this.percentComplete == 0) {
//            paint.setStrokeWidth(circleFine);
//            canvas.drawCircle(halfWidth, halfHeight, halfWidth - metrics.density * 4f, paint);
            paint.setTypeface(icons4);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText("W", halfWidth, halfWidth + textSize / 2.5f, paint);
        } else if (this.percentComplete == 100) {
            paint.setColor(GREEN);
            paint.setStrokeWidth(circleStroke);
            canvas.drawCircle(halfWidth, halfWidth, halfWidth - halfStroke - metrics.density * 2f, paint);
            paint.setTypeface(icons1);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText("4", halfWidth, halfHeight + textSize / 2f, paint);
        } else {
            paint.setColor(ORANGE);
            paint.setStrokeWidth(circleFine);
            canvas.drawCircle(halfWidth, halfWidth, halfWidth - halfStroke - 1f, paint);
            paint.setStrokeWidth(circleStroke);

            RectF rect = new RectF(0 + circleStroke ,0 + circleStroke ,
                    width - circleStroke , width - circleStroke);
            canvas.drawArc(rect, -90,  360F * percentComplete / 100F, false, paint);
            paint.setStyle(Paint.Style.FILL);
            textSize = halfWidth * 0.5f;
            paint.setTextSize(textSize);
            canvas.drawText(percentComplete+"%", halfWidth, halfHeight + textSize / 3f, paint);
        }


    }
}
