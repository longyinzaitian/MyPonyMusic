package com.music.app.storage;

import android.content.Context;

import com.music.app.storage.db.greendao.DaoMaster;
import com.music.app.storage.db.greendao.DaoSession;
import com.music.app.storage.db.greendao.MusicDao;
import com.music.app.storage.db.greendao.OnlineMusicDao;

import org.greenrobot.greendao.database.Database;


public class DBManager {
    private static final String DB_NAME = "database";
    private MusicDao musicDao;
    private OnlineMusicDao onlineMusicDao;

    public static DBManager get() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static DBManager instance = new DBManager();
    }

    public void init(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DB_NAME);
        Database db = helper.getWritableDb();
        DaoSession daoSession = new DaoMaster(db).newSession();
        musicDao = daoSession.getMusicDao();
        onlineMusicDao = daoSession.getOnlineMusicDao();
    }

    private DBManager() {
    }

    public MusicDao getMusicDao() {
        return musicDao;
    }

    public OnlineMusicDao getOnlineMusicDao() {
        return onlineMusicDao;
    }
}
