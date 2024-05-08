package com.xjl.emedia.contract

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.xjl.emedia.activity.MediaPickerActivity
import com.xjl.emedia.bean.MediaPickerBean
import com.xjl.emedia.bean.MediaPickerRequestBean
import com.xjl.emedia.entry.MediaPickerEntry
import com.xjl.emedia.utils.IntentUtil

/**
 * <p>类作用描述  </p>
 *
 * <p>创建时间 2023/3/28 9:41<p>
 * @author 180933664
 * @version v1.0
 * @update [日期] [更改人姓名][变更描述]
 */
class AlbumPickerContract :
    ActivityResultContract<MediaPickerRequestBean, List<MediaPickerBean>?>() {

    override fun createIntent(context: Context, input: MediaPickerRequestBean): Intent {
        val intent = Intent(context, MediaPickerActivity::class.java)
        if (input.mediaPickerEntry == null) {
            input.mediaPickerEntry = MediaPickerEntry(context)
        }
        intent.putExtra(MediaPickerRequestBean::class.java.simpleName, input)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<MediaPickerBean>? {
        return IntentUtil.parserMediaResultData(intent)
    }
}