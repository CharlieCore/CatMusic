package com.xicheng.catmusic.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.IBinder;
import android.widget.MediaController;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.xicheng.catmusic.R;
import com.xicheng.catmusic.constants.Constants;
import com.xicheng.catmusic.mvp.presenter.PlayerPresenter;
import com.xicheng.catmusic.utils.logUtil;

/**
 * Created by Square
 * Date :2020/6/1
 * Description :
 * Version :
 */
public class MusicPlayService extends Service {

    private PlayerPresenter mPlayerPresenter;
    static String tag = "MusicPlayService";
    private NotificationManager manager;
    private RemoteViews remoteViews;

    @Override

    public void onCreate() {
        super.onCreate();
        //打开通知
        initNotification();
        //广播接收者
        initReceiver();


    }

    /**
     * 广播接收
     */
    private void initReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_VIEW_LAST);
        intentFilter.addAction(Constants.ACTION_VIEW_NEXT);
        intentFilter.addAction(Constants.ACTION_VIEW_PLAY);
        intentFilter.addAction(Constants.ACTION_VIEW_Id);
        registerReceiver(receiver, intentFilter);
    }


    //通知管理
    private void initNotification() {
        String CHANNEL_ONE_ID = "CHANNEL_ONE_ID";
        String CHANNEL_ONE_NAME = "CHANNEL_ONE_ID";
        if(manager == null)
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(false);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
        }

        //remoteViews = new RemoteViews(getPackageName(), R.layout.notification_normal);
        /*.setCustomContentView(remoteViews)
                .setCustomBigContentView(remoteViews)*/


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ONE_ID)
                .setSmallIcon(R.mipmap.app_icon)
                .build();
        this.startForeground(1, notification);
        logUtil.d(tag, "服务onCreate");
        if (mPlayerPresenter == null) {
            mPlayerPresenter = new PlayerPresenter(this);
            mPlayerPresenter.initPlayer();
        }

    }
    private void viewChange(){
        logUtil.d(tag, "viewChange");
        Bitmap bitmap = loadingCover();
        if(bitmap!=null){
//            remoteViews.setImageViewBitmap(R.id.largeIcon,bitmap);
//            remoteViews.setTextViewText(R.id.appName,Constants.songTitle);

        }else {
            remoteViews.setImageViewResource(R.id.foregroundImage,R.drawable.cover_image);
        }
    }

    private Bitmap loadingCover() {
        if (!Constants.noSong) {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(Constants.songUrl);
            byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();
            if (picture != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
               return bitmap;
            } else {
                return null;
            }
        } else {
           return null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mPlayerPresenter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayerPresenter = null;
        //stopForeground(24);
        unregisterReceiver(receiver);

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            logUtil.d(tag, "接收到广播");
            String action = intent.getAction();
            if (action.equals(Constants.ACTION_VIEW_LAST)) {
                mPlayerPresenter.lastSong();
                viewChange();
            }
            if (action.equals(Constants.ACTION_VIEW_NEXT)) {
                mPlayerPresenter.nextSong();
                viewChange();
            }
            if (action.equals(Constants.ACTION_VIEW_PLAY)) {
                mPlayerPresenter.playOrPause();
            }
            if (action.equals(Constants.ACTION_VIEW_Id)) {
                mPlayerPresenter.playById();
                viewChange();
            }

        }
    };

}
