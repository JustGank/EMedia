package com.xjl.emedia.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.xjl.emedia.activity.VideoRecordActivity
import com.xjl.emedia.databinding.FragmentPreviewBinding
import com.xjl.emedia.logger.Logger
import com.xjl.emedia.utils.IntentUtil
import java.io.File

/**
 * Created by 180933664 on 2019/6/12.
 */
class PreviewFragment : Fragment() {

    private val TAG = PreviewFragment::class.java.simpleName

    companion object {
        fun getINSTANCE(path: String?): PreviewFragment {
            val previewFragment = PreviewFragment()
            val bundle = Bundle()
            bundle.putString("filePath", path)
            previewFragment.setArguments(bundle)
            return previewFragment
        }
    }

    lateinit var binding: FragmentPreviewBinding

    private var filePath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPreviewBinding.inflate(layoutInflater)
        filePath = arguments?.getString("filePath")
        Logger.i("$TAG onCreateView fileUri=$filePath")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            player.setOnClickListener {
                activity?.let {
                    filePath?.let { it1 ->
                        IntentUtil.openLocalVideo(
                            it,
                            it1
                        )
                    }
                }
            }

            cancelSelect.setOnClickListener { cancelSelect() }

            select.setOnClickListener {
                val intent = Intent()
                intent.putExtra(VideoRecordActivity.INTENT_EXTRA_VIDEO_PATH, filePath)
                activity?.setResult(Activity.RESULT_OK, intent)
                activity?.finish()
            }

        }

        activity?.let {
            Glide.with(it)
                .load(Uri.fromFile(File(filePath)))
                .apply(RequestOptions().apply {
                    skipMemoryCache(false)
                    centerCrop()
                }).into(binding.playerBackground)
        }


    }

    fun cancelSelect() {
        filePath?.let {
            File(it).delete()
            activity?.setResult(VideoRecordActivity.RESULT_CODE_FOR_RECORD_VIDEO_CANCEL)
            activity?.finish()
        }
    }

    fun onKeyDown() {
        cancelSelect()
    }

}
