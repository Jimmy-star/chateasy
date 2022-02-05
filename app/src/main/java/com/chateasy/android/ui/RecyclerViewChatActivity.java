package com.chateasy.android.ui;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;

import com.chateasy.android.adapter.ChatListViewAdapter;
import com.chateasy.android.adapter.ChatRecyclerAdapter;
import com.chateasy.android.animator.SlideInOutBottomItemAnimator;
import com.chateasy.android.common.ChatConst;
import com.chateasy.android.db.ChatMessageBean;
import com.chateasy.android.ui.base.BaseActivity;
import com.chateasy.android.utils.KeyBoardUtils;
import com.chateasy.android.widget.AudioRecordButton;
import com.chateasy.android.widget.pulltorefresh.PullToRefreshRecyclerView;
import com.chateasy.android.widget.pulltorefresh.WrapContentLinearLayoutManager;
import com.chateasy.android.widget.pulltorefresh.base.PullToRefreshView;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2022/1/29.
 */

public class RecyclerViewChatActivity extends BaseActivity {
    private PullToRefreshRecyclerView myList;
    private ChatRecyclerAdapter tbAapter;
    private SendMessageHandler sendMessageHandler;
    private WrapContentLinearLayoutManager wcLinearLayoutManager;

    String content = ""; //发送和接收信息
    //发送和接收图片
    int i = 0;
    String filePath = "";
    //接收语音
    float seconds = 0.0f;
    String voiceFilePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void findView(){
        super.findView();
        pullList.setSlideView(new PullToRefreshView(this).getSlideView(PullToRefreshView.RECYCLERVIEW));
        myList = (PullToRefreshRecyclerView) pullList.returnMylist();
    }

    @Override
    protected void onDestroy(){
        tbList.clear();
        tbAapter.notifyDataSetChanged();
        myList.setAdapter(null);
        sendMessageHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    protected void init(){
        setTitle("RecyclerView");
        tbAapter = new ChatRecyclerAdapter(this, tbList);
        wcLinearLayoutManager = new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        myList.setLayoutManager(wcLinearLayoutManager);
        myList.setItemAnimator(new SlideInOutBottomItemAnimator(myList));
        myList.setAdapter(tbAapter);
        sendMessageHandler = new SendMessageHandler(this);
        tbAapter.isPicRefresh = true;
        tbAapter.notifyDataSetChanged();
        tbAapter.setSendErrorListener(new ChatRecyclerAdapter.SendErrorListener() {
            @Override
            public void onClick(int position) {
                ChatMessageBean tbub = tbList.get(position);
                if(tbub.getType() == ChatRecyclerAdapter.TO_USER_VOICE){
                    sendVoice(tbub.getUserVoiceTime(), tbub.getUserVoicePath());
                    tbList.remove(position);
                }else if(tbub.getType() == ChatRecyclerAdapter.TO_USER_IMG){
                    sendImage(tbub.getImageLocal());
                    tbList.remove(position);
                }
            }
        });
        tbAapter.setVoiceIsReadListener(new ChatRecyclerAdapter.VoiceIsRead() {
            @Override
            public void voiceOnClick(int position) {
                for(int i = 0;i < tbAapter.unReadPosition.size();i++){
                    if(tbAapter.unReadPosition.get(i).equals(position + "")){
                        tbAapter.unReadPosition.remove(i);
                        break;
                    }
                }
            }
        });
        voiceBtn.setAudioFinishRecorderListener(new AudioRecordButton.AudioFinishRecorderListener() {
            @Override
            public void onStart() {
                tbAapter.stopPlayVoice();
            }

            @Override
            public void onFinished(float seconds, String filePath) {
                sendVoice(seconds, filePath);
            }
        });
        myList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState){
                    case RecyclerView.SCROLL_STATE_IDLE:
                        tbAapter.handler.removeCallbacksAndMessages(null);
                        tbAapter.setIsGif(true);
                        tbAapter.isPicRefresh = false;
                        tbAapter.notifyDataSetChanged();
                        break;

                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        tbAapter.handler.removeCallbacksAndMessages(null);
                        tbAapter.setIsGif(false);
                        tbAapter.isPicRefresh = true;
                        reset();
                        KeyBoardUtils.hideKeyBoard(RecyclerViewChatActivity.this, mEditTextContent);
                        break;

                    default:
                        break;
                }
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        controlKeyboardLayout(activityRootView, pullList);
        super.init();
    }

    private void controlKeyboardLayout(final View root, final View needToScrollView){
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private Rect r = new Rect();
            @Override
            public void onGlobalLayout() {
                //获取当前界面的可视部分
                RecyclerViewChatActivity.this.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
                //获取屏幕的高度
                int screenHeight = RecyclerViewChatActivity.this.getWindow().getDecorView().getHeight();
                //获取键盘的高度
                int heightDifference = screenHeight - r.bottom;
                int recyclerHeight = 0;
                if(wcLinearLayoutManager != null){
                    recyclerHeight = wcLinearLayoutManager.getRecyclerHeight();
                }
                if(heightDifference == 0 || heightDifference == bottomStatusHeight){
                    needToScrollView.scrollTo(0, 0);
                } else{
                    if(heightDifference < recyclerHeight){
                        int contentHeight = wcLinearLayoutManager == null ? 0 : wcLinearLayoutManager.getHeight();
                        if(recyclerHeight < contentHeight){
                            listSlideHeight = heightDifference - (contentHeight - recyclerHeight);
                            needToScrollView.scrollTo(0, listSlideHeight);
                        }
                    }else {
                        listSlideHeight = 0;
                    }
                }

            }
        });
    }

    @Override
    protected void loadRecords(){
        isDown = true;
        if(pagelist != null){
            pagelist.clear();
        }
        pagelist = mChatDbManager.loadPages(page, number);
        position = pagelist.size();
        if(pagelist.size() != 0){
            pagelist.addAll(tbList);
            tbList.clear();
            tbList.addAll(pagelist);
            if(imageList != null){
                imageList.clear();
            }
            if(imagePosition != null){
                imagePosition.clear();
            }
            int key = 0;
            int position = 0;
            for(ChatMessageBean cmb : tbList){
                if(cmb.getType() == ChatListViewAdapter.FROM_USER_IMG || cmb.getType() == ChatListViewAdapter.TO_USER_IMG){
                    imageList.add(cmb.getImageLocal());
                    imagePosition.put(key, position);
                    position++;
                }
                key++;
            }
            tbAapter.setImageList(imageList);
            tbAapter.setImagePosition(imagePosition);
            sendMessageHandler.sendEmptyMessage(PULL_TO_REFRESH_DOWN);
            if(page == 0){
                pullList.refreshComplete();
                pullList.setPullGone();
            }else {
                page--;
            }
        }else {
            if(page == 0){
                pullList.refreshComplete();
                pullList.setPullGone();
            }
        }
    }

    static class SendMessageHandler extends Handler{
        WeakReference<RecyclerViewChatActivity> mActivity;

        SendMessageHandler(RecyclerViewChatActivity activity){
            mActivity = new WeakReference<RecyclerViewChatActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg){
            RecyclerViewChatActivity theActivity = mActivity.get();
            if(theActivity != null){
                switch (msg.what){
                    case REFRESH:
                        theActivity.tbAapter.isPicRefresh = true;
                        theActivity.tbAapter.notifyDataSetChanged();
                        int position = theActivity.tbAapter.getItemCount() - 1 < 0 ? 0 : theActivity.tbAapter.getItemCount() - 1;
                        theActivity.myList.smoothScrollToPosition(position);
                        break;
                    case SEND_OK:
                        theActivity.mEditTextContent.setText("");
                        theActivity.tbAapter.isPicRefresh = true;
                        theActivity.tbAapter.notifyItemInserted(theActivity.tbList.size() - 1);
                        theActivity.myList.smoothScrollToPosition(theActivity.tbAapter.getItemCount() - 1);
                        break;
                    case RECEIVE_OK:
                        theActivity.tbAapter.isPicRefresh = true;
                        theActivity.tbAapter.notifyItemInserted(theActivity.tbList.size() - 1);
                        theActivity.myList.smoothScrollToPosition(theActivity.tbAapter.getItemCount() - 1);
                        break;
                    case PULL_TO_REFRESH_DOWN:
                        theActivity.pullList.refreshComplete();
                        theActivity.tbAapter.notifyDataSetChanged();
                        theActivity.myList.smoothScrollToPosition(theActivity.position - 1);
                        theActivity.isDown = false;
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 发送文字
     */
    protected void sendMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String content = mEditTextContent.getText().toString();
                tbList.add(getTbub(userName, ChatListViewAdapter.TO_USER_MSG, content, null, null, null,
                        null, null, 0f, ChatConst.COMPLETED));
                sendMessageHandler.sendEmptyMessage(SEND_OK);
                RecyclerViewChatActivity.this.content = content;
                receriveHandler.sendEmptyMessageDelayed(0, 1000);
            }
        }).start();
    }

    /**
     * 接收文字
     */
    private void receriveMsgText(final String content){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message = "回复：" + content;
                ChatMessageBean tbub = new ChatMessageBean();
                tbub.setUserName(userName);
                String time = returnTime();
                tbub.setUserContent(message);
                tbub.setTime(time);
                tbub.setType(ChatListViewAdapter.FROM_USER_MSG);
                tbList.add(tbub);
                sendMessageHandler.sendEmptyMessage(RECEIVE_OK);
                mChatDbManager.insert(tbub);
            }
        }).start();
    }

    /**
     * 发送图片
     */
    @Override
    protected void sendImage(final String filePath){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(i == 0){
                    tbList.add(getTbub(userName, ChatListViewAdapter.TO_USER_IMG, null, null, null, filePath,
                            null, null, 0f, ChatConst.SENDING));
                }else if(i == 1){
                    tbList.add(getTbub(userName, ChatListViewAdapter.TO_USER_IMG, null, null, null, filePath,
                            null,null, 0f, ChatConst.SENDERROR));
                }else if(i == 2){
                    tbList.add(getTbub(userName, ChatListViewAdapter.TO_USER_IMG, null, null, null, filePath,
                            null, null, 0f, ChatConst.COMPLETED));
                    i = -1;
                }
                imageList.add(tbList.get(tbList.size() - 1).getImageLocal());
                imagePosition.put(tbList.size() - 1, imageList.size() - 1);
                sendMessageHandler.sendEmptyMessage(SEND_OK);
                RecyclerViewChatActivity.this.filePath = filePath;
                receriveHandler.sendEmptyMessageDelayed(1, 3000);
                i++;
            }
        }).start();
    }

    private void receriveImageText(final String filePath){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ChatMessageBean tbub = new ChatMessageBean();
                tbub.setUserName(userName);
                String time = returnTime();
                tbub.setTime(time);
                tbub.setImageLocal(filePath);
                tbub.setType(ChatListViewAdapter.FROM_USER_IMG);
                tbList.add(tbub);
                imageList.add(tbList.get(tbList.size() - 1).getImageLocal());
                imagePosition.put(tbList.size() - 1, imageList.size() - 1);
                sendMessageHandler.sendEmptyMessage(RECEIVE_OK);
                mChatDbManager.insert(tbub);
            }
        }).start();
    }

    /**
     * 发送语音
     */
    protected void sendVoice(final float seconds, final String filePath){
        new Thread(new Runnable() {
            @Override
            public void run() {
                tbList.add(getTbub(userName, ChatListViewAdapter.TO_USER_VOICE, null, null, null, null,
                        filePath, null, seconds, ChatConst.SENDING));
                RecyclerViewChatActivity.this.seconds = seconds;
                voiceFilePath = filePath;
                receriveHandler.sendEmptyMessageDelayed(2, 3000);
            }
        }).start();
    }

    private void receriveVoiceText(final float seconds, final String filePath){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ChatMessageBean tbub = new ChatMessageBean();
                tbub.setUserName(userName);
                String time = returnTime();
                tbub.setTime(time);
                tbub.setUserVoiceTime(seconds);
                tbub.setUserVoicePath(filePath);
                tbAapter.unReadPosition.add(tbList.size() + "");
                tbub.setType(ChatListViewAdapter.FROM_USER_VOICE);
                tbList.add(tbub);
                sendMessageHandler.sendEmptyMessage(RECEIVE_OK);
                mChatDbManager.insert(tbub);
            }
        }).start();
    }

    /**
     * 为了模拟接收延迟
     */
    private Handler receriveHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 0 :
                    receriveMsgText(content);
                    break;
                case 1:
                    receriveImageText(filePath);
                    break;
                case 2:
                    receriveVoiceText(seconds, voiceFilePath);
                    break;
                default:
                    break;
            }
        }
    };

}
