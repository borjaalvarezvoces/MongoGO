package com.borja.mongogo

import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_details.*
import java.io.Serializable
import java.text.DateFormat
import java.util.*



class DetailsActivity : AppCompatActivity(), Serializable {


    //prueba 4328794032843290
    private lateinit var markerTxtId: TextView
    private lateinit var dateToTxt: TextView

    val permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val PERMISSIONS_REQUEST_ACCESS_CAMERA_AND_WSTORAGE = 1001
    private val IMAGE_CAPTURE_CODE = 1002

    private val arrayImageViews: MutableList<Uri> = mutableListOf()
    var image_uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        dateForMarker()

        markerTxtId = findViewById(R.id.descriptionDetailTxt)
        val markerIdDetail = intent.getSerializableExtra("Id")
        markerTxtId.text = markerIdDetail.toString()

        var inflater = LayoutInflater.from(this)

        button_capture.setOnClickListener{
            takePhoto()
        }

    }
    private fun takePhoto(){
        if(hasNoPermissions()){
            requestPermission()
        } else {
            openCamera()
        }
    }
    private fun hasNoPermissions():Boolean{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        } else {
            return true
        }
    }
    private fun requestPermission(){
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_ACCESS_CAMERA_AND_WSTORAGE)
    }

    private fun dateForMarker(){
        val calendar = Calendar.getInstance()
        val currentDate = DateFormat.getDateInstance().format(calendar.getTime())
        dateToTxt = findViewById(R.id.dateDetailTxt)
        dateToTxt.text = currentDate
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture B")
        values.put(MediaStore.Images.Media.DESCRIPTION,"From the camera B")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)
        //Camera intent

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == PERMISSIONS_REQUEST_ACCESS_CAMERA_AND_WSTORAGE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openCamera();
            } else {
                Toast.makeText(this, "Permission was not granted", Toast.LENGTH_SHORT).show()
            }
        } else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //called when image was captured from camera intent
        if (resultCode == Activity.RESULT_OK){
            image_uri?.let { arrayImageViews.add(it) }
            displayArrayOfViews()
        }
    }

    private fun displayArrayOfViews() {
        for (i in 1..arrayImageViews.size) {
            val newView = ImageView(this)
            newView.setImageURI(image_uri)
            imageLayout.addView(newView)
        }
    }
}

