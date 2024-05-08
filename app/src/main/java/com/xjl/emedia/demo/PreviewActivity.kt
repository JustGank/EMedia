package com.xjl.emedia.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.xjl.emedia.activity.MediaPickerActivity
import com.xjl.emedia.adapter.MediaPickerAdapter
import com.xjl.emedia.demo.databinding.ActivityPreviewBinding
import com.xjl.emedia.logger.Logger
import com.xjl.emedia.utils.IntentUtil.parserMediaResultData
import com.xjl.emedia.utils.ScreenUtil

class PreviewActivity : AppCompatActivity() {

    private val TAG = "PreviewActivity"

    private var compressOpen = false

    lateinit var binding: ActivityPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mediaList = parserMediaResultData(intent)
        compressOpen = intent.getBooleanExtra(MediaPickerActivity.COMPRESS_OPEN, false)
        Logger.i("$TAG onCreate compressOpen=$compressOpen")




        binding.recyclerview.apply {
            setLayoutManager(GridLayoutManager(this@PreviewActivity, 4))
            adapter = MediaPickerAdapter(
                this@PreviewActivity,
                mediaList,
                true,
                ScreenUtil.getScreenSize(this@PreviewActivity)[0] / 4
            )
        }


    }


}
