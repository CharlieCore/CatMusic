package com.xicheng.catmusic.constants;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import com.xicheng.catmusic.PlayerActivity;

/**
 * Created by Square
 * Date :2020/6/6
 * Description :
 * Version :
 */
public class Constants {
    //view发出的action
    public static final String ACTION_VIEW_PLAY = "action_view_play";
    public static final String ACTION_VIEW_LAST = "action_view_last";
    public static final String ACTION_VIEW_NEXT = "action_view_next";
    public static final String ACTION_VIEW_Id = "action_view_Id";
    //service发出的action
    public static final String ACTION_SERVICE_PLAY = "action_service_play";
    public static final String ACTION_SERVICE_LAST = "action_service_last";
    public static final String ACTION_SERVICE_NEXT = "action_service_next";

    //数据库相关
    public static final String DB_NAME = "music_db";
    public static final String TABLE_NAME = "music_table";

    //播放状态相关
    public static Boolean isPlay = false;
    public static int songId = 0;
    public static String songTitle ;
    public static String songAuthor ;
    public static String songAlbum ;
    public static String songUrl;


    public static Boolean isChange = true;
    public static Boolean isPermission = false;
    public static Boolean noSong = false;
}
