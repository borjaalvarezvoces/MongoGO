package com.borja.mongogo


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition

import java.util.*


class MapsActivity : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnInfoWindowLongClickListener {

    private var map: GoogleMap? = null

    private lateinit var pointMarker: Marker
    private val arrayOfMarkers = arrayListOf<Marker>()

    private var lastKnownLocation: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1000
    val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Build the map.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(map: GoogleMap) {
        this.map = map

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()

        setMapLongClick(map)
        deleteMarkerButton()
        // map.setOnInfoWindowClickListener(this)
        map.setOnInfoWindowLongClickListener(this)
        map.setOnMarkerClickListener(this)
    }

    private fun getDeviceLocation() {
        if (hasNoPermissions()) {
            requestPermission()
        } else {
            getUserLocation()
        }
    }

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
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // comentario de prueba
                getUserLocation()
            } else {
                Toast.makeText(this, "Permission was not granted", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.3f, Long: %2$.3f",
                latLng.latitude,
                latLng.longitude
            )
            pointMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.marker_title))
                    .draggable(true)
                    .snippet(snippet)
            )
            pointMarker.tag = 0
            map.animateCamera(CameraUpdateFactory.newLatLng(pointMarker.position))
            arrayOfMarkers.add(pointMarker)
            println(pointMarker.id)
            println(arrayOfMarkers)
        }
    }

    private fun deleteMarkerButton() {
        val btnDeleteMarker = findViewById<Button>(R.id.btn_delete_marker)
        btnDeleteMarker.setOnClickListener {
            println(arrayOfMarkers)
            arrayOfMarkers.removeAt(1).remove()
            println(arrayOfMarkers)
        }
    }

    override fun onInfoWindowLongClick(marker: Marker?) {
        println(marker?.id)
        println(pointMarker.id)

        val markerId = marker?.id
        val intentMarker = Intent(this, DetailsActivity::class.java)
        intentMarker.putExtra("Id", markerId)
        startActivity(intentMarker)
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        return false
    }
}



