package com.chateasy.android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2022/1/21.
 */

public class ScreenUtil {
    private static int screenWidth = 0;
    private static int screenHeight = 0;
    private static int screenTotalHeight = 0;
    private static int statusBarHeight = 0;

    private static final int TITLE_HEIGHT = 0;

    public static int dip2px(Context context, float dpValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int getScreenWidth(Context context){
        if(screenWidth != 0){
            return screenWidth;
        }
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        return screenWidth;
    }

    public static int getScreenHeight(Context context){
        if(screenHeight != 0){
            return screenHeight;
        }
        int top = 0;
        if(context instanceof Activity){
            top = ((Activity) context).getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
            if(top == 0){
                top = (int) (TITLE_HEIGHT * getScreenDensity(context));
            }
        }
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        screenHeight = dm.heightPixels - top;
        return screenHeight;
    }

    public static int getScreenTotalHeight(Context context){
        if(screenTotalHeight != 0){
            return screenTotalHeight;
        }
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        screenTotalHeight = displayMetrics.heightPixels;
        return screenTotalHeight;
    }

    public static float getScreenDensity(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);
        return metric.density;
    }

    public static float getScreenDensityDpi(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);
        return metric.densityDpi;
    }

    public static int getStatusBarHeight(Context context){
        if(statusBarHeight != 0){
            return statusBarHeight;
        }
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0;
        try{
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        }catch(Exception e1){
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    public static boolean isTablet(Context context){
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static int getDpi(Context context){
        int dpi = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Class c;
        try{
            c = Class.forName("android.view.Display");
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, displayMetrics);
            dpi = displayMetrics.heightPixels;
        }catch (Exception e){
            e.printStackTrace();
        }
        return dpi;
    }

    public static int getBottomStatusHeight(Context context){
        int totalHeight = getDpi(context);
        int contentHeight = getScreenHeight(context);
        return totalHeight - contentHeight;
    }

    public static int getTytleHeight(Activity activity){
        return activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
    }

    public static int getStatusHeight(Context context){
        int statusHeight = -1;
        try{
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        }catch(Exception e){
            e.printStackTrace();
        }
        return statusHeight;
    }

    public static int getNavigationBarHeight(Context context){
        int result = 0;
        if(hasNavBar(context)){
            Resources res = context.getResources();
            int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
            if(resourceId > 0){
                result = res.getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    public static boolean hasNavBar(Context context){
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
        if(resourceId != 0){
            boolean hasNav = res.getBoolean(resourceId);
            String sNavBarOverride = getNavBarOverride();
            if("1".equals(sNavBarOverride)){
                hasNav = false;
            }else if("0".equals(sNavBarOverride)){
                hasNav = true;
            }
            return hasNav;
        }else{
            return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }
    }

    public static String getNavBarOverride(){
        String sNavBarOverride = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try{
                Class c = Class.forName("android.os.SystemProperties");
                Method m = c.getDeclaredMethod("get", String.class);
                m.setAccessible(true);
                sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
            }catch (Throwable e){
            }
        }
        return sNavBarOverride;
    }
}
