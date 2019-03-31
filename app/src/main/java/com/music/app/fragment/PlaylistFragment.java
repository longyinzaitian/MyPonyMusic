package com.music.app.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.music.app.R;
import com.music.app.activity.OnlineMusicActivity;
import com.music.app.activity.SearchMusicActivity;
import com.music.app.adapter.SongPlaylistAdapter;
import com.music.app.application.AppCache;
import com.music.app.constants.Actions;
import com.music.app.constants.Extras;
import com.music.app.constants.Keys;
import com.music.app.enums.LoadStateEnum;
import com.music.app.model.SongListInfo;
import com.music.app.utils.FileUtils;
import com.music.app.utils.NetworkUtils;
import com.music.app.utils.ViewUtils;
import com.music.app.utils.binding.Bind;

import java.util.List;

/**
 * 在线音乐
 *
 * @author .
 */
public class PlaylistFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    @Bind(R.id.lv_playlist)
    private RecyclerView lvPlaylist;
    @Bind(R.id.ll_loading)
    private LinearLayout llLoading;
    @Bind(R.id.ll_load_fail)
    private LinearLayout llLoadFail;
    @Bind(R.id.title)
    private TextView mTitle;
    @Bind(R.id.search_rv)
    private RelativeLayout mSearchRv;

    private List<SongListInfo> mSongLists;
    private LocalBroadcastManager manager;
    private SongPlaylistAdapter adapter;
    private LinearLayoutManager mGridLayoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTitle.setText("网 络 歌 曲");
        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            ViewUtils.changeViewState(lvPlaylist, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
            return;
        }

        mSongLists = AppCache.get().getSongListInfos();
        if (mSongLists.isEmpty()) {
            String[] titles = getResources().getStringArray(R.array.online_music_list_title);
            String[] types = getResources().getStringArray(R.array.online_music_list_type);
            for (int i = 0; i < titles.length; i++) {
                SongListInfo info = new SongListInfo();
                info.setTitle(titles[i]);
                info.setType(types[i]);
                mSongLists.add(info);
            }
        }
        adapter = new SongPlaylistAdapter(mSongLists, getActivity());
        mGridLayoutManager = new LinearLayoutManager(getContext());
        lvPlaylist.setLayoutManager(mGridLayoutManager);
        lvPlaylist.setAdapter(adapter);

        registerReceiver();
        setBackGround();
    }

    @Override
    public void onDestroy() {
        if (manager != null) {
            manager.unregisterReceiver(receiver);
        }
        if (adapter != null) {
            adapter.resetDurationLen();
        }
        super.onDestroy();
    }

    private void setBackGround() {
        View v = (View) (lvPlaylist.getParent());
        Bitmap bitmap = FileUtils.getBackGroundBitmap(getContext());
        if (bitmap != null) {
            v.setBackgroundDrawable(new BitmapDrawable(bitmap));
        } else {
            v.setBackgroundDrawable(null);
        }
    }

    private void registerReceiver() {
        manager = LocalBroadcastManager.getInstance(getContext());
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
    protected void setListener() {
        adapter.setOnItemClickListener(this);
        mSearchRv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), SearchMusicActivity.class));
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SongListInfo songListInfo = mSongLists.get(position);
        Intent intent = new Intent(getContext(), OnlineMusicActivity.class);
        intent.putExtra(Extras.MUSIC_LIST_TYPE, songListInfo);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (lvPlaylist == null) {
            return;
        }
        int position = mGridLayoutManager.findFirstVisibleItemPosition();
        int offset = (lvPlaylist.getChildAt(0) == null) ? 0 : lvPlaylist.getChildAt(0).getTop();
        outState.putInt(Keys.PLAYLIST_POSITION, position);
        outState.putInt(Keys.PLAYLIST_OFFSET, offset);
    }

    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        if (lvPlaylist == null) {
            return;
        }

        lvPlaylist.post(new Runnable() {
            @Override
            public void run() {
                int position = savedInstanceState.getInt(Keys.PLAYLIST_POSITION);
                int offset = savedInstanceState.getInt(Keys.PLAYLIST_OFFSET);
                mGridLayoutManager.scrollToPositionWithOffset(position, offset);
            }
        });
    }
}
