package com.xjl.emedia.logger


/**
 * Created by x33664 on 2019/1/29.
 */
object Logger {
    private const val TAG = "EMedia"
    private var openLogger = true
    private var loggerInterface: LoggerInterface = DefaultLogger()

    @JvmStatic
    fun isOpen(open: Boolean) {
        openLogger = open
    }

    @JvmStatic
    fun setLogger(loggerInterface: LoggerInterface) {
        this.loggerInterface = loggerInterface
    }

    @JvmStatic
    fun d(content: String) {
        if (openLogger) loggerInterface.d(TAG, content)
    }

    @JvmStatic
    fun i(content: String) {
        if (openLogger) loggerInterface.i(TAG, content)
    }

    @JvmStatic
    fun w(content: String) {
        if (openLogger) loggerInterface.w(TAG, content)
    }

    @JvmStatic
    fun e(content: String) {
        if (openLogger) loggerInterface.e(TAG, content)
    }
}
