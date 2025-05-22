package org.wit.placefinder
import org.wit.placefinder.MainActivity.Companion.places
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class PlacemarkDetailsActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var descTextView: TextView
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button

    private var title: String? = null
    private var description: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_placemark_details)

        titleTextView = findViewById(R.id.placemarkTitle)
        descTextView = findViewById(R.id.placemarkDescription)
        editButton = findViewById(R.id.editPlacemarkButton)
        deleteButton = findViewById(R.id.deletePlacemarkButton)

        title = intent.getStringExtra("title")
        description = intent.getStringExtra("description")

        titleTextView.text = title
        descTextView.text = description

        editButton.setOnClickListener {
            showEditDialog()
        }

        deleteButton.setOnClickListener {
            confirmDelete()
        }
    }

    private fun showEditDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_place, null)
        val titleInput = dialogView.findViewById<TextView>(R.id.placeTitleInput)
        val descInput = dialogView.findViewById<TextView>(R.id.placeDescInput)

        titleInput.text = title
        descInput.text = description

        AlertDialog.Builder(this)
            .setTitle("Edit Place")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newTitle = titleInput.text.toString()
                val newDesc = descInput.text.toString()


                MainActivity.places.find { it.title == title }?.apply {
                    title = newTitle
                    description = newDesc
                }

                Toast.makeText(this, "Place updated", Toast.LENGTH_SHORT).show()


                setResult(RESULT_OK)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete this place?")
            .setPositiveButton("Yes") { _, _ ->
                val iterator = MainActivity.places.iterator()
                while (iterator.hasNext()) {
                    val place = iterator.next()
                    if (place.title == title) {
                        iterator.remove()
                        Toast.makeText(this, "Place deleted", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                        return@setPositiveButton
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }


}
