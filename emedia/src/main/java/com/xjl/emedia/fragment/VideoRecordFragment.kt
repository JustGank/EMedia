package com.xjl.emedia.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Rect
import android.hardware.camera2.CameraCharacteristics
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.provider.MediaStore
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Chronometer.OnChronometerTickListener
import android.widget.TextView
import android.widget.Toast
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.PendingRecording
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.xjl.emedia.R
import com.xjl.emedia.activity.VideoRecordActivity
import com.xjl.emedia.bean.MediaRecordRequestBean
import com.xjl.emedia.bean.RecordQuality
import com.xjl.emedia.databinding.FragmentVideoRecorderBinding
import com.xjl.emedia.impl.PreOnClickListener
import com.xjl.emedia.logger.Logger
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

/**
 * Created by 180933664 on 2019/6/12.
 */
class VideoRecordFragment : Fragment() {

    private val TAG = VideoRecordFragment::class.java.simpleName
    private val FOCUS_AREA_SIZE = 500

    private val RATIO_4_3_VALUE = 4.0 / 3.0
    private val RATIO_16_9_VALUE = 16.0 / 9.0

    private val ORIENTATIONS = SparseIntArray()

    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 90)
        ORIENTATIONS.append(Surface.ROTATION_90, 0)
        ORIENTATIONS.append(Surface.ROTATION_180, 270)
        ORIENTATIONS.append(Surface.ROTATION_270, 180)
    }


    private val displayManager by lazy { requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager }

    lateinit var cameraProvider: ProcessCameraProvider

    private var camera: Camera? = null

    private var preview: Preview? = null

    var videoCapture: VideoCapture<Recorder>? = null

    private var displayId = -1

    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA

    private var flashMode by Delegates.observable(ImageCapture.FLASH_MODE_OFF) { _, _, new ->
        binding.buttonFlash.setImageResource(
            when (new) {
                ImageCapture.FLASH_MODE_AUTO -> R.mipmap.emedia_flash_auto
                ImageCapture.FLASH_MODE_ON -> R.mipmap.emedia_flash_open
                else -> R.mipmap.emedia_flash_close
            }
        )
    }

    private var isTorchOn = false

    private var isRecording = false

    /**
     * 当屏幕方向转换时会进行回调，改变预览和录像的方向
     */
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit

        @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@VideoRecordFragment.displayId) {
                preview?.targetRotation = view.display.rotation
                videoCapture?.targetRotation = view.display.rotation
            }
        } ?: Unit
    }


    private var countUp: Long = 0

    lateinit var recordRequstBean: MediaRecordRequestBean
    private var preOnClickListener: PreOnClickListener? = null

    lateinit var binding: FragmentVideoRecorderBinding
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View? {
        if (!hasCamera(requireActivity())) {
            //这台设备没有发现摄像头
            recordRequstBean.videoRecordEntry?.let { entry ->
                Toast.makeText(requireActivity(), entry.dont_have_camera_error, Toast.LENGTH_SHORT)
                    .show()
            }
            requireActivity().setResult(VideoRecordActivity.RESULT_CODE_FOR_RECORD_VIDEO_FAILED)
            requireActivity().finish()
        }
        binding = FragmentVideoRecorderBinding.inflate(i)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recordRequstBean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().intent?.getParcelableExtra(
                MediaRecordRequestBean::class.java.simpleName,
                MediaRecordRequestBean::class.java
            )!!
        } else {
            requireActivity().intent?.getParcelableExtra(
                MediaRecordRequestBean::class.java.simpleName
            )!!
        }

        recordRequstBean.apply {
            if (!TextUtils.isEmpty(saveDirPath)) {
                Logger.i("$TAG onViewCreated saveDirPath=$saveDirPath")
                val file = File(saveDirPath!!)
                if (!file.exists()) {
                    file.mkdirs()
                }
            }

            preOnClickListenerClass?.getConstructor()?.let {
                preOnClickListener = it.newInstance() as PreOnClickListener
                Logger.i("$TAG onViewCreated preOnClickListenerClass is  ${preOnClickListener!!::class.java.name}")
            } ?: Logger.i("$TAG onViewCreated preOnClickListenerClass is null.")

        }



        binding.buttonCapture.let {
            ViewCompat.requestApplyInsets(it)
            ViewCompat.setOnApplyWindowInsetsListener(it) { view, windowInsets ->
                val layoutParams = it.layoutParams as ViewGroup.MarginLayoutParams
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    layoutParams.bottomMargin =
                        windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                } else {
                    layoutParams.marginEnd =
                        windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).right
                }
                it.layoutParams = layoutParams
                windowInsets
            }
            it.setOnClickListener { recordVideo() }
        }

        binding.buttonChangeCamera.setOnClickListener { toggleCamera() }

        binding.buttonFlash.let {
//            ViewCompat.requestApplyInsets(it)
//            ViewCompat.setOnApplyWindowInsetsListener(it) { view, windowInsets ->
//                val layoutParams = it.layoutParams as ViewGroup.MarginLayoutParams
//                layoutParams.topMargin =
//                    windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top
//                it.layoutParams = layoutParams
//                windowInsets
//            }
            it.setOnClickListener { switchFlash() }
        }

        binding.buttonQuality.setOnClickListener { if (recordRequstBean.recordQuality == RecordQuality.ALL) showQualityList() }

        displayManager.registerDisplayListener(displayListener, null)

        binding.apply {
            buttonFlash.visibility = if (recordRequstBean.showLight) View.VISIBLE else View.GONE
            buttonQuality.visibility = if (recordRequstBean.showRatio) View.VISIBLE else View.GONE
            binding.buttonQuality.isClickable = recordRequstBean.recordQuality == RecordQuality.ALL

            previewView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(p0: View) {
                    Logger.i("$TAG onViewAttachedToWindow")
                    displayManager.registerDisplayListener(displayListener, null)
                }

                override fun onViewDetachedFromWindow(p0: View) {
                    Logger.i("$TAG onViewDetachedFromWindow")
                    displayManager.unregisterDisplayListener(displayListener)
                }

            })


            previewView.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    try {
                        focusOnTouch(event.x, event.y)
                    } catch (e: Exception) {
                        Logger.i(
                            "$TAG ${
                                getString(
                                    R.string.fail_when_camera_try_autofocus,
                                    e.toString()
                                )
                            }"
                        )
                        //do nothing
                    }
                }
                true
            }
        }

        startCamera()

        autoFocus()
    }

    private val focus_delay_time = 1000
    private fun autoFocus() {
        val dm = resources.displayMetrics
        Handler().postDelayed({
            focusOnTouch(
                (dm.widthPixels / 2).toFloat(),
                (dm.heightPixels / 2).toFloat()
            )
        }, focus_delay_time.toLong())
    }

    // 计算旋转角度的方法
    private fun sensorToDeviceRotation(displayRotation: Int, sensorOrientation: Int): Int {
        // 根据设备当前的显示旋转和传感器方向计算所需旋转角度
        Log.i(
            TAG,
            "sensorToDeviceRotation displayRotation : $displayRotation, sensorOrientation : $sensorOrientation"
        )
        return when (displayRotation) {
            Surface.ROTATION_0 -> sensorOrientation
            Surface.ROTATION_90 -> (sensorOrientation + 90) % 360
            Surface.ROTATION_180 -> (sensorOrientation + 180) % 360
            Surface.ROTATION_270 -> (sensorOrientation + 270) % 360
            else -> sensorOrientation
        }
    }

    private fun focusOnTouch(x: Float, y: Float) {
        camera?.let {
        }
    }

    //检查设备是否有摄像头
    private fun hasCamera(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }


    //质量列表
    fun showQualityList() {
        if (!isRecording) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && binding.listOfQualities.visibility == View.GONE
            ) {
                binding.listOfQualities.visibility = View.VISIBLE
                binding.listOfQualities.animate().setDuration(200).alpha(0.95f)
                    .withEndAction { }
            } else {
                binding.listOfQualities.visibility = View.VISIBLE
            }
        }
    }


    //闪光灯
    fun switchFlash() {
        flashMode =
            if (flashMode == ImageCapture.FLASH_MODE_OFF) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
        binding.buttonFlash.setImageResource(
            if (flashMode == ImageCapture.FLASH_MODE_OFF)
                R.mipmap.emedia_flash_close else R.mipmap.emedia_flash_open
        )
        isTorchOn = flashMode == ImageCapture.FLASH_MODE_ON
        flashMode = if (isTorchOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
        camera?.cameraControl?.enableTorch(isTorchOn)
    }


    //选择摄像头
    fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            // The display information
            val metrics = DisplayMetrics().also { binding.previewView.display.getRealMetrics(it) }
            // The ratio for the output image and preview
            val aspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
            // The display rotation
            val rotation = binding.previewView.display.rotation

            val localCameraProvider = cameraProvider
                ?: throw IllegalStateException("Camera initialization failed.")

            // The Configuration of camera preview
            preview = Preview.Builder()
                .setTargetAspectRatio(aspectRatio) // set the camera aspect ratio
                .setTargetRotation(rotation) // set the camera rotation
                .build()

            val qualitySelector = if (recordRequstBean.recordQuality == RecordQuality.ALL) {
                val cameraInfo = localCameraProvider.availableCameraInfos.filter {
                    Camera2CameraInfo.from(it)
                        .getCameraCharacteristic(CameraCharacteristics.LENS_FACING) == if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
                        CameraSelector.LENS_FACING_BACK
                    } else {
                        CameraSelector.LENS_FACING_FRONT
                    }
                }

                val supportedQualities = QualitySelector.getSupportedQualities(cameraInfo[0])

                val adapter = StableArrayAdapter(
                    activity,
                    android.R.layout.simple_list_item_1, supportedQualities
                )
                binding.listOfQualities.adapter = adapter
                binding.listOfQualities.onItemClickListener =
                    AdapterView.OnItemClickListener { parent, view, position, id ->
                        val quality = parent.getItemAtPosition(position) as Quality
                        binding.apply {
                            buttonQuality.text = qualityToText(quality)
                            listOfQualities.visibility = View.GONE
                        }

                        val qualitySelector = QualitySelector.from(supportedQualities[position])
                        onQualiyChange(qualitySelector)
                    }
                QualitySelector.fromOrderedList(
                    listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD),
                    /**
                     * 返回一个回退策略，该策略会选择最接近且低于输入质量的质量级别。
                     * 如果这样做不能得到一个受支持的质量级别，则选择最接近且高于输入质量的质量级别。
                     * */
                    FallbackStrategy.lowerQualityOrHigherThan(Quality.UHD)
                )
            } else {
                val quality = if (recordRequstBean.recordQuality == RecordQuality.QUALITY_2160P) {
                    Quality.UHD
                } else if (recordRequstBean.recordQuality == RecordQuality.QUALITY_1080P) {
                    Quality.FHD
                } else if (recordRequstBean.recordQuality == RecordQuality.QUALITY_720P) {
                    Quality.HD
                } else {
                    Quality.SD
                }

                binding.apply {
                    buttonQuality.text = qualityToText(quality)
                }

                QualitySelector.from(quality)
            }
            onQualiyChange(qualitySelector)

        }, ContextCompat.getMainExecutor(requireContext()))
        //切换摄像头后自动关对焦
        autoFocus()
    }

    private fun onQualiyChange(qualitySelector: QualitySelector) {
        val localCameraProvider = cameraProvider
        val recorder = Recorder.Builder()
            .setExecutor(ContextCompat.getMainExecutor(requireContext()))
            .setQualitySelector(qualitySelector)
            .build()

        videoCapture = VideoCapture.withOutput(recorder)

        localCameraProvider.unbindAll() // unbind the use-cases before rebinding them

        // Bind all use cases to the camera with lifecycle
        camera = localCameraProvider.bindToLifecycle(
            viewLifecycleOwner, // current lifecycle owner
            lensFacing, // either front or back facing
            preview, // camera preview use case
            videoCapture, // video capture use case
        )

        // Attach the viewfinder's surface provider to preview use case
        preview?.setSurfaceProvider(binding.previewView.surfaceProvider)
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        Logger.i("$TAG aspectRatio previewRatio: $previewRatio")
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }


    var recording: Recording? = null

    @SuppressLint("MissingPermission")
    fun recordVideo() {
        if (recording != null) {
            if (countUp < recordRequstBean.recordMinTime) {
                Toast.makeText(
                    activity,
                    recordRequstBean.videoRecordEntry?.video_too_short,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                finishRecord(false)
            }
            return
        }

        val recorder = videoCapture?.output!!
        val name = "${System.currentTimeMillis()}.mp4"
        val pendingRecording: PendingRecording =
            if (TextUtils.isEmpty(recordRequstBean.saveDirPath)) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, name)
                }
                recorder.prepareRecording(
                    requireContext(), MediaStoreOutputOptions.Builder(
                        requireContext().contentResolver,
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    ).setContentValues(contentValues)
                        .build()
                )
            } else {
                recorder.prepareRecording(
                    requireContext(),
                    FileOutputOptions.Builder(File(recordRequstBean.saveDirPath, name)).build()
                )
            }

        recording = pendingRecording?.withAudioEnabled()
            ?.start(ContextCompat.getMainExecutor(requireContext())) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        //录制开启后隐藏 反转摄像头
                        binding.buttonChangeCamera.visibility = View.GONE
                        binding.buttonCapture.setImageResource(R.mipmap.player_stop)
                        startChronometer()
                    }

                    is VideoRecordEvent.Finalize -> {
                        if (!event.hasError()) {
                            Logger.i("$TAG, Video capture succeeded:${event.outputResults.outputUri}")
                            onFinishRecordValueable?.onFinish(event.outputResults.outputUri.path)
                        } else {
                            recording?.close()
                            recording = null
                            Logger.e("$TAG Video capture ends with error: ${event.error}")
                        }
                        stopChronometer()
                    }
                }
            }
        isRecording = !isRecording
    }


    //切换前置后置摄像头
    fun toggleCamera() {
        if (preOnClickListener != null) {
            if (preOnClickListener?.preOnClick(view, activity) == false) {
                return
            }
        } else {
            Logger.i("$TAG changeCamera switchCameraListener preOnClickListener is null ")
        }
        lensFacing = if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        if (!isRecording) {
            startCamera()
        }
    }


    fun finishRecord(isInterrupt: Boolean) {

        recording?.stop()

        //录制结束显示翻转摄像头
        binding.apply {
            buttonChangeCamera.visibility = View.VISIBLE
            buttonCapture.setImageResource(R.mipmap.player_record)
        }

        val msg = if (isInterrupt) recordRequstBean.videoRecordEntry?.video_interrupt
        else recordRequstBean.videoRecordEntry?.video_captured
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()


        isRecording = false


    }

    private var onFinishRecordValueable: OnFinishRecordValueable? = null
    fun setOnFinishRecordValueable(onFinishRecordValueable: OnFinishRecordValueable?) {
        this.onFinishRecordValueable = onFinishRecordValueable
    }

    interface OnFinishRecordValueable {
        fun onFinish(fileParh: String?)
    }

    private fun changeRequestedOrientation(orientation: Int) {
        requireActivity().requestedOrientation = orientation
    }


    //修改录像质量
    private fun qualityToText(quality: Quality): String {
        return when (quality) {
            Quality.HD -> "720p"
            Quality.FHD -> "1080p"
            Quality.UHD -> "2160p"
            else -> "480p"
        }
    }

    private inner class StableArrayAdapter(
        context: Context?, val textViewResourceId: Int,
        objects: List<Quality>
    ) : ArrayAdapter<Quality?>(context!!, textViewResourceId, objects) {

        var mIdMap = HashMap<Quality, Int>()

        init {
            for (i in objects.indices) {
                mIdMap[objects[i]] = i
            }
        }

        override fun getItemId(position: Int): Long {
            val item = getItem(position)
            return mIdMap[item]?.toLong() ?: 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = layoutInflater.inflate(textViewResourceId, parent, false)
            view.findViewById<TextView>(android.R.id.text1).apply {
                text = qualityToText(getItem(position)!!)
            }
            return view
        }

        override fun hasStableIds(): Boolean {
            return true
        }
    }


    //开启计时器
    private fun startChronometer() {
        binding.textChrono.visibility = View.VISIBLE
        val startTime = SystemClock.elapsedRealtime()
        binding.textChrono.onChronometerTickListener = OnChronometerTickListener {
            countUp = (SystemClock.elapsedRealtime() - startTime) / 1000
            if (countUp % 2 == 0L) {
                binding.chronoRecordingImage.visibility = View.VISIBLE
            } else {
                binding.chronoRecordingImage.visibility = View.INVISIBLE
            }
            val asText =
                String.format("%02d", countUp / 60) + ":" + String.format("%02d", countUp % 60)
            binding.textChrono.text = asText
            if (recordRequstBean.limitTime != 0 && countUp == recordRequstBean.limitTime.toLong()) {
                finishRecord(false)
            }
        }
        binding.textChrono.start()
    }

    //停止计时器
    private fun stopChronometer() {
        binding.textChrono.stop()
        binding.chronoRecordingImage.visibility = View.INVISIBLE
        binding.textChrono.visibility = View.INVISIBLE
    }

    fun onKeyDown() {
        if (isRecording) {
            recording?.stop()
            val mp4 = File("filePath")
            if (mp4.exists() && mp4.isFile) {
                mp4.delete()
            }
        }
        requireActivity().setResult(VideoRecordActivity.RESULT_CODE_FOR_RECORD_VIDEO_CANCEL)

        requireActivity().finish()
    }

    private fun calculateFocusArea(x: Float, y: Float): Rect {
        val left = clamp(
            java.lang.Float.valueOf(x / binding.previewView.getWidth() * 2000 - 1000).toInt(),
            FOCUS_AREA_SIZE
        )
        val top = clamp(
            java.lang.Float.valueOf(y / binding.previewView.getHeight() * 2000 - 1000).toInt(),
            FOCUS_AREA_SIZE
        )
        return Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE)
    }

    private fun clamp(touchCoordinateInCameraReper: Int, focusAreaSize: Int): Int {
        val result: Int
        result =
            if (abs(touchCoordinateInCameraReper.toDouble()) + focusAreaSize / 2 > 1000) {
                if (touchCoordinateInCameraReper > 0) {
                    1000 - focusAreaSize / 2
                } else {
                    -1000 + focusAreaSize / 2
                }
            } else {
                touchCoordinateInCameraReper - focusAreaSize / 2
            }
        return result
    }

}
