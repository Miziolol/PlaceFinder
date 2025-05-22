package org.wit.placefinder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
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
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.content.Context
import android.widget.Button
import com.google.gson.Gson
import java.io.File

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var map: GoogleMap


    companion object {
        var places = mutableListOf(
            Place("SETU Gym", "Student gym facility", 52.251417, -7.179167),
            Place("SETU Library", "Quiet study space", 52.245332, -7.137908),
            Place("SETU Café", "Coffee and snacks", 52.245982, -7.139153),
            Place("IT Building", "Main computing labs", 52.245687, -7.137295),
            Place("Bus Station", "Pick up and drop off", 52.245048, -7.137783)
        )
    }
    private fun savePlaces() {
        val gson = Gson()
        val jsonString = gson.toJson(places)

        try {
            val file = File(filesDir, "places.json")
            file.writeText(jsonString)
            Toast.makeText(this, "Places saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving places", Toast.LENGTH_SHORT).show()
        }
    }
    private fun confirmSave() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Save")
            .setMessage("Are you sure you want to save the current data?")
            .setPositiveButton("Yes") { _, _ ->
                savePlaces()
                Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }
    private fun loadPlaces() {
        val gson = Gson()
        val file = File(filesDir, "places.json")

        if (file.exists()) {
            try {
                val jsonString = file.readText()
                val loadedPlaces = gson.fromJson(jsonString, Array<Place>::class.java).toList()

                places.clear()
                places.addAll(loadedPlaces)

                addAllMarkers()
                Toast.makeText(this, "Places loaded successfully", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error loading places", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun confirmLoad() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Load")
            .setMessage("Are you sure you want to load the saved data? This will overwrite current data.")
            .setPositiveButton("Yes") { _, _ ->
                loadPlaces()
                Toast.makeText(this, "Data loaded successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val saveButton = findViewById<Button>(R.id.saveButton)
        val loadButton = findViewById<Button>(R.id.loadButton)

        saveButton.setOnClickListener {
            confirmSave()
        }

        loadButton.setOnClickListener {
            confirmLoad()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            addAllMarkers()
        }
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

        map.setOnMarkerClickListener { marker ->
            val title = marker.title
            val description = marker.snippet

            val intent = Intent(this, PlacemarkDetailsActivity::class.java)
            intent.putExtra("title", title)
            intent.putExtra("description", description)
            startActivityForResult(intent, 1001)  // Request code 1001

            true
        }

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

        val addPlaceFab = findViewById<FloatingActionButton>(R.id.addPlaceFab)


        addPlaceFab.setOnClickListener {
            showAddPlaceDialog()
        }

    }
    private fun addMarker(place: Place) {
        val location = LatLng(place.lat, place.lng)
        map.addMarker(
            MarkerOptions()
                .position(location)
                .title(place.title)
                .snippet(place.description)
        )
    }
    private fun showAddPlaceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_place, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.placeTitleInput)
        val descInput = dialogView.findViewById<EditText>(R.id.placeDescInput)

        AlertDialog.Builder(this)
            .setTitle("Add New Place")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val title = titleInput.text.toString()
                val desc = descInput.text.toString()

                Toast.makeText(this, "Tap on the map to choose a location", Toast.LENGTH_LONG).show()

                map.setOnMapClickListener { latLng ->
                    val newPlace = Place(title, desc, latLng.latitude, latLng.longitude)
                    places.add(newPlace)
                    addMarker(newPlace)

                    map.setOnMapClickListener(null)
                }

            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addAllMarkers() {
        map.clear()

        for (place in places) {
            addMarker(place)
        }
    }

    private fun filterMarkers(query: String) {
        map.clear()

        for (place in places) {
            val isMatch = place.title.lowercase().contains(query.lowercase())

            if (isMatch || query.isEmpty()) {
                addMarker(place)
            }
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
    var title: String,
    var description: String,
    val lat: Double,
    val lng: Double
)
