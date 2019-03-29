package com.music.app.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.music.app.R;
import com.music.app.activity.OnlineMusicActivity;
import com.music.app.activity.SingerListActivity;
import com.music.app.application.AppCache;
import com.music.app.constants.Extras;
import com.music.app.http.HttpCallback;
import com.music.app.http.HttpClient;
import com.music.app.model.OnlineMusic;
import com.music.app.model.OnlineMusicList;
import com.music.app.model.SongListInfo;
import com.music.app.utils.binding.Bind;
import com.music.app.utils.binding.ViewBinder;

import java.util.ArrayList;
import java.util.List;

/**
 * 歌单列表适配器
 *
 * @author .
 */
public class SongPlaylistAdapter extends RecyclerView.Adapter {

    private static final int TYPE_PROFILE = 0;
    private static final int TYPE_MUSIC_LIST = 1;
    private static final int TYPE_FIRST = 2;
    private Activity mContext;
    private List<SongListInfo> mData = new ArrayList<>();
    private AdapterView.OnItemClickListener onItemClickListener;
    private static int duration_len = 0;

    public SongPlaylistAdapter(List<SongListInfo> data, Activity context) {
        mData.addAll(data);
        mData.remove(0);
        mData.remove(1);
        mContext = context;
        initSongInfo();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_PROFILE) {
            return new ViewHolderProfile(LayoutInflater.from(mContext).inflate(R.layout.view_holder_playlist_profile, parent, false));
        } else if (viewType == TYPE_MUSIC_LIST) {
            return new ViewHolderMusicList(LayoutInflater.from(mContext).inflate(R.layout.view_holder_playlist, parent, false));
        } else if (viewType == TYPE_FIRST) {
            return new ViewHolderFirst(LayoutInflater.from(mContext).inflate(R.layout.include_re_ge_xin_ge_layout, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ViewHolderProfile) {
            ViewHolderProfile holderProfile = (ViewHolderProfile) holder;
//            holderProfile.tvProfile.setText(songListInfo.getTitle());
        } else if (holder instanceof ViewHolderMusicList) {
            ViewHolderMusicList holderMusicList = (ViewHolderMusicList) holder;
            holderMusicList.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(null, null,
                                holder.getAdapterPosition()+1,
                                holder.getAdapterPosition()+1);
                    }
                }
            });
            duration_len ++;
            SongListInfo songListInfo = mSongLists.get(position + 1);
            getMusicListInfo(songListInfo, holderMusicList);
            holderMusicList.vDivider.setVisibility(isShowDivider(position) ? View.VISIBLE : View.GONE);
        } else if (holder instanceof ViewHolderFirst) {
            ViewHolderFirst viewHolderFirst = (ViewHolderFirst) holder;
            viewHolderFirst.mReGeBang.setOnClickListener((View v) ->
                startActivity(0)
            );

            viewHolderFirst.mXinGeBang.setOnClickListener((View v) ->
                startActivity(1)
            );

            viewHolderFirst.mSinger.setOnClickListener((View v) -> {
                startActivity(2);
            });
        }
    }

    private List<SongListInfo> mSongLists;

    private void startActivity(int index) {
        if (mSongLists == null || mSongLists.isEmpty()) {
            return;
        }
        if (index == 2) {
            mContext.startActivity(new Intent(mContext, SingerListActivity.class));
        } else {
            Intent intent = new Intent(mContext, OnlineMusicActivity.class);
            SongListInfo songListInfo = mSongLists.get(index);
            intent.putExtra(Extras.MUSIC_LIST_TYPE, songListInfo);
            mContext.startActivity(intent);
        }
    }

    private void initSongInfo() {
        mSongLists = AppCache.get().getSongListInfos();
        if (mSongLists.isEmpty()) {
            String[] titles = mContext.getResources().getStringArray(R.array.online_music_list_title);
            String[] types = mContext.getResources().getStringArray(R.array.online_music_list_type);
            for (int i = 0; i < titles.length; i++) {
                SongListInfo info = new SongListInfo();
                info.setTitle(titles[i]);
                info.setType(types[i]);
                mSongLists.add(info);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_FIRST;
        }else if (mData.get(position-1).getType().equals("#")) {
            return TYPE_PROFILE;
        } else {
            return TYPE_MUSIC_LIST;
        }
    }

    private boolean isShowDivider(int position) {
        return position != mData.size() - 1;
    }

    private void getMusicListInfo(final SongListInfo songListInfo, final ViewHolderMusicList holderMusicList) {
        if (songListInfo.getCoverUrl() == null) {
            holderMusicList.tvMusic1.setTag(songListInfo.getTitle());
            holderMusicList.tvTitle.setText(songListInfo.getTitle());
            holderMusicList.ivCover.setImageResource(R.drawable.default_cover);
            holderMusicList.tvMusic1.setText("1.加载中…");
            holderMusicList.tvMusic2.setText("2.加载中…");
            holderMusicList.tvMusic3.setText("3.加载中…");
            holderMusicList.ivCover.postDelayed(new Runnable() {
                @Override
                public void run() {
                        HttpClient.getSongListInfo(songListInfo.getType(), 3, 0, new HttpCallback<OnlineMusicList>() {
                            @Override
                            public void onSuccess(OnlineMusicList response) {
                                holderMusicList.tvMusic1.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (response == null || response.getSong_list() == null) {
                                            return;
                                        }
                                        if (!songListInfo.getTitle().equals(holderMusicList.tvMusic1.getTag())) {
                                            return;
                                        }
                                        parse(response, songListInfo);
                                        setData(songListInfo, holderMusicList);
                                    }
                                });
                            }

                            @Override
                            public void onFail(Exception e) {
                                Log.e("SongPlayListAdapter", "onFail exception e");
                            }
                        });

                }
            }, duration_len * 1000);
        } else {
            holderMusicList.tvMusic1.setTag(null);
            setData(songListInfo, holderMusicList);
        }
    }

    private void parse(OnlineMusicList response, SongListInfo songListInfo) {
        List<OnlineMusic> onlineMusics = response.getSong_list();
        songListInfo.setCoverUrl(response.getBillboard().getPic_s260());
        if (onlineMusics.size() >= 1) {
            songListInfo.setMusic1(mContext.getString(R.string.song_list_item_title_1,
                    onlineMusics.get(0).getTitle(), onlineMusics.get(0).getArtist_name()));
        } else {
            songListInfo.setMusic1("");
        }
        if (onlineMusics.size() >= 2) {
            songListInfo.setMusic2(mContext.getString(R.string.song_list_item_title_2,
                    onlineMusics.get(1).getTitle(), onlineMusics.get(1).getArtist_name()));
        } else {
            songListInfo.setMusic2("");
        }
        if (onlineMusics.size() >= 3) {
            songListInfo.setMusic3(mContext.getString(R.string.song_list_item_title_3,
                    onlineMusics.get(2).getTitle(), onlineMusics.get(2).getArtist_name()));
        } else {
            songListInfo.setMusic3("");
        }
    }

    private void setData(SongListInfo songListInfo, ViewHolderMusicList holderMusicList) {
        holderMusicList.tvTitle.setText(songListInfo.getTitle());
        holderMusicList.tvMusic1.setText(songListInfo.getMusic1());
        holderMusicList.tvMusic2.setText(songListInfo.getMusic2());
        holderMusicList.tvMusic3.setText(songListInfo.getMusic3());
        if (mContext == null || mContext.isFinishing() || mContext.isDestroyed()) {
            return;
        }

        Glide.with(mContext)
                .load(songListInfo.getCoverUrl())
                .placeholder(R.drawable.default_cover)
                .error(R.drawable.default_cover)
                .into(holderMusicList.ivCover);
    }

    public static class ViewHolderProfile extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_profile)
        private TextView tvProfile;

        public ViewHolderProfile(View view) {
            super(view);
            ViewBinder.bind(this, view);
        }
    }

    public static class ViewHolderMusicList extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_cover)
        private ImageView ivCover;
        @Bind(R.id.tv_music_1)
        private TextView tvMusic1;
        @Bind(R.id.tv_music_2)
        private TextView tvMusic2;
        @Bind(R.id.tv_music_3)
        private TextView tvMusic3;
        @Bind(R.id.v_divider)
        private View vDivider;
        @Bind(R.id.tv_music_title)
        private TextView tvTitle;

        public ViewHolderMusicList(View view) {
            super(view);
            ViewBinder.bind(this, view);
        }
    }

    public static class ViewHolderFirst extends RecyclerView.ViewHolder {
        @Bind(R.id.music_re_ge_bang)
        private ImageView mReGeBang;
        @Bind(R.id.music_xin_ge_bang)
        private ImageView mXinGeBang;
        @Bind(R.id.music_singer)
        private  ImageView mSinger;
        public ViewHolderFirst(View itemView) {
            super(itemView);
            ViewBinder.bind(this, itemView);
        }
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void resetDurationLen() {
        duration_len = 0;
    }
}
