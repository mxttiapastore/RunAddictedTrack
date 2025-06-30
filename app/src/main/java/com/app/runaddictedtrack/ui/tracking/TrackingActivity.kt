package com.app.runaddictedtrack.ui.tracking

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.Manifest
import com.app.runaddictedtrack.R
import com.app.runaddictedtrack.data.local.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.sqrt

class TrackingActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    private var userId: Int = -1
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager
    private var stepCounterSensor: Sensor? = null
    private var stepCount = 0
    private var lastAcceleration = 0f
    private val accelerationThreshold = 10f
    private val minTimeBetweenSteps = 300L
    private var lastStepTime = 0L
    private var lastLocation: Location? = null
    private var totalDistance = 0.0
    private lateinit var chronometer: Chronometer
    private lateinit var tvDistanceValue: TextView
    private lateinit var tvStepsValue: TextView
    private var isTracking = false
    private var startTime = 0L

    // Lista per tracciare il percorso
    private val routePoints = mutableListOf<Location>()

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
        private const val UPDATE_INTERVAL = 1000L
        private const val UPDATE_DISTANCE = 1f
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracciamento)

        dbHelper = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)
        Log.d("TrackingActivity", "Received USER_ID: $userId")

        initializeViews()
        setupServices()
        startTracking()
    }

    private fun initializeViews() {
        chronometer = findViewById(R.id.chronometer)
        tvDistanceValue = findViewById(R.id.tvDistanceValue)
        tvStepsValue = findViewById(R.id.tvStepsValue)

        val btnStop = findViewById<Button>(R.id.btnStop)
        btnStop.setOnClickListener {
            stopTracking()
        }
    }

    private fun setupServices() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @SuppressLint("MissingPermission")
    private fun startTracking() {
        if (isTracking) return

        // Reset valori
        stepCount = 0
        totalDistance = 0.0
        routePoints.clear()

        // Imposta i valori iniziali delle view
        tvDistanceValue.text = "0.00 km"
        tvStepsValue.text = "0"

        // Avvia il cronometro
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
        startTime = System.currentTimeMillis()
        isTracking = true

        // Setup sensori
        setupStepDetection()

        // Setup GPS
        if (checkLocationPermissions()) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                UPDATE_INTERVAL,
                UPDATE_DISTANCE,
                this
            )
        } else {
            requestLocationPermissions()
        }

        Toast.makeText(this, "Tracciamento avviato", Toast.LENGTH_SHORT).show()
    }

    private fun setupStepDetection() {
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (stepCounterSensor == null) {
            // Fallback all'accelerometro
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (stepCounterSensor != null) {
                Toast.makeText(this, "Usando accelerometro per rilevare i passi", Toast.LENGTH_SHORT).show()
            }
        }

        stepCounterSensor?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    private fun stopTracking() {
        if (!isTracking) return

        chronometer.stop()
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(this)
        isTracking = false

        // Calcola la durata effettiva dal cronometro
        val elapsedTime = SystemClock.elapsedRealtime() - chronometer.base
        val endTime = System.currentTimeMillis()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val coords = if (routePoints.isNotEmpty()) {
            routePoints.map { "{\"lat\": ${it.latitude}, \"lng\": ${it.longitude}}" }
                .joinToString(",", "[", "]")
        } else {
            ""
        }

        // Salva nel database usando la durata in millisecondi
        val activityId = dbHelper.insertActivity(
            userId,
            dateFormat.format(startTime),
            dateFormat.format(endTime),
            totalDistance / 1000, // Converti in km
            stepCount,
            coords
        )

        if (activityId != -1L) {
            Toast.makeText(this, "Attività salvata!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Errore nel salvataggio", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_STEP_DETECTOR -> {
                    stepCount++
                    runOnUiThread {
                        tvStepsValue.text = stepCount.toString()
                    }
                }

                Sensor.TYPE_ACCELEROMETER -> {
                    detectStepsFromAccelerometer(it)
                }
            }
        }
    }

    private fun detectStepsFromAccelerometer(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()

        val acceleration = sqrt(
            event.values[0] * event.values[0] +
                    event.values[1] * event.values[1] +
                    event.values[2] * event.values[2]
        )

        val delta = acceleration - lastAcceleration
        lastAcceleration = acceleration

        if (delta > accelerationThreshold &&
            (currentTime - lastStepTime) > minTimeBetweenSteps) {

            stepCount++
            lastStepTime = currentTime
            runOnUiThread {
                tvStepsValue.text = stepCount.toString()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onLocationChanged(location: Location) {
        // Aggiungi il punto al percorso
        routePoints.add(location)

        // Calcola la distanza
        lastLocation?.let { previousLocation ->
            val distance = previousLocation.distanceTo(location)
            totalDistance += distance

            runOnUiThread {
                tvDistanceValue.text = "%.2f km".format(totalDistance / 1000)
            }
        }

        lastLocation = location

        // Aggiorna la mappa se implementata
        updateMapWithNewLocation(location)
    }

    private fun updateMapWithNewLocation(location: Location) {
        // Qui implementerai l'aggiornamento della mappa
        // Per ora placeholder
        Log.d("TrackingActivity", "Nuova posizione: ${location.latitude}, ${location.longitude}")
    }

    override fun onProviderDisabled(provider: String) {
        Toast.makeText(this, "GPS disabilitato", Toast.LENGTH_SHORT).show()
    }

    override fun onProviderEnabled(provider: String) {
        Toast.makeText(this, "GPS abilitato", Toast.LENGTH_SHORT).show()
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    private fun checkLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTracking()
            } else {
                Toast.makeText(this, "Permessi GPS necessari", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Non fermare il tracciamento in pausa, continua in background
    }

    override fun onResume() {
        super.onResume()
        if (isTracking && stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isTracking) {
            sensorManager.unregisterListener(this)
            locationManager.removeUpdates(this)
        }
    }
}