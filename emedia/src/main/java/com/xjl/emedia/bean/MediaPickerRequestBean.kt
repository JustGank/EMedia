package com.xjl.emedia.bean

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Parcel
import android.os.Parcelable
import com.xjl.emedia.R
import com.xjl.emedia.entry.MediaPickerEntry

class MediaPickerRequestBean() : Parcelable {
    var max_chose_num = 1
    var pickerType = PickerType.PHOTO_VIDEO
    var subjectBackground = R.color.subject_background
    var subjectTextColor = R.color.subject_text_color
    var backImgRes = 0
    var compressOption: ImageCompressOption? = null
    var maxPhotoSize = (4 * 1024 * 1024).toLong()
    var maxVideoSize = (30 * 1024 * 1024).toLong()
    var overSizeVisible = true
    var openPreview = false
    var previewActivity: Class<out Activity>? = null
    var openSkipMemoryCache = false
    var openBottomMoreOperate = false
    var rowNum = 4
    var screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    var mediaPickerEntry: MediaPickerEntry? = null

    constructor(parcel: Parcel) : this() {
        max_chose_num = parcel.readInt()
        pickerType= parcel.readSerializable() as PickerType
        subjectBackground = parcel.readInt()
        subjectTextColor = parcel.readInt()
        backImgRes = parcel.readInt()
        compressOption=parcel.readParcelable(MediaPickerEntry::class.java.classLoader)
        maxPhotoSize = parcel.readLong()
        maxVideoSize = parcel.readLong()
        overSizeVisible = parcel.readByte() != 0.toByte()
        openPreview = parcel.readByte() != 0.toByte()
        previewActivity= parcel.readSerializable() as Class<out Activity>?
        openSkipMemoryCache = parcel.readByte() != 0.toByte()
        openBottomMoreOperate = parcel.readByte() != 0.toByte()
        rowNum = parcel.readInt()
        screenOrientation = parcel.readInt()
        mediaPickerEntry = parcel.readParcelable(MediaPickerEntry::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(max_chose_num)
        parcel.writeSerializable(pickerType)
        parcel.writeInt(subjectBackground)
        parcel.writeInt(subjectTextColor)
        parcel.writeInt(backImgRes)
        parcel.writeParcelable(compressOption,flags)
        parcel.writeLong(maxPhotoSize)
        parcel.writeLong(maxVideoSize)
        parcel.writeByte(if (overSizeVisible) 1 else 0)
        parcel.writeByte(if (openPreview) 1 else 0)
        parcel.writeSerializable(previewActivity)
        parcel.writeByte(if (openSkipMemoryCache) 1 else 0)
        parcel.writeByte(if (openBottomMoreOperate) 1 else 0)
        parcel.writeInt(rowNum)
        parcel.writeInt(screenOrientation)
        parcel.writeParcelable(mediaPickerEntry, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MediaPickerRequestBean> {
        override fun createFromParcel(parcel: Parcel): MediaPickerRequestBean {
            return MediaPickerRequestBean(parcel)
        }

        override fun newArray(size: Int): Array<MediaPickerRequestBean?> {
            return arrayOfNulls(size)
        }
    }
}