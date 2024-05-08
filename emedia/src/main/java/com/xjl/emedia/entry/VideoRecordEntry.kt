package com.xjl.emedia.entry

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.xjl.emedia.R

/**
 *
 * 类作用描述
 *
 * 创建时间 2023/4/21 9:43
 *
 *
 *
 * @author 180933664
 * @version v1.0
 * @update [日期] [更改人姓名][变更描述]
 */
class VideoRecordEntry : Parcelable {
    
    var dont_have_camera_error: String = ""
    
    var dont_have_front_camera: String = ""
    
    var only_have_one_camera: String = ""
    var click_too_fast: String = ""
    
    var video_too_short: String = ""
    
    var video_captured: String = ""
    
    var video_interrupt: String = ""
    
    var camera_init_fail: String = ""
    
    var changing_flashLight_mode: String = ""
    
    var save_path_null: String = ""
    
    var please_gave_permission: String = ""
    
    var camera_occupancy: String = ""

    constructor(
        dont_have_camera_error: String,
        dont_have_front_camera: String,
        only_have_one_camera: String,
        click_too_fast: String,
        video_too_short: String,
        video_captured: String,
        video_interrupt: String,
        camera_init_fail: String,
        changing_flashLight_mode: String,
        save_path_null: String,
        please_gave_permission: String,
        camera_occupancy: String
    ) {
        this.dont_have_camera_error = dont_have_camera_error
        this.dont_have_front_camera = dont_have_front_camera
        this.only_have_one_camera = only_have_one_camera
        this.click_too_fast = click_too_fast
        this.video_too_short = video_too_short
        this.video_captured = video_captured
        this.video_interrupt = video_interrupt
        this.camera_init_fail = camera_init_fail
        this.changing_flashLight_mode = changing_flashLight_mode
        this.save_path_null = save_path_null
        this.please_gave_permission = please_gave_permission
        this.camera_occupancy = camera_occupancy
    }

    constructor(context: Context) {
        dont_have_camera_error = context.getString(R.string.dont_have_camera_error)
        dont_have_front_camera = context.getString(R.string.dont_have_front_camera)
        only_have_one_camera = context.getString(R.string.only_have_one_camera)
        click_too_fast = context.getString(R.string.click_too_fast)
        video_too_short = context.getString(R.string.video_too_short)
        video_captured = context.getString(R.string.video_captured)
        video_interrupt = context.getString(R.string.video_interrupt)
        camera_init_fail = context.getString(R.string.camera_init_fail)
        changing_flashLight_mode = context.getString(R.string.changing_flashLight_mode)
        save_path_null = context.getString(R.string.save_path_null)
        please_gave_permission = context.getString(R.string.please_gave_permission)
        camera_occupancy = context.getString(R.string.camera_occupancy)
    }

    protected constructor(parcel: Parcel) {
        dont_have_camera_error = parcel.readString()?:""
        dont_have_front_camera = parcel.readString()?:""
        only_have_one_camera = parcel.readString()?:""
        click_too_fast = parcel.readString()?:""
        video_too_short = parcel.readString()?:""
        video_captured = parcel.readString()?:""
        video_interrupt = parcel.readString()?:""
        camera_init_fail = parcel.readString()?:""
        changing_flashLight_mode = parcel.readString()?:""
        save_path_null = parcel.readString()?:""
        please_gave_permission = parcel.readString()?:""
        camera_occupancy = parcel.readString()?:""
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(dont_have_camera_error)
        dest.writeString(dont_have_front_camera)
        dest.writeString(only_have_one_camera)
        dest.writeString(click_too_fast)
        dest.writeString(video_too_short)
        dest.writeString(video_captured)
        dest.writeString(video_interrupt)
        dest.writeString(camera_init_fail)
        dest.writeString(changing_flashLight_mode)
        dest.writeString(save_path_null)
        dest.writeString(please_gave_permission)
        dest.writeString(camera_occupancy)
    }

    companion object CREATOR : Parcelable.Creator<VideoRecordEntry> {
        override fun createFromParcel(parcel: Parcel): VideoRecordEntry {
            return VideoRecordEntry(parcel)
        }

        override fun newArray(size: Int): Array<VideoRecordEntry?> {
            return arrayOfNulls(size)
        }
    }


}
