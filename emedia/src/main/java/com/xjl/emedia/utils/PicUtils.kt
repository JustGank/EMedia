package com.xjl.emedia.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.text.TextUtils
import com.xjl.emedia.logger.Logger
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Created by 180933664 on 2019/7/12.
 */
object PicUtils {
    private const val TAG = "PicUtils"

    @JvmStatic
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
            Logger.i("$TAG readPictureDegree orientation=$orientation , degree=$degree")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return degree
    }

    @JvmStatic
    fun isSaveCompressPicture(
        filePath: String?,
        outputPath: String?,
        compressWidth: Int,
        compressHeight: Int
    ): Boolean {
        val degree = FileUtil.readPictureDegree(filePath)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true //设置为ture,只读取图片的大小，不把它加载到内存中去
        BitmapFactory.decodeFile(filePath, options)
        options.inJustDecodeBounds = false
        options.inSampleSize =
            FileUtil.calculateInSampleSize(options, compressWidth, compressHeight)

        // Decode bitmap with inSampleSize set
        var mbitmap = BitmapFactory.decodeFile(filePath, options)
        val baos = ByteArrayOutputStream()
        mbitmap!!.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            baos
        ) //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        var options1 = 100
        while (baos.toByteArray().size / 1024 > 100 && options1 >= 0) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset() //重置baos即清空baos
            mbitmap.compress(
                Bitmap.CompressFormat.JPEG,
                options1,
                baos
            ) //这里压缩options%，把压缩后的数据存放到baos中
            options1 -= 10 //每次都减少10
        }
        val isBm = ByteArrayInputStream(baos.toByteArray()) //把压缩后的数据baos存放到ByteArrayInputStream中
        mbitmap = BitmapFactory.decodeStream(isBm, null, null)
        mbitmap = FileUtil.rotateBitmap(mbitmap, degree)
        return  saveFileByBitemap(filePath, outputPath, mbitmap)
    }

    fun saveFileByBitemap(filePath: String?, outputPath: String?, mbitmap: Bitmap?): Boolean {
        var mbitmap = mbitmap
        var fout: FileOutputStream? = null
        try {
            File(outputPath)
            fout = FileOutputStream(if (TextUtils.isEmpty(outputPath)) filePath else outputPath)
            return mbitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, fout)
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            try {
                fout!!.flush()
                fout.close()
                if (mbitmap != null) {
                    mbitmap.recycle()
                    mbitmap = null
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        return false
    }

}
