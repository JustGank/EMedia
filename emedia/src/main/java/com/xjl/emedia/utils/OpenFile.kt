package com.xjl.emedia.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.xjl.emedia.logger.Logger
import com.xjl.emedia.logger.Logger.i
import java.io.File
import java.util.Locale

/**
 * Created by x33664 on 2019/3/11.
 */
object OpenFile {
    fun openFile(context: Context, FilePath: String?): Boolean {
        val file = File(FilePath)
        val mimeType = getMIMEType(file)

        Logger.i("OpenFile openFile mimeType : $mimeType")
        return if (mimeType == "*/*") {
            false
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) //设置标记
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            i(context.packageName)
            val tempUri =
                FileProvider.getUriForFile(context, context.packageName + ".provider", file)
            intent.setDataAndType(tempUri, mimeType) //设置类型
            context.startActivity(intent)
            true
        }
    }

    private fun getMIMEType(file: File): String {
        var type = "*/*"
        val fName = file.name
        //获取后缀名前的分隔符"."在fName中的位置。
        val dotIndex = fName.lastIndexOf(".")
        if (dotIndex < 0) return type
        /* 获取文件的后缀名 */
        val fileType = fName.substring(dotIndex, fName.length).lowercase(
            Locale.getDefault()
        )
        if (fileType == null || "" == fileType) return type
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (i in MIME_MapTable.indices) {
            if (fileType == MIME_MapTable[i][0]) type = MIME_MapTable[i][1]
        }
        return type
    }

    public val MIME_MapTable = arrayOf(
        arrayOf(".m4a", "audio/*"),
        arrayOf(".m4b", "audio/*"),
        arrayOf(".m4p", "audio/*"),
        arrayOf(".mp2", "audio/*"),
        arrayOf(".mp3", "audio/*"),
        arrayOf(".mp3", "audio/*"),
        arrayOf(".mid", "audio/*"),
        arrayOf(".xmf", "audio/*"),
        arrayOf(".ogg", "audio/*"),
        arrayOf(".wav", "audio/*"),
        arrayOf(".wma", "audio/*"),
        arrayOf(".wmv", "audio/*"),
        arrayOf(".mpga", "audio/*"),
        arrayOf(".m3u", "audio/*"),
        arrayOf(".rmvb", "audio/*"),
        arrayOf(".3gp", "video/3gpp"),
        arrayOf(".m4u", "video/vnd.mpegurl"),
        arrayOf(".m4v", "video/x-m4v"),
        arrayOf(".mov", "video/quicktime"),
        arrayOf(".mp4", "video/mp4"),
        arrayOf(".asf", "video/x-ms-asf"),
        arrayOf(".avi", "video/x-msvideo"),
        arrayOf(".mpe", "video/mpeg"),
        arrayOf(".mpeg", "video/mpeg"),
        arrayOf(".mpg", "video/mpeg"),
        arrayOf(".mpg4", "video/mp4"),
        arrayOf(".bmp", "image/bmp"),
        arrayOf(".gif", "image/gif"),
        arrayOf(".jpeg", "image/jpeg"),
        arrayOf(".jpg", "image/jpeg"),
        arrayOf(".png", "image/png"),
        arrayOf(".apk", "application/vnd.android.package-archive"),
        arrayOf(".pps", "application/vnd.ms-powerpoint"),
        arrayOf(".ppt", "application/vnd.ms-powerpoint"),
        arrayOf("xls", "application/vnd.ms-excel"),
        arrayOf("xlsx", "application/vnd.ms-excel"),
        arrayOf(".doc", "application/msword"),
        arrayOf(".docx", "application/msword"),
        arrayOf(".pdf", "application/pdf"),
        arrayOf("chm", "application/x-chm"),
        arrayOf(".c", "text/plain"),
        arrayOf(".conf", "text/plain"),
        arrayOf(".cpp", "text/plain"),
        arrayOf(".java", "text/plain"),
        arrayOf(".h", "text/plain"),
        arrayOf(".log", "text/plain"),
        arrayOf(".prop", "text/plain"),
        arrayOf(".rc", "text/plain"),
        arrayOf(".sh", "text/plain"),
        arrayOf(".txt", "text/plain"),
        arrayOf(".xml", "text/plain"),
        arrayOf(".htm", "text/html"),
        arrayOf(".html", "text/html"),
        arrayOf(".bin", "application/octet-stream"),
        arrayOf(".exe", "application/octet-stream"),
        arrayOf(".class", "application/octet-stream"),
        arrayOf(".gtar", "application/x-gtar"),
        arrayOf(".gz", "application/x-gzip"),
        arrayOf(".jar", "application/java-archive"),
        arrayOf(".js", "application/x-javascript"),
        arrayOf(".mpc", "application/vnd.mpohun.certificate"),
        arrayOf(".msg", "application/vnd.ms-outlook"),
        arrayOf(".rar", "application/x-rar-compressed"),
        arrayOf(".rtf", "application/rtf"),
        arrayOf(".tar", "application/x-tar"),
        arrayOf(".tgz", "application/x-compressed"),
        arrayOf(".wps", "application/vnd.ms-works"),
        arrayOf(".z", "application/x-compress"),
        arrayOf(".zip", "application/zip"),
        arrayOf("", "*/*")
    )
}
