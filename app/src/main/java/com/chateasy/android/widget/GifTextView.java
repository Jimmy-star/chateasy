package com.chateasy.android.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.EditText;

import com.chateasy.android.utils.FaceData;
import com.chateasy.android.utils.GifOpenHelper;
import com.chateasy.android.utils.ScreenUtil;
import com.chateasy.android.utils.ThreadPoolUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2022/1/24.
 */

public class GifTextView extends EditText {
    private static final int DELAYED = 300;
    private boolean isGif;
    public TextRunnable rTextRunnable;

    private class SpanInfo{
        ArrayList<Bitmap> mapList;
        @SuppressWarnings("unused")
        int start, end, frameCount, currentFrameIndex, delay;

        public SpanInfo(){
            mapList = new ArrayList<Bitmap>();
            start = end = frameCount = currentFrameIndex = delay = 0;
        }
    }

    private ArrayList<SpanInfo> spanInfoList = null; //用于处理一个TextView中出现多个要匹配的图片的情况
    private Handler handler; //用于处理从子线程TextView传来的消息
    private String myText; //存储TextView应该显示的文本
    private StartHandler mStartHandler = new StartHandler(this);

    public GifTextView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        GifTextView.this.setFocusableInTouchMode(false);
    }

    public GifTextView(Context context, AttributeSet attrs){
        super(context, attrs);
        GifTextView.this.setFocusableInTouchMode(false);
    }

    public GifTextView(Context context){
        super(context);
        GifTextView.this.setFocusableInTouchMode(false);
    }

    /**
     * 对要显示在TextView上的文本进行解析，看看是否文本中有需要与Gif或者静态图片匹配的文本
     * 若有，就调用parseGif对该文本对应的Gif图片进行解析或者调用parseBmp解析静态图片
     */
    private boolean parseText(String inputStr){
        myText = inputStr;
        Pattern mpattern = Pattern.compile("\\[[^\\]]+\\]");
        Matcher mMatcher = mpattern.matcher(inputStr);
        boolean hasGif = false;
        while(mMatcher.find()){
            String faseName = mMatcher.group();
            Integer faceId = null;
            if((faceId =FaceData.gifFaceInfo.get(faseName)) != null){
                if(isGif){
                    parseGif(faceId, mMatcher.start(), mMatcher.end());
                }else{
                    parseBmp(faceId, mMatcher.start(), mMatcher.end());
                }
            }
            hasGif = true;
        }
        return hasGif;
    }

    /**
     * 对静态图片进行解析，创建一个spanInfo对象，并将spanInfo对象添加到spanInfoList中
     */
    @SuppressWarnings("unused")
    private void parseBmp(int resourceId, int start, int end){
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), resourceId);
        ImageSpan imageSpan = new ImageSpan(getContext(), bitmap);
        SpanInfo spanInfo = new SpanInfo();
        spanInfo.currentFrameIndex = 0;
        spanInfo.frameCount = 1;
        spanInfo.start = start;
        spanInfo.end = end;
        spanInfo.delay = 100;
        spanInfo.mapList.add(bitmap);
        spanInfoList.add(spanInfo);
    }

    /**
     * 解析Gif图片（frameCount参数大于1）
     */
    private void parseGif(int resourceId, int start, int end){
        GifOpenHelper helper = new GifOpenHelper();
        helper.read(getContext().getResources().openRawResource(resourceId));
        SpanInfo spanInfo = new SpanInfo();
        spanInfo.currentFrameIndex = 0;
        spanInfo.frameCount = helper.getFrameCount();
        spanInfo.start = start;
        spanInfo.end = end;
        spanInfo.mapList.add(helper.getImage());
        for(int i =1;i < helper.getFrameCount();i++){
            spanInfo.mapList.add(helper.nextBitmap());
        }
        spanInfo.delay = helper.nextDelay();
        spanInfoList.add(spanInfo);
    }

    public void setSpanText(Handler handler, final String text, boolean isGif){
        this.handler = handler;
        this.isGif = isGif;
        spanInfoList = new ArrayList<SpanInfo>();
        if(parseText(text)){
            mStartHandler.sendEmptyMessage(0);
            if(parseMessage(this)){
                startPost();
            }
        }else {
            mStartHandler.sendEmptyMessage(1);
            setText(myText);
        }
    }

    public static class StartHandler extends Handler{
        private final WeakReference<GifTextView> mGifWeakPreference;
        public StartHandler(GifTextView gifTextView){
            mGifWeakPreference = new WeakReference<GifTextView>(gifTextView);
        }

        @Override
        public void handleMessage(Message message){
            GifTextView gifTextView = mGifWeakPreference.get();
            if(gifTextView != null){
                if(message.what == 0){
                    gifTextView.startPost();
                }else if(message.what == 1){
                    gifTextView.setText(gifTextView.myText);
                }
            }
        }
    }

    public boolean parseMessage(GifTextView gifTextView){
        if(gifTextView.myText != null && !gifTextView.myText.equals("")){
            SpannableString sb = new SpannableString("" + gifTextView.myText);
            int gifCount = 0;
            SpanInfo info = null;
            for(int i = 0;i < gifTextView.spanInfoList.size();i++){
                info = gifTextView.spanInfoList.get(i);
                if(info.mapList.size() > 1){
                    gifCount++;
                }
                Bitmap bitmap = info.mapList.get(info.currentFrameIndex);
                info.currentFrameIndex = (info.currentFrameIndex + 1) % (info.frameCount);
                //控制当前应该显示帧的序号
                int size = ScreenUtil.dip2px(gifTextView.getContext(), 30);
                if(gifCount != 0){
                    bitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);
                }else{
                    bitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);
                }
                ImageSpan imageSpan = new ImageSpan(gifTextView.getContext(), bitmap);
                if(info.end <= sb.length()){
                    sb.setSpan(imageSpan, info.start, info.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }else{
                    break;
                }
            }
            gifTextView.setText(sb);
            if(gifCount != 0){
                return true;
            }else{
                return false;
            }
        }
        return false;
    }

    public void startPost(){
        rTextRunnable = new TextRunnable(this);
        handler.post(rTextRunnable); //利用UI线程的Handler将r添加进消息队列中
    }

    public static final class TextRunnable implements Runnable{
        private final WeakReference<GifTextView> mWeakReference;
        public TextRunnable(GifTextView f){
            mWeakReference = new WeakReference<GifTextView>(f);
        }

        @Override
        public void run(){
            GifTextView gifTextView = mWeakReference.get();
            if(gifTextView != null){
                //节省内存
                if(gifTextView.parseMessage(gifTextView)){
                    gifTextView.handler.postDelayed(this, DELAYED);
                }
            }
        }
    }

}
