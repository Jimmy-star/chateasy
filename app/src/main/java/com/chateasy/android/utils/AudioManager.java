package com.chateasy.android.utils;

import android.media.MediaRecorder;
import android.os.Handler;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Administrator on 2022/1/22.
 */

public class AudioManager {
    public static final int MSG_ERROR_AUDIO_RECORD = -4;
    private MediaRecorder mRecorder;
    private String mDirString;
    private String mCurrentFilePathString;
    private Handler handler;
    private boolean isPrepared;
    private static AudioManager mInstance;
    public AudioStageListener mListener;
    private int vocAuthority[] = new int[10];
    private int vocNum = 0;
    private boolean check = true;

    private AudioManager(String dir){
        mDirString = dir;
    }

    public static AudioManager getInstance(String dir){
        if(mInstance == null){
            synchronized (AudioManager.class){
                if(mInstance == null){
                    mInstance = new AudioManager(dir);
                }
            }
        }
        return mInstance;
    }

    public void setHandler(Handler handler){
        this.handler = handler;
    }

    public interface AudioStageListener{
        void wellPrepared();
    }

    public void setOnAudioStageListener(AudioStageListener listener){
        mListener = listener;
    }

    public void setVocDir(String dir){
        mDirString = dir;
    }

    @SuppressWarnings("deprecation")
    public void prepareAudio(){
        try{
            isPrepared = false;
            File dir = new File(mDirString);
            if(!dir.exists()){
                dir.mkdirs();
            }
            String fileNameString = generalFileName();
            File file = new File(dir, fileNameString);
            mCurrentFilePathString = file.getAbsolutePath();
            mRecorder = new MediaRecorder();
            //设置输出文件
            mRecorder.setOutputFile(file.getAbsolutePath());
            //设置mediaRecorder的音频源是麦克风
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置文件音频的输出格式为amr
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            //设置音频的编码格式为amr
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.prepare();
            mRecorder.start();
            //准备结束，可以录制了
            if(mListener != null){
                mListener.wellPrepared();
            }
            isPrepared = true;
        }catch (IllegalStateException e){
            e.printStackTrace();
            if(handler != null){
                handler.sendEmptyMessage(MSG_ERROR_AUDIO_RECORD);
            }
        }catch (IOException e){
            e.printStackTrace();
            if(handler != null){
                handler.sendEmptyMessage(MSG_ERROR_AUDIO_RECORD);
            }
        }catch (Exception e){
            e.printStackTrace();
            if(handler != null){
                handler.sendEmptyMessage(MSG_ERROR_AUDIO_RECORD);
            }
        }
    }

    /**
     * 随机生成文件名称
     */
    private String generalFileName(){
        return UUID.randomUUID().toString() + ".amr";
    }

    public int getVoiceLevel(int maxLevel){
        if(isPrepared){
            try{
                int vocLevel = mRecorder.getMaxAmplitude();
                if(check){
                    if(vocNum >= 10){
                        Set<Integer> set = new HashSet<Integer>();
                        for(int i = 0;i < vocNum;i++){
                            set.add(vocAuthority[i]);
                        }
                        if(set.size() == 1){
                            if(handler != null){
                                handler.sendEmptyMessage(MSG_ERROR_AUDIO_RECORD);
                            }
                            vocNum = 0;
                            vocAuthority = null;
                            vocAuthority = new int[10];
                        }else{
                            check = false;
                        }
                    }else{
                        vocAuthority[vocNum] = vocLevel;
                        vocNum++;
                    }
                }
                return maxLevel * vocLevel / 32768 + 1;
            }catch (Exception e){
                if(handler != null){
                    handler.sendEmptyMessage(MSG_ERROR_AUDIO_RECORD);
                }
            }
        }
        return 1;
    }

    /**
     * 释放资源
     */
    public void release(){
        if(null != mRecorder){
            isPrepared = false;
            try{
                mRecorder.stop();
                mRecorder.release();
            }catch (Exception e){
                e.printStackTrace();
            }
            mRecorder = null;
        }
    }

    /**
     * 取消，删除文件
     */
    public void cancel(){
        release();
        if(mCurrentFilePathString != null){
            File file = new File(mCurrentFilePathString);
            file.delete();
            mCurrentFilePathString = null;
        }
    }

    public String getCurrentFilePath(){
        return mCurrentFilePathString;
    }

}
