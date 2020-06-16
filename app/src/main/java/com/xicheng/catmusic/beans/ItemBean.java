package com.xicheng.catmusic.beans;

/**
 * Created by Square
 * Date :2020/6/1
 * Description :
 * Version :
 */
public class ItemBean {

    //歌曲编号
    public int id;
    //歌曲名
    public String titles;
    //歌曲的专辑名
    public String album;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitles() {
        return titles;
    }

    public void setTitles(String titles) {
        this.titles = titles;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    //歌曲的歌手名
    public String author;
    //歌曲文件的路径
    public String url;
    //歌曲的总播放时长
    public int duration;


}
