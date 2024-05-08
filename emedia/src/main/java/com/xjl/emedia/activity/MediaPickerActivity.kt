package com.xjl.emedia.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.xjl.emedia.R
import com.xjl.emedia.adapter.MediaPickerAdapter
import com.xjl.emedia.adapter.PopPicFolderAdapter
import com.xjl.emedia.bean.MediaForderBean
import com.xjl.emedia.bean.MediaPickerBean
import com.xjl.emedia.bean.MediaPickerRequestBean
import com.xjl.emedia.bean.PickerType
import com.xjl.emedia.databinding.ActivityMediaPickerBinding
import com.xjl.emedia.entry.MediaPickerEntry
import com.xjl.emedia.logger.Logger
import com.xjl.emedia.popwindow.PicFolderListPopwindow
import com.xjl.emedia.utils.FileUtil.getFileFolderPath
import com.xjl.emedia.utils.IntentUtil.isImage
import com.xjl.emedia.utils.IntentUtil.isVideo
import com.xjl.emedia.utils.IntentUtil.openLocalImage
import com.xjl.emedia.utils.IntentUtil.openLocalVideo
import com.xjl.emedia.utils.PicUtils
import com.xjl.emedia.utils.ScreenUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.Collections

/**
 * Created by x33664 on 2018/11/7.
 */
class MediaPickerActivity : AppCompatActivity() {

    private val TAG = MediaPickerActivity::class.java.simpleName

    companion object {
        const val RESULT_LIST = "result_list"
        const val COMPRESS_OPEN = "compress_open"
        const val FINISH_MEDIA_PICKER_ACTIVITY = "finish_media_picker_activity"
    }

    private var adapter: MediaPickerAdapter? = null
    private val mediaPickerBeanList: MutableList<MediaPickerBean> = ArrayList()
    private val pickedList = ArrayList<MediaPickerBean>()
    private val mediaFolderMap: HashMap<String, MediaForderBean> = HashMap()
    private val mediaFolderListMap = HashMap<String, ArrayList<MediaPickerBean>>()
    private var popwindow: PicFolderListPopwindow? = null
    private var popPicFolderAdapter: PopPicFolderAdapter? = null
    private val CODE_FOR_WRITE_PERMISSION = 100

    /**
     * 图片查询参数
     */
    private val projectionImg = arrayOf(
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.SIZE
    )
    private val whereImg = (MediaStore.Images.Media.MIME_TYPE + "=? or "
            + MediaStore.Images.Media.MIME_TYPE + "=? or "
            + MediaStore.Images.Media.MIME_TYPE + "=?")
    private val whereImgArgs = arrayOf("image/jpeg", "image/png", "image/jpg")

    /**
     * 视频查询参数
     */
    var projectionVideo = arrayOf(
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.SIZE
    )
    var whereVideo = (MediaStore.Images.Media.MIME_TYPE + "=? or "
            + MediaStore.Video.Media.MIME_TYPE + "=?")
    var whereVideoArgs = arrayOf("video/mp4", "video/3gp")
    var simpleDateFormat = SimpleDateFormat("mm:ss")

    private var waitingDialog: Any? = null
    private var screentWidth = 0
    private var screenHeight = 0

    //跨页面关闭广播接收器
    private var finishActivityReceiver: FinshActivityReceiver? = null

    private lateinit var mediaPickerRequestBean: MediaPickerRequestBean

    private lateinit var viewBinding: ActivityMediaPickerBinding

    private lateinit var entry: MediaPickerEntry

    override fun onCreate(savedInstanceState: Bundle?) {
        mediaPickerRequestBean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                MediaPickerRequestBean::class.java.simpleName,
                MediaPickerRequestBean::class.java
            )!!
        } else {
            intent.getParcelableExtra(MediaPickerRequestBean::class.java.simpleName)!!
        }
        mediaPickerRequestBean.apply {
            waitingDialog = dialog_class.getConstructor(Context::class.java)
                .newInstance(this@MediaPickerActivity)
            this@MediaPickerActivity.entry =
                mediaPickerEntry ?: MediaPickerEntry(this@MediaPickerActivity)

            Logger.i("$TAG onCreate screenOrientation=$screenOrientation")
            requestedOrientation = screenOrientation
        }

        super.onCreate(savedInstanceState)
        viewBinding = ActivityMediaPickerBinding.inflate(layoutInflater)
        super.setContentView(viewBinding.root)

        val decorView = window.decorView
        val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        decorView.systemUiVisibility = option
        window.statusBarColor = Color.TRANSPARENT

        val screenSize = ScreenUtil.getScreenSize(this)
        screentWidth = screenSize[0]
        screenHeight = screenSize[1]

        initView()
        getMediaFiles()
        registReveiver()
    }

    private fun initView() {
        viewBinding.apply {
            ivBack.setImageResource(R.mipmap.ic_menu_back)
            ivBack.setOnClickListener { finish() }
            titleContianer.setBackgroundResource(mediaPickerRequestBean.subjectBackground)
            titleTv.setTextColor(getColor(mediaPickerRequestBean.subjectTextColor))
            titleTv.text = entry.photo_album
            chosedNum.setOnClickListener {
                if (pickedList.size > 0) {
                    if (mediaPickerRequestBean.openCompress) {
                        asyncTask.execute()
                    } else {
                        postResult()
                    }
                }
            }
            moreContainer.setBackgroundResource(mediaPickerRequestBean.subjectBackground)
            moreContainer.visibility =
                if (mediaPickerRequestBean.openBottomMoreOperate) View.VISIBLE else View.GONE
            allPic.setTextColor(getColor(mediaPickerRequestBean.subjectTextColor))
            allPic.text = entry.all_pics
            orginalPic.setTextColor(getColor(mediaPickerRequestBean.subjectTextColor))
            orginalPic.text = entry.original_pics
            orginalPicSelect.setOnClickListener {
                mediaPickerRequestBean.openCompress = !mediaPickerRequestBean.openCompress
                orginalPicSelect.setBackgroundResource(if (mediaPickerRequestBean.openCompress) R.mipmap.all_unselected else R.mipmap.all_selected)
            }
            orginalPicSelect.setBackgroundResource(if (mediaPickerRequestBean.openCompress) R.mipmap.all_unselected else R.mipmap.all_selected)
            orginalPic.setOnClickListener {
                mediaPickerRequestBean.openCompress = !mediaPickerRequestBean.openCompress
                orginalPicSelect.setBackgroundResource(if (mediaPickerRequestBean.openCompress) R.mipmap.all_unselected else R.mipmap.all_selected)
            }
            preview.visibility =
                if (mediaPickerRequestBean.previewActivity == null) View.GONE else View.VISIBLE
            preview.setOnClickListener { postPickedListToPreviewActivity() }
            preview.setTextColor(getColor(mediaPickerRequestBean.subjectTextColor))
            imageFolderContainer.setOnClickListener {
                popwindow?.let {
                    if (it.isShowing) {
                        it.dismiss()
                    } else {
                        it.showAtLocation(
                            orginalPicSelect,
                            Gravity.TOP or Gravity.LEFT, -1,
                            (screenHeight - imageFolderContainer.height - ScreenUtil.dip2px(
                                this@MediaPickerActivity,
                                320f
                            ))
                        )
                    }
                }
            }
            /**
             * 根据一行的显示数量控制item的高
             */
            val itemHeight = screentWidth / mediaPickerRequestBean.rowNum
            recyclerview.setLayoutManager(
                GridLayoutManager(
                    this@MediaPickerActivity,
                    mediaPickerRequestBean.rowNum
                )
            )
            adapter = MediaPickerAdapter(
                this@MediaPickerActivity,
                ArrayList(),
                mediaPickerRequestBean.openSkipMemoryCache,
                itemHeight
            )
            recyclerview.setAdapter(adapter)
            adapter?.onItemClickListener = medidClickListener
            (recyclerview.itemAnimator as SimpleItemAnimator?)?.supportsChangeAnimations = false
        }
    }

    private fun getMediaFiles() {
        mediaPickerBeanList.clear()
        pickedList.clear()

        val permissions: MutableList<String> = mutableListOf()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    application,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (ContextCompat.checkSelfPermission(
                    application,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (ContextCompat.checkSelfPermission(
                        application,
                        Manifest.permission.READ_MEDIA_VIDEO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                }
            }

        } else {
            if (ContextCompat.checkSelfPermission(
                    application,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

        }

        Logger.i("$TAG getMediaFiles no granted permissions :${permissions.toString()}")

        if (permissions.isEmpty()) {
            startGetMediaThread()
        } else {
            ActivityCompat.requestPermissions(
                this@MediaPickerActivity, permissions.toTypedArray(),
                CODE_FOR_WRITE_PERMISSION
            )
        }
    }

    private fun registReveiver() {
        finishActivityReceiver = FinshActivityReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(FINISH_MEDIA_PICKER_ACTIVITY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(finishActivityReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(finishActivityReceiver, intentFilter)
        }
    }

    private fun startGetMediaThread() {
        mediaFolderMap.clear()
        Thread {
            if (mediaPickerRequestBean.pickerType == PickerType.ONLY_PHOTO || mediaPickerRequestBean.pickerType == PickerType.PHOTO_VIDEO) {
                val imageCursor = contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projectionImg,
                    whereImg, whereImgArgs, MediaStore.Images.Media.DATE_MODIFIED + " desc"
                )
                if (imageCursor != null) {
                    while (imageCursor.moveToNext()) {
                        val size =
                            imageCursor.getLong(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
                        if (mediaPickerRequestBean.overSizeVisible || size <= mediaPickerRequestBean.maxPhotoSize) {
                            val mediaPickerBean = MediaPickerBean()
                            mediaPickerBean.type = 1
                            mediaPickerBean.size = imageCursor.getLong(
                                imageCursor.getColumnIndexOrThrow(
                                    MediaStore.Images.Media.SIZE
                                )
                            )
                            //获取图片的生成日期
                            val columnIndex =
                                imageCursor.getColumnIndex(MediaStore.Images.Media.DATA)
                            if (columnIndex > -1) {
                                val data = imageCursor.getBlob(columnIndex)
                                val path = String(data, 0, data.size - 1)
                                mediaPickerBean.mediaFilePath = path
                                val time = File(path).lastModified()
                                mediaPickerBean.data = (time / 1000).toInt()
                                mediaPickerBeanList!!.add(mediaPickerBean)
                                putFolderPathToMap(mediaPickerBean)
                            } else Logger.w("$TAG startGetMediaThread invalid columnIndex $columnIndex")

                        }
                    }
                    Logger.i(
                        "$TAG startGetMediaThread image cursor length " + mediaPickerBeanList.size
                    )
                }
            }
            if (mediaPickerRequestBean.pickerType == PickerType.ONLY_VIDEO
                || mediaPickerRequestBean.pickerType == PickerType.PHOTO_VIDEO
            ) {
                val videoCursor = contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projectionVideo,
                    whereVideo,
                    whereVideoArgs,
                    MediaStore.Video.Media.DATE_ADDED + " desc "
                )
                if (videoCursor != null) {
                    while (videoCursor.moveToNext()) {
                        val size =
                            videoCursor.getLong(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))
                        if (mediaPickerRequestBean.overSizeVisible || size <= mediaPickerRequestBean.maxVideoSize) {
                            val mediaPickerBean = MediaPickerBean()
                            mediaPickerBean.type = 2
                            mediaPickerBean.mediaFilePath = videoCursor.getString(
                                videoCursor.getColumnIndexOrThrow(
                                    MediaStore.Video.Media.DATA
                                )
                            )
                            mediaPickerBean.duration = simpleDateFormat.format(
                                videoCursor.getInt(
                                    videoCursor.getColumnIndexOrThrow(
                                        MediaStore.Video.Media.DURATION
                                    )
                                )
                            )
                            mediaPickerBean.data =
                                (File(mediaPickerBean.mediaFilePath).lastModified() / 1000).toInt()
                            mediaPickerBean.size = videoCursor.getLong(
                                videoCursor.getColumnIndexOrThrow(
                                    MediaStore.Video.Media.SIZE
                                )
                            )
                            mediaPickerBeanList!!.add(mediaPickerBean)
                            putFolderPathToMap(mediaPickerBean)
                        }
                    }
                    Log.e(
                        TAG, "startGetMediaThread image cursor length + video cursor length"
                                + mediaPickerBeanList!!.size
                    )
                }
            }
            Collections.sort(mediaPickerBeanList)
            runOnUiThread {
                adapter!!.setList(mediaPickerBeanList)
                if (mediaPickerRequestBean.openBottomMoreOperate) {
                    initFolderPop()
                }
            }
        }.start()
    }

    private fun putFolderPathToMap(pickerBean: MediaPickerBean) {
        //如果没有开启则不再处理文件夹Map
        if (!mediaPickerRequestBean.openBottomMoreOperate) {
            return
        }

        val folderPath = getFileFolderPath(pickerBean.mediaFilePath)
        var mediaFileBean = mediaFolderMap[folderPath]
        var folderBeans = mediaFolderListMap[folderPath]
        if (mediaFileBean == null) {
            mediaFileBean = MediaForderBean(folderPath)
            mediaFileBean.num = 1
            mediaFileBean.coverFilePath = pickerBean.mediaFilePath
            mediaFolderMap[folderPath] = mediaFileBean
            folderBeans = ArrayList()
            folderBeans.add(pickerBean)
            mediaFolderListMap[folderPath] = folderBeans
        } else {
            mediaFileBean.num++
            folderBeans?.add(pickerBean)
        }

    }

    private fun initFolderPop() {

        //如果说所有照片的文件夹中的对象数为0，那么就不再进行初始化。
        if (mediaPickerBeanList.size == 0) {
            return
        }

        //先添加所有照片的第一个元素
        val list: MutableList<MediaForderBean> = ArrayList()
        val mediaForderBean = MediaForderBean("")
        mediaForderBean.folderName = entry.all_pics
        mediaForderBean.num = 0
        mediaForderBean.coverFilePath = mediaPickerBeanList[0].mediaFilePath
        list.add(mediaForderBean)
        list.addAll(mediaFolderMap.values)
        popwindow = PicFolderListPopwindow(this, list, R.color.divid_line_color, entry!!.ticket)
        popPicFolderAdapter = popwindow!!.adapter
        popPicFolderAdapter!!.setOnItemClickListener(picFolderItemClickListener)
        popwindow!!.width = screentWidth
    }

    var picFolderItemClickListener = object : PopPicFolderAdapter.OnItemClickListener {
        override fun onClick(position: Int, v: View, mediaForderBean: MediaForderBean) {
            if (mediaForderBean.num == 0) {
                viewBinding.titleTv.text = entry.photo_album
                viewBinding.allPic.text = entry.all_pics
                adapter!!.setList(mediaPickerBeanList)
            } else {
                viewBinding.titleTv.text = mediaForderBean.folderName
                viewBinding.allPic.text = mediaForderBean.folderName
                mediaFolderListMap[mediaForderBean.folderPath]?.let { adapter!!.setList(it) }
            }
            if (popwindow != null) {
                popwindow!!.dismiss()
            }
        }
    }
    var medidClickListener = object : MediaPickerAdapter.OnItemClickListener {
        override fun onSelectedClicked(position: Int, bean: MediaPickerBean) :Boolean{
            if (bean.type == 1 && bean.size > mediaPickerRequestBean.maxPhotoSize) {
                showToast(entry.photo_over_size)
                return false
            } else if (bean.type == 2 && bean.size > mediaPickerRequestBean.maxVideoSize) {
                showToast(entry.video_over_size)
                return false
            }
            if (pickedList.contains(bean)) {
                pickedList.remove(bean)
            } else {
                if (pickedList.size < mediaPickerRequestBean.max_chose_num) {
                    pickedList.add(bean)
                } else {
                    showToast(entry.over_maxinum)
                    return false
                }
            }
            if (pickedList.size == 0) {
                viewBinding.chosedNum.visibility = View.GONE
                viewBinding.preview.visibility = View.GONE
            } else {
                viewBinding.chosedNum.visibility = View.VISIBLE
                viewBinding.chosedNum.text =
                    entry.send + "(" + pickedList.size + "/" + mediaPickerRequestBean.max_chose_num + ")"
                if (mediaPickerRequestBean.previewActivity != null) {
                    viewBinding.preview.visibility = View.VISIBLE
                    viewBinding.preview.text = entry.preview + "(" + pickedList.size + ")"
                }
            }
            return true
        }

        override fun onCoverClicked(position: Int, bean: MediaPickerBean) {
            if (mediaPickerRequestBean.previewActivity != null) {
                postPickedListToPreviewActivity(bean)
            } else if (mediaPickerRequestBean.openPreview) {
                if (isImage(bean.mediaFilePath)) {
                    openLocalImage(this@MediaPickerActivity, bean.mediaFilePath)
                } else if (isVideo(bean.mediaFilePath)) {
                    openLocalVideo(this@MediaPickerActivity, bean.mediaFilePath)
                }
            }
        }
    }
    private var toast: Toast? = null
    protected fun showToast(s: String?) {
        Log.e(TAG, "toast is null=" + (toast == null))
        if (toast == null) {
            toast = Toast.makeText(this, s, Toast.LENGTH_SHORT)
        } else {
            toast!!.setText(s)
        }
        toast!!.show()
    }


    private fun postPickedListToPreviewActivity() {
        if (mediaPickerRequestBean.previewActivity != null) {
            val intent = Intent(this@MediaPickerActivity, mediaPickerRequestBean.previewActivity)
            intent.putExtra(RESULT_LIST, pickedList)
            intent.putExtra(COMPRESS_OPEN, mediaPickerRequestBean.openCompress)
            startActivity(intent)
        }
    }

    private fun postPickedListToPreviewActivity(mediaPickerBean: MediaPickerBean) {
        if (mediaPickerRequestBean.previewActivity != null) {
            val tempList = ArrayList<MediaPickerBean>()
            tempList.add(mediaPickerBean)
            val intent = Intent(this@MediaPickerActivity, mediaPickerRequestBean.previewActivity)
            intent.putExtra(RESULT_LIST, tempList)
            intent.putExtra(COMPRESS_OPEN, mediaPickerRequestBean.openCompress)
            startActivity(intent)
        }
    }

    var asyncTask: AsyncTask<*, *, *> = object : AsyncTask<Any?, Any?, Any?>() {
        override fun onPreExecute() {
            super.onPreExecute()
            if (null != waitingDialog && waitingDialog is Dialog) {
                (waitingDialog as Dialog).show()
            }
        }

        override fun doInBackground(objects: Array<Any?>): Any? {
            for (i in pickedList.indices) {
                if (pickedList[i].type == 1) {
                    val tempOutPath =
                        mediaPickerRequestBean.outputPath + "/" + System.currentTimeMillis() + ".jpg"
                    PicUtils.isSaveCompressPicture(
                        pickedList[i].mediaFilePath,
                        tempOutPath,
                        mediaPickerRequestBean.compressWidth,
                        mediaPickerRequestBean.compressHeight
                    )
                    pickedList[i].mediaFilePath = tempOutPath
                    pickedList[i].size = File(mediaPickerRequestBean.outputPath).length()
                    Logger.i("$TAG compressed file length=" + pickedList[i].size)
                }
            }
            return null
        }

        override fun onPostExecute(o: Any?) {
            super.onPostExecute(o)
            if (null != waitingDialog && waitingDialog is Dialog) {
                (waitingDialog as Dialog).dismiss()
            }
            postResult()
        }
    }

    private fun postResult() {
        val intent = Intent()
        intent.putExtra(RESULT_LIST, pickedList)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun setWaitingDialog(dialog: Dialog?) {
        waitingDialog = dialog
    }

    override fun onDestroy() {
        super.onDestroy()
        if (popwindow != null && popwindow!!.isShowing) {
            popwindow!!.dismiss()
        }
        if (finishActivityReceiver != null) {
            unregisterReceiver(finishActivityReceiver)
        }
    }

    private inner class FinshActivityReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == FINISH_MEDIA_PICKER_ACTIVITY) {
                finish()
            }
        }
    }

}
