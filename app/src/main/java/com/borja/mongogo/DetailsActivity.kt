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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private lateinit var markerTxtAddress: TextView
    private lateinit var dateToTxt: TextView
    private lateinit var descriptionTxt: TextView


    var permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val PERMISSIONS_REQUEST_ACCESS_CAMERA_AND_WSTORAGE = 1001
    private val IMAGE_CAPTURE_CODE = 1002

    var listUriImageViews: MutableList<Uri> = mutableListOf()
    var listStringImageViewsFromDB: MutableList<String> = mutableListOf()
    var listStringImageViews: MutableList<String?> = mutableListOf()
    var listUriImageViewsFromDB: MutableList<Uri> = mutableListOf()
    var image_uri: Uri? = null


    private var markerID = ""
    private var setDescriptionDB = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        dateForMarker()
        markerTxtAddress = findViewById(R.id.addressDtailTxt_id)
        markerTxtId = findViewById(R.id.markerIdDetailTxt_id)

        val address = intent.getSerializableExtra("Address")
        markerID = intent?.getStringExtra("Id").toString()

        markerTxtId.text = markerID
        markerTxtAddress.text = address.toString()

        button_capture_id.setOnClickListener {
            takePhoto()
        }
        button_del_photos_id.setOnClickListener {
            delPhotosConfirmation()
        }
        buttonSave_id.setOnClickListener {
            saveMarkerInfoConfirmation()
        }
        fillMarkerInfoFromDB(markerID)
    }

    /*
        Obtiene la fecha actual y cubre dateDetailTxt_id con ella
     */
    private fun dateForMarker() {
        val calendar = Calendar.getInstance()
        val currentDate = DateFormat.getDateInstance().format(calendar.getTime())
        dateToTxt = findViewById(R.id.dateDetailTxt_id)
        dateToTxt.text = currentDate
    }

    /*
        Comprueba si el usuario ha aceptado permisos de uso de camara antes de abrirla, si no, los solicita
     */
    private fun takePhoto() {
        if (hasNoPermissions()) {
            requestPermission()
        } else {
            openCamera()
        }
    }

    /*
        Comprueba que la version del SDK de android es superior a "marshmallow" para solicitar UserPermisions en tiempo de ejecucion
        proporciona permisos de Camara y Write_External_Storage
        @return true
     */
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

    /*
        Comprueba con la variable que guarda los permisos si estos han sido garantizados
        @param requestCode: Int, permissions: Array<out String>, grantResults: IntArray
     */
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

    /*
        Abre la camara y genera un archivo "image_ui" que guardará en nuestro dispositivo la informacion de la foto que saquemos
        llama a startActivityForResult con el intent de la camara y la informacion proporcionada para guardar la foto
     */
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

    /*
        Una vez la fotografia es capturada y aceptada se guarda en un List "listUriImageViews" que posteriormente
        se mostrará en la ventana de detalle, en otro "listStringImageViews" que se utilizará para almacenar las imagenes en la DB
        @param requestCode: Int, resultCode: Int, data: Intent?
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //called when image was captured from camera intent
        if (resultCode == Activity.RESULT_OK) {
            image_uri?.let { listUriImageViews.add(it) }
            image_uri?.toString().let { listStringImageViews.add(it) }

            setMarkerImagesDB()
            displayRecycleManager()

            button_del_photos_id.visibility = View.VISIBLE
        }
    }

    /*
        Pasa al RecycleManager la informacion necesaria para que gestione el listado de fotos aceptadas por el usuario y a continuacion las muestre
        Crea por cada una de las Uris en el lisado una nueva "Photo" pasandole a esta clase un titulo "Foto: i" y una Uri concreta que extrae recursivamente del listado
        que el Adapter a continuacion utilizará como cover, para pintar el detalle completo de cada una de las imagenes.
        Establece las caracteristicas del LinearLayoutManager y pasa al PhotoAdapter del recycler el listado necesario para pintar las imagenes
     */
    private fun displayRecycleManager() {
        val listArrayImageView: MutableList<Photo> = mutableListOf()
        val recycler = findViewById<RecyclerView>(R.id.recycler_id)
        val layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recycler.layoutManager = layoutManager
        for (i in listUriImageViews.indices) {
            listArrayImageView.add(i, (Photo("Foto:${i + 1}", listUriImageViews[i])))
        }
        recycler.adapter = PhotoAdapter(listArrayImageView)
    }

    /*
        Crea y muestra un AlertDialog para obtener confirmacion o cancelacion por parte del usuario antes de borrar todas las imagenes que se muestran actualmente
     */
    private fun delPhotosConfirmation() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Atención!")
        builder.setMessage("Estás seguro de querer borrar todas las fotos")
        builder.setPositiveButton("Si") { dialogInterface: DialogInterface, i: Int ->
            delPhotos()
        }
        builder.setNegativeButton("No") { dialogInterface: DialogInterface, i: Int -> }
        builder.show()
    }

    /*
        Vacia de imagenes el listado para que el Recycler no tenga imagenes que mostrar,
        Oculta la visibilidad del boton para que este no se muestre cuando no hay fotos.
     */
    private fun delPhotos() {
        listUriImageViews.clear()
        displayRecycleManager()
        button_del_photos_id.visibility = View.GONE
    }

    /*
        Crea un AlertDialog para informar al usuario que la informacion indicada para este marcador será guardada si acepta la confirmacion
        de ser así cierra la ventana de Detalle y regresa al mapa.
     */
    private fun saveMarkerInfoConfirmation() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Guardar")
        builder.setMessage("Estás seguro de querer guardar este marcador?")
        builder.setPositiveButton("Si") { dialogInterface: DialogInterface, i: Int ->
            setMarkerDescriptionDB()
            finish()
        }
        builder.setNegativeButton("No") { dialogInterface: DialogInterface, i: Int -> }
        builder.show()
    }

    /*
        Instancia la base de datos para actualizar la informacion del marcador añadiendo el contenido de la descripcion
        que el usuario escribe en su correspondiente campo "descriptionDetailTxt_id"
     */
    private fun setMarkerDescriptionDB() {
        setDescriptionDB = descriptionDetailTxt_id.text.toString()
        db.collection("markersGeo").document(markerID).set(
            hashMapOf("description" to setDescriptionDB),
            SetOptions.merge()
        )
    }

    /*
        Instancia la base de datos para obtener para un determinado marcador, la informacion correspondiente a la descripcion de este
        así cómo el listado de Strings que corresponden a las Uris utilizadas para mostrar cada una de las imagenes
        A continuacion llama a displayRecycleManagerDB() para que pinte las imagenes provinientes de la DB al abrir el marcador.
        @param id del marcador correspondiente como un String
     */
    private fun fillMarkerInfoFromDB(id: String) {
        descriptionTxt = findViewById(R.id.descriptionDetailTxt_id)
        db.collection("markersGeo").document(id).get().addOnSuccessListener { document ->
            if (document != null) {
                descriptionTxt.text = document.get("description") as String
                listStringImageViewsFromDB = document.get("images") as MutableList<String>
                displayRecycleManagerDB()
            } else {
                Log.d("Fail", "No such document")
            }
        }
    }

    /*
        Instancia la base de datos para actualizar el contenido de las imagenes guardadas en ella.
     */
    private fun setMarkerImagesDB() {
        db.collection("markersGeo").document(markerID).set(
            hashMapOf("images" to listStringImageViews),
            SetOptions.merge()
        )
    }

    /*
        Funciona como el anterior RecyclerManager per en esta ocasion con el listado de imagenes provinientes de la DB
        con el fin de que estas se muestren automaticamente al abrir el detail de este marcador en concreto.
     */
    private fun displayRecycleManagerDB() {
        for (i in listStringImageViewsFromDB.indices) {
            val aux = Uri.parse(listStringImageViewsFromDB[i])
            listUriImageViewsFromDB.add(aux)
        }

        val listArrayImageView: MutableList<Photo> = mutableListOf()
        val recycler = findViewById<RecyclerView>(R.id.recycler_id)
        val layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recycler.layoutManager = layoutManager
        for (i in listUriImageViewsFromDB.indices) {
            listArrayImageView.add(i, (Photo("Foto:${i + 1}", listUriImageViewsFromDB[i])))
        }
        recycler.adapter = PhotoAdapter(listArrayImageView)
    }
}