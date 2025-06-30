package com.app.runaddictedtrack

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Applica il tema salvato all'avvio dell'app
        val sharedPreferences = getSharedPreferences("APP_SETTINGS", MODE_PRIVATE)
        val darkModeValue = sharedPreferences.getInt("DARK_MODE", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(darkModeValue)
    }
}