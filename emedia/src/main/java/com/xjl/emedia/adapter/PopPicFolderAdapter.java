package com.xjl.emedia.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xjl.emedia.R;
import com.xjl.emedia.bean.MediaFileBean;

import java.util.List;

public class PopPicFolderAdapter extends RecyclerView.Adapter<PopPicFolderAdapter.ViewHolder> {

    List<MediaFileBean> list;

    Activity activity;

    LayoutInflater inflater;

    int divid_line_color;

    private RequestOptions requestOptions;

    public PopPicFolderAdapter(List<MediaFileBean> list, Activity activity, int divid_line_color) {
        this.list = list;
        this.activity = activity;
        this.inflater = this.activity.getLayoutInflater();
        this.divid_line_color = divid_line_color;
        this.requestOptions = new RequestOptions();
        this.requestOptions.centerCrop();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.pop_pic_folder_list_item, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MediaFileBean mediaFileBean = list.get(position);

        Glide.with(activity).load("file://" + mediaFileBean.coverFilePath)
                .apply(requestOptions)
                .into(holder.cover);

        holder.title.setText(mediaFileBean.folderName);

        holder.num.setVisibility(mediaFileBean.num == 0 ? View.GONE : View.VISIBLE);

        holder.num.setText(mediaFileBean.num + activity.getString(R.string.ticket));

        holder.divid_line.setBackgroundColor(activity.getResources().getColor(divid_line_color));

        new Listener(position, holder.container);

    }


    private class Listener implements View.OnClickListener {

        int position;
        RelativeLayout container;

        public Listener(int position, RelativeLayout container) {
            this.position = position;
            this.container = container;
            this.container.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.container) {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(position, container, list.get(position));
                }
            }
        }
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {

        public void onClick(int position, View v, MediaFileBean mediaFileBean);

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView cover;
        TextView title;
        TextView num;
        View divid_line;
        RelativeLayout container;

        public ViewHolder(View itemView) {
            super(itemView);

            cover = (ImageView) itemView.findViewById(R.id.cover);
            title = (TextView) itemView.findViewById(R.id.title);
            num = (TextView) itemView.findViewById(R.id.num);
            divid_line = itemView.findViewById(R.id.divid_line);
            container = (RelativeLayout) itemView.findViewById(R.id.container);
        }
    }
}
