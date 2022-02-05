package com.chateasy.android.app;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import com.chateasy.android.db.base.BaseManager;

/**
 * Created by Administrator on 2022/1/23.
 */

public class ChatApplication extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        BaseManager.initOpenHelper(this);
    }
}
