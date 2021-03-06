package com.chateasy.android.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chateasy.android.R;
import com.chateasy.android.common.ChatConst;
import com.chateasy.android.db.ChatMessageBean;
import com.chateasy.android.ui.ImageViewActivity;
import com.chateasy.android.utils.FileSaveUtil;
import com.chateasy.android.widget.BubbleImageView;
import com.chateasy.android.widget.CustomShapeTransformation;
import com.chateasy.android.widget.GifTextView;
import com.chateasy.android.widget.MediaManager;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2022/1/29.
 */

public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<ChatMessageBean> userList = new ArrayList<ChatMessageBean>();
    private ArrayList<String> imageList = new ArrayList<String>();
    private HashMap<Integer, Integer> imagePosition = new HashMap<Integer, Integer>();
    public final static int FROM_USER_MSG = 0;
    public final static int TO_USER_MSG = 1;
    public final static int FROM_USER_IMG = 2;
    public final static int TO_USER_IMG = 3;
    public final static int FROM_USER_VOICE = 4;
    public final static int TO_USER_VOICE = 5;
    private int mMinItemWidth;
    private int mMaxItemWidth;
    public Handler handler;
    private Animation an;
    private SendErrorListener sendErrorListener;
    private VoiceIsRead voiceIsRead;
    public List<String> unReadPosition = new ArrayList<String>();
    private int voicePlayPosition = -1;
    private LayoutInflater mLayoutInflater;
    private boolean isGif = true;
    public boolean isPicRefresh = true;

    public interface SendErrorListener{
        public void onClick(int position);
    }

    public void setSendErrorListener(SendErrorListener sendErrorListener){
        this.sendErrorListener = sendErrorListener;
    }

    public interface VoiceIsRead{
        public void voiceOnClick(int position);
    }

    public void setVoiceIsReadListener(VoiceIsRead voiceIsRead){
        this.voiceIsRead = voiceIsRead;
    }

    public ChatRecyclerAdapter(Context context, List<ChatMessageBean> userList){
        this.context = context;
        this.userList = userList;
        mLayoutInflater = LayoutInflater.from(context);
        //??????????????????
        WindowManager wManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wManager.getDefaultDisplay().getMetrics(outMetrics);
        mMaxItemWidth = (int) (outMetrics.widthPixels * 0.5f);
        mMinItemWidth = (int) (outMetrics.widthPixels * 0.15f);
        handler = new Handler();
    }

    public void setIsGif(boolean isGif){
        this.isGif = isGif;
    }

    public void setImageList(ArrayList<String> imageList){
        this.imageList = imageList;
    }

    public void setImagePosition(HashMap<Integer, Integer> imagePosition){
        this.imagePosition = imagePosition;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = null;
        RecyclerView.ViewHolder holder = null;
        switch (viewType){
            case FROM_USER_MSG:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_msgfrom_list_item, parent, false);
                holder = new FromUserMsgViewHolder(view);
                break;
            case FROM_USER_IMG:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_imagefrom_list_item, parent, false);
                holder = new FromUserImageViewHolder(view);
                break;
            case FROM_USER_VOICE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_voicefrom_list_item, parent, false);
                holder = new FromUserVoiceViewHolder(view);
                break;
            case TO_USER_MSG:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_msgto_list_item, parent, false);
                holder = new ToUserMsgViewHolder(view);
                break;
            case TO_USER_IMG:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_imageto_list_item, parent, false);
                holder = new ToUserImgViewHolder(view);
                break;
            case TO_USER_VOICE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_voiceto_list_item, parent, false);
                holder = new FromUserMsgViewHolder(view);
                break;
        }
        return holder;
    }

    class FromUserMsgViewHolder extends RecyclerView.ViewHolder{
        private ImageView headicon;
        private TextView chat_time;
        private GifTextView content;

        public FromUserMsgViewHolder(View view){
            super(view);
            headicon = (ImageView) view.findViewById(R.id.tb_other_user_icon);
            chat_time = (TextView) view.findViewById(R.id.chat_time);
            content = (GifTextView) view.findViewById(R.id.content);
        }
    }

    class FromUserImageViewHolder extends RecyclerView.ViewHolder{
        private ImageView headicon;
        private TextView chat_time;
        private BubbleImageView image_Msg;

        public FromUserImageViewHolder(View view){
            super(view);
            headicon = (ImageView) view.findViewById(R.id.tb_other_user_icon);
            chat_time = (TextView) view.findViewById(R.id.chat_time);
            image_Msg = (BubbleImageView) view.findViewById(R.id.image_message);
        }
    }

    class FromUserVoiceViewHolder extends RecyclerView.ViewHolder{
        private ImageView headicon;
        private TextView chat_time;
        private LinearLayout voice_group;
        private TextView voice_time;
        private FrameLayout voice_image;
        private View receiver_voice_unread;
        private View voice_anim;

        public FromUserVoiceViewHolder(View view){
            super(view);
            headicon = (ImageView) view.findViewById(R.id.tb_other_user_icon);
            chat_time = (TextView) view.findViewById(R.id.chat_time);
            voice_group = (LinearLayout) view.findViewById(R.id.voice_group);
            voice_time = (TextView) view.findViewById(R.id.voice_time);
            receiver_voice_unread = (View) view.findViewById(R.id.receiver_voice_unread);
            voice_image = (FrameLayout) view.findViewById(R.id.voice_receiver_image);
            voice_anim = (View) view.findViewById(R.id.id_receiver_recorder_anim);
        }
    }

    class ToUserMsgViewHolder extends RecyclerView.ViewHolder{
        private ImageView headicon;
        private TextView chat_time;
        private GifTextView content;
        private ImageView sendFailImg;

        public ToUserMsgViewHolder(View view){
            super(view);
            headicon = (ImageView) view.findViewById(R.id.tb_other_user_icon);
            chat_time = (TextView) view.findViewById(R.id.mychat_time);
            content = (GifTextView) view.findViewById(R.id.content);
            sendFailImg = (ImageView) view.findViewById(R.id.mysend_fail_img);
        }
    }

    class ToUserImgViewHolder extends RecyclerView.ViewHolder{
        private ImageView headicon;
        private TextView chat_time;
        private BubbleImageView image_Msg;
        private LinearLayout image_group;
        private ImageView sendFailImg;

        public ToUserImgViewHolder(View view){
            super(view);
            headicon = (ImageView) view.findViewById(R.id.tb_my_user_icon);
            chat_time = (TextView) view.findViewById(R.id.mychat_time);
            sendFailImg = (ImageView) view.findViewById(R.id.mysend_fail_img);
            image_group = (LinearLayout) view.findViewById(R.id.image_group);
            image_Msg = (BubbleImageView) view.findViewById(R.id.image_message);
        }
    }

    class ToUserVoiceViewHolder extends RecyclerView.ViewHolder{
        private ImageView headicon;
        private TextView chat_time;
        private LinearLayout voice_group;
        private TextView voice_time;
        private FrameLayout voice_image;
        private View receiver_voice_unread;
        private View voice_anim;
        private ImageView  sendFailImg;

        public ToUserVoiceViewHolder(View view){
            super(view);
            headicon = (ImageView) view.findViewById(R.id.tb_my_user_icon);
            chat_time = (TextView) view.findViewById(R.id.mychat_time);
            voice_group = (LinearLayout) view.findViewById(R.id.voice_group);
            voice_time = (TextView) view.findViewById(R.id.voice_time);
            voice_image = (FrameLayout) view.findViewById(R.id.voice_image);
            //       receiver_voice_unread = (View) view.findViewById(R.id.receiver_voice_unread);
            voice_anim = (View) view.findViewById(R.id.id_recorder_anim);
            sendFailImg = (ImageView) view.findViewById(R.id.mysend_fail_img);

        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){
        ChatMessageBean tbub = userList.get(position);
        int itemViewType = getItemViewType(position);
        switch (itemViewType){
            case FROM_USER_MSG:
                fromMsgUserLayout((FromUserMsgViewHolder) holder, tbub, position);
                break;
            case FROM_USER_IMG:
                fromImgUserLayout((FromUserImageViewHolder) holder, tbub, position);
                break;
            case FROM_USER_VOICE:
                fromVoiceUserLayout((FromUserVoiceViewHolder) holder, tbub, position);
                break;
            case TO_USER_MSG:
                toMsgUserLayout((ToUserMsgViewHolder) holder, tbub, position);
                break;
            case TO_USER_IMG:
                toImgUserLayout((ToUserImgViewHolder) holder, tbub, position);
                break;
            case TO_USER_VOICE:
                toVoiceUserLayout((ToUserVoiceViewHolder) holder, tbub, position);
                break;
        }
    }

    @Override
    public int getItemViewType(int position){
        return userList.get(position).getType();
    }

    @Override
    public int getItemCount(){
        return userList.size();
    }

    private void fromMsgUserLayout(final FromUserMsgViewHolder holder, final ChatMessageBean tbub, final int position){
        holder.headicon.setBackgroundResource(R.mipmap.tongbao_hiv);
        if(position != 0){
            String showTime = getTime(tbub.getTime(), userList.get(position - 1).getTime());
            if(showTime != null){
                holder.chat_time.setVisibility(View.VISIBLE);
                holder.chat_time.setText(showTime);
            }else{
                holder.chat_time.setVisibility(View.GONE);
            }
        }else{
            String showTime = getTime(tbub.getTime(), null);
            holder.chat_time.setVisibility(View.VISIBLE);
            holder.chat_time.setText(showTime);
        }
        holder.content.setVisibility(View.VISIBLE);
        holder.content.setSpanText(handler, tbub.getUserContent(), isGif);
    }

    private void fromImgUserLayout(final FromUserImageViewHolder holder, final ChatMessageBean tbub, final int position){
        holder.headicon.setBackgroundResource(R.mipmap.tongbao_hiv);
        if(position != 0){
            String showTime = getTime(tbub.getTime(), userList.get(position - 1).getTime());
            if(showTime != null){
                holder.chat_time.setVisibility(View.VISIBLE);
                holder.chat_time.setText(showTime);
            }else{
                holder.chat_time.setVisibility(View.GONE);
            }
        }else{
            String showTime = getTime(tbub.getTime(), null);
            holder.chat_time.setVisibility(View.VISIBLE);
            holder.chat_time.setText(showTime);
        }
        if(isPicRefresh){
            final String imageSrc = tbub.getImageLocal() == null ? "" : tbub.getImageLocal();
            final String imageUrlSrc = tbub.getImageUrl() == null ? "" : tbub.getImageUrl();
            final String imageIconUrl = tbub.getImageIconUrl() == null ? "" : tbub.getImageIconUrl();
            File file = new File(imageSrc);
            final boolean hasLocal = !imageSrc.equals("") && FileSaveUtil.isFileExists(file);
            int res;
            res = R.drawable.chatfrom_bg_focused;
            Glide.with(context).load(imageSrc).placeholder(R.mipmap.cygs_cs).transform(new CustomShapeTransformation(context, res)).into(holder.image_Msg);

            holder.image_Msg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopPlayVoice();
                    Intent intent = new Intent(context, ImageViewActivity.class);
                    intent.putStringArrayListExtra("images", imageList);
                    intent.putExtra("clickedIndex", imagePosition.get(position));
                    context.startActivity(intent);
                }
            });
        }
    }

    private void fromVoiceUserLayout(final FromUserVoiceViewHolder holder, final  ChatMessageBean tbub, final int position){
        holder.headicon.setBackgroundResource(R.mipmap.tongbao_hiv);
        if(position != 0){
            String showTime = getTime(tbub.getTime(), userList.get(position - 1).getTime());
            if(showTime != null){
                holder.chat_time.setVisibility(View.VISIBLE);
                holder.chat_time.setText(showTime);
            }else{
                holder.chat_time.setVisibility(View.GONE);
            }
        }else{
            String showTime = getTime(tbub.getTime(), null);
            holder.chat_time.setVisibility(View.VISIBLE);
            holder.chat_time.setText(showTime);
        }
        holder.voice_group.setVisibility(View.VISIBLE);
        if(holder.receiver_voice_unread != null){
            holder.receiver_voice_unread.setVisibility(View.GONE);
        }
        if(holder.receiver_voice_unread != null && unReadPosition != null){
            for(String unRead : unReadPosition){
                if(unRead.equals(position + "")){
                    holder.receiver_voice_unread.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }
        AnimationDrawable drawable;
        holder.voice_anim.setId(position);
        if(position == voicePlayPosition){
            holder.voice_anim.setBackgroundResource(R.mipmap.receiver_voice_node_playing003);
            holder.voice_anim.setBackgroundResource(R.drawable.voice_play_receiver);
            drawable = (AnimationDrawable) holder.voice_anim.getBackground();
            drawable.start();
        }else {
            holder.voice_anim.setBackgroundResource(R.mipmap.receiver_voice_node_playing003);
        }
        holder.voice_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.receiver_voice_unread != null){
                    holder.receiver_voice_unread.setVisibility(View.GONE);
                    holder.voice_anim.setBackgroundResource(R.mipmap.receiver_voice_node_playing003);
                    stopPlayVoice();
                    voicePlayPosition = holder.voice_anim.getId();
                    AnimationDrawable drawable;
                    holder.voice_anim.setBackgroundResource(R.drawable.voice_play_receiver);
                    drawable = (AnimationDrawable) holder.voice_anim.getBackground();
                    drawable.start();
                    String voicePath = tbub.getUserVoicePath() == null ? "" : tbub.getUserVoicePath();
                    File file = new File(voicePath);
                    if(!(!voicePath.equals("") && FileSaveUtil.isFileExists(file))){
                        voicePath = tbub.getUserVoiceUrl() == null ? "" : tbub.getUserVoiceUrl();
                    }
                    if(voiceIsRead != null){
                        voiceIsRead.voiceOnClick(position);
                    }
                    MediaManager.playSound(voicePath, new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            voicePlayPosition = -1;
                            holder.voice_anim.setBackgroundResource(R.mipmap.receiver_voice_node_playing003);
                        }
                    });

                }
            }
        });
        float voiceTime = tbub.getUserVoiceTime();
        BigDecimal b = new BigDecimal(voiceTime);
        float f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        holder.voice_time.setText(f1 + "\"");
        ViewGroup.LayoutParams lParams = holder.voice_image.getLayoutParams();
        lParams.width = (int) (mMinItemWidth + mMaxItemWidth / 60f * tbub.getUserVoiceTime());
        holder.voice_image.setLayoutParams(lParams);
    }

    private void toMsgUserLayout(final ToUserMsgViewHolder holder, final  ChatMessageBean tbub, final int position){
        holder.headicon.setBackgroundResource(R.mipmap.grzx_tx_s);
        holder.headicon.setImageDrawable(context.getResources().getDrawable(R.mipmap.grzx_tx_s));
        if(position != 0){
            String showTime = getTime(tbub.getTime(), userList.get(position - 1).getTime());
            if(showTime != null){
                holder.chat_time.setVisibility(View.VISIBLE);
                holder.chat_time.setText(showTime);
            }else{
                holder.chat_time.setVisibility(View.GONE);
            }
        }else{
            String showTime = getTime(tbub.getTime(), null);
            holder.chat_time.setVisibility(View.VISIBLE);
            holder.chat_time.setText(showTime);
        }
        holder.content.setVisibility(View.VISIBLE);
        holder.content.setSpanText(handler, tbub.getUserContent(), isGif);
    }

    private void toImgUserLayout(final ToUserImgViewHolder holder, final ChatMessageBean tbub, final int position){
        holder.headicon.setBackgroundResource(R.mipmap.grzx_tx_s);
        switch (tbub.getSendState()){
            case ChatConst.SENDING:
                an = AnimationUtils.loadAnimation(context, R.anim.update_loading_progressbar_anim);
                LinearInterpolator lin = new LinearInterpolator();
                an.setInterpolator(lin);
                an.setRepeatCount(-1);
                holder.sendFailImg.setBackgroundResource(R.mipmap.xsearch_loading);
                holder.sendFailImg.startAnimation(an);
                an.startNow();
                holder.sendFailImg.setVisibility(View.VISIBLE);
                break;

            case ChatConst.COMPLETED:
                holder.sendFailImg.clearAnimation();
                holder.sendFailImg.setVisibility(View.GONE);
                break;

            case ChatConst.SENDERROR:
                holder.sendFailImg.clearAnimation();
                holder.sendFailImg.setBackgroundResource(R.mipmap.msg_state_fail_resend_pressed);
                holder.sendFailImg.setVisibility(View.VISIBLE);
                holder.sendFailImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(sendErrorListener != null){
                            sendErrorListener.onClick(position);
                        }
                    }
                });
                break;

            default:
                break;
        }
        holder.headicon.setImageDrawable(context.getResources().getDrawable(R.mipmap.grzx_tx_s));
        if(position != 0){
            String showTime = getTime(tbub.getTime(), userList.get(position - 1).getTime());
            if(showTime != null){
                holder.chat_time.setVisibility(View.VISIBLE);
                holder.chat_time.setText(showTime);
            }else{
                holder.chat_time.setVisibility(View.GONE);
            }
        }else{
            String showTime = getTime(tbub.getTime(), null);
            holder.chat_time.setVisibility(View.VISIBLE);
            holder.chat_time.setText(showTime);
        }
        //??????if?????????
        holder.image_group.setVisibility(View.VISIBLE);
        final String imageSrc = tbub.getImageLocal() == null ? "" : tbub.getImageLocal();
        final String imageUrlSrc = tbub.getImageUrl() == null ? "" : tbub.getImageUrl();
        final String imageIconUrl = tbub.getImageIconUrl() == null ? "" : tbub.getImageIconUrl();
        File file = new File(imageSrc);
        final boolean hasLocal = !imageSrc.equals("") && FileSaveUtil.isFileExists(file);
        int res;
        res = R.drawable.chatto_bg_focused;
        Glide.with(context).load(imageSrc).placeholder(R.mipmap.cygs_cs)
                .transform(new CustomShapeTransformation(context, res)).into(holder.image_Msg);
        holder.image_Msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayVoice();
                Intent intent = new Intent(context, ImageViewActivity.class);
                intent.putStringArrayListExtra("images", imageList);
                intent.putExtra("clickedIndex", imagePosition.get(position));
                context.startActivity(intent);
            }
        });
    }

    private void toVoiceUserLayout(final ToUserVoiceViewHolder holder, final ChatMessageBean tbub, final int position){
        holder.headicon.setBackgroundResource(R.mipmap.grzx_tx_s);
        switch (tbub.getSendState()){
            case ChatConst.SENDING:
                an = AnimationUtils.loadAnimation(context, R.anim.update_loading_progressbar_anim);
                LinearInterpolator lin = new LinearInterpolator();
                an.setInterpolator(lin);
                an.setRepeatCount(-1);
                holder.sendFailImg.setBackgroundResource(R.mipmap.xsearch_loading);
                holder.sendFailImg.startAnimation(an);
                an.startNow();
                holder.sendFailImg.setVisibility(View.VISIBLE);
                break;

            case ChatConst.COMPLETED:
                holder.sendFailImg.clearAnimation();
                holder.sendFailImg.setVisibility(View.GONE);
                break;

            case ChatConst.SENDERROR:
                holder.sendFailImg.clearAnimation();
                holder.sendFailImg.setBackgroundResource(R.mipmap.msg_state_fail_resend_pressed);
                holder.sendFailImg.setVisibility(View.VISIBLE);
                holder.sendFailImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(sendErrorListener != null){
                            sendErrorListener.onClick(position);
                        }
                    }
                });
                break;

            default:
                break;
        }
        holder.headicon.setImageDrawable(context.getResources().getDrawable(R.mipmap.grzx_tx_s));
        if(position != 0){
            String showTime = getTime(tbub.getTime(), userList.get(position - 1).getTime());
            if(showTime != null){
                holder.chat_time.setVisibility(View.VISIBLE);
                holder.chat_time.setText(showTime);
            }else{
                holder.chat_time.setVisibility(View.GONE);
            }
        }else{
            String showTime = getTime(tbub.getTime(), null);
            holder.chat_time.setVisibility(View.VISIBLE);
            holder.chat_time.setText(showTime);
        }
        holder.voice_group.setVisibility(View.VISIBLE);
        if(holder.receiver_voice_unread != null){
            holder.receiver_voice_unread.setVisibility(View.GONE);
        }
        if(holder.receiver_voice_unread != null && unReadPosition != null){
            for(String unRead : unReadPosition){
                if(unRead.equals(position + "")){
                    holder.receiver_voice_unread.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }
        AnimationDrawable drawable;
        holder.voice_anim.setId(position);
        if(position == voicePlayPosition){
            holder.voice_anim.setBackgroundResource(R.mipmap.adj);
            holder.voice_anim.setBackgroundResource(R.drawable.voice_play_send);
            drawable = (AnimationDrawable) holder.voice_anim.getBackground();
            drawable.start();
        }else {
            holder.voice_anim.setBackgroundResource(R.mipmap.adj);
        }
        holder.voice_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.receiver_voice_unread != null){
                    holder.receiver_voice_unread.setVisibility(View.GONE);
                    holder.voice_anim.setBackgroundResource(R.mipmap.adj);
                    stopPlayVoice();
                    voicePlayPosition = holder.voice_anim.getId();
                    AnimationDrawable drawable;
                    holder.voice_anim.setBackgroundResource(R.drawable.voice_play_send);
                    drawable = (AnimationDrawable) holder.voice_anim.getBackground();
                    drawable.start();
                    String voicePath = tbub.getUserVoiceUrl() == null ? "" : tbub.getUserVoiceUrl();
                    if(voiceIsRead != null){
                        voiceIsRead.voiceOnClick(position);
                    }
                    MediaManager.playSound(voicePath, new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            voicePlayPosition = -1;
                            holder.voice_anim.setBackgroundResource(R.mipmap.adj);
                        }
                    });
                }
            }
        });
        float voiceTime = tbub.getUserVoiceTime();
        BigDecimal b = new BigDecimal(voiceTime);
        float f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        holder.voice_time.setText(f1 + "");
        ViewGroup.LayoutParams lParams = holder.voice_image.getLayoutParams();
        lParams.width = (int) (mMinItemWidth + mMaxItemWidth / 60f * tbub.getUserVoiceTime());
        holder.voice_image.setLayoutParams(lParams);
    }

    @SuppressLint("SimpleDateFormat")
    public String getTime(String time, String before){
        String show_Time = null;
        if(before != null){
            try {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date now = df.parse(time);
                Date date = df.parse(before);
                long l = now.getTime() - date.getTime();
                long day = l / (24 * 60 * 60 *1000);
                long hour = (l / (60 * 60 * 1000) - day * 24);
                long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
                if(min >= 1){
                    show_Time = time.substring(11);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            show_Time = time.substring(11);
        }
        String getDay = getDay(time);
        if(show_Time != null && getDay != null){
            show_Time = getDay + "" + show_Time;
        }
        return show_Time;
    }

    @SuppressLint("SimpleDateFormat")
    public static String returnTime(){
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date =sDateFormat.format(new Date());
        return date;
    }

    @SuppressLint("SimpleDateFormat")
    public String getDay(String time){
        String showDay = null;
        String nowTime = returnTime();
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now = df.parse(nowTime);
            Date date = df.parse(time);
            long l = now.getTime() - date.getTime();
            long day = l / (24 * 60 * 60 *1000);
            if(day >= 365){
                showDay = time.substring(0, 10);
            }else if(day >= 1 && day < 365){
                showDay = time.substring(5, 10);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return showDay;
    }

    public static Bitmap getLocalBitmap(String url){
        try{
            ByteArrayOutputStream out;
            FileInputStream fis = new FileInputStream(url);
            BufferedInputStream bis = new BufferedInputStream(fis);
            out = new ByteArrayOutputStream();
            @SuppressWarnings("unused")
            int hasRead = 0;
            byte[] buffer = new byte[1024 * 2];
            while ((hasRead = bis.read(buffer)) > 0){
                out.write(buffer);
                out.flush();
            }
            out.close();
            fis.close();
            bis.close();
            byte[] data = out.toByteArray();
            //????????????
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 3;
            return BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        }catch (FileNotFoundException e){
            e.printStackTrace();
            return null;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public void stopPlayVoice(){
        if(voicePlayPosition != -1){
            View voicePlay = (View) ((Activity) context).findViewById(voicePlayPosition);
            if(voicePlay != null){
                if(getItemViewType(voicePlayPosition) == FROM_USER_VOICE){
                    voicePlay.setBackgroundResource(R.mipmap.receiver_voice_node_playing003);
                }else {
                    voicePlay.setBackgroundResource(R.mipmap.adj);
                }
            }
            MediaManager.pause();
            voicePlayPosition = -1;
        }
    }

}
