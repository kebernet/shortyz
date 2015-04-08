package com.totsp.crossword.view;

import java.util.Timer;
import java.util.logging.Logger;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;


@SuppressWarnings("deprecation")
public class ScrollingImageView extends FrameLayout implements OnGestureListener {
    private static final Logger LOG = Logger.getLogger("com.totsp.crossword");
    private AuxTouchHandler aux = null;
    private ClickListener ctxListener;
    private GestureDetector gestureDetector;
    private ImageView imageView;
    private ScaleListener scaleListener = null;
    private ScrollLocation scaleScrollLocation;
    private Timer longTouchTimer = new Timer();
    private boolean longTouched;
    private float maxScale = 2.5f;
    private float minScale = 0.25f;
    private float runningScale = 1.0f;
    private boolean haveAdded = false;
    public ScrollingImageView(Context context, AttributeSet as) {
        super(context, as);
        gestureDetector = new GestureDetector(this);
        gestureDetector.setIsLongpressEnabled(true);
        imageView = new ImageView(context);
        

        if (android.os.Build.VERSION.SDK_INT >= 8) {
            try {
                aux = (AuxTouchHandler) Class.forName("com.totsp.crossword.view.MultitouchHandler")
                                             .newInstance();
                aux.init(this);
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void setBitmap(Bitmap bitmap) {
        this.setBitmap(bitmap, true);
    }

    public void setBitmap(Bitmap bitmap, boolean rescale) {
        if (bitmap == null) {
            return;
        }

        LOG.finest("New Bitmap Size: " + bitmap.getWidth() + " x " + bitmap.getHeight());

        if (rescale){
//            if (imageView != null) {
//                this.removeView(imageView);
//            }
//            
            
        	FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
            imageView.setImageBitmap(bitmap);
            if(!haveAdded){
	            this.addView(imageView, params);
	            haveAdded = true;
            } else {
            	imageView.setLayoutParams(params);
            }
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    public void setContextMenuListener(ClickListener l) {
        this.ctxListener = l;
    }

    public ImageView getImageView() {
        return this.imageView;
    }

    public void setMaxScale(float maxScale) {
        this.maxScale = maxScale;
    }

    public float getMaxScale() {
        return maxScale;
    }

    public int getMaxScrollX() {
        return imageView.getWidth() - this.getWidth();
    }

    public int getMaxScrollY() {
        return imageView.getHeight() - this.getHeight();
    }

    public void setMinScale(float minScale) {
        this.minScale = minScale;
    }

    public float getMinScale() {
        return minScale;
    }

    public void setScaleListener(ScaleListener scaleListener) {
        this.scaleListener = scaleListener;
    }
    
    private float currentScale = 1.0f;
    
    public void setCurrentScale(float scale){
    	this.currentScale = scale;
    }

    public boolean isVisible(Point p) {
        int currentMinX = this.getScrollX();
        int currentMaxX = this.getWidth() + this.getScrollX();
        int currentMinY = this.getScrollY();
        int currentMaxY = this.getHeight() + this.getScrollY();

        return (p.x >= currentMinX) && (p.x <= currentMaxX) && (p.y >= currentMinY) && (p.y <= currentMaxY);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if ((aux != null) && aux.onTouchEvent(ev)) {
            return true;
        }

        gestureDetector.onTouchEvent(ev);

        return true;
    }

    public void ensureVisible(Point p) {
        int maxScrollX = this.getMaxScrollX();
        int x = p.x;
        int maxScrollY = this.getMaxScrollY();
        ;
        
        

        int y = p.y;

        int currentMinX = this.getScrollX();
        int currentMaxX = this.getWidth() + this.getScrollX();
        int currentMinY = this.getScrollY();
        int currentMaxY = this.getHeight() + this.getScrollY();

        LOG.info("X range " + currentMinX + " to " + currentMaxX);
        LOG.info("Desired X:" + x);
        LOG.info("Y range " + currentMinY + " to " + currentMaxY);
        LOG.info("Desired Y:" + y);

        if ((x < currentMinX) || (x > currentMaxX)) {
            this.scrollTo((x > maxScrollX) ? maxScrollX : (x), this.getScrollY());
        }

        if ((y < currentMinY) || (y > currentMaxY)) {
            LOG.info("Y adjust " + (y > maxScrollY ? maxScrollY : (y)));
            this.scrollTo(this.getScrollX(), (y > maxScrollY) ? maxScrollY : (y));
        }
    }

    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public void onLongPress(MotionEvent e) {
        if ((aux != null) && aux.inProgress()) {
            return;
        }

        final Point p = this.resolveToImagePoint(e.getX(), e.getY());

        if (ScrollingImageView.this.ctxListener != null) {
            ScrollingImageView.this.ctxListener.onContextMenu(p);
            ScrollingImageView.this.longTouched = true;
        }
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        this.longTouchTimer.cancel();
        this.longTouched = false;

        int scrollWidth = imageView.getWidth() - this.getWidth();

        if ((imageView.getWidth()) > this.getWidth()) {
            if ((this.getScrollX() >= 0) && (this.getScrollX() <= scrollWidth) && (scrollWidth > 0)) {
                int moveX = (int) distanceX;

                if (((moveX + this.getScrollX()) >= 0) &&
                        ((Math.abs(moveX) + Math.abs(this.getScrollX())) <= scrollWidth)) {
                    this.scrollBy(moveX, 0);
                } else {
                    if (distanceX >= 0) {
                        int xScroll = scrollWidth - Math.max(Math.abs(moveX), Math.abs(this.getScrollX()));
                        this.scrollBy(xScroll, 0);
                    } else {
                        this.scrollBy(-Math.min(Math.abs(moveX), Math.abs(this.getScrollX())), 0);
                    }
                }
            }
        }

        int scrollHeight = imageView.getHeight() - this.getHeight();

        if ((imageView.getHeight()) > this.getHeight()) {
            if ((this.getScrollY() >= 0) && (this.getScrollY() <= scrollHeight) && (scrollHeight > 0)) {
                int moveY = (int) distanceY;

                if (((moveY + this.getScrollY()) >= 0) &&
                        ((Math.abs(moveY) + Math.abs(this.getScrollY())) <= scrollHeight)) {
                    this.scrollBy(0, moveY);
                } else {
                    if (distanceY >= 0) {
                        this.scrollBy(0, scrollHeight - Math.max(Math.abs(moveY), Math.abs(this.getScrollY())));
                    } else {
                        this.scrollBy(0, -Math.min(Math.abs(moveY), Math.abs(this.getScrollY())));
                    }
                }
            }
        }

        return true;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        Point p = this.resolveToImagePoint(e.getX(), e.getY());
        this.longTouchTimer.cancel();

        if (this.longTouched == true) {
            this.longTouched = false;
        } else {
            if (this.ctxListener != null) {
                this.ctxListener.onTap(p);
            }
        }

        return true;
    }

    public Point resolveToImagePoint(float x, float y) {
        return this.resolveToImagePoint((int) x, (int) y);
    }

    public Point resolveToImagePoint(int x, int y) {
        return new Point(x + this.getScrollX(), y + this.getScrollY());
    }

    public void scrollBy(int x, int y) {
        int scrollWidth = imageView.getWidth() - this.getWidth();
        int scrollHeight = imageView.getHeight() - this.getHeight();

        if ((this.getScrollX() + x) < 0) {
            x = 0;
        } else if ((this.getScrollX() + x) > scrollWidth) {
            x = scrollWidth;
        }

        if ((this.getScrollY() + y) < 0) {
            y = 0;
        } else if ((this.getScrollY() + y) > scrollHeight) {
            y = scrollHeight;
        }

        //System.out.println("scrollBy(" + x + "," + y + ")");
        super.scrollTo(this.getScrollX() + x, this.getScrollY() + y);

        if (this.getScrollX() < 0) {
            this.scrollTo(0, this.getScrollY());
        } else if (this.getScrollX() > (this.imageView.getWidth() - this.getWidth())) {
            this.scrollTo(this.imageView.getWidth() - this.getWidth(), this.getScrollY());
        }

        if (this.getScrollY() < 0) {
            this.scrollTo(this.getScrollX(), 0);
        } else if (this.getScrollY() > (this.imageView.getHeight() - this.getHeight())) {
            this.scrollTo(this.getScrollX(), this.imageView.getHeight() - this.getHeight());
        }
    }

    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);

        //    	if(x ==0 && y ==0 ){
        //    		try{
        //    			throw new RuntimeException();
        //    		} catch(Exception e){
        //    			e.printStackTrace();
        //    		}
        //    	}
    }

    public void zoom(float scale, int x, int y) {
        if (this.scaleScrollLocation == null) {
            this.scaleScrollLocation = new ScrollLocation(this.resolveToImagePoint(x, y), this.imageView);
        }

        if ((runningScale * scale) < minScale) {
            scale = 1.0F;
        }

        if ((runningScale * scale) > maxScale) {
            scale = 1.0F;
        }

        if(scale * this.currentScale > 2.5 ){
        	return;
        } 
        if(scale * this.currentScale < .5){
        	return;
        }
        int h = imageView.getHeight();
        int w = imageView.getWidth();
        h *= scale;
        w *= scale;
        runningScale *= scale;
        currentScale *= scale;
        this.removeView(imageView);
        this.addView(imageView, new FrameLayout.LayoutParams(w,h));
        this.scaleScrollLocation.fixScroll(w, h, false);
    }

    public void zoomEnd() {
        if ((this.scaleListener != null) && (this.scaleScrollLocation != null)) {
            scaleListener.onScale(runningScale,
                this.scaleScrollLocation.findNewPoint(imageView.getWidth(), imageView.getHeight()));
            this.scaleScrollLocation.fixScroll(imageView.getWidth(), imageView.getHeight(), true);

            Point origin = this.resolveToImagePoint(0, 0);

            if (origin.x < 0) {
                this.scrollTo(0, this.getScrollY());
            }

            if (origin.y < 0) {
                this.scrollBy(this.getScrollX(), 0);
            }
        }

        this.scaleScrollLocation = null;
        runningScale = 1.0f;
    }

    public static interface AuxTouchHandler {
        boolean inProgress();

        void init(ScrollingImageView view);

        boolean onTouchEvent(MotionEvent ev);
    }

    public static interface ClickListener {
        public void onContextMenu(Point e);

        public void onTap(Point e);
    }

    public static interface ScaleListener {
        void onScale(float scale, Point center);
    }

    public static class Point {
        int x;
        int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Point() {
        }

        public int distance(Point p) {
            double d = Math.sqrt(((double) this.x - (double) p.x) + ((double) this.y - (double) p.y));

            return (int) Math.round(d);
        }
        
        @Override
        public String toString(){
        	return "["+x+", "+y+"]";
        }
    }

    private class ScrollLocation {
        private final double percentAcrossImage;
        private final double percentDownImage;
        private final int absoluteX;
        private final int absoluteY;

        public ScrollLocation(Point p, ImageView imageView) {
            this.percentAcrossImage = (double) p.x / (double) imageView.getWidth();
            this.percentDownImage = (double) p.y / (double) imageView.getHeight();
            this.absoluteX = p.x - ScrollingImageView.this.getScrollX();
            this.absoluteY = p.y - ScrollingImageView.this.getScrollY();
        }

        public Point findNewPoint(int newWidth, int newHeight) {
            int newX = (int) Math.round((double) newWidth * this.percentAcrossImage);
            int newY = (int) Math.round((double) newHeight * this.percentDownImage);

            return new Point(newX, newY);
        }

        public void fixScroll(int newWidth, int newHeight, boolean snap) {
            Point newPoint = this.findNewPoint(newWidth, newHeight);

            int newScrollX = newPoint.x - this.absoluteX;
            int newScrollY = newPoint.y - this.absoluteY;

            int maxX = ScrollingImageView.this.getMaxScrollX();
            int maxY = ScrollingImageView.this.getMaxScrollY();

            if (snap && (newScrollX > maxX)) {
                newScrollX = maxX;
            }

            if (snap && (newScrollY > maxY)) {
                newScrollY = maxY;
            }

            ScrollingImageView.this.scrollTo(newScrollX, newScrollY);
        }
    }
}
