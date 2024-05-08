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
class MediaPickerEntry :  Parcelable {

    var photo_album: String = ""

    var all_pics: String = ""

    var photo_over_size: String = ""

    var video_over_size: String = ""

    var over_maxinum: String = ""

    var send: String = ""

    var preview: String = ""

    var original_pics: String = ""

    var ticket: String = ""

    constructor(
        photo_album: String,
        all_pics: String,
        photo_over_size: String,
        video_over_size: String,
        over_maxinum: String,
        send: String,
        preview: String,
        original_pics: String,
        ticket: String
    ) {
        this.photo_album = photo_album
        this.all_pics = all_pics
        this.photo_over_size = photo_over_size
        this.video_over_size = video_over_size
        this.over_maxinum = over_maxinum
        this.send = send
        this.preview = preview
        this.original_pics = original_pics
        this.ticket = ticket
    }

    constructor(context: Context) {
        photo_album = context.getString(R.string.photo_album)
        all_pics = context.getString(R.string.all_pics)
        photo_over_size = context.getString(R.string.photo_over_size)
        video_over_size = context.getString(R.string.video_over_size)
        over_maxinum = context.getString(R.string.over_maxinum)
        send = context.getString(R.string.send)
        preview = context.getString(R.string.preview)
        original_pics = context.getString(R.string.original_pics)
        ticket = context.getString(R.string.ticket)
    }

    protected constructor(parcel: Parcel) {
        photo_album = parcel.readString() ?: ""
        all_pics = parcel.readString() ?: ""
        photo_over_size = parcel.readString() ?: ""
        video_over_size = parcel.readString() ?: ""
        over_maxinum = parcel.readString() ?: ""
        send = parcel.readString() ?: ""
        preview = parcel.readString() ?: ""
        original_pics = parcel.readString() ?: ""
        ticket = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(photo_album)
        parcel.writeString(all_pics)
        parcel.writeString(photo_over_size)
        parcel.writeString(video_over_size)
        parcel.writeString(over_maxinum)
        parcel.writeString(send)
        parcel.writeString(preview)
        parcel.writeString(original_pics)
        parcel.writeString(ticket)
    }


    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MediaPickerEntry> {
        override fun createFromParcel(parcel: Parcel): MediaPickerEntry {
            return MediaPickerEntry(parcel)
        }

        override fun newArray(size: Int): Array<MediaPickerEntry?> {
            return arrayOfNulls(size)
        }
    }


}
