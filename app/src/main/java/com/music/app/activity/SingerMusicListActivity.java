package com.music.app.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.music.app.R;
import com.music.app.adapter.OnMoreClickListener;
import com.music.app.adapter.SearchMusicAdapter;
import com.music.app.constants.Actions;
import com.music.app.enums.LoadStateEnum;
import com.music.app.executor.DownloadSearchedMusic;
import com.music.app.executor.PlaySearchedMusic;
import com.music.app.executor.ShareOnlineMusic;
import com.music.app.http.HttpCallback;
import com.music.app.http.HttpClient;
import com.music.app.model.Music;
import com.music.app.model.SearchMusic;
import com.music.app.utils.FileUtils;
import com.music.app.utils.ToastUtils;
import com.music.app.utils.ViewUtils;
import com.music.app.utils.binding.Bind;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SingerMusicListActivity extends BaseActivity  implements
        AdapterView.OnItemClickListener, OnMoreClickListener {

    @Bind(R.id.lv_search_music_list)
    private ListView lvSearchMusic;
    @Bind(R.id.ll_loading)
    private LinearLayout llLoading;
    @Bind(R.id.ll_load_fail)
    private LinearLayout llLoadFail;
    private List<SearchMusic.Song> mSearchMusicList = new ArrayList<>();
    private SearchMusicAdapter mAdapter = new SearchMusicAdapter(mSearchMusicList);
    private ProgressDialog mProgressDialog;
    private LocalBroadcastManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singer_music_list);

        if (!checkServiceAlive()) {
            return;
        }

        lvSearchMusic.setAdapter(mAdapter);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading));
        ((TextView) llLoadFail.findViewById(R.id.tv_load_fail_text)).setText(R.string.search_empty);

        registerReceiver();
        setBackGround();

        String searchText = getIntent() != null ? getIntent().getStringExtra(Actions.ACTION_SEARCH_TEXT): "";
        if (!("").equals(searchText)) {
            onQueryTextSubmit(searchText);
            setTitle(searchText);
        }
    }

    private void setBackGround() {
        Bitmap bitmap = FileUtils.getBackGroundBitmap(SingerMusicListActivity.this);
        if (bitmap != null) {
            lvSearchMusic.setBackgroundDrawable(new BitmapDrawable(bitmap));
        } else {
            lvSearchMusic.setBackgroundDrawable(null);
        }
    }

    private void registerReceiver() {
        manager = LocalBroadcastManager.getInstance(SingerMusicListActivity.this);
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
    protected void onDestroy() {
        if (manager != null) {
            manager.unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    @Override
    protected int getDarkTheme() {
        return R.style.AppThemeDark_Search;
    }

    @Override
    protected void setListener() {
        lvSearchMusic.setOnItemClickListener(this);
        mAdapter.setOnMoreClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_search_music, menu);
//        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
//        searchView.setMaxWidth(Integer.MAX_VALUE);
//        searchView.onActionViewExpanded();
//        searchView.setQueryHint(getString(R.string.search_tips));
//        searchView.setOnQueryTextListener(this);
//        searchView.setSubmitButtonEnabled(true);
//        try {
//            Field field = searchView.getClass().getDeclaredField("mGoButton");
//            field.setAccessible(true);
//            ImageView mGoButton = (ImageView) field.get(searchView);
//            mGoButton.setImageResource(R.drawable.ic_menu_search);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onQueryTextSubmit(String query) {
        ViewUtils.changeViewState(lvSearchMusic, llLoading, llLoadFail, LoadStateEnum.LOADING);
        searchMusic(query);
        return false;
    }

    private void searchMusic(String keyword) {
        HttpClient.searchMusic(keyword, new HttpCallback<SearchMusic>() {
            @Override
            public void onSuccess(SearchMusic response) {
                if (response == null || response.getSong() == null) {
                    ViewUtils.changeViewState(lvSearchMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                    return;
                }
                ViewUtils.changeViewState(lvSearchMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_SUCCESS);
                mSearchMusicList.clear();
                mSearchMusicList.addAll(response.getSong());
                mAdapter.notifyDataSetChanged();
                lvSearchMusic.requestFocus();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        lvSearchMusic.setSelection(0);
                    }
                });
            }

            @Override
            public void onFail(Exception e) {
                ViewUtils.changeViewState(lvSearchMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        new PlaySearchedMusic(this, mSearchMusicList.get(position)) {
            @Override
            public void onPrepare() {
                mProgressDialog.show();
            }

            @Override
            public void onExecuteSuccess(Music music) {
                mProgressDialog.cancel();
                getPlayService().play(music);
                ToastUtils.show(getString(R.string.now_play, music.getTitle()));
            }

            @Override
            public void onExecuteFail(Exception e) {
                mProgressDialog.cancel();
                ToastUtils.show(R.string.unable_to_play);
            }
        }.execute();
    }

    @Override
    public void onMoreClick(int position) {
        final SearchMusic.Song song = mSearchMusicList.get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(song.getSongname());
        String path = FileUtils.getMusicDir() + FileUtils.getMp3FileName(song.getArtistname(), song.getSongname());
        File file = new File(path);
        int itemsId = file.exists() ? R.array.search_music_dialog_no_download : R.array.search_music_dialog;
        dialog.setItems(itemsId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:// 分享
                        share(song);
                        break;
                    case 1:// 下载
                        download(song);
                        break;
                    default:
                        break;
                }
            }
        });

        dialog.show();
    }

    private void share(SearchMusic.Song song) {
        new ShareOnlineMusic(this, song.getSongname(), song.getSongid()) {
            @Override
            public void onPrepare() {
                mProgressDialog.show();
            }

            @Override
            public void onExecuteSuccess(Void aVoid) {
                mProgressDialog.cancel();
            }

            @Override
            public void onExecuteFail(Exception e) {
                mProgressDialog.cancel();
            }
        }.execute();
    }

    private void download(final SearchMusic.Song song) {
        new DownloadSearchedMusic(this, song) {
            @Override
            public void onPrepare() {
                mProgressDialog.show();
            }

            @Override
            public void onExecuteSuccess(Void aVoid) {
                mProgressDialog.cancel();
                ToastUtils.show(getString(R.string.now_download, song.getSongname()));
            }

            @Override
            public void onExecuteFail(Exception e) {
                mProgressDialog.cancel();
                ToastUtils.show(R.string.unable_to_download);
            }
        }.execute();
    }
}
