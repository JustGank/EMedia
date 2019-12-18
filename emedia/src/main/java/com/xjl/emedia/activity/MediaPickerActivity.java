package com.xjl.emedia.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xjl.emedia.R;
import com.xjl.emedia.adapter.MediaPickerAdapter;
import com.xjl.emedia.adapter.PopPicFolderAdapter;
import com.xjl.emedia.bean.MediaFileBean;
import com.xjl.emedia.bean.MediaPickerBean;
import com.xjl.emedia.builder.EPickerBuilder;
import com.xjl.emedia.popwindow.PicFolderListPopwindow;
import com.xjl.emedia.utils.FileUtil;
import com.xjl.emedia.utils.IntentUtil;
import com.xjl.emedia.utils.ScreenUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by x33664 on 2018/11/7.
 */

public class MediaPickerActivity extends Activity implements View.OnClickListener {

    private final String TAG = MediaPickerActivity.class.getSimpleName();

    public static final String RESULT_LIST = "result_list";
    public static final String COMPRESS_OPEN = "compress_open";
    public static final String FINISH_MEDIA_PICKER_ACTIVITY="finish_media_picker_activity";

    protected RelativeLayout title_contianer;
    protected ImageView ivBack;
    protected TextView title_tv;
    protected TextView chosedNum;
    protected RecyclerView recyclerview;
    protected RelativeLayout more_container;
    protected LinearLayout imagesfolder_container;
    protected TextView all_pic;
    protected TextView orginal_pic;
    protected ImageView orginal_pic_select;
    protected TextView preview;

    protected MediaPickerAdapter adapter;
    private List<MediaPickerBean> mediaPickerBeanList = new ArrayList<>();
    private ArrayList<MediaPickerBean> pickedList = new ArrayList<>();
    private HashMap<String, MediaFileBean> mediaFolderMap = new HashMap<>();
    private HashMap<String, ArrayList<MediaPickerBean>> mediaFolderListMap = new HashMap<>();
    private PicFolderListPopwindow popwindow;
    private PopPicFolderAdapter popPicFolderAdapter;

    private final int CODE_FOR_WRITE_PERMISSION = 100;
    private final int LOAD_FINISH_REFRESH = 1001;
    private int PICKED_MEDIA_MAX_SIZE = 9;
    private EPickerBuilder.PickerType pickerType = EPickerBuilder.PickerType.PHOTO_VIDEO;
    private int subjectBackground;
    private int subjectTextColor;
    private int backImgRes;
    private Class dialog_class;
    private int compressWidth, compressHeight;
    private long maxPhotoSize = 4 * 1024 * 1024;
    private long maxVideoSize = 30 * 1024 * 1024;//视频文件默认最大为30M
    private boolean overSizeVisible = true;
    private boolean compressOpen = false;
    private boolean openPreview = false;
    private Class previewActivity = null;
    private boolean openSkipMemoryCache = false;
    private boolean openBottomMoreOperate=false;
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
                    if(openBottomMoreOperate){
                        initFolderPop();
                    }
                    break;
            }
        }
    };

    private Object waitingDialog;

    private int screentWidth = 0, screenHeight = 0;

    //跨页面关闭广播接收器
    private FinshActivityReceiver finishActivityReceiver;

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

        int[] screenSize = ScreenUtil.getScreenSize(this);
        screentWidth = screenSize[0];
        screenHeight = screenSize[1];

        parserIntent();
        initView();
        getMedia();
        registReveiver();

    }

    private void parserIntent() {
        PICKED_MEDIA_MAX_SIZE = getIntent().getIntExtra("max_chose_num", 1);
        pickerType = (EPickerBuilder.PickerType) getIntent().getSerializableExtra("pickerType");
        subjectBackground = getIntent().getIntExtra("resSubjectBackground", R.color.subject_background);
        subjectTextColor = getIntent().getIntExtra("subjectTextColor", R.color.subject_text_color);
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
        previewActivity = (Class) getIntent().getSerializableExtra("previewActivity");
        openSkipMemoryCache = getIntent().getBooleanExtra("openSkipMemoryCache", false);
        openBottomMoreOperate=getIntent().getBooleanExtra("openBottomMoreOperate",false);
    }

    private void initView() {

        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivBack.setOnClickListener(MediaPickerActivity.this);
        ivBack.setImageResource(R.mipmap.ic_menu_back);
        title_contianer = (RelativeLayout) findViewById(R.id.title_contianer);
        title_contianer.setBackgroundResource(subjectBackground);
        title_tv = (TextView) findViewById(R.id.title_tv);
        title_tv.setTextColor(getResources().getColor(subjectTextColor));
        chosedNum = (TextView) findViewById(R.id.chosed_num);
        chosedNum.setOnClickListener(MediaPickerActivity.this);
        recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        more_container = (RelativeLayout) findViewById(R.id.more_container);
        more_container.setBackgroundResource(subjectBackground);
        more_container.setVisibility(openBottomMoreOperate?View.VISIBLE:View.GONE);
        all_pic = (TextView) findViewById(R.id.all_pic);
        all_pic.setTextColor(getResources().getColor(subjectTextColor));
        orginal_pic = (TextView) findViewById(R.id.orginal_pic);
        orginal_pic.setTextColor(getResources().getColor(subjectTextColor));
        orginal_pic_select = (ImageView) findViewById(R.id.orginal_pic_select);
        orginal_pic_select.setOnClickListener(MediaPickerActivity.this);
        orginal_pic_select.setBackgroundResource(compressOpen ? R.mipmap.all_unselected : R.mipmap.all_selected);
        findViewById(R.id.orginal_pic).setOnClickListener(this);
        preview = (TextView) findViewById(R.id.preview);
        preview.setVisibility(previewActivity == null ? View.GONE : View.VISIBLE);
        preview.setOnClickListener(MediaPickerActivity.this);
        preview.setTextColor(getResources().getColor(subjectTextColor));
        imagesfolder_container = (LinearLayout) findViewById(R.id.imagesfolder_container);
        imagesfolder_container.setOnClickListener(MediaPickerActivity.this);

        recyclerview.setLayoutManager(new GridLayoutManager(MediaPickerActivity.this, 4));
        recyclerview.setAdapter(
                adapter = new MediaPickerAdapter(MediaPickerActivity.this, new ArrayList<MediaPickerBean>()
                        , openSkipMemoryCache));

        adapter.setOnItemClickListener(medidClickListener);

        ((SimpleItemAnimator) recyclerview.getItemAnimator()).setSupportsChangeAnimations(false);

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

    private void registReveiver(){
        finishActivityReceiver=new FinshActivityReceiver();

        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(FINISH_MEDIA_PICKER_ACTIVITY);

        registerReceiver(finishActivityReceiver,intentFilter);
    }

    private void startGetMediaThread() {
        mediaFolderMap.clear();
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
                                putFolderPathToMap(mediaPickerBean);

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
                                putFolderPathToMap(mediaPickerBean);
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

    private void putFolderPathToMap(MediaPickerBean pickerBean) {
        //如果没有开启则不再处理文件夹Map
        if(!openBottomMoreOperate){
            return;
        }

        if (mediaFolderMap != null) {
            String folderPath = FileUtil.getFileFolderPath(pickerBean.mediaFilePath);
            MediaFileBean mediaFileBean = mediaFolderMap.get(folderPath);
            ArrayList<MediaPickerBean> folderBeans = mediaFolderListMap.get(folderPath);
            if (mediaFileBean == null) {
                mediaFileBean = new MediaFileBean(folderPath);
                mediaFileBean.num = 1;
                mediaFileBean.coverFilePath = pickerBean.mediaFilePath;
                mediaFolderMap.put(folderPath, mediaFileBean);

                folderBeans = new ArrayList<>();
                folderBeans.add(pickerBean);
                mediaFolderListMap.put(folderPath, folderBeans);
            } else {
                mediaFileBean.num++;
                folderBeans.add(pickerBean);
            }
        }
    }

    private void initFolderPop() {

        //如果说所有照片的文件夹中的对象数为0，那么就不再进行初始化。
        if(mediaPickerBeanList==null||mediaPickerBeanList.size()==0){
            return;
        }


        //先添加所有照片的第一个元素
        List<MediaFileBean> list = new ArrayList<>();
        MediaFileBean mediaFileBean = new MediaFileBean("");
        mediaFileBean.folderName = getString(R.string.all_pics);
        mediaFileBean.num = 0;
        mediaFileBean.coverFilePath = mediaPickerBeanList.get(0).mediaFilePath;
        list.add(mediaFileBean);
        list.addAll(mediaFolderMap.values());

        popwindow = new PicFolderListPopwindow(this, list, R.color.divid_line_color);
        popPicFolderAdapter = popwindow.getAdapter();
        popPicFolderAdapter.setOnItemClickListener(picFolderItemClickListener);
        popwindow.setWidth(screentWidth);
    }


    PopPicFolderAdapter.OnItemClickListener picFolderItemClickListener = new PopPicFolderAdapter.OnItemClickListener() {
        @Override
        public void onClick(int position, View v, MediaFileBean mediaFileBean) {
            if (mediaFileBean.num == 0) {
                title_tv.setText(R.string.photo_album);
                all_pic.setText(R.string.all_pics);
                adapter.setList(mediaPickerBeanList);
            } else {
                title_tv.setText(mediaFileBean.folderName);
                all_pic.setText(mediaFileBean.folderName);
                adapter.setList(mediaFolderListMap.get(mediaFileBean.folderPath));
            }
            if (popwindow != null) {
                popwindow.dismiss();
            }

        }
    };


    MediaPickerAdapter.OnItemClickListener medidClickListener = new MediaPickerAdapter.OnItemClickListener() {
        @Override
        public void onSelectedClicked(int position, MediaPickerBean bean) {

            if ((bean.type == 1 && bean.getSize() > maxPhotoSize)) {
                showToast(getString(R.string.photo_over_size));
                return;
            } else if (bean.type == 2 && bean.getSize() > maxVideoSize) {
                showToast(getString(R.string.video_over_size));
                return;
            }


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
                preview.setVisibility(View.GONE);
            } else {
                chosedNum.setVisibility(View.VISIBLE);
                chosedNum.setText(getString(R.string.send) + "(" + pickedList.size() + "/" + PICKED_MEDIA_MAX_SIZE + ")");

                if (previewActivity != null) {
                    preview.setVisibility(View.VISIBLE);
                    preview.setText(getString(R.string.preview) + "(" + pickedList.size() + ")");
                }

            }

        }

        @Override
        public void onCoverClicked(int position, MediaPickerBean bean) {
            if (previewActivity != null) {
                postPickedListToPreviewActivity(bean);
            } else if (openPreview) {
                if (IntentUtil.isImage(bean.getMediaFilePath())) {
                    IntentUtil.openLocalImage(MediaPickerActivity.this, bean.getMediaFilePath());
                } else if (IntentUtil.isVideo(bean.getMediaFilePath())) {
                    IntentUtil.openLocalVideo(MediaPickerActivity.this, bean.getMediaFilePath());
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
        } else if (view.getId() == R.id.imagesfolder_container) {
            if (popwindow != null) {
                if (popwindow.isShowing()) {
                    popwindow.dismiss();
                } else {
                    int yPosition = (int) (screenHeight - imagesfolder_container.getHeight() - ScreenUtil.dip2px(this, 320));
                    popwindow.showAtLocation(orginal_pic_select,
                            Gravity.TOP | Gravity.LEFT, -1, yPosition);
                }
            }

        } else if (view.getId() == R.id.orginal_pic_select || view.getId() == R.id.orginal_pic) {
            compressOpen = !compressOpen;
            orginal_pic_select.setBackgroundResource(compressOpen ? R.mipmap.all_unselected : R.mipmap.all_selected);
        } else if (view.getId() == R.id.preview) {
            postPickedListToPreviewActivity();
        }
    }


    private void postPickedListToPreviewActivity() {
        if (previewActivity != null) {
            Intent intent = new Intent(MediaPickerActivity.this, previewActivity);
            intent.putExtra(RESULT_LIST, pickedList);
            intent.putExtra(COMPRESS_OPEN, compressOpen);
            startActivity(intent);
        }

    }

    private void postPickedListToPreviewActivity(MediaPickerBean mediaPickerBean) {
        if (previewActivity != null) {
            ArrayList<MediaPickerBean> tempList = new ArrayList<>();
            tempList.add(mediaPickerBean);
            Intent intent = new Intent(MediaPickerActivity.this, previewActivity);
            intent.putExtra(RESULT_LIST, tempList);
            intent.putExtra(COMPRESS_OPEN, compressOpen);
            startActivity(intent);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (popwindow != null && popwindow.isShowing()) {
            popwindow.dismiss();
        }

        if(finishActivityReceiver!=null){
            unregisterReceiver(finishActivityReceiver);
        }

    }

    private class FinshActivityReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(FINISH_MEDIA_PICKER_ACTIVITY)){
                finish();
            }
        }
    }

}
