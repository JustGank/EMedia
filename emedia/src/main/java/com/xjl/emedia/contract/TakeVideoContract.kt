package com.xjl.emedia.contract

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.xjl.emedia.activity.VideoRecordActivity
import com.xjl.emedia.bean.MediaRecordRequestBean
import com.xjl.emedia.entry.VideoRecordEntry


/**
 * <p>创建时间 2023/3/28 9:47<p>
 * @author 180933664
 * @version v1.0
 *
 */
class TakeVideoContract : ActivityResultContract<MediaRecordRequestBean, String?>() {

    override fun createIntent(context: Context, input: MediaRecordRequestBean): Intent {
        val intent = Intent(context, VideoRecordActivity::class.java)
        if (input.videoRecordEntry == null) {
            input.videoRecordEntry = VideoRecordEntry(context)
        }
        intent.putExtra(MediaRecordRequestBean::class.java.simpleName,input)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        intent?.let {
            return it.getStringExtra(VideoRecordActivity.INTENT_EXTRA_VIDEO_PATH)
        }
        return null
    }
}