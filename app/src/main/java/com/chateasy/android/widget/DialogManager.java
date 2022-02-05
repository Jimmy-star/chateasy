package com.chateasy.android.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.chateasy.android.R;
import com.chateasy.android.utils.ScreenUtil;

/**
 * Created by Administrator on 2022/1/22.
 */

public class DialogManager {
    private Dialog mDialog;
    private ImageView mIcon;
    private ImageView mVoice;
    private TextView mLable; //应该是mLabel
    private Context mContext;

    public DialogManager(Context context){
        mContext = context;
    }

    public void showRecordingDialog(){
        mDialog = new Dialog(mContext, R.style.Theme_audioDialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //用layoutinflater来引用布局
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.layout_voice_dialog_manager, null);
        mDialog.setContentView(view);

        mIcon = (ImageView) mDialog.findViewById(R.id.dialog_icon);
        mVoice = (ImageView) mDialog.findViewById(R.id.dialog_voice);
        mLable = (TextView) mDialog.findViewById(R.id.recorder_dialog_text);
        Window dialogWindow = mDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        int width = ScreenUtil.getScreenWidth(mContext) / 2;
        lp.width = width;
        lp.height = width;
        dialogWindow.setAttributes(lp);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    /**
     * 记录正在录音时的dialog界面
     */
    public void recording(){
        if(mDialog != null && mDialog.isShowing()){
            mIcon.setVisibility(View.GONE);
            mVoice.setVisibility(View.VISIBLE);
            mLable.setVisibility(View.VISIBLE);
            mLable.setText(R.string.shouzhishanghua);
        }
    }

    /**
     * 取消界面
     */
    public void wanToCancel(){
        if(mDialog != null && mDialog.isShowing()){
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLable.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.mipmap.cancel);
            mLable.setText(R.string.want_to_cancel);
        }
    }
    /**
     * 时间过短
     */
    public void tooShort(){
        if(mDialog != null && mDialog.isShowing()){
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLable.setVisibility(View.VISIBLE);
            mIcon.setImageResource(R.mipmap.voice_to_short);
            mLable.setText(R.string.tooshort);
        }
    }

    /**
     * 时间过长
     */
    public void tooLong(){
        if(mDialog != null && mDialog.isShowing()){
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLable.setVisibility(View.VISIBLE);
            mIcon.setImageResource(R.mipmap.voice_to_short); //图片都是一个感叹号
            mLable.setText(R.string.toolong);
        }
    }

    /**
     * 隐藏dialog
     */
    public void dismissDialog(){
        if(mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void updateVoiceLevel(int level){
        if(mDialog != null && mDialog.isShowing()){
            int resId;
            if(level >= 1 && level < 2){
                resId = mContext.getResources().getIdentifier("tb_voice1", "mipmap", mContext.getPackageName());
            }else if(level >= 2 && level < 3){
                resId =mContext.getResources().getIdentifier("tb_voice2", "mipmap", mContext.getPackageName());
            }else{
                resId = mContext.getResources().getIdentifier("tb_voice3", "mipmap", mContext.getPackageName());
            }
            mVoice.setImageResource(resId);
        }
    }

}
