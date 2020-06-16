package com.xicheng.catmusic.Interfaces;

public interface IPlayerCtrl {

    /**
     * 绑定ui控制
     * @param playViewCtrl
     */
    void registerPlayViewCtrl(IPlayViewCtrl playViewCtrl);
    /**
     * 取消ui控制权
     */
    void unRegisterPlayViewCtrl();

    /**
     * 进度
     * @param seek
     */
    void seekTouch(int seek);

    void updateSeek();

    void seekChange(int seek);
}
