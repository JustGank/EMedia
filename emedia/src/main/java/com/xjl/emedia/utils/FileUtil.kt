package com.xjl.emedia.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.text.TextUtils
import java.io.File
import java.io.IOException

object FileUtil {

    /**
     * 根据路径获得图片并压缩然后旋转，返回是否成功
     */
    fun readPictureDegree(path: String?): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(path!!)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return degree
    }

    fun rotateBitmap(bitmap: Bitmap?, rotate: Int): Bitmap? {
        if (bitmap == null) return null
        val w = bitmap.width
        val h = bitmap.height
        val mtx = Matrix()
        mtx.postRotate(rotate.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true)
    }

    //计算图片的缩放值
    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1
        if (width > height && width > reqWidth) {
            inSampleSize = (width / reqWidth)
        } else if (width < height && height > reqHeight) {
            inSampleSize = (height / reqHeight)
        }
        if (inSampleSize <= 0) inSampleSize = 1
        return inSampleSize
    }

    fun createImageFile(mImagePath: String): File {
        val mImageName = System.currentTimeMillis().toString() + ".jpg"
        val mImageFile = File(mImagePath + File.separator, mImageName)
        mImageFile.parentFile.mkdirs()
        return mImageFile
    }

    fun createVideoFile(mVideoPath: String): File {
        val mVideoName = System.currentTimeMillis().toString() + ".mp4"
        val mVideoFile = File(mVideoPath + File.separator, mVideoName)
        mVideoFile.parentFile.mkdirs()
        return mVideoFile
    }

    @JvmStatic
    fun getFileFolderPath(filePath: String): String {
        var folderPath = ""
        if (!TextUtils.isEmpty(filePath)) {
            val subEndPosition = filePath.lastIndexOf(File.separator)
            folderPath = filePath.substring(0, subEndPosition)
        }
        return folderPath
    }
}
