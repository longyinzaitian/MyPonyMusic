package com.music.app.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.music.app.R;
import com.music.app.adapter.FragmentAdapter;
import com.music.app.application.AppCache;
import com.music.app.constants.Extras;
import com.music.app.executor.NaviMenuExecutor;
import com.music.app.executor.WeatherExecutor;
import com.music.app.fragment.LocalMusicFragment;
import com.music.app.fragment.PlayFragment;
import com.music.app.fragment.PlaylistFragment;
import com.music.app.fragment.SettingFragment;
import com.music.app.model.Music;
import com.music.app.service.OnPlayerEventListener;
import com.music.app.service.PlayService;
import com.music.app.utils.CoverLoader;
import com.music.app.utils.PermissionReq;
import com.music.app.utils.Preferences;
import com.music.app.utils.SystemUtils;
import com.music.app.utils.ToastUtils;
import com.music.app.utils.binding.Bind;


public class MusicActivity extends BaseActivity implements View.OnClickListener, OnPlayerEventListener,
        NavigationView.OnNavigationItemSelectedListener {
    @Bind(R.id.drawer_layout)
    private DrawerLayout drawerLayout;
    @Bind(R.id.navigation_view)
    private NavigationView navigationView;
    @Bind(R.id.iv_menu)
    private ImageView ivMenu;
    @Bind(R.id.iv_search)
    private ImageView ivSearch;
    @Bind(R.id.tv_local_music)
    private TextView tvLocalMusic;
    @Bind(R.id.tv_online_music)
    private TextView tvOnlineMusic;
    @Bind(R.id.music_container)
    private ViewPager mMusicContainer;
    @Bind(R.id.fl_play_bar)
    private FrameLayout flPlayBar;
    @Bind(R.id.iv_play_bar_cover)
    private ImageView ivPlayBarCover;
    @Bind(R.id.tv_play_bar_title)
    private TextView tvPlayBarTitle;
    @Bind(R.id.tv_play_bar_artist)
    private TextView tvPlayBarArtist;
    @Bind(R.id.iv_play_bar_play)
    private ImageView ivPlayBarPlay;
    @Bind(R.id.iv_play_bar_next)
    private ImageView ivPlayBarNext;
    @Bind(R.id.iv_play_bar_pre)
    private ImageView ivPlayBarPre;
    @Bind(R.id.v_play_bar_playlist)
    private ImageView ivPlayList;
    @Bind(R.id.pb_play_bar)
    private ProgressBar mProgressBar;

    @Bind(R.id.ll_local)
    private LinearLayout mLlLocal;
    @Bind(R.id.icon_local_page_im_view)
    private ImageView mImLocal;
    @Bind(R.id.music_local)
    private TextView mLocalTx;

    @Bind(R.id.ll_network)
    private LinearLayout mLlNetwork;
    @Bind(R.id.icon_network_page_im_view)
    private ImageView mImNetwork;
    @Bind(R.id.music_network)
    private TextView mNetworkTx;

    @Bind(R.id.ll_setting)
    private LinearLayout mLlSetting;
    @Bind(R.id.icon_setting_page_im_view)
    private ImageView mImSetting;
    @Bind(R.id.music_setting)
    private TextView mSettingTx;

    private View vNavigationHeader;
    private LocalMusicFragment mLocalMusicFragment;
    private PlaylistFragment mPlayListFragment;
    private SettingFragment mSettingFragment;
    private Fragment mPreFragment;
    private static final String TAG_LOCAL = "local";
    private static final String TAG_NETWORK = "network";
    private static final String TAG_SETTING = "setting";
    private PlayFragment mPlayFragment;
    private boolean isPlayFragmentShow = false;
    private MenuItem timerItem;

    private SensorManager mSensorManager;
    private boolean isShaking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        if (!checkServiceAlive()) {
            return;
        }

        getPlayService().setOnPlayEventListener(this);

        setupView();
        updateWeather();
        onChangeImpl(getPlayService().getPlayingMusic());
        parseIntent();
        mSensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensorManager.registerListener(mSensorEventListener,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_GAME);
        }

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                if (newState == DrawerLayout.STATE_DRAGGING) {
                    drawerLayout.closeDrawers();
                }
            }
        });

        mLocalMusicFragment = new LocalMusicFragment();
        mPlayListFragment = new PlaylistFragment();
        mSettingFragment = new SettingFragment();
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
        fragmentAdapter.addFragment(mLocalMusicFragment);
        fragmentAdapter.addFragment(mPlayListFragment);
        fragmentAdapter.addFragment(mSettingFragment);
        mMusicContainer.setAdapter(fragmentAdapter);
        mMusicContainer.setOffscreenPageLimit(3);
        mMusicContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    changeBottomMenuTextColor(TAG_LOCAL);
                } else if (position == 1) {
                    changeBottomMenuTextColor(TAG_NETWORK);
                } else if (position == 2) {
                    changeBottomMenuTextColor(TAG_SETTING);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 搖一搖感应器的监听器
     */
    private SensorEventListener mSensorEventListener =
            new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (isShaking){
                        return;
                    }

                    if (Sensor.TYPE_ACCELEROMETER == event.sensor.getType()) {
                        float[] values = event.values;
                        int yuZhi = 8;
                        //监听三个方向上的变化，数据变化剧烈，next()方法播放下一首歌曲*/
                        if (Math.abs(values[0]) > yuZhi && Math.abs(values[1]) > yuZhi
                                && Math.abs(values[2]) > yuZhi) {
                            isShaking = true;
                            if (Preferences.getShakeMusicEnable()) {
                                next();
                            }
                            // 延迟200毫秒 防止抖动
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    isShaking = false;
                                }
                            }, 200);
                        }
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        parseIntent();
    }

    @Override
    protected void setListener() {
        ivMenu.setOnClickListener(this);
        ivSearch.setOnClickListener(this);
        tvLocalMusic.setOnClickListener(this);
        tvOnlineMusic.setOnClickListener(this);
        flPlayBar.setOnClickListener(this);
        ivPlayBarPlay.setOnClickListener(this);
        ivPlayBarNext.setOnClickListener(this);
        ivPlayBarPre.setOnClickListener(this);
        ivPlayList.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);

        mLocalTx.setOnClickListener(this);
        mNetworkTx.setOnClickListener(this);
        mSettingTx.setOnClickListener(this);
        mLlLocal.setOnClickListener(this);
        mLlNetwork.setOnClickListener(this);
        mLlSetting.setOnClickListener(this);
    }

    private void setupView() {
        // add navigation header
        vNavigationHeader = LayoutInflater.from(this).inflate(R.layout.navigation_header, navigationView, false);
        navigationView.addHeaderView(vNavigationHeader);

        // setup view pager
        changeFragment(TAG_LOCAL);
        tvLocalMusic.setSelected(true);
    }

    private void updateWeather() {
        PermissionReq.with(this)
                .permissions(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .result(new PermissionReq.Result() {
                    @Override
                    public void onGranted() {
                        new WeatherExecutor(getPlayService(), vNavigationHeader).execute();
                    }

                    @Override
                    public void onDenied() {
                        ToastUtils.show(R.string.no_permission_location);
                    }
                })
                .request();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(Extras.EXTRA_NOTIFICATION)) {
            showPlayingFragment();
            setIntent(new Intent());
        }
    }

    @Override
    public void onChange(Music music) {
        onChangeImpl(music);
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mPlayFragment.onChange(music);
        }
    }

    @Override
    public void onPlayerStart() {
        ivPlayBarPlay.setSelected(true);
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mPlayFragment.onPlayerStart();
        }
    }

    @Override
    public void onPlayerPause() {
        ivPlayBarPlay.setSelected(false);
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mPlayFragment.onPlayerPause();
        }
    }

    /**
     * 更新播放进度
     */
    @Override
    public void onPublish(int progress) {
        mProgressBar.setProgress(progress);
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mPlayFragment.onPublish(progress);
        }
    }

    @Override
    public void onBufferingUpdate(int percent) {
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mPlayFragment.onBufferingUpdate(percent);
        }
    }

    @Override
    public void onTimer(long remain) {
        if (timerItem == null) {
            timerItem = navigationView.getMenu().findItem(R.id.action_timer);
        }
        String title = getString(R.string.menu_timer);
        timerItem.setTitle(remain == 0 ? title : SystemUtils.formatTime(title + "(mm:ss)", remain));
    }

    @Override
    public void onMusicListUpdate() {
        if (mLocalMusicFragment != null && mLocalMusicFragment.isAdded()) {
            mLocalMusicFragment.onMusicListUpdate();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.iv_menu:
//                drawerLayout.openDrawer(GravityCompat.START);
//                break;
            case R.id.iv_search:
                startActivity(new Intent(this, SearchMusicActivity.class));
                break;
            case R.id.ll_local:
            case R.id.music_local:
                mMusicContainer.setCurrentItem(0);
                break;
            case R.id.ll_network:
            case R.id.music_network:
                mMusicContainer.setCurrentItem(1);
                break;
            case R.id.ll_setting:
            case R.id.music_setting:
                mMusicContainer.setCurrentItem(2);
                break;

            case R.id.fl_play_bar:
                showPlayingFragment();
                break;
            case R.id.iv_play_bar_play:
                play();
                break;
            case R.id.iv_play_bar_next:
                next();
                break;
            case R.id.iv_play_bar_pre:
                pre();
                break;
            case R.id.v_play_bar_playlist:
                Intent intent = new Intent(MusicActivity.this, PlaylistActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void changeBottomMenuTextColor(String tag) {
        mLocalTx.setTextColor(ContextCompat.getColor(MusicActivity.this, R.color.grey));
        mNetworkTx.setTextColor(ContextCompat.getColor(MusicActivity.this, R.color.grey));
        mSettingTx.setTextColor(ContextCompat.getColor(MusicActivity.this, R.color.grey));
        mImLocal.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);
        mImNetwork.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);
        mImSetting.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);

        if (tag.equals(TAG_LOCAL)) {
            mLocalTx.setTextColor(ContextCompat.getColor(MusicActivity.this, R.color.color_blue));
            mImLocal.setColorFilter(getResources().getColor(R.color.color_blue), PorterDuff.Mode.SRC_IN);
        } else if (tag.equals(TAG_NETWORK)) {
            mNetworkTx.setTextColor(ContextCompat.getColor(MusicActivity.this, R.color.color_blue));
            mImNetwork.setColorFilter(getResources().getColor(R.color.color_blue), PorterDuff.Mode.SRC_IN);
        } else if (tag.equals(TAG_SETTING)) {
            mSettingTx.setTextColor(ContextCompat.getColor(MusicActivity.this, R.color.color_blue));
            mImSetting.setColorFilter(getResources().getColor(R.color.color_blue), PorterDuff.Mode.SRC_IN);
        }
    }

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private void changeFragment(String tag) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment != null && fragment == mPreFragment) {
            Log.i("changeFragment()", "fragment == mPreFragment");
            return;
        }
        if (fragment == null) {
            if (tag.equals(TAG_LOCAL)) {
                fragment = new LocalMusicFragment();
                mLocalMusicFragment = (LocalMusicFragment) fragment;
            } else if (tag.equals(TAG_NETWORK)) {
                fragment = new PlaylistFragment();
            } else if (tag.equals(TAG_SETTING)) {
                fragment = new SettingFragment();
            }

            if (mPreFragment != null) {
                fragmentTransaction.hide(mPreFragment).add(R.id.music_container, fragment, tag).commitAllowingStateLoss();
            } else {
                fragmentTransaction.add(R.id.music_container, fragment, tag).commitAllowingStateLoss();
            }
        } else {
            if (mPreFragment != null) {
                fragmentTransaction.hide(mPreFragment).show(fragment).commitAllowingStateLoss();
            } else {
                fragmentTransaction.show(fragment).commitAllowingStateLoss();
            }
        }
        mPreFragment = fragment;
    }

    @Override
    public void recreate() {
        if (fragmentManager != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (mPreFragment != null) {
                fragmentTransaction.hide(mPreFragment).commitAllowingStateLoss();
            }
        }
        super.recreate();
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        drawerLayout.closeDrawers();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                item.setChecked(false);
            }
        }, 500);
        return NaviMenuExecutor.onNavigationItemSelected(null, this);
    }

    private void onChangeImpl(Music music) {
        if (music == null) {
            return;
        }

        Bitmap cover = CoverLoader.getInstance().loadThumbnail(music);
        ivPlayBarCover.setImageBitmap(cover);
        tvPlayBarTitle.setText(music.getTitle());
        tvPlayBarArtist.setText(music.getArtist());
        ivPlayBarPlay.setSelected(getPlayService().isPlaying() || getPlayService().isPreparing());
        mProgressBar.setMax((int) music.getDuration());
        mProgressBar.setProgress((int) getPlayService().getCurrentPosition());

        if (mLocalMusicFragment != null && mLocalMusicFragment.isAdded()) {
            mLocalMusicFragment.onItemPlay();
        }
    }

    private void play() {
        getPlayService().playPause();
    }

    private void next() {
        getPlayService().next();
    }

    private void pre() {
        getPlayService().prev();
    }

    private void showPlayingFragment() {
        if (isPlayFragmentShow) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_slide_up, 0);
        if (mPlayFragment == null) {
            mPlayFragment = new PlayFragment();
            ft.replace(android.R.id.content, mPlayFragment);
        } else {
            ft.show(mPlayFragment);
        }
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = true;
    }

    private void hidePlayingFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.fragment_slide_down);
        ft.hide(mPlayFragment);
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = false;
    }

    @Override
    public void onBackPressed() {
        if (mPlayFragment != null && isPlayFragmentShow) {
            hidePlayingFragment();
            return;
        }
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mLocalMusicFragment != null) {
            mLocalMusicFragment.onSaveInstanceState(outState);
        }
        if (mPlayListFragment != null) {
            mPlayListFragment.onSaveInstanceState(outState);
        }

        if (mSettingFragment != null) {
            mSettingFragment.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mLocalMusicFragment != null) {
                    mLocalMusicFragment.onRestoreInstanceState(savedInstanceState);
                }

                if (mPlayListFragment != null) {
                    mPlayListFragment.onRestoreInstanceState(savedInstanceState);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        PlayService service = AppCache.get().getPlayService();
        if (service != null) {
            service.setOnPlayEventListener(null);
        }

        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mSensorEventListener);
        }
        super.onDestroy();
    }
}
