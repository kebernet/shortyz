package com.totsp.crossword.view.recycler;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
    public static interface OnItemClickListener {
        public void onItemClick(View view, int position);
        public void onItemLongClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;
    private GestureDetector gestureDetector;

    public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
        onItemClickListener = listener;

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent e)
            {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e){
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());

                if(childView != null && onItemClickListener != null){
                    onItemClickListener.onItemLongClick(childView, recyclerView.getChildPosition(childView));
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());

        if(childView != null && onItemClickListener != null && gestureDetector.onTouchEvent(e)){
            onItemClickListener.onItemClick(childView, view.getChildPosition(childView));
        }

        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent){}

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean b) {

    }
}