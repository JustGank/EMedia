package com.xjl.emedia.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.xjl.emedia.R
import com.xjl.emedia.bean.MediaForderBean

class PopPicFolderAdapter(
    var list: List<MediaForderBean>,
    var activity: Activity,
    var divid_line_color: Int,
    var ticket: String
) : RecyclerView.Adapter<PopPicFolderAdapter.ViewHolder>() {
    var inflater: LayoutInflater
    private val requestOptions: RequestOptions
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.pop_pic_folder_list_item, null))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaForderBean = list[position]
        Glide.with(activity).load("file://" + mediaForderBean.coverFilePath)
            .apply(requestOptions)
            .into(holder.cover)
        holder.title.text = mediaForderBean.folderName
        holder.num.visibility =
            if (mediaForderBean.num == 0) View.GONE else View.VISIBLE
        holder.num.text = mediaForderBean.num.toString() + ticket
        holder.divid_line.setBackgroundColor(activity.resources.getColor(divid_line_color))
        Listener(position, holder.container)
    }

    private inner class Listener(var position: Int, var container: RelativeLayout) :
        View.OnClickListener {
        init {
            container.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            if (v.id == R.id.container) {
                if (onItemClickListener != null) {
                    onItemClickListener!!.onClick(position, container, list[position])
                }
            }
        }
    }

    private var onItemClickListener: OnItemClickListener? = null

    init {
        inflater = activity.layoutInflater
        requestOptions = RequestOptions()
        requestOptions.centerCrop()
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onClick(position: Int, v: View, mediaForderBean: MediaForderBean)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cover: ImageView
        var title: TextView
        var num: TextView
        var divid_line: View
        var container: RelativeLayout

        init {
            cover = itemView.findViewById<View>(R.id.cover) as ImageView
            title = itemView.findViewById<View>(R.id.title) as TextView
            num = itemView.findViewById<View>(R.id.num) as TextView
            divid_line = itemView.findViewById(R.id.divid_line)
            container = itemView.findViewById<View>(R.id.container) as RelativeLayout
        }
    }
}
