package me.wcy.music.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import me.wcy.music.R;
import me.wcy.music.fragment.MenuBackgroundFragment;

/**
 * @author 15361
 * @date 2018/3/25
 */

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
}
