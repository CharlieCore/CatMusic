package com.xicheng.catmusic;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xicheng.catmusic.Adapters.ListViewAdapter;
import com.xicheng.catmusic.Interfaces.IPlayerCtrl;
import com.xicheng.catmusic.constants.Constants;
import com.xicheng.catmusic.beans.ItemBean;
import com.xicheng.catmusic.mvp.model.initData;
import com.xicheng.catmusic.service.MusicPlayService;
import com.xicheng.catmusic.utils.logUtil;

import java.util.List;

import static com.xicheng.catmusic.constants.Constants.*;

/**
 * Created by Square
 * Date :2020/6/1
 * Description :
 * Version :
 */
public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private TextView barSongTitle;
    private TextView barSongAuthor;
    private ImageButton barLast;
    private ImageButton barPlay;
    private ImageButton barNext;
    private List<ItemBean> mData;
    private ListViewAdapter listViewAdapter;
    private PlayerConnection mPlayerConnection;
    private IPlayerCtrl mIPlayCtrl;
    private ConstraintLayout barMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //请求权限
        initRequestPermissions();
        //初始化数据
        getData();
        //注册广播接收
        registerMyReceiver();
        //启动服务
        initService();
        //绑定服务
        initBindService();
        //初始化view
        initView();
        //设置监听事件
        initListener();
        updateView();
    }

    private void getData() {
        mData = initData.getDate(this);
    }

    /*
     * 启动服务
     */
    private void initService() {
        startService(new Intent(this, MusicPlayService.class));
    }

    /**
     * 绑定服务
     */
    private void initBindService() {
        Intent intent = new Intent(this, MusicPlayService.class);
        mPlayerConnection = new PlayerConnection();
        bindService(intent, mPlayerConnection, BIND_AUTO_CREATE);
    }

    private class PlayerConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIPlayCtrl = (IPlayerCtrl) service;
            logUtil.d(TAG, "与建立联系");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPlayerConnection = null;
        }
    }


    /**
     * 设置监听事件
     */
    private void initListener() {
        //条目被点击，开始播放
        listViewAdapter.setOnItemClickListener(new ListViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //根据id播放音乐
                sendMyBroadcast(ACTION_VIEW_Id);
                songId = position;
            }
        });

        //播放按钮被点击
        barPlay.setOnClickListener(new View.OnClickListener() {  //实现继续播放与暂停播放
            @Override
            public void onClick(View v) {
                logUtil.d(TAG, "播放按钮被点击");
                sendMyBroadcast(ACTION_VIEW_PLAY);
            }
        });


        //上一首按钮被点击
        barLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMyBroadcast(ACTION_VIEW_LAST);
            }
        });

        //下一首按钮被点击
        barNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMyBroadcast(ACTION_VIEW_NEXT);
            }
        });

        //当底栏被单击时，跳转到播放界面
        barMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("id", songId);
                bundle.putBoolean("isPlayer", isPlay);
                intent.putExtras(bundle);
                startActivityForResult(intent, 2);
            }
        });
    }

    //发送广播
    private void sendMyBroadcast(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    /**
     * 根据当前数据刷新界面
     */
    private void updateView() {
        logUtil.d(TAG, "更新主进程界面 + songId = " + songId);
        barSongTitle.setText(mData.get(songId).titles);
        barSongAuthor.setText(mData.get(songId).author);
        if (isPlay) {
            //更换图标为暂停 1>
            barPlay.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_icon, null));
        } else {
            //更换图标为播放 ||
            barPlay.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play_icon, null));
        }
    }

    /**
     * 从播放界面返回时调用，更新数据
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        initBindService();
        updateView();
    }

    /**
     * 初始化view
     */
    private void initView() {
        logUtil.d(TAG, "初始化主进程界面 + songId = " + songId);
        recyclerView = this.findViewById(R.id.recycler_view);
        barLast = this.findViewById(R.id.bar_last);
        barPlay = this.findViewById(R.id.bar_play);
        barNext = this.findViewById(R.id.bar_next);
        barSongTitle = this.findViewById(R.id.bar_song_title);
        barSongAuthor = this.findViewById(R.id.bar_song_author);
        barMain = findViewById(R.id.bar_main);
        //barSongTitle.setText(mData.get(songId).titles);
        //barSongAuthor.setText(mData.get(songId).author);
        updateView();
        //显示列表界面
        showList(true, false);

    }

    /**
     * 显示首页的列表布局
     *
     * @param isVertical 垂直还是水平
     * @param isReverse  正序还是倒序
     */
    private void showList(boolean isVertical, boolean isReverse) {

        //创建布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //水平还是垂直
        layoutManager.setOrientation(isVertical ? RecyclerView.VERTICAL : RecyclerView.HORIZONTAL);
        //正向还是反向
        layoutManager.setReverseLayout(isReverse);
        //绑定布局管理器
        recyclerView.setLayoutManager(layoutManager);

        //创建适配器
        listViewAdapter = new ListViewAdapter(mData);
        //绑定adapter
        recyclerView.setAdapter(listViewAdapter);

    }

    /**
     * 请求权限
     */
    private void initRequestPermissions() {

        int permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PlayerActivity.EXTERNAL_STORAGE_REQ_CODE);
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Constants.isPermission = true;
        }
    }

    @Override
    protected void onRestart() {
        logUtil.d(TAG, "返回主进程");
        super.onRestart();
        updateView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayerConnection != null) {
//            //释放资源
//            mIPlayCtrl.unRegisterPlayViewCtrl();
            unbindService(mPlayerConnection);
        }
        //unbindService(mPlayerConnection);
        SharedPreferences catMusic_setting = this.getSharedPreferences("catMusic_setting", MODE_PRIVATE);
        SharedPreferences.Editor editor = catMusic_setting.edit();
        editor.putInt("songId", Constants.songId);
        logUtil.d(TAG, "存储songId " + Constants.songId);
        editor.commit();
        unregisterReceiver(mBroadcastReceiver);
    }

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
                    updateView();
                    break;
                case "play":
                    updateView();
                    if (!isPlay) {
                        barPlay.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play_icon, null));
                    } else {
                        barPlay.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_icon, null));
                    }
                    break;
            }
        }
    };

}
