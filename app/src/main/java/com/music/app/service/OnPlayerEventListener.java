package com.music.app.service;

import com.music.app.model.Music;

/**
 * 播放进度监听器
 * @author .
 */
public interface OnPlayerEventListener {

    /**
     * 切换歌曲
     * @param music music
     */
    void onChange(Music music);

    /**
     * 继续播放
     */
    void onPlayerStart();

    /**
     * 暂停播放
     */
    void onPlayerPause();

    /**
     * 更新进度
     * @param progress int
     */
    void onPublish(int progress);

    /**
     * 缓冲百分比
     * @param percent int
     */
    void onBufferingUpdate(int percent);

    /**
     * 更新定时停止播放时间
     * @param remain int
     */
    void onTimer(long remain);

    /**
     * 列表更新
     */
    void onMusicListUpdate();
}
