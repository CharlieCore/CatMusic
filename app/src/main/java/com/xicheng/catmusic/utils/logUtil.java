package com.xicheng.catmusic.utils;

import android.util.Log;

/**
 * Created by Square
 * Date :2020/6/5
 * Description :
 * Version :
 */
public class logUtil {
    private static boolean isShowInfo = true;
    public static void d(String TAG, String msg){
        if (isShowInfo) {
            Log.d(TAG, msg);
        }
    }
}
