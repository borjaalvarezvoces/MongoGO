package com.borja.mongogo

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_details.*
import java.io.Serializable
import java.text.DateFormat
import java.util.*


class DetailsActivity : AppCompatActivity(), Serializable {

    private val db = FirebaseFirestore.getInstance()

    //aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
    private lateinit var markerTxtId: TextView
    private lateinit var markerTxtLtLng: TextView
    private lateinit var dateToTxt: TextView
    private lateinit var descriptionTxt: TextView


    val permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val PERMISSIONS_REQUEST_ACCESS_CAMERA_AND_WSTORAGE = 1001
    private val IMAGE_CAPTURE_CODE = 1002

    val arrayImageViews: MutableList<Uri> = mutableListOf()
    var image_uri: Uri? = null


    private var pointMarker = ""
    private var pruebaString = "abcd"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        dateForMarker()
        markerTxtLtLng = findViewById(R.id.markerTxtLtLng)
        markerTxtId = findViewById(R.id.streetDetailTxt)

        val markerLatDetail = intent.getSerializableExtra("Lat")
        val markerLngDetail = intent.getSerializableExtra("Lng")
        pointMarker = intent?.getStringExtra("Id").toString()

        markerTxtId.text = pointMarker
        markerTxtLtLng.text = markerLatDetail.toString() + markerLngDetail.toString()

        println("markereeeeeeeeeeee   3   $pointMarker")


        button_capture.setOnClickListener {
            takePhoto()
        }
        button_del_photos.setOnClickListener {
            delPhotosConfirmation()
        }
        buttonSave_id.setOnClickListener {
            guardarMarkerInfo()
        }

        updateMarkersDB()

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

            button_del_photos.visibility = View.VISIBLE
        }
    }

    private fun displayRecycleManager() {
        val listArrayImageView: MutableList<Photo> = mutableListOf()
        val recycler = findViewById<RecyclerView>(R.id.recycler_id)
        val layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recycler.layoutManager = layoutManager
        for (i in arrayImageViews.indices) {
            listArrayImageView.add(i, (Photo("Foto:${i + 1}", arrayImageViews[i])))
        }
        recycler.adapter = PhotoAdapter(listArrayImageView)

    }

    private fun delPhotosConfirmation() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Atención!")
        builder.setMessage("Estás seguro de querer borrar todas las fotos?")
        builder.setPositiveButton("Si") { dialogInterface: DialogInterface, i: Int ->
            delPhotos()
        }
        builder.setNegativeButton("No") { dialogInterface: DialogInterface, i: Int -> }
        builder.show()
    }

    private fun delPhotos() {
        arrayImageViews.clear()
        displayRecycleManager()
        button_del_photos.visibility = View.GONE
    }


    private fun guardarMarkerInfo() {
        pruebaString = descriptionDetailTxt_id.getText().toString()
        db.collection("markersGeo").document(pointMarker).set(
            hashMapOf("description" to pruebaString),
            SetOptions.merge()
        )

    }


    private fun updateMarkersDB() {
        descriptionTxt = findViewById(R.id.descriptionDetailTxt_id)
        Toast.makeText(this, "Boton pulsado", Toast.LENGTH_SHORT).show()
        db.collection("markersGeo").document(pointMarker).get().addOnSuccessListener { document ->
            if (document != null) {
                descriptionTxt.setText(document.get("description") as String)
            } else {
                Log.d("Fail", "No such document")
            }
        }
    }
}