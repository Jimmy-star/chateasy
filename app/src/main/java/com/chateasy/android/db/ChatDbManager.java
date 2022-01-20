package com.chateasy.android.db;

import com.chateasy.android.db.base.BaseManager;

import org.greenrobot.greendao.AbstractDao;

/**
 * Created by Administrator on 2022/1/20.
 */

public class ChatDbManager extends BaseManager<ChatMessageBean, Long> {
    @Override
    public AbstractDao<ChatMessageBean, Long> getAbstractDao(){
        return daoSession.getChatMessageBeanDao();
    }

}
