package com.xjl.emedia.logger

import android.util.Log

class DefaultLogger : LoggerInterface {
    override fun d(tag: String, content: String) {
        Log.d(tag, content)
    }

    override fun i(tag: String, content: String) {
        Log.i(tag, content)
    }

    override fun w(tag: String, content: String) {
        Log.w(tag, content)
    }

    override fun e(tag: String, content: String) {
        Log.e(tag, content)
    }
}