package com.music.app.executor;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.music.app.R;
import com.music.app.activity.ChangeBackgroundActivity;
import com.music.app.activity.CollectionActivity;
import com.music.app.activity.SettingActivity;
import com.music.app.application.AppCache;
import com.music.app.service.PlayService;
import com.music.app.service.QuitTimer;
import com.music.app.utils.Preferences;
import com.music.app.utils.ToastUtils;

/**
 * 导航菜单执行器
 *
 * @author .
 */
public class NaviMenuExecutor {

    public static boolean onNavigationItemSelected(View v, Activity activity) {
        switch (v.getId()) {
            case R.id.action_setting:
                startActivity(activity, SettingActivity.class);
                break;
            case R.id.action_night:
                nightMode(activity);
                break;
            case R.id.action_timer:
                timerDialog(activity);
                break;
            case R.id.action_exit:
                exit(activity);
                break;
            case R.id.action_background:
                startActivity(activity, ChangeBackgroundActivity.class);
                break;
            case R.id.action_collect:
                startActivity(activity, CollectionActivity.class);
                break;
            default:
                break;
        }
        return false;
    }

    private static void startActivity(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
    }

    private static void nightMode(final Activity activity) {
        Preferences.saveNightMode(!Preferences.isNightMode());
        activity.recreate();
    }

    private static void timerDialog(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.menu_timer)
                .setItems(activity.getResources().getStringArray(R.array.timer_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int[] times = activity.getResources().getIntArray(R.array.timer_int);
                        startTimer(activity, times[which]);
                    }
                })
                .show();
    }

    private static void startTimer(Context context, int minute) {
        QuitTimer.getInstance().start(minute * 60 * 1000);
        if (minute > 0) {
            ToastUtils.show(context.getString(R.string.timer_set, String.valueOf(minute)));
        } else {
            ToastUtils.show(R.string.timer_cancel);
        }
    }

    private static void exit(Activity activity) {
        Preferences.clearUserInfo();
        activity.finish();
        PlayService service = AppCache.get().getPlayService();
        if (service != null) {
            service.quit();
        }
    }
}
