/*
 * Copyright 2019 copyright eastar Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package dev.eastar.galleryloader

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import java.util.*

/**
 *```kotlin
 *<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="18"/>
 *
 *GalleryLoader.builder(activity)
 *  .setCrop(true, 100, 100)
 *  .setSource(GalleryLoader.Source.CAMERA)
 *  .setOnGalleryLoadedListener(this::showToast)
 *  .setOnCancelListener { Log.toast(activity, "canceled") }
 *  .load()
 *
 * <style name="GalleryLoaderTheme" parent="Theme.AppCompat.Light.NoActionBar">
 *     <item name="android:windowIsTranslucent">true</item>
 *     <item name="android:windowBackground">@android:color/transparent</item>
 *     <item name="android:windowContentOverlay">@null</item>
 *     <item name="android:windowIsFloating">false</item>
 *     <item name="android:backgroundDimEnabled">false</item>
 *     <item name="android:windowNoTitle">true</item>
 *     <item name="windowNoTitle">true</item>
 *     <item name="windowActionBar">false</item>
 *     <item name="android:windowAnimationStyle">@null</item>
 * </style>
 *```
 */
class GalleryLoader : AppCompatActivity() {
    companion object {
        const val REQ_CAMERA = 4901
        const val REQ_GALLERY = 4902
        const val REQ_CROP = 4913

        const val EXTRA_CROP = "CROP"
        const val EXTRA_SOURCE = "SOURCE"
        const val EXTRA_SOURCE_GALLERY = "SOURCE_GALLERY"
        const val EXTRA_SOURCE_CAMERA = "SOURCE_CAMERA"
        const val EXTRA_CROP_WIDTH = "CROP_WIDTH"
        const val EXTRA_CROP_HEIGHT = "CROP_HEIGHT"
        @JvmStatic
        fun builder(context: Context): Builder {
            return Builder(context)
        }

        fun deleteTemps(context: Context) {
            GalleryLoaderFileProvider.deleteTemps(context)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        load()

    }

    private fun load() {
        when (intent?.getStringExtra(EXTRA_SOURCE)) {
            EXTRA_SOURCE_GALLERY -> startGallery()
            EXTRA_SOURCE_CAMERA -> startCamera()
            else -> {
                AlertDialog.Builder(this@GalleryLoader)
                        .setTitle("Select from?")
                        .setItems(R.array.camera_or_gallery) { _, position ->
                            intent?.putExtra(EXTRA_SOURCE, if (position == 0) EXTRA_SOURCE_CAMERA else EXTRA_SOURCE_GALLERY)
                            load()
                        }
                        .setOnCancelListener { finish() }
                        .show()
                        .setCanceledOnTouchOutside(false)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) overridePendingTransition(0, 0)
    }

    private fun startGallery() {
        Intent(Intent.ACTION_PICK).apply {
            setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.CONTENT_TYPE)
            resolveActivity(packageManager)
        }.also {
            startActivityForResult(it, REQ_GALLERY)
        }
    }

    private fun startCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE).apply {
            resolveActivity(packageManager)
            mTragetUri = GalleryLoaderFileProvider.createTempUri(this@GalleryLoader, "camera", ".jpg")
            putExtra(MediaStore.EXTRA_OUTPUT, mTragetUri)
        }.also {
            startActivityForResult(it, REQ_CAMERA)
        }
    }

    private lateinit var mTragetUri: Uri

    private fun startCrop(sourceUri: Uri, w: Int, h: Int) {
        val targetUri = GalleryLoaderFileProvider.createTempUri(this@GalleryLoader, "crop", ".jpg")
        val intent = Intent("com.android.camera.action.CROP").apply {
            setDataAndType(sourceUri, "image/*")
            putExtra("crop", "true")
            putExtra("aspectX", w)
            putExtra("aspectY", h)
            putExtra("outputX", w)
            putExtra("outputY", h)
            putExtra("scale", true)
            putExtra(MediaStore.EXTRA_OUTPUT, targetUri)
            putExtra("return-data", false)
            putExtra("outputFormat", Bitmap.CompressFormat.JPEG.name) //Bitmap 형태로 받기 위해 해당 작업 진행
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)//for google+
        }
        val cropIntents = packageManager.queryIntentActivities(intent, 0)
        cropIntents.forEach {
            runCatching {
                grantUriPermission(it.activityInfo.packageName, sourceUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                grantUriPermission(it.activityInfo.packageName, targetUri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        }
        if (cropIntents.isEmpty()) {
            fire(null)
            return
        }

        mTragetUri = targetUri
        startActivityForResult(intent, REQ_CROP)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            fire(null)
            return
        }
        val crop = intent?.getBooleanExtra(EXTRA_CROP, false) ?: false
        if (crop) {
            intent?.putExtra(EXTRA_CROP, false)
            val w = intent?.getIntExtra(EXTRA_CROP_WIDTH, resources.displayMetrics.widthPixels) ?: resources.displayMetrics.widthPixels
            val h = intent?.getIntExtra(EXTRA_CROP_HEIGHT, resources.displayMetrics.widthPixels) ?: resources.displayMetrics.widthPixels
            when (requestCode) {
                REQ_GALLERY -> startCrop(GalleryLoaderFileProvider.copyForCrop(this, data?.data), w, h)
                REQ_CAMERA -> startCrop(mTragetUri, w, h)
            }
            return
        }

        when (requestCode) {
            REQ_GALLERY -> fire(GalleryLoaderFileProvider.copyForResult(this, data?.data))
            REQ_CROP, REQ_CAMERA -> fire(GalleryLoaderFileProvider.copyForResult(this, mTragetUri))
        }
    }

    private fun fire(data: Uri?) {
        //Log.p(if (data == null) Log.WARN else Log.INFO, data ?: Uri.EMPTY)
        GalleryLoaderObserver.notifyObservers(data)
        //FileProviderHelper.deleteTempFolder(this)
        finish()
    }

    class Builder internal constructor(private val context: Context) {
        private var mOnGalleryLoadedListener: ((Uri) -> Unit)? = null
        private var mOnCancelListener: (() -> Unit)? = null
        private var mSource = ""
        private var isCrop = false
        private var width = 0
        private var height = 0

        fun setOnGalleryLoadedListener(onGalleryLoadedListener: ((Uri) -> Unit)?): Builder {
            mOnGalleryLoadedListener = onGalleryLoadedListener
            return this
        }

        fun setOnCancelListener(onCancelListener: (() -> Unit)?): Builder {
            mOnCancelListener = onCancelListener
            return this
        }

        @JvmOverloads
        fun setCrop(
                width: Int = context.resources.displayMetrics.widthPixels,
                height: Int = context.resources.displayMetrics.heightPixels,
                isCrop: Boolean = true): Builder {
            this@Builder.isCrop = isCrop
            this@Builder.width = width
            this@Builder.height = height
            return this
        }

        fun setSource(source: String): Builder {
            mSource = source
            return this
        }

        fun load() {
            GalleryLoaderObserver.onceUpdate(Observer { _, arg ->
                if (arg is Uri) mOnGalleryLoadedListener?.invoke(arg)
                else mOnCancelListener?.invoke()
            })

            Intent(context, GalleryLoader::class.java).apply {
                putExtra(EXTRA_CROP, isCrop && !android.os.Build.MODEL.contains("Android SDK"))
                putExtra(EXTRA_CROP_WIDTH, width)
                putExtra(EXTRA_CROP_HEIGHT, height)
                putExtra(EXTRA_SOURCE, mSource)
            }.also {
                context.startActivity(it)
            }
        }
    }
}