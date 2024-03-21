package com.xjl.emedia.entry;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.xjl.emedia.R;

/**
 * <p>类作用描述  </p>
 * <p>创建时间 2023/4/21 9:43<p>
 *
 * @author 180933664
 * @version v1.0
 * @update [日期] [更改人姓名][变更描述]
 */
public class MediaPickerEntry implements Parcelable {

    public String photo_album = "";
    public String all_pics = "";
    public String photo_over_size = "";
    public String video_over_size = "";
    public String over_maxinum = "";
    public String send = "";
    public String preview = "";
    public String original_pics = "";
    public String ticket = "";

    public MediaPickerEntry(String photo_album,
                            String all_pics,
                            String photo_over_size,
                            String video_over_size,
                            String over_maxinum,
                            String send,
                            String preview,
                            String original_pics,
                            String ticket) {
        this.photo_album = photo_album;
        this.all_pics = all_pics;
        this.photo_over_size = photo_over_size;
        this.video_over_size = video_over_size;
        this.over_maxinum = over_maxinum;
        this.send = send;
        this.preview = preview;
        this.original_pics = original_pics;
        this.ticket = ticket;
    }

    public MediaPickerEntry(Context context) {
        photo_album = context.getString(R.string.photo_album);
        all_pics = context.getString(R.string.all_pics);
        photo_over_size = context.getString(R.string.photo_over_size);
        video_over_size = context.getString(R.string.video_over_size);
        over_maxinum = context.getString(R.string.over_maxinum);
        send = context.getString(R.string.send);
        preview = context.getString(R.string.preview);
        original_pics = context.getString(R.string.original_pics);
        ticket = context.getString(R.string.ticket);
    }

    protected MediaPickerEntry(Parcel in) {
        photo_album = in.readString();
        all_pics = in.readString();
        photo_over_size = in.readString();
        video_over_size = in.readString();
        over_maxinum = in.readString();
        send = in.readString();
        preview = in.readString();
        original_pics = in.readString();
        ticket = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(photo_album);
        dest.writeString(all_pics);
        dest.writeString(photo_over_size);
        dest.writeString(video_over_size);
        dest.writeString(over_maxinum);
        dest.writeString(send);
        dest.writeString(preview);
        dest.writeString(original_pics);
        dest.writeString(ticket);
    }

    public static final Creator<MediaPickerEntry> CREATOR = new Creator<MediaPickerEntry>() {
        @Override
        public MediaPickerEntry createFromParcel(Parcel in) {
            return new MediaPickerEntry(in);
        }

        @Override
        public MediaPickerEntry[] newArray(int size) {
            return new MediaPickerEntry[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
