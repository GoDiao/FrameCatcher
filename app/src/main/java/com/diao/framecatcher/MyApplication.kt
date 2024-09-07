package com.diao.framecatcher

import android.app.Application
import android.util.Log
import androidx.camera.lifecycle.ProcessCameraProvider

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            ProcessCameraProvider.getInstance(this)
        } catch (e: Exception) {
            Log.e("MyApplication", "Error initializing CameraX", e)
        }
    }
}
