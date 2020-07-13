package com.xjl.emedia_demo;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xjl.emedia.activity.MediaPickerActivity;
import com.xjl.emedia.adapter.MediaPickerAdapter;
import com.xjl.emedia.bean.MediaPickerBean;
import com.xjl.emedia.utils.IntentUtil;

import java.util.List;

public class PreviewActivity extends AppCompatActivity {

    private static final String TAG = "PreviewActivity";

    List<MediaPickerBean> mediaList ;

    private boolean compressOpen=false;

    RecyclerView recyclerView;

    MediaPickerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preview);

        mediaList= IntentUtil.parserMediaResultData(getIntent());

        compressOpen=getIntent().getBooleanExtra(MediaPickerActivity.COMPRESS_OPEN,false);

        Log.e(TAG,"compressOpen="+compressOpen);

        recyclerView=(RecyclerView)findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(PreviewActivity.this,4));


        adapter=new MediaPickerAdapter(PreviewActivity.this,mediaList);

        recyclerView.setAdapter(adapter);


    }
}
