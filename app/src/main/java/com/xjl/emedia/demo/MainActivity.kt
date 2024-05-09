package com.xjl.emedia.demo

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.xjl.emedia.bean.Constants
import com.xjl.emedia.bean.MediaPickerRequestBean
import com.xjl.emedia.bean.MediaRecordRequestBean
import com.xjl.emedia.bean.PickerType
import com.xjl.emedia.bean.RecordQuality
import com.xjl.emedia.contract.AlbumPickerContract
import com.xjl.emedia.contract.TakeFileContract
import com.xjl.emedia.contract.TakePhotoContract
import com.xjl.emedia.contract.TakeVideoContract
import com.xjl.emedia.demo.databinding.ActivityMainBinding
import com.xjl.emedia.impl.RecordPreOnClickListener
import com.xjl.emedia.logger.Logger
import com.xjl.emedia.utils.EMediaPermissionUtil
import com.xjl.emedia.utils.PicUtils.readPictureDegree
import java.io.File

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private var albumLauncher: ActivityResultLauncher<MediaPickerRequestBean>? = null
    private var photoLauncher: ActivityResultLauncher<String>? = null
    private var videoLauncher: ActivityResultLauncher<MediaRecordRequestBean>? = null
    private var fileLauncher: ActivityResultLauncher<String?>? = null

    lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val deniedPermissions = permissions.filterNot { it.value }
                Logger.i("$TAG onCreate deniedPermissions:$deniedPermissions")
                if (deniedPermissions.isNotEmpty()) {
                    permissionLauncher.launch(EMediaPermissionUtil.getAllNotGrantedPermissions(this@MainActivity))
                }
            }
        permissionLauncher.launch(EMediaPermissionUtil.getAllNotGrantedPermissions(this@MainActivity))

        photoLauncher = registerForActivityResult(TakePhotoContract()) {

            if (!TextUtils.isEmpty(it)) {
                val temp = File(it)
                Logger.i("$TAG Take image success,file path : ${temp.absolutePath}")
                readPictureDegree(temp.absolutePath)
            } else {
                Logger.w("$TAG Image file not exist!")
            }
        }

        albumLauncher = registerForActivityResult(AlbumPickerContract()) { mediaList ->
            mediaList?.forEach { it ->
                Logger.i("  albumLauncher result file path : ${it.mediaFilePath}")
            }
        }

        videoLauncher = registerForActivityResult(TakeVideoContract()) {
            if (!TextUtils.isEmpty(it)) {
                Logger.i("$TAG Take video success,file path : $it")
            } else {
                Logger.w("$TAG Video file not exist!")
            }
        }

        fileLauncher = registerForActivityResult(TakeFileContract()) {

            if (TextUtils.isEmpty(it)) {
                Toast.makeText(this, "无效的文件", Toast.LENGTH_SHORT).show()
            } else {
                val temp = File(it)
                if (temp.exists()) {
                    Logger.i("$TAG Take file success,file path : ${temp.absolutePath}")
                } else {
                    Logger.w("$TAG file not exist!")
                }
            }
        }

    }

    fun startAlbum(view: View) {
        albumLauncher?.launch(MediaPickerRequestBean().apply {
            max_chose_num = 3
            maxPhotoSize = 3 * 1024 * 1024
            maxVideoSize = 10 * 1024 * 1024
            pickerType = PickerType.PHOTO_VIDEO
            openBottomMoreOperate = true
            overSizeVisible = true
            previewActivity = PreviewActivity::class.java
            openSkipMemoryCache = true
            rowNum = 5
            screenOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
        })
    }

    fun takePhoto(view: View) {
        photoLauncher?.launch(Constants.getAppImageDir())
    }

    fun takeVideo(view: View) {
        videoLauncher?.launch(MediaRecordRequestBean().apply {
            recordMinTime = 3
            limitTime = 15
            recordQuality = RecordQuality.QUALITY_480P
            showLight = true
            showRatio = true
            preOnClickListenerClass = RecordPreOnClickListener::class.java
            saveDirPath = externalCacheDir?.absolutePath + File.separator + "Videos"
        })
    }

    fun takeFile(view: View) {
        fileLauncher?.launch("*/*")
    }

}
