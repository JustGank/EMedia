package com.xjl.emedia.bean

import android.app.ProgressDialog
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.xjl.emedia.logger.Logger
import java.io.File

class ImageCompressOption(
    val outputPath: String = "",
    var openCompress: Boolean = true,
    val compressWidth: Int = 720,
    val compressHeight: Int = 1080,
    val dialog_class: Class<*> = ProgressDialog::class.java
) : Parcelable {
    private val TAG = "PickerImageCompressOpti"


    init {
        if (!TextUtils.isEmpty(outputPath)) {
            val file = File(outputPath)
            if (!file.exists()) {
                file.mkdirs()
            }
        }
    }

    fun valid(): Boolean {
        if (outputPath.isEmpty()) {
            Logger.w("$TAG outputPath is empty")
            return false
        }
        if (compressWidth < 1) {
            Logger.w("$TAG compressWidth : $compressWidth is not valid!")
            return false
        }

        if (compressHeight < 1) {
            Logger.w("$TAG compressHeight : $compressHeight is not valid!")
            return false
        }
        return true
    }

    constructor(parcel: Parcel) : this(

        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readSerializable() as Class<*>
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(outputPath)
        parcel.writeByte(if (openCompress) 1 else 0)
        parcel.writeInt(compressWidth)
        parcel.writeInt(compressHeight)
        parcel.writeSerializable(dialog_class)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageCompressOption> {
        override fun createFromParcel(parcel: Parcel): ImageCompressOption {
            return ImageCompressOption(parcel)
        }

        override fun newArray(size: Int): Array<ImageCompressOption?> {
            return arrayOfNulls(size)
        }
    }
}