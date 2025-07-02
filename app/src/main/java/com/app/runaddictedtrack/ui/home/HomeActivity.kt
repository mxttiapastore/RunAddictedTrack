package com.app.runaddictedtrack.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.runaddictedtrack.R
import com.app.runaddictedtrack.data.local.DatabaseHelper
import com.app.runaddictedtrack.ui.login.LoginActivity
import com.app.runaddictedtrack.ui.history.HistoryActivity
import com.app.runaddictedtrack.ui.settings.SettingsActivity
import com.app.runaddictedtrack.ui.info.InformazioniActivity
import com.app.runaddictedtrack.ui.tracking.TrackingActivity

class HomeActivity : AppCompatActivity() {

    private var userId: Int = -1
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        dbHelper = DatabaseHelper(this)

        // Recupera userId dall’Intent (che costruiremo in LoginActivity)
        userId = intent.getIntExtra("USER_ID", -1)
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USER,
            arrayOf(DatabaseHelper.COLUMN_USER_NAME),
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null)
        cursor.use{
            if (cursor.moveToFirst()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME))
                val tvWelcome = findViewById<TextView>(R.id.tvHomeWelcome)
                tvWelcome.text = getString(R.string.home_title, name)
            }
        }
        val tvWelcome = findViewById<TextView>(R.id.tvHomeWelcome)
        val btnStart = findViewById<Button>(R.id.btnStartTracking)
        val btnHistory = findViewById<Button>(R.id.btnViewHistory)
        val btnInformazioni = findViewById<Button>(R.id.btnInformazioni)
        val btnSettings = findViewById<Button>(R.id.btnSettings)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Mostra “Benvenuto, NomeUtente!” → in futuro: prendi il nome da dbHelper
        tvWelcome.text = getString(R.string.home_title, "Utente")

        btnStart.setOnClickListener {
            val intent = Intent(this, TrackingActivity::class.java)
            intent.putExtra("USER_ID", userId)
            Log.d("HomeActivity", "Passing USER_ID: $userId")
            startActivity(intent)
        }

        btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        btnInformazioni.setOnClickListener {
            val intent = Intent(this, InformazioniActivity::class.java)
            startActivity(intent)
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            // Torniamo a LoginActivity e chiudiamo Home
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}