package com.xjl.emedia.bean;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by x33664 on 2018/11/8.
 */

public class MediaPickerBean implements Serializable, Comparable<MediaPickerBean> {

    public long size;
    public String mediaFilePath;
    public boolean isPicked = false;
    public int type;
    public int data;
    public String duration;

    public MediaPickerBean() {
    }

    ;

    public MediaPickerBean(int createTime, String mediaFilePath, int type, int data) {
        this.size = createTime;
        this.mediaFilePath = mediaFilePath;
        this.data = data;
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMediaFilePath() {
        return mediaFilePath;
    }

    public void setMediaFilePath(String mediaFilePath) {
        this.mediaFilePath = mediaFilePath;
    }

    public boolean isPicked() {
        return isPicked;
    }

    public void setPicked(boolean picked) {
        isPicked = picked;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }


    @Override
    public int compareTo(@NonNull MediaPickerBean o) {
        return o.data - this.data;
    }
}
