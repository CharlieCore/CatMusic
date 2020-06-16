package com.xicheng.catmusic.mvp.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.MediaStore;

import com.xicheng.catmusic.constants.Constants;
import com.xicheng.catmusic.beans.ItemBean;
import com.xicheng.catmusic.utils.logUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Square
 * Date :2020/6/1
 * Description :
 * Version :
 */
public class initData {
    static String TAG = "initData";
    private static List<ItemBean> mData;
    private static final int EXTERNAL_STORAGE_REQ_CODE = 1;
    static Context mContext;

    public static List<ItemBean> getDate(Context context) {
        mContext = context;
        if (mData == null) {
            initDate();
            SharedPreferences catMusic_setting = mContext.getSharedPreferences("catMusic_setting", Context.MODE_PRIVATE);
            Constants.songId = catMusic_setting.getInt("songId", 0);
            ;
            logUtil.d(TAG, "初始化数据: songId = " + Constants.songId);
        }
        return mData;
    }

    //初始化数据
    private static void initDate() {
        //创建数据集合
        mData = new ArrayList<>();

        if (Constants.isPermission) {
            ArrayList<HashMap<String, Object>> musicList = scanAllAudioFiles();
            //创建数据
            for (int i = 0; i < musicList.size(); i++) {
                //创建对象
                ItemBean data = new ItemBean();
                data.id = i;
                data.titles = (String) musicList.get(i).get("musicTitle");
                data.album = (String) musicList.get(i).get("musicAlbum");
                data.author = (String) musicList.get(i).get("music_author");
                data.url = (String) musicList.get(i).get("musicFileUrl");
                data.duration = (int) musicList.get(i).get("music_duration");
                mData.add(data);
            }
        }
        if (mData.size() == 0) {
            Constants.noSong = true;
            ItemBean data0 = new ItemBean();
            data0.id = 0;
            data0.titles = "光辉岁月";
            data0.album = "光辉岁月";
            data0.author = "beyond";
            data0.url = "musicFileUrl";
            data0.duration = 1000;
            mData.add(data0);
        }
        Constants.songUrl = mData.get(Constants.songId).url;
    }


    //扫描SD卡中的音频文件
    public static ArrayList<HashMap<String, Object>> scanAllAudioFiles() {
        //生成动态集合，用于存储数据
        ArrayList<HashMap<String, Object>> myList = new ArrayList<HashMap<String, Object>>();

        //查询媒体数据库
        Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        //遍历媒体数据库
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {

                //歌曲编号
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                //歌曲名
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                //歌曲的专辑名：MediaStore.Audio.Media.ALBUM
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                //歌曲的歌手名： MediaStore.Audio.Media.ARTIST
                String author = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                //歌曲文件的路径 ：MediaStore.Audio.Media.DATA
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                //歌曲的总播放时长 ：MediaStore.Audio.Media.DURATION
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                //歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
                Long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));

                if (size > 1024 * 800) {//如果文件大小大于800K，将该文件信息存入到map集合中
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("musicId", id);
                    map.put("musicTitle", title);
                    map.put("musicFileUrl", url);
                    map.put("musicAlbum", album);
                    map.put("music_author", author);
                    map.put("music_duration", duration);
                    myList.add(map);
                }
                cursor.moveToNext();
            }
        }
        //返回存储数据的集合
        return myList;
    }

}
