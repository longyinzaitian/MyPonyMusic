package com.music.app.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.music.app.R;
import com.music.app.adapter.OnMoreClickListener;
import com.music.app.adapter.PlaylistAdapter;
import com.music.app.constants.Actions;
import com.music.app.model.Music;
import com.music.app.storage.DBManager;
import com.music.app.utils.FileUtils;
import com.music.app.utils.binding.Bind;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 播放列表
 */
public class PlaylistActivity extends BaseActivity
        implements AdapterView.OnItemClickListener,
        OnMoreClickListener {
    @Bind(R.id.lv_playlist)
    private ListView lvPlaylist;
    private List<Music> musicList;

    private PlaylistAdapter adapter;
    private LocalBroadcastManager manager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        musicList = DBManager.get().getMusicDao().queryBuilder()
            .list();
        Collections.sort(musicList, new Comparator<Music>() {
            @Override
            public int compare(Music o1, Music o2) {
                return (int)(o1.getId() - o2.getId());
            }
        });

        adapter = new PlaylistAdapter(musicList, getPlayService().getPlayingMusic());
        lvPlaylist.setAdapter(adapter);
        adapter.setIsPlaylist(true);
        adapter.setOnMoreClickListener(this);
        lvPlaylist.setOnItemClickListener(this);
        registerReceiver();
        setBackGround();
    }

    private void setBackGround() {
        Bitmap bitmap = FileUtils.getBackGroundBitmap(PlaylistActivity.this);
        if (bitmap != null) {
            lvPlaylist.setBackgroundDrawable(new BitmapDrawable(bitmap));
        } else {
            lvPlaylist.setBackgroundDrawable(null);
        }
    }

    private void registerReceiver() {
        manager = LocalBroadcastManager.getInstance(PlaylistActivity.this);
        IntentFilter intentFilter = new IntentFilter(Actions.ACTION_CHANGE_BACK_GROUND);
        manager.registerReceiver(receiver, intentFilter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }

            if (intent.getAction().equals(Actions.ACTION_CHANGE_BACK_GROUND)) {
                setBackGround();
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (musicList == null || musicList.isEmpty()) {
            return;
        }

        getPlayService().play(musicList.get(position));
        adapter.setPlayMusic(musicList.get(position));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onMoreClick(int position) {
        if (musicList == null || musicList.isEmpty()) {
            return;
        }

        String[] items = new String[]{"移除"};
        Music music = musicList.get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(music.getTitle());
        dialog.setItems(items, (dialog1, which) -> {
            DBManager.get().getMusicDao().delete(music);
            musicList.remove(music);
            adapter.notifyDataSetChanged();
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        if (manager != null) {
            manager.unregisterReceiver(receiver);
        }
        super.onDestroy();
    }
}
