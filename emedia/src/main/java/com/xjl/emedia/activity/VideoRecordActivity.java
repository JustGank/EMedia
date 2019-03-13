package com.xjl.emedia.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.xjl.emedia.R;
import com.xjl.emedia.builder.ERecordBuilder;
import com.xjl.emedia.widget.CameraPreview;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by x33664 on 2019/2/13.
 */

public class VideoRecordActivity extends Activity {

    private static final String TAG = "VideoRecordActivity";

    public static final int RESULT_CODE_FOR_RECORD_VIDEO_CANCEL = 401;
    public static final int RESULT_CODE_FOR_RECORD_VIDEO_FAILED = 404;
    public static final String INTENT_EXTRA_VIDEO_PATH = "intent_extra_video_path";

    private static final int FOCUS_AREA_SIZE = 500;
    private String filePath;

    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mediaRecorder;
    private static boolean cameraFront = false;
    private static boolean flash = false;
    private long countUp;
    boolean recording = false;

    private int quality = CamcorderProfile.QUALITY_480P;
    private int recordMinTime, limitTime;
    private ERecordBuilder.RecordQuality recordQuality;
    private String savePath;
    /**
     * 初始化控件
     */
    private LinearLayout camera_preview;
    private Button buttonQuality;
    private Chronometer textChrono;
    private ImageView chronoRecordingImage;
    private ListView listOfQualities;
    private LinearLayout buttonsLayout;
    private ImageView button_ChangeCamera;
    private ImageView button_capture;
    private ImageView buttonFlash;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        recordMinTime = getIntent().getIntExtra("recordMinTime", 2);
        limitTime = getIntent().getIntExtra("limitTime", 0);
        recordQuality = (ERecordBuilder.RecordQuality) getIntent().getSerializableExtra("recordQuality");
        savePath = getIntent().getStringExtra("savePath");

        if (TextUtils.isEmpty(savePath)) {
            Toast.makeText(VideoRecordActivity.this, getResources().getString(R.string.save_path_null), Toast.LENGTH_SHORT).show();
            finish();
        } else if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(VideoRecordActivity.this, getResources().getString(R.string.please_gave_permission)
                    , Toast.LENGTH_SHORT).show();
            finish();
        } else {
            initView();
            initializemPreview();

            final DisplayMetrics dm = getResources().getDisplayMetrics();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    focusOnTouch(dm.widthPixels / 2 , dm.heightPixels / 2);
                }
            },1000);


        }

    }


    private void initView() {
        camera_preview = (LinearLayout) findViewById(R.id.camera_preview);
        buttonQuality = (Button) findViewById(R.id.buttonQuality);
        textChrono = (Chronometer) findViewById(R.id.textChrono);
        chronoRecordingImage = (ImageView) findViewById(R.id.chronoRecordingImage);
        listOfQualities = (ListView) findViewById(R.id.listOfQualities);
        buttonsLayout = (LinearLayout) findViewById(R.id.buttonsLayout);
        button_ChangeCamera = (ImageView) findViewById(R.id.button_ChangeCamera);
        button_capture = (ImageView) findViewById(R.id.button_capture);
        buttonFlash = (ImageView) findViewById(R.id.buttonFlash);
    }


    public void initializemPreview() {
        mPreview = new CameraPreview(this, mCamera);
        camera_preview.addView(mPreview);
        button_capture.setOnClickListener(captrureListener);
        button_ChangeCamera.setOnClickListener(switchCameraListener);
        if (recordQuality == ERecordBuilder.RecordQuality.ALL) {
            buttonQuality.setOnClickListener(qualityListener);
        }
        buttonFlash.setOnClickListener(flashListener);
        camera_preview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        focusOnTouch(event.getX(), event.getY());
                    } catch (Exception e) {
                        Log.i(TAG, getString(R.string.fail_when_camera_try_autofocus, e.toString()));
                        //do nothing
                    }
                }
                return true;
            }
        });
    }

    private void focusOnTouch(float x, float y) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getMaxNumMeteringAreas() > 0) {
                Rect rect = calculateFocusArea(x, y);
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                meteringAreas.add(new Camera.Area(rect, 800));
                parameters.setFocusAreas(meteringAreas);
                mCamera.setParameters(parameters);
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            } else {
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            }
        }
    }


    public void onResume() {
        super.onResume();
        if (!hasCamera(getApplicationContext())) {
            //这台设备没有发现摄像头
            Toast.makeText(getApplicationContext(), R.string.dont_have_camera_error
                    , Toast.LENGTH_SHORT).show();
            setResult(RESULT_CODE_FOR_RECORD_VIDEO_FAILED);
            releaseCamera();
            releaseMediaRecorder();
            finish();
        }
        if (mCamera == null) {
            releaseCamera();
            final boolean frontal = cameraFront;

            int cameraId = findFrontFacingCamera();
            if (cameraId < 0) {
                //前置摄像头不存在
                switchCameraListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(VideoRecordActivity.this, R.string.dont_have_front_camera, Toast.LENGTH_SHORT).show();
                    }
                };

                //尝试寻找后置摄像头
                cameraId = findBackFacingCamera();
                if (flash) {
                    mPreview.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    buttonFlash.setImageResource(R.mipmap.ic_flash_on_white);
                }
            } else if (!frontal) {
                cameraId = findBackFacingCamera();
                if (flash) {
                    mPreview.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    buttonFlash.setImageResource(R.mipmap.ic_flash_on_white);
                }
            }

            mCamera = Camera.open(cameraId);
            mPreview.refreshCamera(mCamera);
            reloadQualities(cameraId);

        }
    }

    //检查设备是否有摄像头
    private boolean hasCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 找前置摄像头,没有则返回-1
     *
     * @return cameraId
     */
    private int findFrontFacingCamera() {
        int cameraId = -1;
        //获取摄像头个数
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    /**
     * 找后置摄像头,没有则返回-1
     *
     * @return cameraId
     */
    private int findBackFacingCamera() {
        int cameraId = -1;
        //获取摄像头个数
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    //reload成像质量
    private void reloadQualities(int idCamera) {

        int maxQualitySupported = CamcorderProfile.QUALITY_480P;
        final ArrayList<String> list = new ArrayList<String>();
        if (recordQuality == ERecordBuilder.RecordQuality.ALL) {
            maxQualitySupported = CamcorderProfile.QUALITY_480P;
            if (CamcorderProfile.hasProfile(idCamera, CamcorderProfile.QUALITY_480P)) {
                list.add("480p");
                maxQualitySupported = CamcorderProfile.QUALITY_480P;
            }
            if (CamcorderProfile.hasProfile(idCamera, CamcorderProfile.QUALITY_720P)) {
                list.add("720p");
                maxQualitySupported = CamcorderProfile.QUALITY_720P;
            }
            if (CamcorderProfile.hasProfile(idCamera, CamcorderProfile.QUALITY_1080P)) {
                list.add("1080p");
                maxQualitySupported = CamcorderProfile.QUALITY_1080P;
            }
            if (CamcorderProfile.hasProfile(idCamera, CamcorderProfile.QUALITY_2160P)) {
                list.add("2160p");
                maxQualitySupported = CamcorderProfile.QUALITY_2160P;
            }

        } else {
            if (recordQuality == ERecordBuilder.RecordQuality.QUALITY_480P) {
                maxQualitySupported = CamcorderProfile.QUALITY_480P;
            } else if (recordQuality == ERecordBuilder.RecordQuality.QUALITY_720P) {
                maxQualitySupported = CamcorderProfile.QUALITY_720P;
            } else if (recordQuality == ERecordBuilder.RecordQuality.QUALITY_1080P) {
                maxQualitySupported = CamcorderProfile.QUALITY_1080P;
            } else if (recordQuality == ERecordBuilder.RecordQuality.QUALITY_2160P) {
                maxQualitySupported = CamcorderProfile.QUALITY_2160P;
            }

            /***
             * 如果没有进行降级
             * 由CamcorderProfile 的数据结构可知最低标准为480 所以最小值再次减去1的调试件是大于4
             * 每次减去1 如果存在跳出循环
             */
            while (!CamcorderProfile.hasProfile(idCamera, maxQualitySupported)
                    && maxQualitySupported > 4) {
                maxQualitySupported = maxQualitySupported - 1;
            }
        }

        changeVideoQuality(maxQualitySupported);

        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listOfQualities.setAdapter(adapter);

        listOfQualities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);

                buttonQuality.setText(item);
                if (item.equals("480p")) {
                    changeVideoQuality(CamcorderProfile.QUALITY_480P);
                } else if (item.equals("720p")) {
                    changeVideoQuality(CamcorderProfile.QUALITY_720P);
                } else if (item.equals("1080p")) {
                    changeVideoQuality(CamcorderProfile.QUALITY_1080P);
                } else if (item.equals("2160p")) {
                    changeVideoQuality(CamcorderProfile.QUALITY_2160P);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    listOfQualities.animate().setDuration(200).alpha(0)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    listOfQualities.setVisibility(View.GONE);
                                }
                            });
                } else {
                    listOfQualities.setVisibility(View.GONE);
                }
            }

        });

    }

    //质量列表
    View.OnClickListener qualityListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!recording) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                        && listOfQualities.getVisibility() == View.GONE) {
                    listOfQualities.setVisibility(View.VISIBLE);
                    listOfQualities.animate().setDuration(200).alpha(95)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                }
                            });
                } else {
                    listOfQualities.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    //闪光灯
    View.OnClickListener flashListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!cameraFront) {
                if (flash) {
                    flash = false;
                    buttonFlash.setImageResource(R.mipmap.ic_flash_off_white);
                    setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                } else {
                    flash = true;
                    buttonFlash.setImageResource(R.mipmap.ic_flash_on_white);
                    setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                }
            }
        }
    };

    //切换前置后置摄像头
    View.OnClickListener switchCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!recording) {
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {
                    releaseCamera();
                    chooseCamera();
                } else {
                    //只有一个摄像头不允许切换
                    Toast.makeText(getApplicationContext(), R.string.only_have_one_camera
                            , Toast.LENGTH_SHORT).show();
                }
            }
        }
    };


    //选择摄像头
    public void chooseCamera() {
        if (cameraFront) {
            //当前是前置摄像头
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview
                mCamera = Camera.open(cameraId);
                // mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
                reloadQualities(cameraId);
            }
        } else {
            //当前为后置摄像头
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview
                mCamera = Camera.open(cameraId);
                if (flash) {
                    flash = false;
                    buttonFlash.setImageResource(R.mipmap.ic_flash_off_white);
                    mPreview.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
                // mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
                reloadQualities(cameraId);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    View.OnClickListener captrureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (recording) {
                if (countUp < recordMinTime) {
                    Toast.makeText(VideoRecordActivity.this, "视频时间过短", Toast.LENGTH_SHORT).show();
                    return;
                }
                //如果正在录制点击这个按钮表示录制完成
                finishRecord();
            } else {
                //准备开始录制视频
                if (!prepareMediaRecorder()) {
                    Toast.makeText(VideoRecordActivity.this, getString(R.string.camera_init_fail), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CODE_FOR_RECORD_VIDEO_FAILED);
                    releaseCamera();
                    releaseMediaRecorder();
                    finish();
                }
                //开始录制视频
                runOnUiThread(new Runnable() {
                    public void run() {
                        // If there are stories, add them to the table
                        try {
                            mediaRecorder.start();
                            startChronometer();
                            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                changeRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            } else {
                                changeRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            }
                            button_capture.setImageResource(R.mipmap.player_stop);
                        } catch (final Exception ex) {
                            Log.i("---", "Exception in thread");
                            setResult(RESULT_CODE_FOR_RECORD_VIDEO_FAILED);
                            releaseCamera();
                            releaseMediaRecorder();
                            finish();
                        }
                    }
                });
                recording = true;
            }
        }
    };

    private void finishRecord() {
        mediaRecorder.stop(); //停止
        stopChronometer();
        button_capture.setImageResource(R.mipmap.player_record);
        changeRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        releaseMediaRecorder();
        Toast.makeText(VideoRecordActivity.this, R.string.video_captured, Toast.LENGTH_SHORT).show();
        recording = false;
        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_VIDEO_PATH, filePath);
        setResult(Activity.RESULT_OK, intent);
        releaseCamera();
        releaseMediaRecorder();
        finish();
    }

    private void changeRequestedOrientation(int orientation) {
        setRequestedOrientation(orientation);
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            mCamera.lock();
        }
    }

    private boolean prepareMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (cameraFront) {
                mediaRecorder.setOrientationHint(270);
            } else {
                mediaRecorder.setOrientationHint(90);
            }
        }

        mediaRecorder.setProfile(CamcorderProfile.get(quality));

        filePath = savePath + File.separator + System.currentTimeMillis() + ".mp4";
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.mkdirs();
        }
        mediaRecorder.setOutputFile(filePath);

        /**
         * 不设置则没有限制
         * https://developer.android.com/reference/android/media/MediaRecorder.html#setMaxDuration(int)
         * https://developer.android.com/reference/android/media/MediaRecorder.html#setMaxFileSize(int)
         * 设置视频文件最大size 1G
         * mediaRecorder.setMaxFileSize(CameraConfig.MAX_FILE_SIZE_RECORD);
         * 关于此选项可以设置也可以不设置 设置之后 视频会在20秒处 停止录制 需要我们手动处理
         * mediaRecorder.setMaxDuration(limitTime * 1000);
         * */

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;

    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    //修改录像质量
    private void changeVideoQuality(int quality) {
        this.quality = quality;
        if (quality == CamcorderProfile.QUALITY_480P)
            buttonQuality.setText("480p");
        if (quality == CamcorderProfile.QUALITY_720P)
            buttonQuality.setText("720p");
        if (quality == CamcorderProfile.QUALITY_1080P)
            buttonQuality.setText("1080p");
        if (quality == CamcorderProfile.QUALITY_2160P)
            buttonQuality.setText("2160p");
    }


    private class StableArrayAdapter extends ArrayAdapter<String> {
        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

    //闪光灯
    public void setFlashMode(String mode) {
        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)
                    && mCamera != null
                    && !cameraFront) {

                mPreview.setFlashMode(mode);
                mPreview.refreshCamera(mCamera);

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.changing_flashLight_mode,
                    Toast.LENGTH_SHORT).show();
        }
    }

    //计时器
    private void startChronometer() {
        textChrono.setVisibility(View.VISIBLE);
        final long startTime = SystemClock.elapsedRealtime();
        textChrono.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer arg0) {
                countUp = (SystemClock.elapsedRealtime() - startTime) / 1000;

                if (countUp % 2 == 0) {
                    chronoRecordingImage.setVisibility(View.VISIBLE);
                } else {
                    chronoRecordingImage.setVisibility(View.INVISIBLE);
                }

                String asText = String.format("%02d", countUp / 60) + ":" + String.format("%02d", countUp % 60);
                textChrono.setText(asText);
                if (limitTime != 0 && countUp == limitTime) {
                    finishRecord();
                }
            }
        });
        textChrono.start();
    }

    private void stopChronometer() {
        textChrono.stop();
        chronoRecordingImage.setVisibility(View.INVISIBLE);
        textChrono.setVisibility(View.INVISIBLE);
    }

    public static void reset() {
        flash = false;
        cameraFront = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (recording) {
            mediaRecorder.stop();
            if (textChrono != null && textChrono.isActivated())
                textChrono.stop();
            releaseMediaRecorder();
            recording = false;
            File mp4 = new File(filePath);
            if (mp4.exists() && mp4.isFile()) {
                mp4.delete();
            }
        }
        setResult(RESULT_CODE_FOR_RECORD_VIDEO_CANCEL);
        releaseCamera();
        releaseMediaRecorder();
        finish();
        return super.onKeyDown(keyCode, event);
    }


    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / mPreview.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / mPreview.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize / 2 > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                result = 1000 - focusAreaSize / 2;
            } else {
                result = -1000 + focusAreaSize / 2;
            }
        } else {
            result = touchCoordinateInCameraReper - focusAreaSize / 2;
        }
        return result;
    }

    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                // do something...
                Log.i("tap_to_focus", "success!");
            } else {
                // do something...
                Log.i("tap_to_focus", "fail!");
            }
        }
    };

}
