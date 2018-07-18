package com.music.app.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import com.music.app.R;

import com.music.app.fragment.MenuBackgroundFragment;

public class ChangeBackgroundActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_back_ground);

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.act_change_back_ground_container,
                        new MenuBackgroundFragment())
                .commitAllowingStateLoss();
    }

    @Override
    protected void setListener() {
        super.setListener();
    }
}
