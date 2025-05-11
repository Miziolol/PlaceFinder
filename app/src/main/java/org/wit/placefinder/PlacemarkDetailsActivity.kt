package org.wit.placefinder

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PlacemarkDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_placemark_details)

        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")

        val titleTextView = findViewById<TextView>(R.id.placemarkTitle)
        val descTextView = findViewById<TextView>(R.id.placemarkDescription)

        titleTextView.text = title
        descTextView.text = description
    }
}
