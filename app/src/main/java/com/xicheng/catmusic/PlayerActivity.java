package com.xicheng.catmusic;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.xicheng.catmusic.Interfaces.IPlayViewCtrl;
import com.xicheng.catmusic.Interfaces.IPlayerCtrl;
import com.xicheng.catmusic.constants.Constants;
import com.xicheng.catmusic.beans.ItemBean;
import com.xicheng.catmusic.mvp.model.initData;
import com.xicheng.catmusic.service.MusicPlayService;
import com.xicheng.catmusic.utils.logUtil;

import java.util.List;

import static com.xicheng.catmusic.constants.Constants.ACTION_SERVICE_LAST;
import static com.xicheng.catmusic.constants.Constants.ACTION_SERVICE_NEXT;
import static com.xicheng.catmusic.constants.Constants.ACTION_SERVICE_PLAY;
import static com.xicheng.catmusic.constants.Constants.ACTION_VIEW_LAST;
import static com.xicheng.catmusic.constants.Constants.ACTION_VIEW_NEXT;
import static com.xicheng.catmusic.constants.Constants.ACTION_VIEW_PLAY;
import static com.xicheng.catmusic.constants.Constants.isPlay;
import static com.xicheng.catmusic.constants.Constants.songId;


/**
 * Created by Square
 * Date :2020/6/1
 * Description :
 * Version :
 */
public class PlayerActivity extends Activity {
    String TAG = "PlayerActivity";
    public static final int EXTERNAL_STORAGE_REQ_CODE = 10;
    private List<ItemBean> mData;
    private ImageButton btn_play;
    private ImageButton btn_last;
    private ImageButton btn_next;
    private TextView songTitle;
    private TextView songAuthor;
    private SeekBar seekBar;
    private TextView songTime;
    private TextView songDuration;
    private boolean isTouch = false;
    private PlayerConnection mPlayerConnection;
    private IPlayerCtrl mIPlayCtrl;
    private ImageView musicCover;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        //设置全屏显示
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /*初始化数据*/
        initDate();
        //注册广播接收者
        registerMyReceiver();

        //获取控件
        initView();
        /*初始化界面文字*/
        updateView();

        initBindService();
        //设置事件监听
        initListener();

    }


    /**
     * 绑定服务
     */
    private void initBindService() {
        mPlayerConnection = new PlayerConnection();
        Intent intent = new Intent(this, MusicPlayService.class);
        bindService(intent, mPlayerConnection, BIND_AUTO_CREATE);
    }

    private class PlayerConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIPlayCtrl = (IPlayerCtrl) service;
            mIPlayCtrl.registerPlayViewCtrl(iPlayViewCtrl);
            SeekBarTask seekBarTask = new SeekBarTask();
            seekBarTask.execute();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    private void initListener() {

        //播放按钮
        btn_play.setOnClickListener(new View.OnClickListener() {  //实现继续播放与暂停播放
            @Override
            public void onClick(View v) {
                sendMyBroadcast(ACTION_VIEW_PLAY);
            }
        });


        //上一首
        btn_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMyBroadcast(ACTION_VIEW_LAST);
            }
        });

        //下一首
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMyBroadcast(ACTION_VIEW_NEXT);
            }
        });

        //进度条
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isTouch) {
                    mIPlayCtrl.seekTouch(seekBar.getProgress());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTouch = false;
                mIPlayCtrl.seekChange(seekBar.getProgress());
            }
        });

    }


    private void initDate() {
        //获取数据
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        songId = bundle.getInt("id");
        mData =initData.getDate(this);
    }

    private void initView() {
        //获取“播放/暂停”按钮
        btn_play = findViewById(R.id.bar_play);
        //上一首
        btn_last = findViewById(R.id.bar_last);
        //下一首
        btn_next = findViewById(R.id.bar_next);
        //专辑图片
        musicCover = findViewById(R.id.musicCover);
        //歌名
        songTitle = findViewById(R.id.song_title);
        /*演唱者*/
        songAuthor = findViewById(R.id.song_author);
        /*进度条*/
        seekBar = findViewById(R.id.seek_bar);
        songTime = findViewById(R.id.song_time);
        songDuration = findViewById(R.id.song_duration);

    }

    //更新界面上的文字
    private void updateView() {
        logUtil.d(TAG, "updateView " + songId);
        if (isPlay) {
            btn_play.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_icon, null));
        } else {
            btn_play.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play_icon, null));
        }
        songTitle.setText(mData.get(songId).titles);
        songAuthor.setText(mData.get(songId).author);
        int duration = mData.get(songId).duration / 1000;
        seekBar.setMax(duration);
        String str;
        //封面
        loadingCover(mData.get(songId).url);
        if (duration % 60 < 10) {
            str = "" + duration / 60 + ":0" + duration % 60;
        } else {
            str = "" + duration / 60 + ":" + duration % 60;
        }
        songDuration.setText(str);
    }

    private void loadingCover(String mediaUri) {
        if (!Constants.noSong) {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(mediaUri);
            byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();
            if (picture != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
                musicCover.setImageDrawable(null);
                musicCover.setImageBitmap(bitmap);
            } else {
                musicCover.setImageDrawable(getDrawable(R.drawable.cover_image));
            }
        } else {
            musicCover.setImageDrawable(getDrawable(R.drawable.cover_image));
        }
    }

    /**
     * 发送广播
     *
     * @param action action
     */
    private void sendMyBroadcast(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    @Override
    protected void onDestroy() {                //当前Activity销毁时,停止正在播放的音频,并释放MediaPlayer所占用的资源
        Intent intent = new Intent();
        intent.putExtra("songId", songId);
        setResult(2, intent);
        finish();
        //释放资源
        if (mPlayerConnection != null) {
            //释放资源
            mIPlayCtrl.unRegisterPlayViewCtrl();
            unbindService(mPlayerConnection);
        }
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    private IPlayViewCtrl iPlayViewCtrl = new IPlayViewCtrl() {
        @Override
        public void onSeekChange(int seek) {
            Log.d(TAG, "Seek" + seek);
            if (seek != 0) {
                String str;
                if (seek % 60 < 10) {
                    str = seek / 60 + ":0" + seek % 60;
                } else
                    str = seek / 60 + ":" + seek % 60;
                songTime.setText(str);
            }
        }

        @Override
        public void onSeekUpdate(int seek) {
            if (!isTouch) {
                if (seek != 0) {
                    seek = seek / 1000;
                    String str;
                    seekBar.setProgress(seek);
                    if (seek % 60 < 10) {
                        str = seek / 60 + ":0" + seek % 60;
                    } else
                        str = seek / 60 + ":" + seek % 60;
                    songTime.setText(str);

                }
            }
        }

        @Override
        public void onSeekChangeStop(int seek) {
            isPlay = true;
            isTouch = false;
        }

    };

    /**
     * 注册广播接收者
     */
    private void registerMyReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_SERVICE_LAST);
        intentFilter.addAction(Constants.ACTION_SERVICE_NEXT);
        intentFilter.addAction(Constants.ACTION_SERVICE_PLAY);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }
    //广播
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_SERVICE_LAST:
                    changeView("last");
                    break;
                case ACTION_SERVICE_NEXT:
                    changeView("next");
                    break;
                case ACTION_SERVICE_PLAY:
                    changeView("play");
                    break;
            }
        }

        private void changeView(String view) {
            switch (view) {
                case "last":
                case "next":
                    songTitle.setText(mData.get(songId).titles);
                    songAuthor.setText(mData.get(songId).author);
                    updateView();
                    break;
                case "play":
                    if (isPlay) {
                        btn_play.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_icon, null));
                    } else {
                        btn_play.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play_icon, null));
                    }
                    break;
            }
        }
    };

    class SeekBarTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "后台");
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (isTouch) {
            } else
                mIPlayCtrl.updateSeek();
        }
    }


}
