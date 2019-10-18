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


package dev.eastar.galleryloader.demo

import android.content.Context
import android.content.Intent
import dev.eastar.galleryloader.GalleryLoader
import android.log.Log
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var activity: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        activity = this

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Copyright 2016 Eastar Jeong", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }


        GALLERY_LOADER_BY_CAMERA.setOnClickListener { GALLERY_LOADER_BY_CAMERA() }
        GALLERY_LOADER_BY_GALLERY.setOnClickListener { GALLERY_LOADER_BY_GALLERY() }
        GALLERY_LOADER_BY_CAMERA_CROP.setOnClickListener { GALLERY_LOADER_BY_CAMERA_CROP() }
        GALLERY_LOADER_BY_GALLERY_CROP.setOnClickListener { GALLERY_LOADER_BY_GALLERY_CROP() }
        GALLERY_LOADER_BY_SELECT_CROP.setOnClickListener { GALLERY_LOADER_BY_SELECT_CROP() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))).let { true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun GALLERY_LOADER_BY_CAMERA() {
        GalleryLoader.builder(activity)
//                .setCrop(true, 100, 100)
                .setSource(GalleryLoader.Source.CAMERA)
                .setOnGalleryLoadedListener(this::setImage)
                .setOnCancelListener { Log.toast(activity, "canceled") }
                .load()
    }

    fun GALLERY_LOADER_BY_GALLERY() {
        GalleryLoader.builder(activity)
//                .setCrop(true, 100, 100)
                .setSource(GalleryLoader.Source.GALLERY)
                .setOnGalleryLoadedListener(this::setImage)
                .setOnCancelListener { Log.toast(activity, "canceled") }
                .load()
    }

    fun GALLERY_LOADER_BY_CAMERA_CROP() {
        GalleryLoader.builder(activity)
                .setCrop(true, 100, 100)
                .setSource(GalleryLoader.Source.CAMERA)
                .setOnGalleryLoadedListener(this::setImage)
                .setOnCancelListener { Log.toast(activity, "canceled") }
                .load()
    }

    fun GALLERY_LOADER_BY_GALLERY_CROP() {
        GalleryLoader.builder(activity)
                .setCrop(true, 100, 100)
                .setSource(GalleryLoader.Source.GALLERY)
                .setOnGalleryLoadedListener(this::setImage)
                .setOnCancelListener { Log.toast(activity, "canceled") }
                .load()
    }

    fun GALLERY_LOADER_BY_SELECT_CROP() {
        GalleryLoader.builder(activity)
                .setCrop(true, 100, 100)
//                .setSource(GalleryLoader.eSource.GALLERY)
                .setOnGalleryLoadedListener(this::setImage)
                .setOnCancelListener { Log.toast(activity, "canceled") }
                .load()
    }

    private fun setImage(uri: Uri?) {
        Log.e(uri)
        Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show()
        image.setImageURI(uri)
    }

}
