package com.xjl.emedia.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xjl.emedia.R;
import com.xjl.emedia.bean.MediaPickerBean;

import java.util.List;

/**
 * Created by x33664 on 2018/11/8.
 */

public class MediaPickerAdapter extends RecyclerView.Adapter<MediaPickerAdapter.ViewHolder> {

    private Activity activity;
    private LayoutInflater inflater;
    private List<MediaPickerBean> list;
    private boolean openSkipMemoryCache = false;
    private RequestOptions requestOptions;

    public MediaPickerAdapter(Activity activity, List<MediaPickerBean> list) {
        this.activity = activity;
        this.list = list;
        this.inflater = activity.getLayoutInflater();
        initRequestOptions();
    }

    public MediaPickerAdapter(Activity activity, List<MediaPickerBean> list, boolean openSkipMemoryCache) {
        this.activity = activity;
        this.list = list;
        this.inflater = activity.getLayoutInflater();
        this.openSkipMemoryCache = openSkipMemoryCache;
        initRequestOptions();
    }

    private void initRequestOptions(){
        this.requestOptions=new RequestOptions();
        this.requestOptions.skipMemoryCache(openSkipMemoryCache);
        this.requestOptions.centerCrop();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_media_picker, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        MediaPickerBean mediaPickerBean = list.get(position);

        if(!holder.coverUrl.equals(mediaPickerBean.getMediaFilePath())){
            Glide.with(activity)
                    .load("file://" + mediaPickerBean.getMediaFilePath())
                    .apply(requestOptions)
                    .into(holder.cover);

            holder.coverUrl=mediaPickerBean.getMediaFilePath();
        }

        holder.selected.setBackgroundResource(mediaPickerBean.isPicked
                ? R.mipmap.image_choose : R.mipmap.image_not_chose);

        if (mediaPickerBean.type == 1) {
            holder.time.setVisibility(View.GONE);
        } else {
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(mediaPickerBean.getDuration());
        }


        new Listener(holder.container, position);
    }

    public void setList(List<MediaPickerBean> list) {
        this.list.clear();
        this.list.addAll(list);
        notifyDataSetChanged();
    }


    public class Listener implements View.OnClickListener {

        ViewGroup container;
        int position;

        public Listener(ViewGroup container, int position) {
            this.container = container;
            this.position = position;
            this.container.setOnClickListener(this);
            this.container.findViewById(R.id.cover).setOnClickListener(this);
            this.container.findViewById(R.id.selected_container).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.selected_container) {
                if (null != onItemClickListener) {
                    onItemClickListener.onSelectedClicked(position, list.get(position));
                }
            } else if (v.getId() == R.id.cover) {
                if (null != onItemClickListener) {
                    onItemClickListener.onCoverClicked(position, list.get(position));
                }
            } else if (v.getId() == R.id.container) {

            }
        }
    }

    public void notifyPickState(int position) {
        list.get(position).setPicked(!list.get(position).isPicked());
        notifyItemChanged(position);
    }

    OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        public void onSelectedClicked(int position, MediaPickerBean bean);

        public void onCoverClicked(int position, MediaPickerBean bean);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        protected ImageView cover;
        protected ImageView selected;
        protected TextView time;
        protected RelativeLayout container;
        protected String coverUrl="";


        public ViewHolder(View itemView) {
            super(itemView);
            cover = (ImageView) itemView.findViewById(R.id.cover);
            selected = (ImageView) itemView.findViewById(R.id.selected);
            time = (TextView) itemView.findViewById(R.id.time);
            container = (RelativeLayout) itemView.findViewById(R.id.container);
        }
    }


}
