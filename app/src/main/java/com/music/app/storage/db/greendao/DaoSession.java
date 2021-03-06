package com.music.app.storage.db.greendao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.music.app.model.Music;
import com.music.app.model.OnlineMusic;

import com.music.app.storage.db.greendao.MusicDao;
import com.music.app.storage.db.greendao.OnlineMusicDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig musicDaoConfig;
    private final DaoConfig onlineMusicDaoConfig;

    private final MusicDao musicDao;
    private final OnlineMusicDao onlineMusicDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        musicDaoConfig = daoConfigMap.get(MusicDao.class).clone();
        musicDaoConfig.initIdentityScope(type);

        onlineMusicDaoConfig = daoConfigMap.get(OnlineMusicDao.class).clone();
        onlineMusicDaoConfig.initIdentityScope(type);

        musicDao = new MusicDao(musicDaoConfig, this);
        onlineMusicDao = new OnlineMusicDao(onlineMusicDaoConfig, this);

        registerDao(Music.class, musicDao);
        registerDao(OnlineMusic.class, onlineMusicDao);
    }
    
    public void clear() {
        musicDaoConfig.clearIdentityScope();
        onlineMusicDaoConfig.clearIdentityScope();
    }

    public MusicDao getMusicDao() {
        return musicDao;
    }

    public OnlineMusicDao getOnlineMusicDao() {
        return onlineMusicDao;
    }

}
