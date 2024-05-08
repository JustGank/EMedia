package com.xjl.emedia.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by x33664 on 2018/11/8.
 */
class MediaPickerBean : Parcelable, Comparable<MediaPickerBean> {
    
    var size: Long = 0
    
    var mediaFilePath: String = ""
    
    var isPicked = false
    
    var type = 0
    
    var data = 0
    
    var duration: String = ""

    constructor()
    constructor(createTime: Int, mediaFilePath: String, type: Int, data: Int) {
        size = createTime.toLong()
        this.mediaFilePath = mediaFilePath
        this.data = data
        this.type = type
    }

    protected constructor(parcel: Parcel) {
        size = parcel.readLong()
        mediaFilePath = parcel.readString()?:""
        isPicked = parcel.readByte().toInt() != 0
        type = parcel.readInt()
        data = parcel.readInt()
        duration = parcel.readString()?:""
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(size)
        dest.writeString(mediaFilePath)
        dest.writeByte((if (isPicked) 1 else 0).toByte())
        dest.writeInt(type)
        dest.writeInt(data)
        dest.writeString(duration)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun compareTo(o: MediaPickerBean): Int {
        return o.data - data
    }

    companion object CREATOR : Parcelable.Creator<MediaPickerBean> {
        override fun createFromParcel(parcel: Parcel): MediaPickerBean {
            return MediaPickerBean(parcel)
        }

        override fun newArray(size: Int): Array<MediaPickerBean?> {
            return arrayOfNulls(size)
        }
    }


}
