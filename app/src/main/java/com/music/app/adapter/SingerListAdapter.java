package com.music.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.music.app.R;
import com.music.app.activity.SingerMusicListActivity;
import com.music.app.constants.Actions;
import com.music.app.utils.binding.Bind;
import com.music.app.utils.binding.ViewBinder;

/**
 * @author husy
 * @date 2019/3/3
 */
public class SingerListAdapter extends RecyclerView.Adapter<SingerListAdapter.Holder> {

    private String[] mSingerList;
    private Context mContext;

    public SingerListAdapter(Context context, String[] mSingerList) {
        this.mSingerList = mSingerList;
        this.mContext = context;
    }

    @Override
    public SingerListAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SingerListAdapter.Holder(LayoutInflater.from(mContext).inflate(R.layout.item_singer, parent, false));
    }

    @Override
    public void onBindViewHolder(SingerListAdapter.Holder holder, int position) {
        holder.mSingerText.setText(mSingerList[holder.getAdapterPosition()]);
        holder.mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(mContext, SingerMusicListActivity.class);
                intent.putExtra(Actions.ACTION_SEARCH_TEXT, mSingerList[holder.getAdapterPosition()]);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSingerList == null ? 0 : mSingerList.length;
    }

    static class Holder extends RecyclerView.ViewHolder {

        @Bind(R.id.singer_name)
        private TextView mSingerText;

        private View mRootView;

        public Holder(View itemView) {
            super(itemView);
            mRootView = itemView;
            ViewBinder.bind(this, itemView);
        }
    }
}
