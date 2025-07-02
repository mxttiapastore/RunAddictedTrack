package com.app.runaddictedtrack.ui.info

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.runaddictedtrack.R

class InformazioniActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var etSex: EditText
    private lateinit var etAge: EditText
    private lateinit var etMedications: EditText
    private lateinit var etHealthIssues: EditText
    private lateinit var etNotes: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informazioni)

        sharedPreferences = getSharedPreferences("PERSONAL_INFO", MODE_PRIVATE)

        etSex = findViewById(R.id.etSex)
        etAge = findViewById(R.id.etAge)
        etMedications = findViewById(R.id.etMedications)
        etHealthIssues = findViewById(R.id.etHealthIssues)
        etNotes = findViewById(R.id.etNotes)
        btnSave = findViewById(R.id.btnSaveInfo)

        loadSavedInfo()

        btnSave.setOnClickListener {
            saveInfo()
            Toast.makeText(this, R.string.info_salvate, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSavedInfo() {
        etSex.setText(sharedPreferences.getString("SEX", ""))
        etAge.setText(sharedPreferences.getString("AGE", ""))
        etMedications.setText(sharedPreferences.getString("MEDICINE", ""))
        etHealthIssues.setText(sharedPreferences.getString("PROBLEMS", ""))
        etNotes.setText(sharedPreferences.getString("NOTES", ""))
    }

    private fun saveInfo() {
        sharedPreferences.edit()
            .putString("SEX", etSex.text.toString())
            .putString("AGE", etAge.text.toString())
            .putString("MEDICINE", etMedications.text.toString())
            .putString("PROBLEMS", etHealthIssues.text.toString())
            .putString("NOTES", etNotes.text.toString())
            .apply()
    }
}
