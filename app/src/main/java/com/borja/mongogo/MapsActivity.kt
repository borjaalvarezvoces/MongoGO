package com.borja.mongogo


import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_maps.*
import java.util.*


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MapsActivity : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnInfoWindowLongClickListener {

    private var map: GoogleMap? = null

    //AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    private lateinit var pointMarker: Marker

    private var lastKnownLocation: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1000
    val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)


    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Build the map.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //  saveMarkerButton()
        updateMarkersDB()

    }

    override fun onRestart() {
        super.onRestart()
        //   updateMarkersDB()
    }

    /*
        Inicializa el mapa con sus listeners basicos y llama a getDeviceLocation para localizar al usuario
        @param: map, GoogleMap
     */
    override fun onMapReady(map: GoogleMap) {
        this.map = map
        getDeviceLocation()
        setMapLongClick(map)
        map.setOnInfoWindowLongClickListener(this)
        map.setOnMarkerClickListener(this)
    }

    /*
        coprueba si el usuario tiene permisos para utilizar su ubicacion
     */
    private fun getDeviceLocation() {
        if (hasNoPermissions()) {
            requestPermission()
        } else {
            getUserLocation()
        }
    }

    /*
        Comprueba si la version del SDK de android es superior a Marshmallow donde es necesario obtener los permisos de usuario en runTime
        en de ser superior obtiene el acceso a la ubicacion del usuario
     */
    private fun hasNoPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        } else {
            return true
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            permissions,
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
        )
    }

    /*
        Obtiene la ubicacion del usuario si este ha proporcionado el consentimiento para utilizar la ubicacion del dispositivo
        obtiene la latitud y longitud del usuario y centra la camara en el, representando su posicion en el mapa con un zoom determinado.
     */
    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
                lastKnownLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

            }
        }
    }

    /*
        Comprueba que existen permisos de usuario
        param: requestCode: Int, permissions: Array<out String>, grantResults: IntArray
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation()
            } else {
                Toast.makeText(this, "Permission was not granted", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /*
        Realizar un longClick en algun punto del mapa desplazará la cámara y creará un marker, que se representa en esa determinada latitud y longitud con un titulo determinado.
        A continuacion se obtiene las coordenadas del punto para ser posterior transformadas en su calle, numero, cp, provincia, pais y representadas en un snippet en lugar de su titulo
        Al mismo tiempo se crea una instancia de la basde de datos donde se guardará el nuevo marcador con unos determinados parametros requeridos para obtener informacion de dicho punto.
        Por ultimo se inicializa la funcion onInfoWindowLongClick() pasandole por argumento el nuevo marcador "pointMarker" para abrir la ventana de detalle de este y describir nuevas caracteristicas para el marcador
     */
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
/*            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.3f, Long: %2$.3f",
                latLng.latitude,
                latLng.longitude
            )*/
            pointMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.marker_title))
                //.snippet()
            )

            map.animateCamera(CameraUpdateFactory.newLatLng(pointMarker.position))
            val listaAdresses: MutableList<Address>?
            val geocoder = Geocoder(this, Locale.getDefault())
            listaAdresses = geocoder.getFromLocation(
                pointMarker.position.latitude,
                pointMarker.position.longitude,
                1
            )
            val address = listaAdresses?.get(0)?.getAddressLine(0)
            pointMarker.title = address
            pointMarker.tag = pointMarker.id

            db.collection("markersGeo").document(pointMarker.id).set(
                hashMapOf(
                    "id" to pointMarker.id,
                    "title" to address,
                    "latitude" to pointMarker.position.latitude,
                    "longitude" to pointMarker.position.longitude,
                    "description" to "",
                    "images" to listOf("")
                )
            )
            onInfoWindowLongClick(pointMarker)
        }
    }

    /*
        Crea una instancia de la base de datos para cargar los marcadores allí almacenados y mostrarlos en el mapa, añadiendo  en este un marcador por cada registro de la DB
        utilizando su posicion y titulo, ligando los nuevos marcadores a los creados inicialmente a traves de su propiedad "tag"
     */
    private fun updateMarkersDB() {
        db.collection("markersGeo").get().addOnSuccessListener {
            for (marker in it) {
                val markerPoint = marker.toObject(MapMarker::class.java)
                Log.i("nosgyutyurytte", markerPoint.toString())
                map?.addMarker(
                    MarkerOptions()
                        .title(markerPoint.title)
                        .position(LatLng(markerPoint.latitude, markerPoint.longitude))
                )!!.tag = markerPoint.id
            }
        }
    }

    /*
        Obtiene el id, tag y title del marcador en el cual hemos realizado una pulsacion larga y crea un intent para iniciar la actividad "DetailsActivity"
        proporcionando por parametro el ID y Address del marcador seleccionado para conectar la nueva actividad a este marcador unequivocamente
                @param: marker
     */
    override fun onInfoWindowLongClick(marker: Marker) {
        val markerId = marker.tag!!.toString()
        val markerAddress = marker.title

        val intentMarker = Intent(this, DetailsActivity::class.java)
        intentMarker.putExtra("Id", markerId)
        intentMarker.putExtra("Address", markerAddress)
        startActivity(intentMarker)
    }

    override fun onMarkerClick(marker: Marker?): Boolean {

/*        Log.i("lalalalalala 30001", marker.toString())
        val markerId = marker?.tag!!.toString()
        val markerAddress = marker.title
        Toast.makeText(this, "Marcador $markerId pulsado", Toast.LENGTH_SHORT).show()
        button_del_marker_id.visibility = View.VISIBLE
        deleteMarker(marker, markerId, markerAddress)*/

        return false

    }
/*    private fun deleteMarker(marker: Marker, markerId: String, markerTitle: String) {
        Log.i("lalalalalala 30002", marker.toString())
        button_del_marker_id.setOnClickListener {
            Log.i("lalalalalala 30002 botonpu", marker.toString())
            Toast.makeText(this, "Marker pulsado $markerId", Toast.LENGTH_SHORT).show()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Atención!")
            builder.setMessage("Estás seguro de querer borrar este marcador \n $markerTitle ??")
            builder.setPositiveButton("Si") { dialogInterface: DialogInterface, i: Int ->
                db.collection("markersGeo").document(markerId).delete()
                println("diosmiodemividaseñormio")
                marker.remove()
                updateMarkersDB()
            }
            builder.setNegativeButton("No") { dialogInterface: DialogInterface, i: Int -> }
            builder.show()
        }

    }*/

}



