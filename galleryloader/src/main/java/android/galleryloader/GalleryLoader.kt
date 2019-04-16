package android.galleryloader

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.log.Log
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import dev.eastar.galleryloader.R

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
        @JvmStatic
        fun builder(context: Context): Builder {
            return Builder(context)
        }
    }

    interface EXTRA {
        companion object {
            const val CROP = "CROP"
            const val SOURCE = "SOURCE"
            const val CROP_WIDTH = "CROP_WIDTH"
            const val CROP_HEIGHT = "CROP_HEIGHT"
        }
    }

    enum class Source {
        CAMERA, GALLERY, UNKNOWN
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        load()
    }

    private fun load() {
        val from = intent?.getSerializableExtra(EXTRA.SOURCE)
        when (from) {
            Source.GALLERY -> startGallery()
            Source.CAMERA -> startCamera()
            else -> {
                AlertDialog.Builder(this@GalleryLoader)
                        .setTitle("Select from?")
                        .setItems(R.array.camera_or_gallery) { _, position ->
                            intent?.putExtra(EXTRA.SOURCE, Source.values()[position])
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

    @Suppress("MemberVisibilityCanBePrivate")
    fun startGallery() {
        Intent(Intent.ACTION_PICK).also { galleryIntent ->
            galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.CONTENT_TYPE)
            galleryIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(galleryIntent, REQ_GALLERY)
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun startCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoURI: Uri = FileProviderHelper.createTempUri(this@GalleryLoader, "camera", ".jpg")
                mTragetUri = photoURI
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQ_CAMERA)
            }
        }
    }

    private var mTragetUri: Uri? = null
    @Suppress("MemberVisibilityCanBePrivate")
    fun startCrop(sourceUri: Uri, w: Int, h: Int) {
        val targetUri = FileProviderHelper.createTempUri(this@GalleryLoader, "crop", ".jpg")
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
        val list = packageManager.queryIntentActivities(intent, 0)
        for (resolveInfo in list) {
            try {
                grantUriPermission(resolveInfo.activityInfo.packageName, sourceUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                grantUriPermission(resolveInfo.activityInfo.packageName, targetUri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            } catch (e: Exception) {
                Log.w(resolveInfo)
            }
        }
        val size = list.size
        if (size == 0) {
            fire(null)
            return
        }
        mTragetUri = targetUri
        startActivityForResult(intent, REQ_CROP)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        Log.e(requestCode, resultCode, data)
        val result = if (resultCode == Activity.RESULT_OK) "RESULT_OK" else "RESULT_CANCELED"
        when (requestCode) {
            REQ_GALLERY -> Log.e(result, "REQ_GALLERY", data?.data)
            REQ_CROP -> Log.e(result, "REQ_CROP", mTragetUri)
            REQ_CAMERA -> Log.e(result, "REQ_CAMERA", mTragetUri)
        }


        if (resultCode != Activity.RESULT_OK) {
            fire(null)
            return
        }
        val crop = intent?.getBooleanExtra(EXTRA.CROP, false) ?: false
        if (crop) {
            intent?.putExtra(EXTRA.CROP, false)
            val w = intent?.getIntExtra(EXTRA.CROP_WIDTH, (100 * resources.displayMetrics.density).toInt())!!
            val h = intent?.getIntExtra(EXTRA.CROP_HEIGHT, (100 * resources.displayMetrics.density).toInt())!!
            when (requestCode) {
                REQ_GALLERY -> startCrop(FileProviderHelper.copyForCrop(this, data?.data), w, h)
                REQ_CAMERA -> mTragetUri?.also { sourceUri -> startCrop(sourceUri, w, h) }
            }
            return
        }

        when (requestCode) {
            REQ_GALLERY -> fire(data?.data)
            REQ_CROP, REQ_CAMERA -> fire(FileProviderHelper.moveForResult(this, mTragetUri))
        }
    }

    private fun fire(data: Uri?) {
        Log.p(if (data == null) Log.WARN else Log.INFO, data ?: Uri.EMPTY)
        GalleryLoaderObserver.notifyObservers(data)
        FileProviderHelper.deleteTempFolder(this)
        finish()
    }

    class Builder internal constructor(private val context: Context) {
        private var mOnGalleryLoadedListener: ((Uri) -> Unit)? = null
        private var mOnCancelListener: (() -> Unit)? = null
        private var mSource: Source = Source.UNKNOWN
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
                isCrop: Boolean,
                width: Int = context.resources.displayMetrics.widthPixels,
                height: Int = context.resources.displayMetrics.heightPixels
        ): Builder {
            this@Builder.isCrop = isCrop
            this@Builder.width = width
            this@Builder.height = height
            return this
        }

        fun setSource(source: Source): Builder {
            mSource = source
            return this
        }

        fun load() {
            GalleryLoaderObserver.onceUpdate(Observer { _, arg ->
                if (arg is Uri) mOnGalleryLoadedListener?.invoke(arg)
                else mOnCancelListener?.invoke()
            })

            Intent(context, GalleryLoader::class.java).apply {
                putExtra(EXTRA.CROP, isCrop && !android.os.Build.MODEL.contains("Android SDK"))
                putExtra(EXTRA.CROP_WIDTH, width)
                putExtra(EXTRA.CROP_HEIGHT, height)
                putExtra(EXTRA.SOURCE, mSource)
            }.also { context.startActivity(it) }
        }
    }
}

val Int.dp: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()