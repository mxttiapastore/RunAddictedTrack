package com.app.runaddictedtrack.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.app.runaddictedtrack.R

class SettingsActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var switchNotifications: SwitchCompat
    private lateinit var switchDarkMode: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("APP_SETTINGS", MODE_PRIVATE)

        initializeViews()
        loadSavedSettings()
        setupSwitchListeners()
    }

    private fun initializeViews() {
        switchNotifications = findViewById(R.id.switchNotifications)
        switchDarkMode = findViewById(R.id.switchDarkMode)
    }

    private fun loadSavedSettings() {
        // Carica lo stato delle notifiche (default: true)
        switchNotifications.isChecked = sharedPreferences.getBoolean("NOTIFICATIONS", true)
        val darkModeValue = sharedPreferences.getInt("DARK_MODE", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        switchDarkMode.isChecked = darkModeValue == AppCompatDelegate.MODE_NIGHT_YES
    }

    private fun setupSwitchListeners() {
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            // Salva la preferenza
            sharedPreferences.edit().putBoolean("NOTIFICATIONS", isChecked).apply()
        }
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            sharedPreferences.edit().putInt("DARK_MODE", mode).apply()
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
}