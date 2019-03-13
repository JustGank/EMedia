package com.xjl.emedia.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xjl.emedia.R;
import com.xjl.emedia.adapter.MediaPickerAdapter;
import com.xjl.emedia.bean.MediaPickerBean;
import com.xjl.emedia.builder.EPickerBuilder;
import com.xjl.emedia.utils.FileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by x33664 on 2018/11/7.
 */

public class MediaPickerActivity extends Activity implements View.OnClickListener {

    private final String TAG = MediaPickerActivity.class.getSimpleName();

    public static final String RESULT_LIST = "result_list";

    protected RelativeLayout title_contianer;
    protected ImageView ivBack;
    protected TextView title_tv;
    protected TextView chosedNum;
    protected RecyclerView recyclerview;
    protected MediaPickerAdapter adapter;
    private List<MediaPickerBean> mediaPickerBeanList = new ArrayList<>();
    private ArrayList<MediaPickerBean> pickedList = new ArrayList<>();

    private final int CODE_FOR_WRITE_PERMISSION = 100;
    private final int LOAD_FINISH_REFRESH = 1001;
    private int PICKED_MEDIA_MAX_SIZE = 9;
    private EPickerBuilder.PickerType pickerType = EPickerBuilder.PickerType.PHOTO_VIDEO;
    private int titleBackground;
    private int titleTextColor;
    private int backImgRes;
    private Class dialog_class;
    private int compressWidth, compressHeight;
    private long maxPhotoSize = 4 * 1024 * 1024;
    private long maxVideoSize = 30 * 1024 * 1024;//视频文件默认最大为30M
    private boolean overSizeVisible = true;
    private boolean compressOpen = false;
    private boolean openPreview = false;
    private boolean openSkipMemoryCache = false;
    private String outputPath;
    /**
     * 图片查询参数
     */
    private String[] projectionImg = {
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE
    };
    private String whereImg = MediaStore.Images.Media.MIME_TYPE + "=? or "
            + MediaStore.Images.Media.MIME_TYPE + "=? or "
            + MediaStore.Images.Media.MIME_TYPE + "=?";
    private String[] whereImgArgs = {"image/jpeg", "image/png", "image/jpg"};

    /**
     * 视频查询参数
     */
    String[] projectionVideo = {
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE
    };
    String whereVideo = MediaStore.Images.Media.MIME_TYPE + "=? or "
            + MediaStore.Video.Media.MIME_TYPE + "=?";
    String[] whereVideoArgs = {"video/mp4", "video/3gp"};

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOAD_FINISH_REFRESH:
                    adapter.setList(mediaPickerBeanList);
                    break;
            }
        }
    };

    private Object waitingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_media_picker);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        parserIntent();
        initView();
        getMedia();
    }

    private void parserIntent() {
        PICKED_MEDIA_MAX_SIZE = getIntent().getIntExtra("max_chose_num", 1);
        pickerType = (EPickerBuilder.PickerType) getIntent().getSerializableExtra("pickerType");
        titleBackground = getIntent().getIntExtra("resTitleBackground", R.color.title_background);
        titleTextColor = getIntent().getIntExtra("titleTextColor", android.R.color.white);
        backImgRes = getIntent().getIntExtra("backImgRes", backImgRes);
        compressOpen = getIntent().getBooleanExtra("openCompress", false);
        outputPath = getIntent().getStringExtra("outputPath");
        dialog_class = (Class) getIntent().getSerializableExtra("dialog_class");
        if (null != dialog_class) {
            try {
                waitingDialog = dialog_class.getConstructor(Context.class)
                        .newInstance(MediaPickerActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        compressWidth = getIntent().getIntExtra("compressWidth", 720);
        compressHeight = getIntent().getIntExtra("compressHeight", 1080);
        maxPhotoSize = getIntent().getLongExtra("photo_max_size", 4 * 1024 * 1024);
        maxVideoSize = getIntent().getLongExtra("video_max_size", 30 * 1024 * 1024);
        overSizeVisible = getIntent().getBooleanExtra("overSizeVisible", true);
        openPreview = getIntent().getBooleanExtra("openPreview", false);
        openSkipMemoryCache = getIntent().getBooleanExtra("openSkipMemoryCache", false);
    }

    private void initView() {
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivBack.setOnClickListener(MediaPickerActivity.this);
        ivBack.setImageResource(R.mipmap.ic_menu_back);
        title_contianer = (RelativeLayout) findViewById(R.id.title_contianer);
        title_contianer.setBackgroundResource(titleBackground);
        title_tv = (TextView) findViewById(R.id.title_tv);
        title_tv.setTextColor(getResources().getColor(titleTextColor));
        chosedNum = (TextView) findViewById(R.id.chosed_num);
        chosedNum.setOnClickListener(MediaPickerActivity.this);
        recyclerview = (RecyclerView) findViewById(R.id.recyclerview);

        ((SimpleItemAnimator) recyclerview.getItemAnimator()).setSupportsChangeAnimations(false);

        recyclerview.setLayoutManager(new GridLayoutManager(MediaPickerActivity.this, 4));
        recyclerview.setAdapter(
                adapter = new MediaPickerAdapter
                        (MediaPickerActivity
                                .this, new ArrayList<MediaPickerBean>()
                                , PICKED_MEDIA_MAX_SIZE
                        ,openPreview,openSkipMemoryCache));


        adapter.setOnItemClickListener(medidClickListener);
    }

    private void getMedia() {
        mediaPickerBeanList.clear();
        pickedList.clear();
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission == PackageManager.PERMISSION_GRANTED) {
            startGetMediaThread();
        }
        //需要弹出dialog让用户手动赋予权限
        else {
            ActivityCompat.requestPermissions(MediaPickerActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    CODE_FOR_WRITE_PERMISSION);
        }
    }

    private void startGetMediaThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (pickerType == EPickerBuilder.PickerType.ONLY_PHOTO || pickerType == EPickerBuilder.PickerType.PHOTO_VIDEO) {
                    Cursor imageCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projectionImg,
                            whereImg, whereImgArgs, MediaStore.Images.Media.DATE_MODIFIED + " desc");
                    if (imageCursor != null) {
                        while (imageCursor.moveToNext()) {
                            long size = imageCursor.getLong(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
                            if (overSizeVisible || size <= maxPhotoSize) {
                                MediaPickerBean mediaPickerBean = new MediaPickerBean();
                                mediaPickerBean.type = 1;
                                mediaPickerBean.size = imageCursor.getLong(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
                                //获取图片的生成日期
                                byte[] data = imageCursor.getBlob(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                                String path = new String(data, 0, data.length - 1);
                                mediaPickerBean.mediaFilePath = path;
                                long time = new File(path).lastModified();
                                mediaPickerBean.data = (int) (time / 1000);
                                mediaPickerBeanList.add(mediaPickerBean);
                            }
                        }
                        Log.e(TAG, "startGetMediaThread image cursor length " + mediaPickerBeanList.size());
                    }
                }

                if (pickerType == EPickerBuilder.PickerType.ONLY_VIDEO || pickerType == EPickerBuilder.PickerType.PHOTO_VIDEO) {
                    Cursor videoCursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            projectionVideo, whereVideo, whereVideoArgs, MediaStore.Video.Media.DATE_ADDED + " desc ");
                    if (videoCursor != null) {
                        while (videoCursor.moveToNext()) {
                            long size = videoCursor.getLong(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                            if (overSizeVisible || size <= maxVideoSize) {
                                MediaPickerBean mediaPickerBean = new MediaPickerBean();
                                mediaPickerBean.type = 2;
                                mediaPickerBean.mediaFilePath = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                                mediaPickerBean.duration = simpleDateFormat.format(videoCursor.getInt(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));
                                mediaPickerBean.data = (int) (new File(mediaPickerBean.mediaFilePath).lastModified() / 1000);
                                mediaPickerBean.size = videoCursor.getLong(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                                mediaPickerBeanList.add(mediaPickerBean);
                            }
                        }
                        Log.e(TAG, "startGetMediaThread image cursor length + video cursor length"
                                + mediaPickerBeanList.size());
                    }
                }
                Collections.sort(mediaPickerBeanList);
                handler.sendEmptyMessage(LOAD_FINISH_REFRESH);
            }
        }).start();
    }

    MediaPickerAdapter.OnItemClickListener medidClickListener = new MediaPickerAdapter.OnItemClickListener() {
        @Override
        public void onClicked(int position, MediaPickerBean bean) {

            if ((bean.type == 1 && bean.getSize() > maxPhotoSize)) {
                showToast(getString(R.string.photo_over_size));
                return;
            } else if (bean.type == 2 && bean.getSize() > maxVideoSize) {
                showToast(getString(R.string.video_over_size));
                return;
            }

            if (PICKED_MEDIA_MAX_SIZE == 1) {
                pickedList.add(bean);
                chosedNum.performClick();
            } else {
                if (pickedList.contains(bean)) {
                    pickedList.remove(bean);
                    adapter.notifyPickState(position);
                } else {
                    if (pickedList.size() < PICKED_MEDIA_MAX_SIZE) {
                        pickedList.add(bean);
                        adapter.notifyPickState(position);
                    } else {
                        showToast(getString(R.string.over_maxinum));
                        return;
                    }
                }
                if (pickedList.size() == 0) {
                    chosedNum.setVisibility(View.GONE);
                } else {
                    chosedNum.setVisibility(View.VISIBLE);
                    chosedNum.setText(getString(R.string.send) + "(" + pickedList.size() + "/" + PICKED_MEDIA_MAX_SIZE + ")");
                }
            }
        }
    };

    private Toast toast;

    protected void showToast(String s) {
        Log.e(TAG, "toast is null=" + (toast == null));
        if (toast == null) {
            toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        } else {
            toast.setText(s);
        }
        toast.show();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.iv_back) {
            finish();
        } else if (view.getId() == R.id.chosed_num) {
            if (pickedList.size() > 0) {
                if (compressOpen) {
                    asyncTask.execute();
                } else {
                    postResult();
                }
            }
        }
    }


    AsyncTask asyncTask = new AsyncTask() {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (null != waitingDialog && waitingDialog instanceof Dialog) {
                ((Dialog) waitingDialog).show();
            }
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            for (int i = 0; i < pickedList.size(); i++) {
                if (pickedList.get(i).type == 1) {
                    String tempOutPath = outputPath + "/" + System.currentTimeMillis() + ".jpg";
                    FileUtil.isSaveCompressPicture(pickedList.get(i).getMediaFilePath(), tempOutPath, compressWidth, compressHeight);
                    pickedList.get(i).setMediaFilePath(tempOutPath);
                    pickedList.get(i).setSize(new File(outputPath).length());
                    Log.e(TAG, "compressed file length=" + pickedList.get(i).getSize());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (null != waitingDialog && waitingDialog instanceof Dialog) {
                ((Dialog) waitingDialog).dismiss();
            }
            postResult();
        }
    };

    private void postResult() {
        Intent intent = new Intent();
        intent.putExtra(RESULT_LIST, pickedList);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }


    public void setWaitingDialog(Dialog dialog) {
        this.waitingDialog = dialog;
    }

}
