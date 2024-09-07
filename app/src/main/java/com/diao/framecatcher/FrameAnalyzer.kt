package com.diao.framecatcher

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class FrameAnalyzer(private val context: Context) : ImageAnalysis.Analyzer {
    private var lastAnalyzedTimestamp = 0L

    override fun analyze(image: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >= TimeUnit.SECONDS.toMillis(2)) {
            Log.d(TAG, "Frame captured at $currentTimestamp")

            // 使用内置的 toBitmap() 方法
            val bitmap = image.toBitmap()

            // 保存 Bitmap 到相册
            saveImageToGallery(bitmap)

            lastAnalyzedTimestamp = currentTimestamp
        }

        image.close()
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val filename = "FrameCatcher_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/FrameCatcher")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            try {
                resolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                }
                Log.d(TAG, "Image saved successfully: $filename")
            } catch (e: IOException) {
                Log.e(TAG, "Error saving image", e)
            }
        }
    }

    companion object {
        private const val TAG = "FrameAnalyzer"
    }
}
