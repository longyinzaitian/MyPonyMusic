package me.wcy.music.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import me.wcy.music.constants.Actions;
import me.wcy.music.utils.FileUtils;

/**
 * @author 15361
 * @date 2018/3/25
 */

public abstract class BaseChangeBgFragment extends BaseFragment {
    private LocalBroadcastManager manager;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerReceiver();
        setBackGround();
    }

    private void setBackGround() {
        Bitmap bitmap = FileUtils.getBackGroundBitmap(getActivity());
        setBackGroundBitmap(bitmap);
    }

    /**
     * 设置背景
     * @param bitmap
     */
    protected abstract void setBackGroundBitmap(Bitmap bitmap);

    private void registerReceiver() {
        manager = LocalBroadcastManager.getInstance(getActivity());
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
    public void onDestroy() {
        if (manager != null) {
            manager.unregisterReceiver(receiver);
        }
        super.onDestroy();
    }
}
