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
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import com.squareup.picasso.Picasso
import java.text.DateFormat
import java.text.SimpleDateFormat


class SetWallpaperActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySetWallpaperBinding
    var list : ArrayList<Photo>? = null
    var uuid = UUID.randomUUID().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetWallpaperBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

         */
        supportActionBar?.hide()


        getUserDetails()


        var downloadUrl : String? = intent.getStringExtra("downloadUrl")
        binding.downloadButton.setOnClickListener {
            if (isWriteStoragePermissionGranted() && isReadStoragePermissionGranted()) {
                if (downloadUrl != null) {
                    download()
                }
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

    private fun getUserDetails() {
        val altDescription : String? = intent.getStringExtra("alt_description")

        if (altDescription.isNullOrEmpty()) {
            binding.details.text = "Resim detayı yok."
        } else {
            binding.details.text = altDescription
        }

        val createdAt : String? = intent.getStringExtra("created_at")

        val dateFormatInput = "yyyy-MM-dd'T'HH:mm:ss"
        val dateFormatOutput = "yyyy-MM-dd"


        val formatInput = SimpleDateFormat(dateFormatInput)
        val formatOutput = SimpleDateFormat(dateFormatOutput)

        val date = formatInput.parse(createdAt)
        val dateString = formatOutput.format(date)

        if (!createdAt.isNullOrEmpty()) {
            binding.createdAt.text = "Yüklenme tarihi: " + dateString
        }

        val userName = intent.getStringExtra("user_name")
        if (!userName.isNullOrEmpty()) {
            binding.userName.text = userName.toString()
        }

        val userImage = intent.getStringExtra("user_profile_image")
        val imageUri : Uri? = Uri.parse(userImage)
        if (!userImage.isNullOrEmpty()) {
            Picasso.get().load(imageUri).into(binding.imageUser)
            //binding.imageUser.setImageURI(imageUri)
        }

        val userBio = intent.getStringExtra("user_bio")

        if (!userBio.isNullOrEmpty()) {
            binding.userBio.text = "Bio: " + userBio
        } else {
            binding.userBio.text = "Bio: Boş"
        }

    }

    private suspend fun downloadImage(filename: String, downloadUrlOfImage: String) : kotlinx.coroutines.flow.Flow<Any> = flow {
        try {

            //binding.progressBar.visibility = View.VISIBLE
            //binding.textViewDownloadMessage.visibility = View.VISIBLE
            
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

            emit(dm.enqueue(request))
            //dm.enqueue(request)

            val cursor = dm.query(DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_PAUSED
                or DownloadManager.STATUS_PENDING
                    or DownloadManager.STATUS_RUNNING or DownloadManager.STATUS_SUCCESSFUL))

            if (cursor != null && cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

                cursor.close()

                when (status) {
                    DownloadManager.STATUS_RUNNING -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.textViewDownloadMessage.visibility = View.VISIBLE
                    }
                }

                println("status: " + status)

            }

            var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    binding.textViewDownloadMessage.visibility = View.INVISIBLE
                    binding.progressBar.visibility = View.INVISIBLE
                }
            }
            registerReceiver(onComplete, IntentFilter(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )

            //Toast.makeText(this@SetWallpaperActivity, "Başladı...", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this@SetWallpaperActivity, "Image download failed.", Toast.LENGTH_SHORT).show()
        }
        return@flow
    }

    fun download() = runBlocking {

        var downloadUrl : String? = intent.getStringExtra("downloadUrl")
        val flow = downloadImage(uuid, downloadUrl!!)
        binding.progressBar.visibility = View.VISIBLE
        binding.textViewDownloadMessage.visibility = View.VISIBLE
        //delay(2000)
        flow.collect { value ->
        }
    }

    fun isReadStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    3
                )
                false
            }
        } else {
            true
        }
    }

    fun isWriteStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    2
                )
                false
            }
        } else {
            true
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        var downloadUrl : String? = intent.getStringExtra("downloadUrl")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {

            2 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (downloadUrl != null) {
                        download()
                    }
                } else {

                }
            }
        }
    }



}