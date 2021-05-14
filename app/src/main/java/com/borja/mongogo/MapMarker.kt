package com.borja.mongogo

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import java.net.URI

data class MapMarker(
    val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val images: List<String>
) {
    constructor() : this(
        "",
        "",
        0.0,
        0.0,
        "",
        listOf<String>()
    )
}