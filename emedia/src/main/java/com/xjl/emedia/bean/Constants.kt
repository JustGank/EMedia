package com.xjl.emedia.bean

import java.io.File

/**
 * <p>类作用描述  </p>
 * <p>创建时间 2023/3/6 16:20<p>
 * @author 180933664
 * @version v1.0
 * @update [日期] [更改人姓名][变更描述]
 */
object Constants {

    var appCacheRoot: String = ""

    fun init(appCacheRoot: String) {
        this.appCacheRoot = appCacheRoot
    }

    fun getAppImageDir(): String {
        return appCacheRoot + File.separator + "Images"
    }

    fun getAppFilesDir(): String {
        return appCacheRoot + File.separator + "Files"
    }

    fun getAppVideosDir(): String {
        return appCacheRoot + File.separator + "Videos"
    }

    fun getAppAudiosDir(): String {
        return appCacheRoot + File.separator + "Audio"
    }

    fun getSdcardAppDir(): String {
        return appCacheRoot + File.separator
    }


}