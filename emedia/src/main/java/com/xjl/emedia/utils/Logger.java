package com.xjl.emedia.utils;

import android.util.Log;

/**
 * Created by x33664 on 2019/1/29.
 */

public class Logger {

    private static final String TAG = "EMedia";

    private static boolean openLogger = true;

    public static void isOpen(boolean open) {
        openLogger = open;
    }

    public static void d(String content) {
        if (openLogger)
            Log.d(TAG, content);
    }

    public static void i(String content) {
        if (openLogger)
            Log.i(TAG, content);
    }

    public static void e(String content) {
        if (openLogger)
            Log.e(TAG, content);
    }

}
