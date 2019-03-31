package com.music.app.executor;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.webkit.MimeTypeMap;

import com.music.app.R;

import com.music.app.application.AppCache;

import com.music.app.utils.FileUtils;
import com.music.app.utils.NetworkUtils;
import com.music.app.utils.Preferences;
import com.music.app.utils.ToastUtils;

/**
 * @author .
 */
public abstract class BaseDownloadMusic implements IExecutor<Void> {
    private Activity mActivity;

    public BaseDownloadMusic(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void execute() {
        checkNetwork();
    }

    private void checkNetwork() {
        boolean mobileNetworkDownload = Preferences.enableMobileNetworkDownload();
        if (NetworkUtils.isActiveNetworkMobile(mActivity) && !mobileNetworkDownload) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.tips);
            builder.setMessage(R.string.download_tips);
            builder.setPositiveButton(R.string.download_tips_sure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    downloadWrapper();
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } else {
            downloadWrapper();
        }
    }

    private void downloadWrapper() {
        onPrepare();
        download();
    }

    /**
     * 下载
     */
    protected abstract void download();

    protected void downloadMusic(String url, String artist, String title, String coverPath) {
        try {
            String fileName = FileUtils.getMp3FileName(artist, title);
            Uri uri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(FileUtils.getFileName(artist, title));
            request.setDescription("正在下载…");
            request.setDestinationInExternalPublicDir(FileUtils.getRelativeMusicDir(), fileName);
            request.setMimeType(MimeTypeMap.getFileExtensionFromUrl(url));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
            // 不允许漫游
            request.setAllowedOverRoaming(false);

            DownloadManager downloadManager = (DownloadManager) AppCache.get().getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager == null) {
                return;
            }

            long id = downloadManager.enqueue(request);
            String musicAbsPath = FileUtils.getMusicDir().concat(fileName);
            DownloadMusicInfo downloadMusicInfo = new DownloadMusicInfo(title, musicAbsPath, coverPath);
            AppCache.get().getDownloadList().put(id, downloadMusicInfo);
        } catch (Throwable th) {
            th.printStackTrace();
            ToastUtils.show("下载失败");
        }
    }
}
