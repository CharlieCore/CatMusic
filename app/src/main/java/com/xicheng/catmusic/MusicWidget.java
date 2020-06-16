package com.xicheng.catmusic;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.xicheng.catmusic.Interfaces.IPlayerCtrl;
import com.xicheng.catmusic.constants.Constants;
import com.xicheng.catmusic.service.MusicPlayService;

import static com.xicheng.catmusic.constants.Constants.*;

public class MusicWidget extends AppWidgetProvider {

    private static final String TAG = "MainActivity";

    public static final int OPEN_ACT_CODE = 111;
    private static RemoteViews remoteViews;
    private IPlayerCtrl mIPlayCtrl;

    private static boolean isInit = false;


    /**
     * 接收到任意广播时触发
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (!isInit) {
            initService(context);
            //initBindService(context);
            isInit = true;
        }
        Log.e(TAG, "onReceive: " + action);
        if (ACTION_SERVICE_LAST.equals(action)) {
            remoteViews.setImageViewResource(R.id.widget_play, R.drawable.pause_icon);
            // mIPlayCtrl.lastSong();
            Log.e(TAG, "上一首 " + action);
            setActions(context);
            //Constants.isPlay = true;
            Intent mIntent = new Intent();
            mIntent.setAction("service_last_song");
            context.sendBroadcast(mIntent);
            viewChange(context);

        }
        if (ACTION_SERVICE_NEXT.equals(action)) {
            remoteViews.setImageViewResource(R.id.widget_play, R.drawable.pause_icon);
            setActions(context);
            // mIPlayCtrl.nextSong();
            Log.e(TAG, "下一首 " + action);
            viewChange(context);

        }
        if (ACTION_SERVICE_PLAY.equals(action)) {
            if (remoteViews == null) {
                remoteViews = new RemoteViews(context.getPackageName(), R.layout.music_widget);
            }
            if (isPlay) {
                remoteViews.setImageViewResource(R.id.widget_play, R.drawable.pause_icon);
            }else remoteViews.setImageViewResource(R.id.widget_play, R.drawable.play_icon);
            setActions(context);
            viewChange(context);
        }

        ComponentName componentName = new ComponentName(context, MusicWidget.class);
        AppWidgetManager.getInstance(context).
                updateAppWidget(componentName, remoteViews);
    }

    void viewChange(Context context) {
        remoteViews.setTextViewText(R.id.widget_title, Constants.songTitle);
        remoteViews.setTextViewText(R.id.widget_author, Constants.songAuthor);
    }

    ;

    /**
     * 启动服务
     */
    private void initService(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(new Intent(context, MusicPlayService.class));
        } else
            context.startService(new Intent(context, MusicPlayService.class));
        if (remoteViews == null) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.music_widget);
        }
        remoteViews.setTextViewText(R.id.widget_title, Constants.songTitle);
        remoteViews.setTextViewText(R.id.widget_author, Constants.songAuthor);
    }

    /**
     * 绑定服务
     */


    /**
     * 当 widget 更新时触发
     * 用户首次添加时也会被调用
     * 如果定义了widget的configure属性，首次添加时不会被调用
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        if (remoteViews == null) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.music_widget);
        }
        remoteViews.setTextViewText(R.id.widget_title, Constants.songTitle);
        remoteViews.setTextViewText(R.id.widget_author, Constants.songAuthor);
        setActions(context);
        Log.d(TAG, "onUpdate: ");
    }

    /**
     * 当 widget 首次添加或者大小被改变时触发
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     * @param newOptions
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        Log.e(TAG, "onAppWidgetOptionsChanged: ");
    }

    /**
     * 当第1个 widget 的实例被创建时触发。也就是说,如果用户对同一个 widget 增加了两次（两个实例）,
     * 那么onEnabled()只会在第一次增加widget时触发
     *
     * @param context
     */
    @Override
    public void onEnabled(Context context) {
    }

    /**
     * 当最后1个 widget 的实例被删除时触发
     *
     * @param context
     */
    @Override
    public void onDisabled(Context context) {
    }

    /**
     * 当 widget 被删除时
     *
     * @param context
     * @param appWidgetIds
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    /*******************************************************************************************************************/
    /**
     * 更新
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.e(TAG, "updateAppWidget: " + context.getPackageName());
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.music_widget);
        remoteViews.setTextViewText(R.id.widget_title, Constants.songTitle);
        remoteViews.setTextViewText(R.id.widget_author, Constants.songAuthor);
        //openAct(context);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

    }


    /**
     * 点击标题 打开activity
     * 通过PendingIntent 添加一个跳转activity
     *
     * @param context
     */
    private static void setActions(Context context) {
        if (remoteViews == null) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.music_widget);
        }
        //上一首
        Intent intentLast = new Intent(ACTION_VIEW_LAST);
        PendingIntent piLast = PendingIntent.getBroadcast(context, 0, intentLast, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_last, piLast);

        //下一首
        Intent intentNext = new Intent(ACTION_VIEW_NEXT);
        PendingIntent piNext = PendingIntent.getBroadcast(context, 0, intentNext, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_next, piNext);

        //播放或暂停
        Intent intentPlay = new Intent(ACTION_VIEW_PLAY);
        PendingIntent piPlay = PendingIntent.getBroadcast(context, 0, intentPlay, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_play, piPlay);

        //进入主界面
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_to_main, pi);
    }



}