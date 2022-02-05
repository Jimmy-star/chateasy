package com.chateasy.android.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chateasy.android.R;
import com.chateasy.android.utils.AudioManager;
import com.chateasy.android.utils.FileSaveUtil;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by Administrator on 2022/1/21.
 */

public class AudioRecordButton extends Button implements AudioManager.AudioStageListener {
    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2;
    private static final int STATE_WANT_TO_CANCEL = 3;
    private static final int DISTANCE_Y_CANCEL = 50;
    private static final int OVERTIME = 60;
    private int mCurrentState = STATE_NORMAL;

    private boolean isRecording = false;
    private DialogManager mDialogManager;
    private float mTime = 0;

    private boolean mReady;
    private AudioManager mAudioManager;
    private String saveDir = FileSaveUtil.voice_dir;
    private AudioFinishRecorderListener mListener;
    private boolean isTouch = false;

    private Handler mp3handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case AudioManager.MSG_ERROR_AUDIO_RECORD:
                    Toast.makeText(getContext(), "录音权限被屏蔽或者录音设备损坏！\n请在设置中检查是否开启权限！", Toast.LENGTH_SHORT).show();
                    mDialogManager.dismissDialog();
                    mAudioManager.cancel();
                    reset();
                    break;
                default:
                    break;
            }
        }
    };

    public AudioRecordButton(Context context){
        this(context, null);
    }

    public AudioRecordButton(Context context, AttributeSet attrs){
        super(context, attrs);
        mDialogManager = new DialogManager(getContext());
        try{
            FileSaveUtil.createSDDirectory(FileSaveUtil.voice_dir);
        }catch (IOException e){
            e.printStackTrace();
        }
        mAudioManager = AudioManager.getInstance(FileSaveUtil.voice_dir);
        mAudioManager.setOnAudioStageListener(this);
        mAudioManager.setHandler(mp3handler);
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v){
                try{
                    FileSaveUtil.createSDDirectory(saveDir);
                }catch (IOException e){
                    e.printStackTrace();
                }
                mAudioManager.setVocDir(saveDir);
                mListener.onStart();
                mReady = true;
                mAudioManager.prepareAudio();
                return false;
            }
        });
    }

    public void setSaveDir(String saveDir){
        this.saveDir = saveDir + saveDir;
    }

    /**
     * 录音完成后的回调,回调给activity，可以获得mTime和文件的路径
     */
    public interface AudioFinishRecorderListener{
        void onStart();
        void onFinished(float seconds, String filePath);
    }

    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener){
        mListener = listener;
    }

    private Runnable mGetVoiceLevelRunnable = new Runnable() {
        @Override
        public void run() {
            while(isRecording){
                try{
                    Thread.sleep(100);
                    mTime += 0.1f;
                    mhandler.sendEmptyMessage(MSG_VOICE_CHANGE);
                    if(mTime >= OVERTIME){
                        mTime = 60;
                        mhandler.sendEmptyMessage(MSG_OVERTIME_SEND);
                        isRecording = false;
                        break;
                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    };

    private static final int MSG_AUDIO_PREPARED = 0x110;
    private static final int MSG_VOICE_CHANGE = 0x111;
    private static final int MSG_DIALOG_DISMISS = 0x112;
    private static final int MSG_OVERTIME_SEND = 0x113;

    private Handler mhandler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case MSG_AUDIO_PREPARED:
                    if(isTouch){
                        mTime = 0;
                        mDialogManager.showRecordingDialog();
                        isRecording = true;
                        new Thread(mGetVoiceLevelRunnable).start();
                    }
                    break;
                case MSG_VOICE_CHANGE:
                    mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(3));
                    break;
                case MSG_DIALOG_DISMISS:
                    isRecording = false;
                    mDialogManager.dismissDialog();
                    break;
                case MSG_OVERTIME_SEND:
                    mDialogManager.tooLong();
                    mhandler.sendEmptyMessageDelayed(MSG_DIALOG_DISMISS, 1300);
                    if(mListener != null){
                        File file = new File(mAudioManager.getCurrentFilePath());
                        if(FileSaveUtil.isFileExists(file)){
                            mListener.onFinished(mTime, mAudioManager.getCurrentFilePath());
                        }else{
                            mp3handler.sendEmptyMessage(AudioManager.MSG_ERROR_AUDIO_RECORD);
                        }
                    }
                    isRecording = false;
                    reset();
                    break;
            }
        };
    };

    @Override
    public void wellPrepared(){
        mhandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                isTouch = true;
                changeState(STATE_RECORDING);
                break;
            case MotionEvent.ACTION_MOVE:
                if(isRecording){
                    if(wantToCancel(x, y)){
                        changeState(STATE_WANT_TO_CANCEL);
                    }else{
                        changeState(STATE_RECORDING);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isTouch = false;
                if(!mReady){
                    reset();
                    return super.onTouchEvent(event);
                }
                if(!isRecording || mTime < 0.6f){
                    mDialogManager.tooShort();
                    mAudioManager.cancel();
                    mhandler.sendEmptyMessageDelayed(MSG_DIALOG_DISMISS, 1300);
                }else if(mCurrentState == STATE_RECORDING){
                    mDialogManager.dismissDialog();
                    mAudioManager.release();
                    if(mListener != null){
                        BigDecimal b = new BigDecimal(mTime);
                        float f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                        File file = new File(mAudioManager.getCurrentFilePath());
                        if(FileSaveUtil.isFileExists(file)){
                            mListener.onFinished(f1, mAudioManager.getCurrentFilePath());
                        }else{
                            mp3handler.sendEmptyMessage(AudioManager.MSG_ERROR_AUDIO_RECORD);
                        }
                    }
                }else if(mCurrentState == STATE_WANT_TO_CANCEL){
                    mAudioManager.cancel();
                    mDialogManager.dismissDialog();
                }
                isRecording = false;
                reset();
                break;
            case MotionEvent.ACTION_CANCEL:
                isTouch = false;
                reset();
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 恢复标志位以及状态
     */
    private void reset(){
        isRecording = false;
        changeState(STATE_NORMAL);
        mReady = false;
        mTime = 0;
    }

    private boolean wantToCancel(int x, int y){
        if(x < 0 || x > getWidth()){
            return true;
        }
        if(y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL){
            return true;
        }
        return false;
    }

    private void changeState(int state){
        if(mCurrentState != state){
            mCurrentState = state;
            switch (mCurrentState){
                case STATE_NORMAL:
                    setBackgroundResource(R.drawable.button_recordnormal);
                    setText(R.string.normal);
                    break;
                case STATE_RECORDING:
                    setBackgroundResource(R.drawable.button_recording);
                    setText(R.string.recording);
                    if(isRecording){
                        mDialogManager.recording();
                    }
                    break;
                case STATE_WANT_TO_CANCEL:
                    setBackgroundResource(R.drawable.button_recording);
                    setText(R.string.want_to_cancel);
                    mDialogManager.wanToCancel();
                    break;
            }
        }
    }

    @Override
    public boolean onPreDraw(){
        return false;
    }
}
