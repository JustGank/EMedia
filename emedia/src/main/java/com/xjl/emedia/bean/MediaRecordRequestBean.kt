package com.xjl.emedia.bean

import android.os.Parcel
import android.os.Parcelable
import com.xjl.emedia.entry.VideoRecordEntry
import com.xjl.emedia.impl.PreOnClickListener

class MediaRecordRequestBean() : Parcelable {
    /**
     * 设置录制最小时间 最小值2秒
     */
    var recordMinTime :Int= 2
        set(unitSeconds) {
            field = if (unitSeconds < 2) {
                2
            } else {
                unitSeconds
            }
        }

    /**
     * 默认录制时间0 代表不限制时间  大于0时代表限制录制时间
     */
    var limitTime :Int = 0
        set(unitSeconds) {
            field = if (unitSeconds < 0) {
                0
            } else {
                unitSeconds + 1
            }
        }

    /**
     * 设置录像的质量 ALL代表显示 选择分辨率列表 即用户可以自己选择在录制页面选择分辨率
     * 其他的选择后 如果不存在那么会设置为最接近硬件支持的分辨率
     */
    var recordQuality: RecordQuality = RecordQuality.ALL

    var saveDirPath: String? = null

    /**
     * 显示闪光灯
     * */
    var showLight = true

    /**
     * 显示选择分辨率
     * */
    var showRatio = true
    var preOnClickListenerClass: Class<out PreOnClickListener>? = null

    /**
     * 语言词条显示内容
     * */
    var videoRecordEntry: VideoRecordEntry? = null

    constructor(parcel: Parcel) : this() {
        recordMinTime=parcel.readInt()
        limitTime=parcel.readInt()
        recordQuality=parcel.readSerializable() as RecordQuality
        saveDirPath = parcel.readString()
        showLight = parcel.readByte() != 0.toByte()
        showRatio = parcel.readByte() != 0.toByte()
        preOnClickListenerClass=parcel.readSerializable() as Class<out PreOnClickListener>?
        videoRecordEntry = parcel.readParcelable(VideoRecordEntry::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(recordMinTime)
        parcel.writeInt(limitTime)
        parcel.writeSerializable(recordQuality)
        parcel.writeString(saveDirPath)
        parcel.writeByte(if (showLight) 1 else 0)
        parcel.writeByte(if (showRatio) 1 else 0)
        parcel.writeSerializable(preOnClickListenerClass)
        parcel.writeParcelable(videoRecordEntry, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MediaRecordRequestBean> {
        override fun createFromParcel(parcel: Parcel): MediaRecordRequestBean {
            return MediaRecordRequestBean(parcel)
        }

        override fun newArray(size: Int): Array<MediaRecordRequestBean?> {
            return arrayOfNulls(size)
        }
    }


}