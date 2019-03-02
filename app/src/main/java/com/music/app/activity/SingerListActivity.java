package com.music.app.activity;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.music.app.R;
import com.music.app.adapter.SingerListAdapter;
import com.music.app.utils.binding.Bind;

public class SingerListActivity extends BaseActivity {

    @Bind(R.id.singer_list)
    private RecyclerView mSingerListView;

    private String[] mSingerListData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singer_list);

        setSingerListData();
        setListView();
    }

    private void setSingerListData() {
        mSingerListData = getResources().getStringArray(R.array.singer_list);
    }

    private void setListView() {
        SingerListAdapter adapter = new SingerListAdapter(SingerListActivity.this, mSingerListData);
        mSingerListView.setLayoutManager(new LinearLayoutManager(SingerListActivity.this));
        mSingerListView.addItemDecoration(new DividerItemDecoration(SingerListActivity.this, DividerItemDecoration.VERTICAL));
        mSingerListView.setAdapter(adapter);
    }
}
