package com.xjl.emedia.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.xjl.emedia.R;
import com.xjl.emedia.bean.BroadcastCMD;
import com.xjl.emedia.fragment.PreviewFragment;
import com.xjl.emedia.fragment.VideoRecordFragment;

/**
 * Created by x33664 on 2019/2/13.
 */

public class VideoRecordActivity extends AppCompatActivity {

    private static final String TAG = VideoRecordActivity.class.getSimpleName();

    public static final int RESULT_CODE_FOR_RECORD_VIDEO_CANCEL = 401;
    public static final int RESULT_CODE_FOR_RECORD_VIDEO_FAILED = 404;
    public static final String INTENT_EXTRA_VIDEO_PATH = "intent_extra_video_path";

    VideoRecordFragment videoRecordFragment;

    PreviewFragment previewFragment;

    FrameLayout frame;

    FragmentManager fragmentManager;

    VideoRecordBroadreceiver videoRecordBroadreceiver;

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

        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(BroadcastCMD.INTERRUPT_RECORD);

        videoRecordBroadreceiver=new VideoRecordBroadreceiver();
        registerReceiver(videoRecordBroadreceiver,intentFilter);

    }

    VideoRecordFragment.OnFinishRecordValueable onFinishRecordValueable =
            new VideoRecordFragment.OnFinishRecordValueable() {
        @Override
        public void onFinish(String filePath) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            previewFragment = PreviewFragment.getINSTANCE(filePath);
            transaction.replace(com.xjl.emedia.R.id.frame, previewFragment).commitAllowingStateLoss();
        }
    };


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (videoRecordFragment != null) {
            videoRecordFragment.releaseCamera();
        }
    }

    private class VideoRecordBroadreceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG,"VideoRecordBroadreceiver onReceive ");
            switch (intent.getAction()){
                case BroadcastCMD.INTERRUPT_RECORD:
                    videoRecordFragment.finishRecord(true);
                    finish();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy");
        if(videoRecordBroadreceiver!=null){
            unregisterReceiver(videoRecordBroadreceiver);
            videoRecordBroadreceiver=null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(previewFragment==null){
            if (videoRecordFragment != null) {
                videoRecordFragment.onKeyDown();
            }
        }else{
            previewFragment.onKeyDown();
        }


        return super.onKeyDown(keyCode, event);
    }

}
