package com.music.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.music.app.R;

import com.music.app.application.AppCache;
import com.music.app.model.Music;
import com.music.app.service.PlayService;
import com.music.app.utils.FileUtils;
import com.music.app.utils.binding.Bind;

import com.music.app.utils.CoverLoader;
import com.music.app.utils.binding.ViewBinder;

/**
 * 本地音乐列表适配器
 */
public class LocalMusicAdapter extends RecyclerView.Adapter<LocalMusicAdapter.MusicViewHolder> {
    private OnMoreClickListener mListener;
    private int mPlayingPosition;
    private AdapterView.OnItemClickListener onItemClickListener;
    private Context mContext;

    public LocalMusicAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public LocalMusicAdapter.MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MusicViewHolder(LayoutInflater.from(mContext).inflate(R.layout.view_holder_music, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LocalMusicAdapter.MusicViewHolder holder, int position) {
        if (position == mPlayingPosition) {
            holder.vPlaying.setVisibility(View.VISIBLE);
        } else {
            holder.vPlaying.setVisibility(View.INVISIBLE);
        }
        Music music = AppCache.get().getMusicList().get(position);
        Bitmap cover = CoverLoader.getInstance().loadThumbnail(music);
        holder.ivCover.setImageBitmap(cover);
        holder.tvTitle.setText(music.getTitle());
        String artist = FileUtils.getArtistAndAlbum(music.getArtist(), music.getAlbum());
        holder.tvArtist.setText(artist);
        holder.ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onMoreClick(position);
                }
            }
        });
        holder.vDivider.setVisibility(isShowDivider(position) ? View.VISIBLE : View.GONE);
        holder.mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(null, holder.mRootView,
                            holder.getAdapterPosition(), holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return AppCache.get().getMusicList().size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private boolean isShowDivider(int position) {
        return position != AppCache.get().getMusicList().size() - 1;
    }

    public void updatePlayingPosition(PlayService playService) {
        if (playService.getPlayingMusic() != null && playService.getPlayingMusic().getType() == Music.Type.LOCAL) {
            mPlayingPosition = playService.getPlayingPosition();
        } else {
            mPlayingPosition = -1;
        }
    }

    public void setOnMoreClickListener(OnMoreClickListener listener) {
        mListener = listener;
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.v_playing)
        View vPlaying;
        @Bind(R.id.iv_cover)
        ImageView ivCover;
        @Bind(R.id.tv_title)
        TextView tvTitle;
        @Bind(R.id.tv_artist)
        TextView tvArtist;
        @Bind(R.id.iv_more)
        ImageView ivMore;
        @Bind(R.id.v_divider)
        View vDivider;
        View mRootView;

        public MusicViewHolder(View view) {
            super(view);
            mRootView = view;
            ViewBinder.bind(this, view);
        }
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
