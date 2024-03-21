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
public class VideoRecordEntry implements Parcelable {

    public String dont_have_camera_error = "";
    public String dont_have_front_camera = "";
    public String only_have_one_camera = "";
    public String click_too_fast = "";

    public String video_too_short = "";
    public String video_captured = "";
    public String video_interrupt = "";
    public String camera_init_fail = "";

    public String changing_flashLight_mode = "";
    public String save_path_null = "";
    public String please_gave_permission = "";
    public String camera_occupancy = "";

    public VideoRecordEntry(String dont_have_camera_error,
                            String dont_have_front_camera,
                            String only_have_one_camera,
                            String click_too_fast,
                            String video_too_short,
                            String video_captured,
                            String video_interrupt,
                            String camera_init_fail,
                            String changing_flashLight_mode,
                            String save_path_null,
                            String please_gave_permission,
                            String camera_occupancy) {
        this.dont_have_camera_error = dont_have_camera_error;
        this.dont_have_front_camera = dont_have_front_camera;
        this.only_have_one_camera = only_have_one_camera;
        this.click_too_fast = click_too_fast;
        this.video_too_short = video_too_short;
        this.video_captured = video_captured;
        this.video_interrupt = video_interrupt;
        this.camera_init_fail = camera_init_fail;
        this.changing_flashLight_mode = changing_flashLight_mode;
        this.save_path_null = save_path_null;
        this.please_gave_permission = please_gave_permission;
        this.camera_occupancy = camera_occupancy;
    }



    public VideoRecordEntry(Context context) {
        dont_have_camera_error=context.getString(R.string.dont_have_camera_error);
        dont_have_front_camera=context.getString(R.string.dont_have_front_camera);
        only_have_one_camera=context.getString(R.string.only_have_one_camera);
        click_too_fast=context.getString(R.string.click_too_fast);

        video_too_short=context.getString(R.string.video_too_short);
        video_captured=context.getString(R.string.video_captured);
        video_interrupt=context.getString(R.string.video_interrupt);
        camera_init_fail=context.getString(R.string.camera_init_fail);

        changing_flashLight_mode=context.getString(R.string.changing_flashLight_mode);
        save_path_null=context.getString(R.string.save_path_null);
        please_gave_permission=context.getString(R.string.please_gave_permission);
        camera_occupancy=context.getString(R.string.camera_occupancy);
    }


    protected VideoRecordEntry(Parcel in) {
        dont_have_camera_error = in.readString();
        dont_have_front_camera = in.readString();
        only_have_one_camera = in.readString();
        click_too_fast = in.readString();
        video_too_short = in.readString();
        video_captured = in.readString();
        video_interrupt = in.readString();
        camera_init_fail = in.readString();
        changing_flashLight_mode = in.readString();
        save_path_null = in.readString();
        please_gave_permission = in.readString();
        camera_occupancy = in.readString();
    }

    public static final Creator<VideoRecordEntry> CREATOR = new Creator<VideoRecordEntry>() {
        @Override
        public VideoRecordEntry createFromParcel(Parcel in) {
            return new VideoRecordEntry(in);
        }

        @Override
        public VideoRecordEntry[] newArray(int size) {
            return new VideoRecordEntry[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dont_have_camera_error);
        dest.writeString(dont_have_front_camera);
        dest.writeString(only_have_one_camera);
        dest.writeString(click_too_fast);
        dest.writeString(video_too_short);
        dest.writeString(video_captured);
        dest.writeString(video_interrupt);
        dest.writeString(camera_init_fail);
        dest.writeString(changing_flashLight_mode);
        dest.writeString(save_path_null);
        dest.writeString(please_gave_permission);
        dest.writeString(camera_occupancy);
    }
}
