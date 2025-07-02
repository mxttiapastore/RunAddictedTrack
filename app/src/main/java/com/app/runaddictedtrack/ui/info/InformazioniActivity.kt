package com.app.runaddictedtrack.ui.info

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.app.runaddictedtrack.R

class InformazioniActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var spinnerSex: Spinner
    private lateinit var etAge: EditText
    private lateinit var etMedications: EditText
    private lateinit var etHealthIssues: EditText
    private lateinit var etNotes: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informazioni)

        sharedPreferences = getSharedPreferences("PERSONAL_INFO", MODE_PRIVATE)

        spinnerSex = findViewById(R.id.spinnerSex)
        etAge = findViewById(R.id.etAge)
        etMedications = findViewById(R.id.etMedications)
        etHealthIssues = findViewById(R.id.etHealthIssues)
        etNotes = findViewById(R.id.etNotes)
        btnSave = findViewById(R.id.btnSaveInfo)

        ArrayAdapter.createFromResource(
            this,
            R.array.sex_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSex.adapter = adapter
        }

        loadSavedInfo()

        btnSave.setOnClickListener {
            saveInfo()
            Toast.makeText(this, R.string.info_salvate, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSavedInfo() {
        spinnerSex.setSelection(sharedPreferences.getInt("SEX_INDEX", 0))
        etAge.setText(sharedPreferences.getString("AGE", ""))
        etMedications.setText(sharedPreferences.getString("MEDICINE", ""))
        etHealthIssues.setText(sharedPreferences.getString("PROBLEMS", ""))
        etNotes.setText(sharedPreferences.getString("NOTES", ""))
    }

    private fun saveInfo() {
        sharedPreferences.edit()
            .putInt("SEX_INDEX", spinnerSex.selectedItemPosition)
            .putString("AGE", etAge.text.toString())
            .putString("MEDICINE", etMedications.text.toString())
            .putString("PROBLEMS", etHealthIssues.text.toString())
            .putString("NOTES", etNotes.text.toString())
            .apply()
    }
}
