package com.music.app.fragment;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.music.app.R;
import com.music.app.activity.MusicInfoActivity;
import com.music.app.activity.OnlineMusicActivity;
import com.music.app.adapter.LocalMusicAdapter;
import com.music.app.adapter.OnMoreClickListener;
import com.music.app.application.AppCache;
import com.music.app.constants.Actions;
import com.music.app.constants.Extras;
import com.music.app.constants.Keys;
import com.music.app.constants.RequestCode;
import com.music.app.model.Music;
import com.music.app.model.SongListInfo;
import com.music.app.utils.FileUtils;
import com.music.app.utils.ToastUtils;
import com.music.app.utils.binding.Bind;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地音乐列表
 */
public class LocalMusicFragment extends BaseFragment implements AdapterView.OnItemClickListener, OnMoreClickListener, View.OnClickListener {
    private static final String TAG = "LocalMusicFragment";
    @Bind(R.id.lv_local_music)
    private RecyclerView lvLocalMusic;
    @Bind(R.id.tv_empty)
    private TextView tvEmpty;
    @Bind(R.id.title)
    private TextView mTitle;
    @Bind(R.id.music_re_ge_bang)
    private ImageView mReGeBang;
    @Bind(R.id.music_xin_ge_bang)
    private ImageView mXinGeBang;

    private LocalMusicAdapter mAdapter;
    private LocalBroadcastManager manager;
    private LinearLayoutManager mLinearLayoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_local_music, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTitle.setText("首      页");
        mAdapter = new LocalMusicAdapter(getActivity());
        mAdapter.setOnMoreClickListener(this);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        lvLocalMusic.setLayoutManager(mLinearLayoutManager);
        lvLocalMusic.setAdapter(mAdapter);
        if (getPlayService().getPlayingMusic() != null && getPlayService().getPlayingMusic().getType() == Music.Type.LOCAL) {
//            lvLocalMusic.setSelection(getPlayService().getPlayingPosition());
            mLinearLayoutManager.scrollToPosition(getPlayService().getPlayingPosition());
        }
        updateView();
        registerReceiver();
        setBackGround();
        initSongInfo();
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

    private void setBackGround() {
        View v = (View) (lvLocalMusic.getParent());
        Bitmap bitmap = FileUtils.getBackGroundBitmap(getContext());
        if (bitmap != null) {
            v.setBackgroundDrawable(new BitmapDrawable(bitmap));
        } else {
            v.setBackgroundDrawable(null);
        }
    }

    @Override
    protected void setListener() {
        mAdapter.setOnItemClickListener(this);
        mReGeBang.setOnClickListener(this);
        mXinGeBang.setOnClickListener(this);
    }

    private List<SongListInfo> mSongLists = new ArrayList<>();
    private void initSongInfo() {
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
    }

    @Override
    public void onClick(View v) {
        if (mSongLists == null || mSongLists.isEmpty()) {
            Log.i("onClick(v)", "mSongLists is null or empty");
            return;
        }

        SongListInfo songListInfo;
        Intent intent = new Intent(getContext(), OnlineMusicActivity.class);
        switch (v.getId()) {
            case R.id.music_re_ge_bang:
                songListInfo = mSongLists.get(3);
                intent.putExtra(Extras.MUSIC_LIST_TYPE, songListInfo);
                startActivity(intent);
                break;
            case R.id.music_xin_ge_bang:
                songListInfo = mSongLists.get(5);
                intent.putExtra(Extras.MUSIC_LIST_TYPE, songListInfo);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        if (manager != null) {
            manager.unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    private void updateView() {
        if (AppCache.get().getMusicList().isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
        mAdapter.updatePlayingPosition(getPlayService());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        getPlayService().play(position);
    }

    @Override
    public void onMoreClick(final int position) {
        final Music music = AppCache.get().getMusicList().get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(music.getTitle());
        dialog.setItems(R.array.local_music_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:// 分享
                        shareMusic(music);
                        break;
                    case 1:// 设为铃声
                        requestSetRingtone(music);
                        break;
                    case 2:// 查看歌曲信息
                        MusicInfoActivity.start(getContext(), music);
                        break;
                    case 3:// 删除
                        deleteMusic(music);
                        break;
                    default:
                        break;
                }
            }
        });
        dialog.show();
    }

    public void onItemPlay() {
        updateView();
        if (getPlayService().getPlayingMusic().getType() == Music.Type.LOCAL) {
            lvLocalMusic.smoothScrollToPosition(getPlayService().getPlayingPosition());
        }
    }

    public void onMusicListUpdate() {
        updateView();
    }

    /**
     * 分享音乐
     */
    private void shareMusic(Music music) {
        File file = new File(music.getPath());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    private void requestSetRingtone(final Music music) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(getContext())) {
            ToastUtils.show(R.string.no_permission_setting);
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getContext().getPackageName()));
            startActivityForResult(intent, RequestCode.REQUEST_WRITE_SETTINGS);
        } else {
            setRingtone(music);
        }
    }

    /**
     * 设置铃声
     */
    private void setRingtone(Music music) {
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(music.getPath());
        // 查询音乐文件在媒体库是否存在
        Cursor cursor = getContext().getContentResolver().query(uri, null,
                MediaStore.MediaColumns.DATA + "=?", new String[]{music.getPath()}, null);
        if (cursor == null) {
            return;
        }
        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            String _id = cursor.getString(0);
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.IS_MUSIC, true);
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
            values.put(MediaStore.Audio.Media.IS_PODCAST, false);

            getContext().getContentResolver().update(uri, values, MediaStore.MediaColumns.DATA + "=?",
                    new String[]{music.getPath()});
            Uri newUri = ContentUris.withAppendedId(uri, Long.valueOf(_id));
            RingtoneManager.setActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_RINGTONE, newUri);
            ToastUtils.show(R.string.setting_ringtone_success);
        }
        cursor.close();
    }

    /**
     * 删除音乐
     */
    private void deleteMusic(final Music music) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        String title = music.getTitle();
        String msg = getString(R.string.delete_music, title);
        dialog.setMessage(msg);
        dialog.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File file = new File(music.getPath());
                if (file.delete()) {
                    boolean playing = (music == getPlayService().getPlayingMusic());
                    AppCache.get().getMusicList().remove(music);
                    if (playing) {
                        getPlayService().stop();
                        getPlayService().playPause();
                    } else {
                        getPlayService().updatePlayingPosition();
                    }
                    updateView();

                    // 刷新媒体库
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://".concat(music.getPath())));
                    getContext().sendBroadcast(intent);
                }
            }
        });
        dialog.setNegativeButton(R.string.cancel, null);
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCode.REQUEST_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(getContext())) {
                ToastUtils.show(R.string.grant_permission_setting);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (lvLocalMusic == null) {
            return;
        }
        int position = mLinearLayoutManager.findFirstVisibleItemPosition();
        int offset = (lvLocalMusic.getChildAt(0) == null) ? 0 : lvLocalMusic.getChildAt(0).getTop();
        outState.putInt(Keys.LOCAL_MUSIC_POSITION, position);
        outState.putInt(Keys.LOCAL_MUSIC_OFFSET, offset);
    }

    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        if (lvLocalMusic == null) {
            return;
        }
        lvLocalMusic.post(new Runnable() {
            @Override
            public void run() {
                int position = savedInstanceState.getInt(Keys.LOCAL_MUSIC_POSITION);
                int offset = savedInstanceState.getInt(Keys.LOCAL_MUSIC_OFFSET);
                mLinearLayoutManager.scrollToPositionWithOffset(position, offset);
            }
        });
    }
}
