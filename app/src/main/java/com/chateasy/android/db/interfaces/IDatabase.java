package com.chateasy.android.db.interfaces;
import java.util.List;
import android.support.annotation.NonNull;
import org.greenrobot.greendao.query.QueryBuilder;

/**
 * Created by Administrator on 2022/1/20.
 */

public interface IDatabase<M, K> {
    boolean insert(M m);
    boolean delete(M m);
    boolean deleteByKey(K key);
    boolean deleteList(List<M> mList);
    boolean deleteByKeyInTx(K... key);
    boolean deleteAll();
    boolean insertOrReplace(@NonNull M m);
    boolean update(M m);
    boolean updateInTx(M... m);
    boolean updateList(List<M> mList);
    M selectByPrimaryKey(K key);
    List<M> loadAll();

    List<M> loadPages(int page, int number);
    long getPages(int number);
    boolean refresh(M m);
    void clearDaoSession();
    boolean dropDatabase();
    void runInTx(Runnable runnable);
    boolean insertList(List<M> mList);
    QueryBuilder<M> getQueryBuilder();
    List<M> queryRaw(String where, String... selectionArg);
}
