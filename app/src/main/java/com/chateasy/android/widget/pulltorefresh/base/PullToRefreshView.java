package com.chateasy.android.widget.pulltorefresh.base;

import android.content.Context;
import android.view.View;

import com.chateasy.android.widget.pulltorefresh.PullToRefreshListView;

/**
 * Created by Administrator on 2022/1/29.
 */

public class PullToRefreshView extends View {
    public static final int LISTVIEW = 0;
    public static final int RECYCLERVIEW = 1;

    public PullToRefreshView(Context context){
        super(context);
    }

    public View getSlideView(int slideViewType){
        View baseView = null;
        switch(slideViewType){
            case LISTVIEW:
                baseView = new PullToRefreshListView(getContext());
                break;
            case RECYCLERVIEW:
                break;
            default:
                baseView = null;
                break;
        }
        return baseView;
    }
}
