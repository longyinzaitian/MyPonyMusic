package com.music.app.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.music.app.R;
import com.music.app.executor.NaviMenuExecutor;
import com.music.app.utils.binding.Bind;

public class SettingFragment extends BaseFragment implements View.OnClickListener {

    @Bind(R.id.action_setting)
    private LinearLayout mActionSetting;
    @Bind(R.id.action_background)
    private LinearLayout mActionBackground;
    @Bind(R.id.action_night)
    private LinearLayout mActionNight;
    @Bind(R.id.action_timer)
    private LinearLayout mActionTimer;
    @Bind(R.id.action_exit)
    private LinearLayout mActionExit;
    @Bind(R.id.title)
    private TextView mTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTitle.setText("设      置");
    }

    @Override
    protected void setListener() {
        super.setListener();
        mActionSetting.setOnClickListener(this);
        mActionBackground.setOnClickListener(this);
        mActionNight.setOnClickListener(this);
        mActionTimer.setOnClickListener(this);
        mActionExit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        NaviMenuExecutor.onNavigationItemSelected(v, getActivity());
    }
}
