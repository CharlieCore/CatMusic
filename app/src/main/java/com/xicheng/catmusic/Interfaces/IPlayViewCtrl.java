package com.xicheng.catmusic.Interfaces;

/**
 * Created by Square
 * Date :2020/6/4
 * Description : 播放状态界面控制
 * Version :
 */
public interface IPlayViewCtrl {
    /**
     * 进度改变
     * @param seek 进度值
     */
    void onSeekChange(int seek);
    void onSeekUpdate(int seek);
    void onSeekChangeStop(int seek);

}
