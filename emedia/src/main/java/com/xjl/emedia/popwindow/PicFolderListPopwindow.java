package com.xjl.emedia.popwindow;

import android.app.Activity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.xjl.emedia.R;
import com.xjl.emedia.adapter.PopPicFolderAdapter;
import com.xjl.emedia.bean.MediaFileBean;

import java.util.List;

public class PicFolderListPopwindow extends PopupWindow {

    PopPicFolderAdapter adapter;

    View rootView;

    RecyclerView recyclerView;


    public PicFolderListPopwindow(Activity activity, List<MediaFileBean> list, int dividLineColor,String ticket) {
        super(activity);

        rootView=LayoutInflater.from(activity).inflate(R.layout.pop_pic_folder_list, null);

        setContentView(rootView);

        recyclerView= (RecyclerView) rootView.findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        adapter=new PopPicFolderAdapter(list,activity,dividLineColor,ticket);

        recyclerView.setAdapter(adapter);

    }

    public PopPicFolderAdapter getAdapter(){
        return adapter;
    }

}
