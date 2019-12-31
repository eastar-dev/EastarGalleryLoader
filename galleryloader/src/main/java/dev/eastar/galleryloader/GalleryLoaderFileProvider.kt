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

import android.content.Context
import android.log.Log
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object GalleryLoaderFileProvider {
    private const val PROVIDER = ".galleryloader_provider"
    private const val GALLERYLOADER_FOLDER = "galleryloader/"

    @Throws(IOException::class)
    fun createTempUri(context: Context, prefix: String = "", suffix: String? = ".jpeg"): Uri {
        val folder = File(context.getExternalFilesDir(null), GALLERYLOADER_FOLDER).also {
            if (it.exists())
                it.mkdirs()
        }
        val authority = context.packageName + PROVIDER
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File.createTempFile("${prefix}_${timeStamp}_", suffix, folder)
        val uri = FileProvider.getUriForFile(context, authority, file)
        Log.e(uri, file)
        return uri
    }

    fun copyForResult(context: Context, source: Uri?): Uri {
        val result = createTempUri(context, "result", ".jpeg")
        source ?: return result
        context.contentResolver.openInputStream(source).use { input ->
            context.contentResolver.openOutputStream(result).use { output ->
                input?.copyTo(output!!, 4096)
            }
        }
        return result
    }

    fun copyForCrop(context: Context, source: Uri?): Uri {
        val result = createTempUri(context, "crop", ".jpeg")
        source ?: return result
        context.contentResolver.openInputStream(source).use { input ->
            context.contentResolver.openOutputStream(result).use { output ->
                input?.copyTo(output!!, 4096)
            }
        }
        return result
    }

    fun deleteTemp(context: Context) {
        val source = File(context.getExternalFilesDir(null), GALLERYLOADER_FOLDER).also {
            if (!it.exists())
                return
        }
        val target = File(context.getExternalFilesDir(null), UUID.randomUUID().toString())
        if (source.renameTo(target))
            deleteRecursive(target)
    }

    private fun deleteRecursive(maybeFolder: File) {
        maybeFolder.listFiles()?.forEach {
            if (maybeFolder.isDirectory)
                deleteRecursive(it)
            else
                it.delete()
        }
        maybeFolder.delete()
    }
}
