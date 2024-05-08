package com.xjl.emedia.demo

import android.app.Application
import com.xjl.emedia.bean.Constants

/**
 * Created by x33664 on 2019/2/14.
 */
class AppApplicaiton : Application() {
    override fun onCreate() {
        super.onCreate()
        this.externalCacheDir?.let {
            Constants.init(it.absolutePath)
        }
    }
}
