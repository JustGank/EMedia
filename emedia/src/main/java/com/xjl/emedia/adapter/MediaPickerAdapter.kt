package com.xjl.emedia.adapter

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.xjl.emedia.R
import com.xjl.emedia.bean.MediaPickerBean
import com.xjl.emedia.databinding.ItemMediaPickerBinding
import java.io.File

/**
 * Created by x33664 on 2018/11/8.
 */
class MediaPickerAdapter : RecyclerView.Adapter<MediaPickerAdapter.ViewHolder> {

    private var activity: Activity
    private var inflater: LayoutInflater
    private var list: List<MediaPickerBean>
    private var openSkipMemoryCache = false
    private lateinit var requestOptions: RequestOptions
    private var itemHeight = 0

    constructor(activity: Activity, list: List<MediaPickerBean>) {
        this.activity = activity
        this.list = list
        inflater = activity.layoutInflater
        initRequestOptions()
    }

    constructor(
        activity: Activity,
        list: List<MediaPickerBean>,
        openSkipMemoryCache: Boolean,
        itemHeight: Int
    ) {
        this.activity = activity
        this.list = list
        inflater = activity.layoutInflater
        this.openSkipMemoryCache = openSkipMemoryCache
        initRequestOptions()
        this.itemHeight = itemHeight
    }

    private fun initRequestOptions() {
        requestOptions = RequestOptions()
        requestOptions.skipMemoryCache(openSkipMemoryCache)
        requestOptions.centerCrop()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMediaPickerBinding.inflate(inflater))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaPickerBean = list[position]
        holder.binding.apply {

            val uri = Uri.fromFile(File(mediaPickerBean.mediaFilePath))
            Glide.with(activity as Context)
                .load(uri)
                .apply(requestOptions)
                .into(cover)

            val layoutParams = cover.layoutParams as RelativeLayout.LayoutParams
            layoutParams.height = itemHeight
            cover.layoutParams = layoutParams
            selected.setBackgroundResource(if (mediaPickerBean.isPicked) R.mipmap.image_choose else R.mipmap.image_not_chose)
            if (mediaPickerBean.type == 1) {
                time.visibility = View.GONE
            } else {
                time.visibility = View.VISIBLE
                time.text = mediaPickerBean.duration
            }
            Listener(container, position)
        }

    }

    fun setList(list: List<MediaPickerBean>) {
        this.list = list
        notifyDataSetChanged()
    }

    inner class Listener(var container: ViewGroup, var position: Int) : View.OnClickListener {
        var selected = container.findViewById<View>(R.id.selected)

        init {
            container.setOnClickListener(this)
            container.findViewById<View>(R.id.cover).setOnClickListener(this)
            container.findViewById<View>(R.id.selected_container).apply {
                setOnClickListener(this@Listener)
                selected = findViewById<View>(R.id.selected)
            }
        }

        override fun onClick(v: View) {
            if (v.id == R.id.selected_container) {
                if(onItemClickListener?.onSelectedClicked(position, list[position])==true){
                    list[position].isPicked = !list[position].isPicked
                    selected.setBackgroundResource(if (list[position].isPicked) R.mipmap.image_choose else R.mipmap.image_not_chose)
                }
            } else if (v.id == R.id.cover) {
                onItemClickListener?.onCoverClicked(position, list[position])
            } else if (v.id == R.id.container) {
            }
        }
    }


    interface OnItemClickListener {
        fun onSelectedClicked(position: Int, bean: MediaPickerBean):Boolean
        fun onCoverClicked(position: Int, bean: MediaPickerBean)
    }

    var onItemClickListener: OnItemClickListener? = null

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(val binding: ItemMediaPickerBinding) :
        RecyclerView.ViewHolder(binding.root)
}
