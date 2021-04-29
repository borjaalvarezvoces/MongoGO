package com.borja.mongogo

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_details.*
import java.io.Serializable
import java.text.DateFormat
import java.util.*


class DetailsActivity : AppCompatActivity(), Serializable {

    private lateinit var markerTxtId: TextView
    private lateinit var dateToTxt: TextView

    val permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val PERMISSIONS_REQUEST_ACCESS_CAMERA_AND_WSTORAGE = 1001
    private val IMAGE_CAPTURE_CODE = 1002


    val arrayImageViews: MutableList<Uri> = mutableListOf()
    var image_uri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        dateForMarker()

        markerTxtId = findViewById(R.id.descriptionDetailTxt)
        val markerIdDetail = intent.getSerializableExtra("Id")
        markerTxtId.text = markerIdDetail.toString()

        button_capture.setOnClickListener {
            takePhoto()
        }
    }

    private fun dateForMarker() {
        val calendar = Calendar.getInstance()
        val currentDate = DateFormat.getDateInstance().format(calendar.getTime())
        dateToTxt = findViewById(R.id.dateDetailTxt)
        dateToTxt.text = currentDate
    }

    private fun takePhoto() {
        if (hasNoPermissions()) {
            requestPermission()
        } else {
            openCamera()
        }
    }

    private fun hasNoPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
        } else {
            return true
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            permissions,
            PERMISSIONS_REQUEST_ACCESS_CAMERA_AND_WSTORAGE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_CAMERA_AND_WSTORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Permission was not granted", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture B")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the camera B")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //Camera intent

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //called when image was captured from camera intent
        if (resultCode == Activity.RESULT_OK) {
            image_uri?.let { arrayImageViews.add(it) }
            displayRecycleManager()
        }
    }

    private fun displayRecycleManager() {
        val listArrayImageView: MutableList<Movie> = mutableListOf()
        val recycler = findViewById<RecyclerView>(R.id.recycler_id)
        val layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recycler.layoutManager = layoutManager

        for (i in arrayImageViews.indices) {
            listArrayImageView.add(i, (Movie("Movie 2 ", arrayImageViews[i])))
        }
        recycler.adapter = MovieAdapter(listArrayImageView)
    }
}
