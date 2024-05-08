package com.xjl.emedia.contract

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import com.xjl.emedia.logger.Logger

/**
 * <p>
 *
 * </p>
 * <p>创建时间 2023/3/28 10:02<p>
 * @author 180933664
 * @version v1.0
 * @update [日期] [更改人姓名][变更描述]
 */
class TakeFileContract : ActivityResultContract<String?, String?>() {

    private val TAG = "TakeFileContract"

    val defaultFileType = "*/*"
    lateinit var context: Context

    /**
     * input传入的是fileType可以传  :
     * */
    override fun createIntent(context: Context, input: String?): Intent {
        this.context = context
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = input ?: defaultFileType
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        intent?.let {
            val uri = it.data
            val fileAbsolutePath = getFileAbsolutePath(context, uri)
            Logger.i("$TAG parseResult  = $fileAbsolutePath")
            return fileAbsolutePath
        }
        return null
    }

    fun getFileAbsolutePath(context: Context?, imageUri: Uri?): String? {
        Logger.i("$TAG getFileAbsolutePath imageUri : $imageUri")
        if (context == null || imageUri == null) {
            return null
        }
        Logger.i("$TAG getFileAbsolutePath imageUri : $imageUri")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return getRealFilePath(context, imageUri)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(
                context,
                imageUri
            )
        ) {
            if (isExternalStorageDocument(imageUri)) {
                val docId = DocumentsContract.getDocumentId(imageUri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(imageUri)) {
                val id = DocumentsContract.getDocumentId(imageUri)
                if (id.startsWith("raw:")) {
                    return id.replaceFirst("raw:".toRegex(), "")
                }
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(imageUri) || isSuiDocument(imageUri)) {
                Logger.e( "$TAG enter isMediaDocument")
                val docId = DocumentsContract.getDocumentId(imageUri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val type = split[0]
                var contentUri = MediaStore.Files.getContentUri("external")
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                if (contentUri == null) {
                    Logger.e("$TAG  getFileAbsolutePath contentUri==null!")
                    return null
                }
                val selection = MediaStore.Images.Media._ID + "=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } // MediaStore (and general)
        else if ("content".equals(imageUri.scheme, ignoreCase = true)) {
            // Return the remote address
            if (isGooglePhotosUri(imageUri)) {
                return imageUri.lastPathSegment
            }
            if (isHuaWeiUri(imageUri)) {
                val uriPath = imageUri.path
                //content://com.huawei.hidisk.fileprovider/root/storage/emulated/0/Android/data/com.xxx.xxx/
                if (uriPath != null && uriPath.startsWith("/root")) {
                    return uriPath.replace("/root".toRegex(), "")
                }
            }
            return getDataColumn(context, imageUri, null, null)
        } else if ("file".equals(imageUri.scheme, ignoreCase = true)) {
            return imageUri.path
        }
        return null
    }

    fun getRealFilePath(context: Context, uri: Uri?): String? {
        if (null == uri) return null
        val scheme = uri.scheme
        var data: String? = null
        if (scheme == null) data = uri.path else if (ContentResolver.SCHEME_FILE == scheme) {
            data = uri.path
        } else if (ContentResolver.SCHEME_CONTENT == scheme) {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.ImageColumns.DATA),
                null,
                null,
                null
            )
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    if (index > -1) {
                        data = cursor.getString(index)
                    }
                }
                cursor.close()
            }
        }
        return data
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }


    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    fun isHuaWeiUri(uri: Uri): Boolean {
        return "com.huawei.hidisk.fileprovider" == uri.authority
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun isSuiDocument(uri: Uri): Boolean {
        return "com.android.documentsui.doc.documents" == uri.authority
    }


    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return null
    }


}