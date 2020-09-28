package com.sonny.xianwan

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import com.permissionx.guolindev.PermissionX
import com.sonny.xianwan.camera.CameraActivity
import com.sonny.xianwan.customview.CustomDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("lifecycle","onCreate")
        // Example of a call to a native method
//        sample_text.text = stringFromJNI()
        sample_text.setOnClickListener {
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_view, null, false)
            val dialog = CustomDialog.Builder(this)
                .View(view)
                .gravity(Gravity.CENTER)
                .build()
                .show()

//            val left = view.findViewById<Button>(R.id.leftBtn)
//            val center = view.findViewById<Button>(R.id.centerBtn)
//            val right = view.findViewById<Button>(R.id.rightBtn)

//            left.setOnClickListener { Toast.makeText(this, "left", Toast.LENGTH_LONG).show() }
//            center.setOnClickListener { Toast.makeText(this, "center", Toast.LENGTH_LONG).show() }
//            right.setOnClickListener { Toast.makeText(this, "right", Toast.LENGTH_LONG).show() }
        }


        openC.setOnClickListener {
            PermissionX.init(this).permissions(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    toCameraActivity()
                }else{

                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        Log.d("lifecycle","onStart")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("lifecycle","onRestart")
    }


    override fun onResume() {
        super.onResume()
        Log.d("lifecycle","onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("lifecycle","onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("lifecycle","onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("lifecycle","onDestroy")
    }


    private fun toCameraActivity(){
        startActivity(Intent(this, CameraActivity::class.java))
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
