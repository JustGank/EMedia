package com.xjl.emedia.contract

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.FileProvider
import com.xjl.emedia.utils.IntentUtil
import java.io.File

class TakePhotoContract : ActivityResultContract<String, String?>() {

    var cameraPicFilePath=""

    /**
     * @param input String是传入的文件路径 如： Constants.getAppImageDir()
     * */
    override fun createIntent(context: Context, input: String): Intent {
        val dir = File(input)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val intent = Intent()
        intent.action = MediaStore.ACTION_IMAGE_CAPTURE
        val filename = System.currentTimeMillis().toString() + ".jpg"
        val f = File(dir, filename)
        val authority= IntentUtil.getFileProvider(context)
        val u = FileProvider.getUriForFile(context, authority, f)
        context.grantUriPermission(context.packageName, u, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, u)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        cameraPicFilePath=input+ File.separator+filename
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String {
        return cameraPicFilePath
    }


}