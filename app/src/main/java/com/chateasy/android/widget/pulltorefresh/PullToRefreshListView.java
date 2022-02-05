package com.chateasy.android.widget.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

/**
 * Created by Administrator on 2022/1/28.
 */

public class PullToRefreshListView extends ListView {
    boolean allowDragBottom = true;
    float downY = 0;
    boolean needConsumeTouch = true;

    public PullToRefreshListView(Context context){
        super(context);
        setDivider(null);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        if(ev.getAction() == MotionEvent.ACTION_DOWN){
            downY = ev.getY();
            needConsumeTouch = true;
            if(getScrollY() == 0){
                allowDragBottom = true;
            }else {
                allowDragBottom = false;
            }
        }else if(ev.getAction() == MotionEvent.ACTION_MOVE){
            if(!needConsumeTouch){
                getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }else if(allowDragBottom){
                if(downY - ev.getRawY() < -2){
                    needConsumeTouch = false;
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                }
            }
        }
        getParent().requestDisallowInterceptTouchEvent(needConsumeTouch);
        return super.dispatchTouchEvent(ev);
    }

    public int getMyScrollY(){
        View c= getChildAt(0);
        if(c == null){
            return 0;
        }
        int firstVisiblePostion = getFirstVisiblePosition();
        int top = c.getTop();
        return -top + firstVisiblePostion * c.getHeight();
    }

}
