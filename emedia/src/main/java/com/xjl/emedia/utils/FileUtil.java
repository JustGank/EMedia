package com.xjl.emedia.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class FileUtil {

    /**
     * 根据路径获得图片并压缩然后旋转，返回是否成功
     */
    public static boolean isSaveCompressPicture(String filePath, String outputPath, int compressWidth, int compressHeight) {
        int degree = readPictureDegree(filePath);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//设置为ture,只读取图片的大小，不把它加载到内存中去
        BitmapFactory.decodeFile(filePath, options);

        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(options, compressWidth, compressHeight);

        // Decode bitmap with inSampleSize set

        Bitmap mbitmap = BitmapFactory.decodeFile(filePath, options);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mbitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options1 = 100;
        while (baos.toByteArray().length / 1024 > 100) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            mbitmap.compress(Bitmap.CompressFormat.JPEG, options1, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options1 -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        mbitmap = BitmapFactory.decodeStream(isBm, null, null);

        mbitmap = rotateBitmap(mbitmap, degree);

        return saveFileByBitemap(filePath, outputPath, mbitmap);

    }

    public static boolean saveFileByBitemap(String filePath, String outputPath, Bitmap mbitmap) {
        FileOutputStream fout = null;
        try {
            new File(outputPath);
            fout = new FileOutputStream(TextUtils.isEmpty(outputPath) ? filePath : outputPath);
            return mbitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                fout.flush();
                fout.close();
                if (mbitmap != null) {
                    mbitmap.recycle();
                    mbitmap = null;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
        if (bitmap == null)
            return null;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }


    //计算图片的缩放值
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;
        if (width > height && width > reqWidth) {
            inSampleSize = (int) (width / reqWidth);
        } else if (width < height && height > reqHeight) {
            inSampleSize = (int) (height / reqHeight);
        }
        if (inSampleSize <= 0)
            inSampleSize = 1;
        return inSampleSize;
    }


    public static File createImageFile(String mImagePath) {
        String mImageName = System.currentTimeMillis() + ".jpg";
        File mImageFile = new File(mImagePath + File.separator, mImageName);
        mImageFile.getParentFile().mkdirs();
        return mImageFile;
    }

    public static File createVideoFile(String mVideoPath) {
        String mVideoName = System.currentTimeMillis() + ".mp4";
        File mVideoFile = new File(mVideoPath + File.separator, mVideoName);
        mVideoFile.getParentFile().mkdirs();
        return mVideoFile;
    }


    public static String getFileFolderPath(String filePath){
        String folderPath="";
        if(!TextUtils.isEmpty(filePath))
        {
            int subEndPosition=filePath.lastIndexOf(File.separator);
            folderPath=filePath.substring(0,subEndPosition);
        }
        return folderPath;
    }

}
