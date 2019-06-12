package com.xjl.emedia.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.xjl.emedia.R;
import com.xjl.emedia.fragment.PreviewFragment;
import com.xjl.emedia.fragment.VideoRecordFragment;

/**
 * Created by x33664 on 2019/2/13.
 */

public class VideoRecordActivity extends FragmentActivity {

    private static final String TAG = VideoRecordActivity.class.getSimpleName();

    public static final int RESULT_CODE_FOR_RECORD_VIDEO_CANCEL = 401;
    public static final int RESULT_CODE_FOR_RECORD_VIDEO_FAILED = 404;
    public static final String INTENT_EXTRA_VIDEO_PATH = "intent_extra_video_path";

    VideoRecordFragment videoRecordFragment;

    PreviewFragment previewFragment;

    FrameLayout frame;

    FragmentManager fragmentManager;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        frame = (FrameLayout) findViewById(R.id.frame);


        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        videoRecordFragment = new VideoRecordFragment();
        videoRecordFragment.setOnFinishRecordValueable(onFinishRecordValueable);
        transaction.add(R.id.frame, videoRecordFragment);
        transaction.commitAllowingStateLoss();
    }

    VideoRecordFragment.OnFinishRecordValueable onFinishRecordValueable = new VideoRecordFragment.OnFinishRecordValueable() {
        @Override
        public void onFinish(String filePath) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            previewFragment = PreviewFragment.getINSTANCE(filePath);
            transaction.replace(R.id.frame, previewFragment).commitAllowingStateLoss();
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
        if (videoRecordFragment != null) {
            videoRecordFragment.releaseCamera();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (videoRecordFragment != null) {
            videoRecordFragment.onKeyDown();
        }
        return super.onKeyDown(keyCode, event);
    }

}
