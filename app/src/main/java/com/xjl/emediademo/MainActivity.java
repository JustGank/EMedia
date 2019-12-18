package com.xjl.emediademo;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.xjl.emedia.bean.MediaPickerBean;
import com.xjl.emedia.builder.EPickerBuilder;
import com.xjl.emedia.builder.ERecordBuilder;
import com.xjl.emedia.utils.FileChooseUtil;
import com.xjl.emedia.utils.IntentUtil;
import com.xjl.emedia.utils.PicUtils;

import java.io.File;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private final String TAG = MainActivity.class.getSimpleName();

    private final String cacheDirPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "EMedia";

    private final String cacheDirPathCompress = cacheDirPath + File.separator + "CompressedImages";

    private final String cacheDirPathImage = cacheDirPath + File.separator + "Images";

    private final String cacheDirPathVideos = cacheDirPath + File.separator + "Videos";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);

        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, 1001);
        }

        initView();
    }

    @Override
    public void onClick(View view) {
        String filePath;
        switch (view.getId()) {
            case R.id.start_album:
                new EPickerBuilder(this)
                        .setPickerType(EPickerBuilder.PickerType.PHOTO_VIDEO)
                        .setMaxChoseNum(9)
                        .setFilterPhotoMaxSize(10)
                        .setProgressDialogClass(ProgressDialog.class)
                        .openCompress(true, cacheDirPathCompress)
                        .overSizeVisible(true)
                        .setOpenPreview(true)
                        .setOpenSkipMemoryCache(true)
                        .setOpenBottomMoreOperate(true)

                        .setPreviewActivity(PreviewActivity.class)
                        .startPicker();
                break;
            case R.id.take_photo:
                filePath = IntentUtil.makePhoto(this, cacheDirPathImage);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && TextUtils.isEmpty(filePath)) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                            10001);
                }
                break;
            case R.id.take_video:
                filePath = IntentUtil.makeVideo(this, cacheDirPathVideos);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && TextUtils.isEmpty(filePath)) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                            10001);
                }
                break;
            case R.id.take_video_custom:
                new ERecordBuilder(MainActivity.this)
                        .setRecordMinTime(3)
                        .setLimitTime(0)
                        .setQuality(ERecordBuilder.RecordQuality.ALL)
                        .setShowLight(false)
                        .setShowRatio(false)
                        .startRecord(cacheDirPathVideos);
                break;
            case R.id.take_file:
                IntentUtil.openFileManager(this);
                break;
        }
    }

    private void initView() {
        findViewById(R.id.start_album).setOnClickListener(MainActivity.this);
        findViewById(R.id.take_photo).setOnClickListener(MainActivity.this);
        findViewById(R.id.take_video).setOnClickListener(MainActivity.this);
        findViewById(R.id.take_video_custom).setOnClickListener(MainActivity.this);
        findViewById(R.id.take_file).setOnClickListener(MainActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "  requestCode=" + requestCode + "    resultCode=" + resultCode + " data is null=" + (data == null));


        if (resultCode == RESULT_CANCELED) {
            return;
        }


        File temp;
        if (requestCode == EPickerBuilder.getRequestCode()) {
            List<MediaPickerBean> mediaList = IntentUtil.parserMediaResultData(requestCode, data);
            for (int i = 0; i < mediaList.size(); i++) {
                Log.e(TAG, mediaList.get(i).getMediaFilePath());
            }
        } else if (requestCode == IntentUtil.TAKE_PHOTO_REQUEST_CODE) {
            temp = IntentUtil.parserTakedPhoto(this, true);
            if (temp != null)
            {
                Log.e(TAG, temp.exists() ? "Image take success,file path:" + temp.getAbsolutePath() : "Image file not exist!");

                PicUtils.readPictureDegree(temp.getAbsolutePath());


            }

        } else if (requestCode == IntentUtil.TAKE_VIDEO_REQUEST_CODE) {
            temp = IntentUtil.parserTakedVideo(this, true);
            if (temp != null)
                Log.e(TAG, temp.exists() ? "Video record success,file path is:" + temp.getAbsolutePath() : "Video file not exist!");
        } else if (requestCode == ERecordBuilder.getRequestCode()) {
            temp = IntentUtil.parserCustomTakedVideo(this, data, true);
            if (temp != null)
                Log.e(TAG, temp.exists() ? "Custom video record success,file path is:" + temp.getAbsolutePath() : "Custom video file not exist!");
        } else if (requestCode == IntentUtil.getTakeFileRequestCode()) {
            if (FileChooseUtil.isDownloadsDocument(data.getData())) {
                Toast.makeText(this, "无效文件", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "  Authority = " + data.getData().getAuthority());
                String filePath = FileChooseUtil.getPathFromUri(this, data.getData());
                Log.e(TAG, filePath + "  Authority=" + data.getData().getAuthority());
            }
        }
    }
}
