package com.app.runaddictedtrack.ui.details

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.runaddictedtrack.R
import com.app.runaddictedtrack.data.local.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class DetailActivity : AppCompatActivity() {

    private var activityId: Int = -1
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        dbHelper = DatabaseHelper(this)
        activityId = intent.getIntExtra("ACTIVITY_ID", -1)

        val tvStart = findViewById<TextView>(R.id.tvDetailStart)
        val tvEnd = findViewById<TextView>(R.id.tvDetailEnd)
        val tvDuration = findViewById<TextView>(R.id.tvDetailDuration)
        val tvDistance = findViewById<TextView>(R.id.tvDetailDistance)
        val tvSteps = findViewById<TextView>(R.id.tvDetailSteps)

        if (activityId != -1) {
            dbHelper.getActivityById(activityId).use { cursor ->
                if (cursor.moveToFirst()) {
                    val startIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ACTIVITY_START)
                    val endIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ACTIVITY_END)
                    val distanceIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ACTIVITY_DISTANCE)
                    val stepsIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ACTIVITY_STEPS)

                    val startTime = cursor.getString(startIndex)
                    val endTime = cursor.getString(endIndex)
                    val distance = cursor.getDouble(distanceIndex)
                    val steps = cursor.getInt(stepsIndex)

                    // Formatta e mostra i dati
                    tvStart.text = startTime
                    tvEnd.text = endTime
                    tvDistance.text = "%.2f km".format(distance)
                    tvSteps.text = steps.toString()

                    // CORREZIONE: Calcolo durata corretto
                    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val startDate = format.parse(startTime)
                    val endDate = format.parse(endTime)
                    val durationMillis = endDate.time - startDate.time

                    val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60

                    tvDuration.text = "%02d:%02d:%02d".format(hours, minutes, seconds)
                }
            }
        }
    }
}