package com.music.app.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.music.app.R;
import com.music.app.adapter.OnMoreClickListener;
import com.music.app.adapter.OnlineMusicAdapter;
import com.music.app.executor.DownloadOnlineMusic;
import com.music.app.executor.ShareOnlineMusic;
import com.music.app.model.OnlineMusic;
import com.music.app.storage.DBManager;
import com.music.app.utils.FileUtils;
import com.music.app.utils.ToastUtils;
import com.music.app.utils.binding.Bind;
import com.music.app.widget.AutoLoadListView;

import java.io.File;
import java.util.List;

public class CollectionActivity extends BaseActivity implements OnMoreClickListener {

    @Bind(R.id.collection_list)
    private AutoLoadListView mRecyclerList;

    private List<OnlineMusic> mMusicList;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading));
        setListData();
    }

    private void setListData() {
        mMusicList =  DBManager.get().getOnlineMusicDao().queryBuilder().list();
        if (mMusicList == null) {
            return;
        }
        OnlineMusicAdapter mAdapter = new OnlineMusicAdapter(mMusicList);
        mAdapter.setOnMoreClickListener(this);
        mRecyclerList.setAdapter(mAdapter);
    }

    @Override
    public void onMoreClick(int position) {
        final OnlineMusic onlineMusic = mMusicList.get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(mMusicList.get(position).getTitle());
        String path = FileUtils.getMusicDir() + FileUtils.getMp3FileName(onlineMusic.getArtist_name(), onlineMusic.getTitle());
        File file = new File(path);
        int itemsId = file.exists() ? R.array.online_music_dialog_without_download : R.array.online_music_dialog;
        dialog.setItems(itemsId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:// 分享
                        share(onlineMusic);
                        break;
                    case 1:// 查看歌手信息
                        artistInfo(onlineMusic);
                        break;
                    case 2:// 收藏
                        ToastUtils.show("已收藏");
                        break;
                    case 3:// 下载
                        download(onlineMusic);
                        break;
                    default:
                        break;
                }
            }
        });
        dialog.show();
    }

    private void share(final OnlineMusic onlineMusic) {
        new ShareOnlineMusic(this, onlineMusic.getTitle(), onlineMusic.getSong_id()) {
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

    private void artistInfo(OnlineMusic onlineMusic) {
        ArtistInfoActivity.start(this, onlineMusic.getTing_uid());
    }

    private void download(final OnlineMusic onlineMusic) {
        new DownloadOnlineMusic(this, onlineMusic) {
            @Override
            public void onPrepare() {
                mProgressDialog.show();
            }

            @Override
            public void onExecuteSuccess(Void aVoid) {
                mProgressDialog.cancel();
                ToastUtils.show(getString(R.string.now_download, onlineMusic.getTitle()));
            }

            @Override
            public void onExecuteFail(Exception e) {
                mProgressDialog.cancel();
                ToastUtils.show(R.string.unable_to_download);
            }
        }.execute();
    }
}
