package com.sonny.xianwan.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import com.sonny.xianwan.utils.CompareSizesByArea
import java.io.File
import java.io.FileOutputStream
import java.util.*


class CameraHelper constructor(activity: CameraActivity) {

    private val context = activity

    private var manager: CameraManager =
        activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private var cameraId = ""
    private var characteristics: CameraCharacteristics? = null
    private lateinit var cameraThread: HandlerThread
    private lateinit var cameraHandler: Handler
    private val defaultCameraFacing = CameraCharacteristics.LENS_FACING_BACK

    private var cameraDevice: CameraDevice? = null
    private lateinit var textureView: TextureView
    private var captureSession: CameraCaptureSession? = null

    private var imageReader: ImageReader? = null
    private lateinit var pictureSurface: Surface
    private var deviceAngel = 0
    private var displayRotation = 0

    //    private var saveImgSize: Size? = null
    private lateinit var previewRequestBuilder: CaptureRequest.Builder

    fun startBackThread() {

        cameraThread = HandlerThread("camera_thread").also { it.start() }
        cameraHandler = Handler(cameraThread.looper)
    }


    fun initCamera(texture: TextureView) {
        if (!cameraThread.isAlive) {
            throw RuntimeException("Please invoke startBackThread() before this")
        }
        textureView = texture

        for (id in manager.cameraIdList) {
            val cameraCharacteristics = manager.getCameraCharacteristics(id)
            if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == defaultCameraFacing) {
                cameraId = id
                characteristics = cameraCharacteristics
                val configMap =
                    cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

                val orientation =
                    cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
                deviceAngel = orientation!!
                Log.d("cameraTag", "orientation -> $orientation")

                val previewSizes = configMap?.getOutputSizes(SurfaceTexture::class.java)
                val previewLarge = Collections.max(
                    Arrays.asList(*previewSizes),
                    CompareSizesByArea()
                )
                val saveSizes = configMap?.getOutputSizes(ImageFormat.JPEG)
                val saveImgSize = Collections.max(
                    Arrays.asList(*saveSizes),
                    CompareSizesByArea()
                )

                imageReader = ImageReader.newInstance(
                    saveImgSize.width,
                    saveImgSize.height,
                    ImageFormat.JPEG,
                    2
                )
                imageReader?.setOnImageAvailableListener(
                    imageReaderAvailableListener,
                    cameraHandler
                )
                pictureSurface = imageReader!!.surface

//                texture.measuredWidth
//                texture.measuredHeight

                displayRotation = context.windowManager.defaultDisplay.orientation
                if (displayRotation == Surface.ROTATION_0 || displayRotation == Surface.ROTATION_180) {
                    textureView.surfaceTexture?.setDefaultBufferSize(
                        previewLarge.height, previewLarge.width
                    )
                } else {
                    textureView.surfaceTexture?.setDefaultBufferSize(
                        previewLarge.height, previewLarge.width
                    )
                }

                Log.d("cameraTag", "display -> ${displayRotation}")

            }
        }
    }

    @SuppressLint("MissingPermission")
    fun openCamera() {
        manager.openCamera(cameraId, deviceCallback, cameraHandler)
    }


    fun tackPicture() {
        val requestBuilder =
            cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        requestBuilder?.addTarget(pictureSurface)
        requestBuilder?.set(CaptureRequest.JPEG_ORIENTATION,getDiffAngel())
        val captureRequest = requestBuilder?.build()

        captureSession?.stopRepeating()
        captureSession?.capture(captureRequest!!, sessionCallback, cameraHandler)
    }

    private fun getDiffAngel(): Int {
        return (deviceAngel - displayRotation + 360) % 360
    }


    private val deviceCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(device: CameraDevice) {
            cameraDevice = device
            createCaptureSession(device)
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraDevice.close()
        }

        override fun onError(p0: CameraDevice, error: Int) {
            activity.showToast("相机打开失败,code -> ${error}")
        }

    }


    private fun createCaptureSession(device: CameraDevice) {
        previewRequestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        val surface = Surface(textureView.surfaceTexture)
        previewRequestBuilder.addTarget(surface)
        //自动对焦
        previewRequestBuilder.set(
            CaptureRequest.CONTROL_AF_MODE,
            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        )



        device.createCaptureSession(
            arrayListOf(surface, pictureSurface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession) {
//                    activity.showToast("开启预览失败")
                }

                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    session.setRepeatingRequest(
                        previewRequestBuilder.build(),
                        sessionCallback,
                        cameraHandler
                    )
                }
            },
            cameraHandler
        )
    }


    private val imageReaderAvailableListener = object : ImageReader.OnImageAvailableListener {
        override fun onImageAvailable(imageReader: ImageReader) {
            Log.d("cameraTag", "imageReaderAvailableListener")
            saveImage(imageReader)
        }
    }

    private fun saveImage(imageReader: ImageReader) {
        val appDir = File(getDCIM())
        if (appDir.exists()) {
            appDir.mkdirs()
        }
        val fileName = "pic${System.currentTimeMillis()}.jpg"

        val image = imageReader.acquireLatestImage()
        if (image == null) {
            return
        }
        val buffer = image.planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer[data]
        try {
            val file = File(appDir, fileName)
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(data)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("cameraTag", "error -> $e")
        } finally {
            image.close()
        }
    }

    //    private fun getDCIM(): String {
//        return Environment.getExternalStorageDirectory().absolutePath + "/DCIM"
//    }
    private fun getDCIM(): String? {
        if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
            return ""
        }
        var path =
            Environment.getExternalStorageDirectory().path + "/dcim/Camera/"
        if (File(path).exists()) {
            return path
        }
        path = Environment.getExternalStorageDirectory().path + "/DCIM/Camera/"
        val file = File(path)
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return ""
            }
        }
        return path
    }

    private val sessionCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
//            super.onCaptureCompleted(session, request, result)
            captureSession?.setRepeatingRequest(previewRequestBuilder.build(), null, cameraHandler)
        }
    }

    fun getCameraId(): String {
        return cameraId
    }


    fun releaseCamera() {
        cameraDevice?.close()
        captureSession?.close()
        cameraThread.quitSafely()
        cameraThread.join()
        captureSession = null
    }


//    fun getOutPutSize(cameraId: String, clz: Class<Any>): Array<out Size>? {
//        val cameraCharacteristics = manager.getCameraCharacteristics(cameraId)
//        val configMaps =
//            cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
//        return configMaps?.getOutputSizes(clz)
//    }
//
//
//    fun getOutPutSize(cameraId: String, format: Int): Array<out Size>? {
//        val cameraCharacteristics = manager.getCameraCharacteristics(cameraId)
//        val configMaps =
//            cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
//        return configMaps?.getOutputSizes(format)
//    }


}