package com.xjl.emedia.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.xjl.emedia.R
import com.xjl.emedia.bean.BroadcastCMD
import com.xjl.emedia.databinding.ActivityVideoRecordBinding
import com.xjl.emedia.fragment.PreviewFragment
import com.xjl.emedia.fragment.VideoRecordFragment
import com.xjl.emedia.fragment.VideoRecordFragment.OnFinishRecordValueable
import com.xjl.emedia.logger.Logger

/**
 * Created by x33664 on 2019/2/13.
 */
class VideoRecordActivity : FragmentActivity() {

    companion object {
        private val TAG = VideoRecordActivity::class.java.simpleName
        const val RESULT_CODE_FOR_RECORD_VIDEO_CANCEL = 401
        const val RESULT_CODE_FOR_RECORD_VIDEO_FAILED = 404
        const val INTENT_EXTRA_VIDEO_PATH = "intent_extra_video_path"
    }

    lateinit var videoRecordFragment: VideoRecordFragment
    lateinit var previewFragment: PreviewFragment

    var videoRecordBroadreceiver: VideoRecordBroadreceiver? = null

    lateinit var binding: ActivityVideoRecordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val decorView = window.decorView
        val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        decorView.systemUiVisibility = option
        window.statusBarColor = Color.TRANSPARENT

        binding = ActivityVideoRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction().let {
            videoRecordFragment = VideoRecordFragment()
            videoRecordFragment.setOnFinishRecordValueable(onFinishRecordValueable)
            it.add(R.id.frame, videoRecordFragment)
            it.commitAllowingStateLoss()
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(BroadcastCMD.INTERRUPT_RECORD)
        videoRecordBroadreceiver = VideoRecordBroadreceiver()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(videoRecordBroadreceiver, intentFilter,Context.RECEIVER_NOT_EXPORTED)
        }else{
            registerReceiver(videoRecordBroadreceiver, intentFilter)
        }
    }

    private val onFinishRecordValueable = object:OnFinishRecordValueable   {
        override fun onFinish(path: String?) {
            val transaction = supportFragmentManager.beginTransaction()
            previewFragment = PreviewFragment.getINSTANCE(path)
            transaction.replace(R.id.frame, previewFragment).commitAllowingStateLoss()
        }
    }

    inner class VideoRecordBroadreceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Logger.i("$TAG VideoRecordBroadreceiver onReceive ")
            when (intent.action) {
                BroadcastCMD.INTERRUPT_RECORD -> {
                    videoRecordFragment.finishRecord(true)
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        videoRecordBroadreceiver?.let { unregisterReceiver(it) }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (::previewFragment.isInitialized) {
            previewFragment.onKeyDown()
        } else {
            videoRecordFragment.onKeyDown()
        }
        return super.onKeyDown(keyCode, event)
    }


}
