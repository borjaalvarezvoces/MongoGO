package com.borja.mongogo

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class MapMarker(
    val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double
) {
    constructor() : this("", "", 0.0, 0.0)
}