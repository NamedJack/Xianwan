package com.sonny.xianwan.camera

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.TextureView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sonny.xianwan.R
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : AppCompatActivity() {

    private lateinit var cameraHelper: CameraHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        cameraHelper = CameraHelper(this)

        action.setOnClickListener { takePicture() }
        action.setOnLongClickListener { takeVideo() }
        setting.setOnClickListener { showSetting() }
    }


    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }


    override fun onResume() {
        super.onResume()
        cameraHelper.startBackThread()
        if (texture.isAvailable) {
            cameraHelper.openCamera()
        } else {
            texture.surfaceTextureListener = textureListener
        }
    }

    override fun onPause() {
        super.onPause()
        cameraHelper.releaseCamera()
    }


    private val textureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

        }

        override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {

        }

        override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
            cameraHelper.releaseCamera()
            return true
        }

        override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
            cameraHelper.initCamera(texture)
            cameraHelper.openCamera()
        }

    }


    private fun showSetting() {

    }

    private fun takeVideo(): Boolean {

        return false
    }

    private fun takePicture() {
        showToast("takePicture")
        cameraHelper.tackPicture()
    }
}