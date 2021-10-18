package com.tknsoftwarestudio.wallpaperapi

import android.Manifest
import android.R.id
import android.app.DownloadManager
import android.app.ProgressDialog
import android.app.WallpaperManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.tknsoftwarestudio.wallpaperapi.databinding.ActivitySetWallpaperBinding
import com.tknsoftwarestudio.wallpaperapi.models.Photo
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import java.io.File
import java.lang.Exception
import java.net.URL
import java.util.*
import java.util.concurrent.Flow
import kotlin.collections.ArrayList
import android.R.id.text1
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class SetWallpaperActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySetWallpaperBinding
    var list : ArrayList<Photo>? = null

    var permissionLauncher: ActivityResultLauncher<String>? = null

    var intentActivityResultLauncher: ActivityResultLauncher<Intent>? = null

    var uuid = UUID.randomUUID().toString()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetWallpaperBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()

        var downloadUrl : String? = intent.getStringExtra("downloadUrl")
        binding.downloadButton.setOnClickListener {
            if (isWriteStoragePermissionGranted() == true && isReadStoragePermissionGranted() == true) {
                download()
            }


        }

        var wallpaperManager : WallpaperManager = WallpaperManager.getInstance(applicationContext)


        var url : String? = intent.getStringExtra("image")
        Glide.with(applicationContext).load(url).into(binding.fullImage)

        binding.setButton.setOnClickListener {
            val bitmap = (binding.fullImage.drawable as BitmapDrawable).bitmap
            wallpaperManager.setBitmap(bitmap)
            Snackbar.make(it, "Ayarlandı", Snackbar.LENGTH_SHORT).show()
        }


    }

    private fun downloadImage(filename: String, downloadUrlOfImage: String) : kotlinx.coroutines.flow.Flow<Any> = flow {
        try {

            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val downloadUri = Uri.parse(downloadUrlOfImage)
            val request = DownloadManager.Request(downloadUri)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(filename)
                .setMimeType("image/jpeg")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_PICTURES,
                    File.separator + filename + ".jpg"
                )

            //emit(dm.enqueue(request))
            val downloadId = dm.enqueue(request)

            println("downid: " + downloadId)
            val cursor = dm.query(DownloadManager.Query().setFilterById(downloadId))
            println("cursor: " + cursor)
            if (cursor != null && cursor.moveToNext()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                println("status: " + status)
                cursor.close();

                if (status == 1) {
                    Toast.makeText(this@SetWallpaperActivity, "İndirildi...", Toast.LENGTH_SHORT).show()

                }
            }

            //Toast.makeText(this@SetWallpaperActivity, "Başladı...", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this@SetWallpaperActivity, "Image download failed.", Toast.LENGTH_SHORT).show()
        }
        return@flow
    }
    fun download() = runBlocking {

        var downloadUrl : String? = intent.getStringExtra("downloadUrl")
        val flow = downloadImage(uuid, downloadUrl!!)
        Toast.makeText(this@SetWallpaperActivity, "İndiriliyor lütfen bekleyin...", Toast.LENGTH_SHORT).show()
        flow.collect { value ->

        }
    }

    fun isReadStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v(TAG, "Permission is granted1")
                true
            } else {
                Log.v(TAG, "Permission is revoked1")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    3
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted1")
            true
        }
    }

    fun isWriteStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v(TAG, "Permission is granted2")
                true
            } else {
                Log.v(TAG, "Permission is revoked2")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    2
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted2")
            true
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            2 -> {
                Log.d(TAG, "External storage2")
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0])
                    //resume tasks needing this permission
                    download()
                } else {

                }
            }
            3 -> {
                Log.d(TAG, "External storage1")
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0])
                    //resume tasks needing this permission
                    //SharePdfFile()
                } else {
                    //progress.dismiss()
                }
            }
        }
    }

}