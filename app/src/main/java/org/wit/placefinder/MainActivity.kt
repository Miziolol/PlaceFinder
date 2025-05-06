package org.wit.placefinder

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var map: GoogleMap


    private val places = listOf(
        Place("SETU Gym", "Student gym facility", 52.251417, -7.179167),
        Place("SETU Library", "Quiet study space", 52.245332, -7.137908),
        Place("SETU Café", "Coffee and snacks", 52.245982, -7.139153),
        Place("IT Building", "Main computing labs", 52.245687, -7.137295),
        Place("Bus Station", "Pick up and drop off", 52.245048, -7.137783)
    )

    private val markerList = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_HYBRID
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true

        val setuWaterford = LatLng(52.2450, -7.1396)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(setuWaterford, 15f))

        enableMyLocation()
        addAllMarkers()

        //https://developer.android.com/reference/android/text/TextWatcher
        val searchBar = findViewById<EditText>(R.id.searchBar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                filterMarkers(query)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun addAllMarkers() {
        for (place in places) {
            val location = LatLng(place.lat, place.lng)
            val marker = map.addMarker(
                MarkerOptions()
                    .position(location)
                    .title(place.title)
                    .snippet(place.description)
            )
            if (marker != null) {
                markerList.add(marker)
            }
        }
    }

    private fun filterMarkers(query: String) {
        for ((index, place) in places.withIndex()) {
            val marker = markerList[index]
            val isMatch = place.title.lowercase().contains(query)
            marker.isVisible = isMatch
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
            val setuWaterford = LatLng(52.2450, -7.1396)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(setuWaterford, 15f))
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                val setuWaterford = LatLng(52.2450, -7.1396)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(setuWaterford, 15f))
            }
        }
    }
}

data class Place(
    val title: String,
    val description: String,
    val lat: Double,
    val lng: Double
)
