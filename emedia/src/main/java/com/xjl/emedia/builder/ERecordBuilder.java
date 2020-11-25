package com.xjl.emedia.builder;

import android.app.Activity;
import android.content.Intent;

import com.xjl.emedia.activity.VideoRecordActivity;
import com.xjl.emedia.impl.PreOnClickListener;

import java.io.Serializable;

/**
 * Created by x33664 on 2019/2/14.
 */

public class ERecordBuilder implements Serializable {

    private Activity activity;

    public ERecordBuilder(Activity activity) {
        this.activity = activity;
    }

    private static int RequestCode = 20004;

    public static int getRequestCode() {
        return RequestCode;
    }

    /**
     * 默认录制时间0 代表不限制时间  大于0时代表限制录制时间
     */
    private int limitTime = 0;

    public ERecordBuilder setLimitTime(int unitSeconds) {
        if (unitSeconds < 0) {
            this.limitTime = 0;
        } else {
            this.limitTime = unitSeconds+1;
        }
        return this;
    }

    public int getLimitTime() {
        return limitTime;
    }

    /**
     * 设置录像的质量 ALL代表显示 选择分辨率列表 即用户可以自己选择在录制页面选择分辨率
     * 其他的选择后 如果不存在那么会设置为最接近硬件支持的分辨率
     */
    private RecordQuality recordQuality;

    public ERecordBuilder setQuality(RecordQuality recordQuality) {
        this.recordQuality = recordQuality;
        return this;
    }

    public RecordQuality getRecordQuality() {
        return this.recordQuality;
    }

    /**
     * 设置录制最小时间 最小值2秒
     */
    private int recordMinTime = 2;

    public ERecordBuilder setRecordMinTime(int unitSeconds) {
        if (unitSeconds < 2) {
            this.recordMinTime = 2;
        } else {
            this.recordMinTime = unitSeconds;
        }
        return this;
    }

    public int getRecordMinTime() {
        return recordMinTime;
    }

    /**
     * 设置保存的路径
     */
    private String savePath;

    public void startRecord(int requestCode, String savePath) {
        RequestCode = requestCode;
        startRecord(savePath);
    }

    private boolean isShowLight = true;

    public ERecordBuilder setShowLight(boolean showLight) {
        this.isShowLight = showLight;
        return this;
    }

    public boolean getShowLight() {
        return isShowLight;
    }

    private boolean isShowRatio = true;

    public ERecordBuilder setShowRatio(boolean showRatio) {
        this.isShowRatio = showRatio;
        return this;
    }

    public boolean getShowRatio() {
        return isShowRatio;
    }


    private Class<? extends PreOnClickListener> preOnClickListener=null;


    public ERecordBuilder setPreOnClickListener(Class<? extends PreOnClickListener> preOnClickListener){
        this.preOnClickListener=preOnClickListener;
        return this;
    }

    public Class<? extends PreOnClickListener> getPreOnClickListener(){
        return preOnClickListener;
    }



    public void startRecord(String savePath) {
        /**
         * 保证录制时间不小于最小时间
         * */
        if (limitTime != 0 && limitTime < recordMinTime) {
            limitTime = recordMinTime;
        }
        this.savePath = savePath;
        Intent intent = new Intent(activity, VideoRecordActivity.class);
        intent.putExtra("recordMinTime", this.recordMinTime);
        intent.putExtra("limitTime", this.limitTime);
        intent.putExtra("recordQuality", this.recordQuality);
        intent.putExtra("savePath", this.savePath);
        intent.putExtra("isShowLight", this.isShowLight);
        intent.putExtra("isShowRatio", this.isShowRatio);
        intent.putExtra("preOnClickListener",this.preOnClickListener);
        activity.startActivityForResult(intent, RequestCode);
    }

    public String getSavePath() {
        return savePath;
    }

    public enum RecordQuality implements Serializable {
        QUALITY_480P, QUALITY_720P, QUALITY_1080P, QUALITY_2160P, ALL
    }

}
