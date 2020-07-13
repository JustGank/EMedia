package com.xjl.emedia.builder;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.text.TextUtils;

import com.xjl.emedia.R;
import com.xjl.emedia.activity.MediaPickerActivity;

import java.io.Serializable;

/**
 * Created by x33664 on 2019/1/24.
 */

public class EPickerBuilder {

    private Activity activity;

    public EPickerBuilder(Activity activity) {
        this.activity = activity;
    }

    /**
     * 设置请求码
     */
    private static int RequestCode = 20001;

    public static int getRequestCode() {
        return RequestCode;
    }

    /**
     * 如果是1就是单选 需要处理单选流程 如果大于1就是多选走多选处理流程
     */
    private int max_chose_num = 1;

    public EPickerBuilder setMaxChoseNum(int num) {
        if (num <= 0) {
            try {
                throw new Exception("Selected num must bigger than zero.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.max_chose_num = num;
        return this;
    }

    /**
     * 设置选择的类型 分为纯图片 视频 和全部
     */
    private PickerType pickerType = PickerType.PHOTO_VIDEO;

    public EPickerBuilder setPickerType(PickerType pickerType) {
        this.pickerType = pickerType;
        return this;
    }

    /**
     * 设置头部和底部两个条的背景颜色
     */
    private int resSubjectBackground = R.color.subject_background;

    public EPickerBuilder setResSubjectBg(int resSubjectBackground) {
        this.resSubjectBackground = resSubjectBackground;
        return this;
    }

    /**
     * 除选择外所有文字的颜色
     */
    private int subjectTextColor = android.R.color.white;

    public EPickerBuilder subjectTextColor(int subjectTextColor) {
        this.subjectTextColor = subjectTextColor;
        return this;
    }

    /**
     * 设置返回箭头的图片样式
     */
    private int backImgRes;

    public EPickerBuilder setBackImgRes(int backImgRes) {
        this.backImgRes = backImgRes;
        return this;
    }

    /**
     * 是否开启压缩
     */
    private boolean openCompress = false;
    private String outputPath = "";

    public EPickerBuilder openCompress(boolean openCompress, String outputPath) {
        if (TextUtils.isEmpty(outputPath)) {
            this.openCompress = false;
        } else {
            this.openCompress = openCompress;
            this.outputPath = outputPath;
        }
        return this;
    }

    /**
     * 压缩是否开启Dialog
     */
    private Class dialog_class = ProgressDialog.class;

    public EPickerBuilder setProgressDialogClass(Class<? extends Dialog> dialogClass) {
        this.dialog_class = dialogClass;
        return this;
    }

    /**
     * 设置压缩参数
     */
    private int compressWidth = 1080, compressHeight = 1920;

    public EPickerBuilder setCompressParams(int compressWidth, int compressHeight) {
        this.compressWidth = compressWidth;
        this.compressHeight = compressHeight;
        return this;
    }

    /**
     * 图片文件大小最大值
     */
    private long photo_max_size = 4 * 1024 * 1024;

    public EPickerBuilder setFilterPhotoMaxSize(int unitM) {
        this.photo_max_size = unitM * 1024 * 1024;
        return this;
    }

    /**
     * 视频文件大小最大值
     */
    private long video_max_size = 30 * 1024 * 1024;

    public EPickerBuilder setFilterVideoMaxSize(int unitM) {
        this.video_max_size = unitM * 1024 * 1024;
        return this;
    }

    /**
     * 超过大小限制的文件是否显示
     */
    private boolean overSizeVisible = true;

    public EPickerBuilder overSizeVisible(boolean overSizeVisible) {
        this.overSizeVisible = overSizeVisible;
        return this;
    }


    /**
     * 是否开启预览
     */

    private boolean openPreview = false;

    public EPickerBuilder setOpenPreview(boolean openPreview) {
        this.openPreview = openPreview;
        return this;
    }

    public boolean getOpenPreview() {
        return openPreview;
    }

    /**
     * 是否设置PreviewActivity 跳转到自定义页面进行已选照片的预览
     */
    private Class<? extends Activity> previewActivity = null;

    public EPickerBuilder setPreviewActivity(Class<? extends Activity> previewActivity) {
        this.previewActivity = previewActivity;
        return this;
    }


    /**
     * 是否开启Glide内存缓存
     */

    private boolean openSkipMemoryCache = false;

    public EPickerBuilder setOpenSkipMemoryCache(boolean openSkipMemoryCache) {
        this.openSkipMemoryCache = openSkipMemoryCache;
        return this;
    }

    public boolean getOpenSkipMemoryCache() {
        return openSkipMemoryCache;
    }

    /**
     *  是否开启底部更多操作
     */
    private boolean openBottomMoreOperate = false;

    public EPickerBuilder setOpenBottomMoreOperate(boolean openBottomMoreOperate) {
        this.openBottomMoreOperate = openBottomMoreOperate;
        return this;
    }

    /**
     * 开启跳转
     */
    public void startPicker(int requestCode) {
        this.RequestCode = requestCode;
        startPicker();
    }

    /**
     * 设置每行显示图片的个数
     */
    private int rowNum = 4;

    public EPickerBuilder setrowNum(int rowNum) {
        this.rowNum = rowNum;
        return this;
    }

    public int getRowNum() {
        return this.rowNum;
    }

    /**
     * 设置屏幕朝向 强制横屏 强制竖屏 跟随系统 默认跟随系统
     * 传系统值就可以
     */
    private int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    public EPickerBuilder setScreenOrientation(int orientation) {
        this.orientation = orientation;
        return this;
    }

    public int getScreenOrientation() {
        return this.orientation;
    }

    public void startPicker() {
        Intent intent = new Intent(activity, MediaPickerActivity.class);
        intent.putExtra("max_chose_num", max_chose_num);
        intent.putExtra("pickerType", pickerType);
        intent.putExtra("resSubjectBackground", resSubjectBackground);
        intent.putExtra("subjectTextColor", subjectTextColor);
        intent.putExtra("backImgRes", backImgRes);
        intent.putExtra("openCompress", openCompress);
        intent.putExtra("outputPath", outputPath);
        intent.putExtra("dialog_class", dialog_class);
        intent.putExtra("compressWidth", compressWidth);
        intent.putExtra("compressHeight", compressHeight);
        intent.putExtra("photo_max_size", photo_max_size);
        intent.putExtra("video_max_size", video_max_size);
        intent.putExtra("overSizeVisible", overSizeVisible);
        intent.putExtra("openPreview", openPreview);
        intent.putExtra("previewActivity", previewActivity);
        intent.putExtra("openSkipMemoryCache", openSkipMemoryCache);
        intent.putExtra("openBottomMoreOperate", openBottomMoreOperate);
        intent.putExtra("rowNum", rowNum);
        intent.putExtra("orientation", orientation);
        this.activity.startActivityForResult(intent, RequestCode);
    }


    public enum PickerType implements Serializable {
        ONLY_PHOTO, ONLY_VIDEO, PHOTO_VIDEO
    }

}

