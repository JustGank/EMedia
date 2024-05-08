package com.xjl.emedia.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.xjl.emedia.activity.MediaPickerActivity
import com.xjl.emedia.bean.MediaPickerBean
import java.io.File

/**
 * Created by x33664 on 2019/1/25.
 */
object IntentUtil {

    @JvmStatic
    fun parserMediaResultData(data: Intent?): List<MediaPickerBean> {
        val tempList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            data?.getParcelableArrayListExtra(
                MediaPickerActivity.RESULT_LIST,
                MediaPickerBean::class.java
            )
        } else {
            data?.getParcelableArrayListExtra<MediaPickerBean>(MediaPickerActivity.RESULT_LIST)
        }
        return tempList?:ArrayList()
    }

    /**
     * 打开本地图片
     */
    @JvmStatic
    fun openLocalImage(activity: Activity, filePath: String?) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri: Uri
        val file = File(filePath)
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(
                activity, activity.applicationContext.packageName
                        + ".provider", file
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //注意加上这句话
        } else {
            uri = Uri.fromFile(file)
        }
        intent.setDataAndType(uri, "image/*")
        activity.startActivity(intent)
    }

    /**
     * 此处与MediaPickerActivity中过滤出来的图片类型相匹配
     */
    @JvmStatic
    fun isImage(filePath: String): Boolean {
        if (TextUtils.isEmpty(filePath)) {
            return false
        }
        val endCase = filePath.substring(filePath.lastIndexOf("."))
        return if (endCase == ".jpeg" || endCase == ".png" || endCase == ".jpg") {
            true
        } else false
    }

    /**
     * 打开本地图片
     */
    @JvmStatic
    fun openLocalVideo(activity: Activity, filePath: String?) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri: Uri
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(
                activity, activity.applicationContext.packageName
                        + ".provider", File(filePath)
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //注意加上这句话
        } else {
            uri = Uri.fromFile(File(filePath))
        }
        intent.setDataAndType(uri, "video/*")
        activity.startActivity(intent)
    }

    /**
     * 此处与MediaPickerActivity中过滤出来的图片类型相匹配
     */
    @JvmStatic
    fun isVideo(filePath: String): Boolean {
        if (TextUtils.isEmpty(filePath)) {
            return false
        }
        val endCase = filePath.substring(filePath.lastIndexOf("."))
        return if (endCase == ".mp4" || endCase == ".3gp") {
            true
        } else false
    }

    /**
     * 打开失败的时候返回false
     */
    @JvmStatic
    fun openMedia(activity: Activity, path: String): Boolean {
        if (isImage(path)) {
            openLocalImage(activity, path)
            return true
        } else if (isVideo(path)) {
            openLocalVideo(activity, path)
            return true
        }
        return false
    }

    fun getFileProvider(context: Context): String {
        return context.packageManager.getPackageInfo(
            context.packageName,
            0
        ).packageName + ".provider"
    }

    fun notifyNewFile(context: Context, file: File) {
        context.sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(file)
            )
        )
    }

}
