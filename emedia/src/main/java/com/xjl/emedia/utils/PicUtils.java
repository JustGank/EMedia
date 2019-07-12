package com.xjl.emedia.utils;

import android.media.ExifInterface;
import android.util.Log;

/**
 * Created by 180933664 on 2019/7/12.
 */

public class PicUtils {

    private static final String TAG = "PicUtils";

    public static int readPictureDegree(String path) {

        int degree=0;

        try {

            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Log.e(TAG, "orientation=" + orientation);
            switch (orientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree=90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
            Log.e(TAG, "orientation=" + orientation+"    degree="+degree);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return degree;
    }

}
