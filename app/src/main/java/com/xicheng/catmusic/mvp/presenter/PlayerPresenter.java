package com.xicheng.catmusic.mvp.presenter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.xicheng.catmusic.Interfaces.IPlayViewCtrl;
import com.xicheng.catmusic.Interfaces.IPlayerCtrl;
import com.xicheng.catmusic.MusicWidget;
import com.xicheng.catmusic.R;
import com.xicheng.catmusic.constants.Constants;
import com.xicheng.catmusic.constants.Constants.*;
import com.xicheng.catmusic.beans.ItemBean;
import com.xicheng.catmusic.mvp.model.initData;
import com.xicheng.catmusic.utils.logUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.xicheng.catmusic.constants.Constants.*;


/**
 * Created by Square
 * Date :2020/6/4
 * Description :
 * Version :
 */
public class PlayerPresenter extends Binder implements IPlayerCtrl {

    private static final String TAG = "PlayerPresenter";
    private MediaPlayer player;
    private File file;
    private String url;
    private final Context mContext;
    private List<ItemBean> mData;
    private IPlayViewCtrl iPlayViewCtrl;
    private boolean isLoop = false;

    public PlayerPresenter(Context context) {
        mContext = context;
    }

    @Override
    public void registerPlayViewCtrl(IPlayViewCtrl playViewCtrl) {
        iPlayViewCtrl = playViewCtrl;
    }


    @Override
    public void unRegisterPlayViewCtrl() {

    }

    public void playOrPause() {
        logUtil.d(TAG, "服务点击播放键");
        if (player == null) {
            playNow();
        }
        if (isPlay == false) {
            player.start();
            isPlay = true;
        } else {
            player.pause();
            isPlay = false;
        }
        Constants.songTitle = mData.get(Constants.songId).titles;
        Constants.songAuthor = mData.get(Constants.songId).author;
        songUrl = mData.get(songId).url;
        Constants.isChange = true;
        sendBroadcast(Constants.ACTION_SERVICE_PLAY);
        sendWidgetBroadcast(ACTION_SERVICE_PLAY);
    }

    public void playById(){
        playNow();
        player.start();
        isPlay = true;
        Constants.songTitle = mData.get(Constants.songId).titles;
        Constants.songAuthor = mData.get(Constants.songId).author;
        songUrl = mData.get(songId).url;
        sendBroadcast(ACTION_SERVICE_PLAY);
        sendWidgetBroadcast(ACTION_SERVICE_PLAY);
    }

    public void lastSong() {
        if (mData == null) {
            mData = initData.getDate(mContext);
            url = mData.get(0).url;
            Constants.songId = mData.get(0).id;
        }
        if (Constants.songId > 0) {
            Constants.songId--;
            playNow();
            player.start();
            isPlay = true;
        } else if (Constants.songId == 0) {
            Constants.songId = mData.size() - 1;
            playNow();
            player.start();
            isPlay = true;
        }
        isPlay = true;
        Constants.songTitle = mData.get(Constants.songId).titles;
        Constants.songAuthor = mData.get(Constants.songId).author;
        songUrl = mData.get(songId).url;
        Constants.isChange = true;
        sendBroadcast(Constants.ACTION_SERVICE_LAST);
        sendWidgetBroadcast(ACTION_SERVICE_LAST);
    }


    public void nextSong() {
        RemoteViews remoteViews = new RemoteViews("com.xicheng.catmusic", R.layout.music_widget);
        remoteViews.setTextViewText(R.id.widget_title, "aaa");
        if (mData == null) {
            mData = initData.getDate(mContext);
            url = mData.get(0).url;
            Constants.songId = mData.get(0).id;
        } else if (Constants.songId < mData.size() - 1) {
            Constants.songId++;
            logUtil.d(TAG, "songId ：" + Constants.songId);
            playNow();
            player.start();
            isPlay = true;
        } else if (Constants.songId == mData.size() - 1) {
            Constants.songId = 0;
            playNow();
            player.start();
            isPlay = true;
        }
        isPlay = true;
        Constants.isChange = true;
        Constants.songTitle = mData.get(Constants.songId).titles;
        Constants.songAuthor = mData.get(Constants.songId).author;
        songUrl = mData.get(songId).url;
        sendBroadcast(ACTION_SERVICE_NEXT);
        sendWidgetBroadcast(ACTION_SERVICE_NEXT);
    }




    public void setLoopWay() {
        if (isLoop) {
            player.setLooping(false);
        } else {
            player.setLooping(true);
        }
    }

    @Override
    public void seekTouch(int seek) {
        if (player != null) {
            iPlayViewCtrl.onSeekChange(seek);
        }
    }

    @Override
    public void updateSeek() {
        if (player != null && player.isPlaying()) {
            int seek = player.getCurrentPosition();
            iPlayViewCtrl.onSeekUpdate(seek);
        }
    }

    @Override
    public void seekChange(int seek) {
        if (player != null && player.isPlaying()) {
            player.seekTo(seek * 1000);
            iPlayViewCtrl.onSeekChangeStop(seek);
        } else if (player != null && !player.isPlaying()) {
            player.start();
            player.seekTo(seek * 1000);
            iPlayViewCtrl.onSeekChangeStop(seek);
        } else if (mData == null) {
            mData = initData.getDate(mContext);
            url = mData.get(0).url;
            Constants.songId = mData.get(0).id;
            playNow();
            player.start();
            player.seekTo(seek * 1000);
            iPlayViewCtrl.onSeekChangeStop(seek);
            isPlay = true;
        }
    }


    /**
     * 准备MediaPlayer
     */
    private void playNow() {
        //如果正在播放，停止
        if (player != null && player.isPlaying()) {
            player.stop();
        }
        if (Constants.noSong) {
            Constants.songTitle = mData.get(Constants.songId).titles;
            Constants.songAuthor = mData.get(Constants.songId).author;
            songUrl = mData.get(songId).url;
            player = new MediaPlayer();
            try {
                AssetFileDescriptor afd = mContext.getAssets().openFd("musics/Beyond-光辉岁月.mp3");
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                player.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    logUtil.d(TAG, "播放完毕");
                    nextSong();
                }
            });
        } else {
            /*访问SD卡下的音频文件*/
            //获取要播放的音频文件
            url = mData.get(Constants.songId).url;
            file = new File(url);
            //加载
            if (file.exists()) {
                //如果音频文件存在就创建一个装载该文件的MediaPlayer对象，不存在将做出提示
                //创建MediaPlayer对象,并解析要播放的音频文件
                //try {
                player = MediaPlayer.create(mContext, Uri.parse(url));
                logUtil.d(TAG, "开始播放" + Uri.parse(url));
                Constants.songTitle = mData.get(Constants.songId).titles;
                Constants.songAuthor = mData.get(Constants.songId).author;
                songUrl = mData.get(songId).url;
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        logUtil.d(TAG, "播放完毕");
                        nextSong();
                    }
                });
            } else {
                //提示音频文件不存在
                Toast.makeText(mContext, "要播放的音频文件不存在！", Toast.LENGTH_SHORT).show();
                return;
            }
        }

    }

    public void initPlayer() {
        mData = initData.getDate(mContext);
    }

    private void sendBroadcast(String action) {
        Intent intent = new Intent(action);
        mContext.sendBroadcast(intent);
    }
    private void sendWidgetBroadcast(String action) {
        Intent intent = new Intent(action);
        intent.setComponent(new ComponentName(mContext, MusicWidget.class));
        mContext.sendBroadcast(intent);
    }
}
