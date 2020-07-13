package com.xjl.emedia.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import com.xjl.emedia.activity.MediaPickerActivity;
import com.xjl.emedia.activity.VideoRecordActivity;
import com.xjl.emedia.bean.MediaPickerBean;
import com.xjl.emedia.builder.EPickerBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by x33664 on 2019/1/25.
 */

public class IntentUtil {

    public static List<MediaPickerBean> parserMediaResultData(int requestCode, Intent data) {
        List<MediaPickerBean> tempBean = new ArrayList<>();
        if (requestCode == EPickerBuilder.getRequestCode() && null != data
                && null != data.getSerializableExtra(MediaPickerActivity.RESULT_LIST)) {
            tempBean.addAll((List) data.getSerializableExtra(MediaPickerActivity.RESULT_LIST));
        }
        return tempBean;
    }

    public static List<MediaPickerBean> parserMediaResultData( Intent data){
        List<MediaPickerBean> tempBean = new ArrayList<>();
        if( null != data && null != data.getSerializableExtra(MediaPickerActivity.RESULT_LIST)){
            tempBean.addAll((List) data.getSerializableExtra(MediaPickerActivity.RESULT_LIST));
        }
        return tempBean;
    }

    /**
     * 有权限的时候正常向下进行 没有权限的时候返回false由开发者自行处理
     * 此处只做权限校验的操作，权限的请求和反馈有用户自行处理
     */

    public static int TAKE_PHOTO_REQUEST_CODE = 20002;

    public static String makePhoto(Activity activity, String imagePath, int request_code) {
        TAKE_PHOTO_REQUEST_CODE = request_code;
        return makePhoto(activity, imagePath);
    }

    private static String currentImageFilePath = "";

    @SuppressLint("NewApi")
    public static String makePhoto(Activity activity, String imagePath) {
        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                activity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();

            File mImageFile = FileUtil.createImageFile(imagePath);
            Intent intent = new Intent();
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Logger.e(activity.getPackageName());
            Uri mImageUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", mImageFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            activity.startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
            return currentImageFilePath = mImageFile.getAbsolutePath();
        }
        return null;
    }

    public static File parserTakedPhoto(Activity activity, boolean isNotify) {
        if (!TextUtils.isEmpty(currentImageFilePath)) {
            File file = new File(currentImageFilePath);
            if (isNotify) {
                activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            }
            return file;
        }
        return null;
    }

    public void startCrop(Activity activity, Uri imageUri, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP"); //剪裁
        intent.setDataAndType(imageUri, "image/*");
        intent.putExtra("scale", true);
        //设置宽高比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //设置裁剪图片宽高
        intent.putExtra("outputX", 340);
        intent.putExtra("outputY", 340);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        //广播刷新相册
        Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentBc.setData(imageUri);
        activity.sendBroadcast(intentBc);
        activity.startActivityForResult(intent, 20002);
    }

    /**
     * 有权限的情况下正常启动，没有权限的情况下自行处理。
     * 此处封装了系统原生的录制视频功能，如果在某些设备上使用自定义录制功能失败那么
     * 可以使用原生的录制视频解决功能上的问题。之后再研究质量上的问题。
     * 这里同样会给出自定义录制视频的封装。
     * 所有录制好的视频在onActivityResult处决定是否要压缩。
     * 此处将压缩和录制进行隔离。
     */

    public static int TAKE_VIDEO_REQUEST_CODE = 20003;

    public static String currentVideoFilePath = "";

    /**
     * 快速使用方法。所有参数默认
     */
    public static String makeVideo(Activity activity, String videoPath) {
        return makeVideo(activity, videoPath, TAKE_VIDEO_REQUEST_CODE);
    }

    public static String makeVideo(Activity activity, String videoPath, int requestCode) {
        TAKE_VIDEO_REQUEST_CODE = requestCode;
        return makeVideo(activity, videoPath, requestCode, 1, 30);
    }

    @SuppressLint("NewApi")
    public static String makeVideo(Activity activity, String videoPath, int requestCode, int videoQuality, int durationSeconds) {
        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && activity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

            Intent intent = new Intent();
            intent.setAction("android.media.action.VIDEO_CAPTURE");
            intent.addCategory("android.intent.category.DEFAULT");

            // 保存录像到指定的路径
            File file = FileUtil.createVideoFile(videoPath);

            Uri uri = Uri.fromFile(file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, durationSeconds);
            if (videoQuality < 0) {
                videoQuality = 0;
            } else if (videoQuality > 1) {
                videoQuality = 1;
            }
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, videoQuality);
            activity.startActivityForResult(intent, requestCode);
            return currentVideoFilePath = file.getAbsolutePath();
        }
        return null;
    }

    /**
     * 得到录制的视频文件 并通知更新多媒体文件。
     */
    public static File parserTakedVideo(Activity activity, boolean isNotify) {
        if (!TextUtils.isEmpty(currentVideoFilePath)) {
            File file = new File(currentVideoFilePath);
            if (isNotify) {
                activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            }
            return file;
        }
        return null;
    }

    /**
     * 得到自定义录制视频的文件 并通知更新多媒体文件
     */
    public static File parserCustomTakedVideo(Activity activity, Intent data, boolean isNotify) {
        if (data != null && !TextUtils.isEmpty(data.getStringExtra(VideoRecordActivity.INTENT_EXTRA_VIDEO_PATH))) {
            File file = new File(data.getStringExtra(VideoRecordActivity.INTENT_EXTRA_VIDEO_PATH));
            if (isNotify) {
                activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            }
            return file;
        }
        return null;
    }


    /**
     * 打开本地图片
     */
    public static void openLocalImage(Activity activity, String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            File file = new File(filePath);
            uri = FileProvider.getUriForFile(activity,
                    activity.getApplicationContext().getPackageName()
                            + ".provider", new File(filePath));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//注意加上这句话

        } else {
            uri = Uri.fromFile(new File(filePath));
        }
        intent.setDataAndType(uri, "image/*");
        activity.startActivity(intent);

    }


    /**
     * 此处与MediaPickerActivity中过滤出来的图片类型相匹配
     */
    public static boolean isImage(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        String endCase = filePath.substring(filePath.lastIndexOf("."));
        if (endCase.equals(".jpeg") ||
                endCase.equals(".png") ||
                endCase.equals(".jpg")) {
            return true;
        }

        return false;
    }

    /**
     * 打开本地图片
     */
    public static void openLocalVideo(Activity activity, String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            File file = new File(filePath);
            uri = FileProvider.getUriForFile(activity,
                    activity.getApplicationContext().getPackageName()
                            + ".provider", new File(filePath));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//注意加上这句话

        } else {
            uri = Uri.fromFile(new File(filePath));
        }
        intent.setDataAndType(uri, "video/*");
        activity.startActivity(intent);

    }


    /**
     * 此处与MediaPickerActivity中过滤出来的图片类型相匹配
     */
    public static boolean isVideo(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        String endCase = filePath.substring(filePath.lastIndexOf("."));
        if (endCase.equals(".mp4") ||
                endCase.equals(".3gp")) {
            return true;
        }

        return false;
    }

    /**
     * 打开失败的时候返回false
     */
    public static boolean openMedia(Activity activity, String path) {
        if (isImage(path)) {
            openLocalImage(activity, path);
            return true;
        } else if (isVideo(path)) {
            openLocalVideo(activity, path);
            return true;
        }
        return false;
    }

    /**
     * 请求获取文件
     */
    public static int TAKE_FILE_REQUEST_CODE = 20005;

    public static int getTakeFileRequestCode() {
        return TAKE_FILE_REQUEST_CODE;
    }

    public static void openFileManager(Activity activity, int custom_requestCode) {
        TAKE_FILE_REQUEST_CODE = custom_requestCode;
        openFileManager(activity);
    }

    public static void openFileManager(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(intent, TAKE_FILE_REQUEST_CODE);
    }





}
