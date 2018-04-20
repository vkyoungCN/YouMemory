package com.vkyoungcn.learningtools;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/*
* 配合复习时的Fragment使用，只有在输入了正确的单词后才能向下一fg滑动。
* */
public class HalfScrollableViewPager extends ViewPager {
    private static final String TAG = "HalfScrollableViewPager";
    private boolean scrollable;
    float initialXValue = 0;


    public HalfScrollableViewPager(Context context) {
        super(context);
        this.scrollable = false;//默认不可滑动
    }

    public HalfScrollableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.scrollable = false;
    }

    //可以向前一页滑动。
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        //if(ev.getOrientation() < 0){//【学习，实践证实一次滑动就产生很多个pointer】
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            initialXValue = event.getX();
        }else if(event.getAction()==MotionEvent.ACTION_MOVE){
            if(detectSwipeToLeft(event,initialXValue)){
                Log.i(TAG, "onInterceptTouchEvent: to left");
                return scrollable && super.onInterceptTouchEvent(event);
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //if(ev.getOrientation() < 0){//【学习，实践证实一次滑动就产生很多个pointer】
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            initialXValue = event.getX();
        }else if(event.getAction()==MotionEvent.ACTION_MOVE){
            if(detectSwipeToLeft(event,initialXValue)){
                Log.i(TAG, "onTouchEvent: to left");
                return scrollable && super.onInterceptTouchEvent(event);
            }
        }

        return super.onTouchEvent(event);
    }

    private boolean detectSwipeToLeft(MotionEvent event, float initialXValue) {
        boolean result = false;

        try {
            float diffX = event.getX() - initialXValue;
                if (diffX < 0) {
                    // swipe from right to left detected ie.SwipeLeft
                    result = true;
                }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
    }


    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }
}
