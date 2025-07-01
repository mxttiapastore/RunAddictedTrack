package com.app.runaddictedtrack.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.app.runaddictedtrack.R

class SettingsActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var switchNotifications: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("APP_SETTINGS", MODE_PRIVATE)

        initializeViews()
        loadSavedSettings()
        setupSwitchListener()
    }

    private fun initializeViews() {
        switchNotifications = findViewById(R.id.switchNotifications)
    }

    private fun loadSavedSettings() {
        // Carica lo stato delle notifiche (default: true)
        switchNotifications.isChecked = sharedPreferences.getBoolean("NOTIFICATIONS", true)
    }

    private fun setupSwitchListener() {
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            // Salva la preferenza
            sharedPreferences.edit().putBoolean("NOTIFICATIONS", isChecked).apply()
        }
    }
}