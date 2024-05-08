package com.xjl.emedia.popwindow

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xjl.emedia.R
import com.xjl.emedia.adapter.PopPicFolderAdapter
import com.xjl.emedia.bean.MediaForderBean

class PicFolderListPopwindow(
    activity: Activity,
    list: List<MediaForderBean>,
    dividLineColor: Int,
    ticket: String
) : PopupWindow(activity) {
    @JvmField
    var adapter: PopPicFolderAdapter
    var rootView: View
    var recyclerView: RecyclerView

    init {
        rootView = LayoutInflater.from(activity).inflate(R.layout.pop_pic_folder_list, null)
        contentView = rootView
        recyclerView = rootView.findViewById<View>(R.id.recyclerview) as RecyclerView
        recyclerView.setLayoutManager(LinearLayoutManager(activity))
        adapter = PopPicFolderAdapter(list, activity, dividLineColor, ticket)
        recyclerView.setAdapter(adapter)
    }
}
