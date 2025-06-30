package com.app.runaddictedtrack.utils

import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.runaddictedtrack.R
import com.app.runaddictedtrack.data.local.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ActivityAdapter(var cursor: Cursor) : RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    fun updateCursor(newCursor: Cursor) {
        cursor.close()
        cursor = newCursor
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        if (cursor.moveToPosition(position)) {
            holder.bind(cursor)
        }
    }

    override fun getItemCount(): Int = cursor.count

    inner class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvActivityDate: TextView = itemView.findViewById(R.id.tvActivityDate)
        private val tvActivityDuration: TextView = itemView.findViewById(R.id.tvActivityDuration)
        private val tvActivityDistance: TextView = itemView.findViewById(R.id.tvActivityDistance)
        private val tvActivitySteps: TextView = itemView.findViewById(R.id.tvActivitySteps)

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION && cursor.moveToPosition(adapterPosition)) {
                    try {
                        val activityId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ACTIVITY_ID))
                        onItemClickListener?.invoke(activityId)
                    } catch (e: IllegalArgumentException) {
                        // Column not found - handle gracefully
                        android.util.Log.e("ActivityAdapter", "Column ${DatabaseHelper.COLUMN_ACTIVITY_ID} not found", e)
                    }
                }
            }
        }

        fun bind(cursor: Cursor) {
            try {
                // Ottieni gli indici delle colonne con controllo di sicurezza
                val startIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ACTIVITY_START)
                val endIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ACTIVITY_END)
                val distanceIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ACTIVITY_DISTANCE)
                val stepsIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ACTIVITY_STEPS)

                // Leggi i valori
                val startTime = cursor.getString(startIndex)
                val endTime = cursor.getString(endIndex)
                val distance = cursor.getDouble(distanceIndex)
                val steps = cursor.getInt(stepsIndex)

                // Formatta la data (mostra solo giorno/mese/anno)
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                val startDate = inputFormat.parse(startTime)
                tvActivityDate.text = if (startDate != null) {
                    outputFormat.format(startDate)
                } else {
                    startTime
                }

                // Calcola e formatta la durata
                val startMillis = inputFormat.parse(startTime)?.time ?: 0
                val endMillis = inputFormat.parse(endTime)?.time ?: 0
                val durationMillis = endMillis - startMillis

                val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60

                tvActivityDuration.text = if (hours > 0) {
                    "%02d:%02d:%02d".format(hours, minutes, seconds)
                } else {
                    "%02d:%02d".format(minutes, seconds)
                }

                // Formatta distanza e passi
                tvActivityDistance.text = "%.2f km".format(distance)
                tvActivitySteps.text = steps.toString()

            } catch (e: IllegalArgumentException) {
                // Column not found - fallback values
                android.util.Log.e("ActivityAdapter", "Required column not found in cursor", e)
                tvActivityDate.text = "Data non disponibile"
                tvActivityDuration.text = "00:00"
                tvActivityDistance.text = "0.00 km"
                tvActivitySteps.text = "0"
            } catch (e: Exception) {
                // Other errors - fallback values
                android.util.Log.e("ActivityAdapter", "Error binding data", e)
                tvActivityDate.text = "Data non disponibile"
                tvActivityDuration.text = "00:00"
                tvActivityDistance.text = "0.00 km"
                tvActivitySteps.text = "0"
            }
        }
    }
}