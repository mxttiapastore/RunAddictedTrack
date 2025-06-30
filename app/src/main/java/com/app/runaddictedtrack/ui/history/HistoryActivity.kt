package com.app.runaddictedtrack.ui.history

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.runaddictedtrack.R
import com.app.runaddictedtrack.data.local.DatabaseHelper
import com.app.runaddictedtrack.ui.details.DetailActivity
import com.app.runaddictedtrack.utils.ActivityAdapter

class HistoryActivity : AppCompatActivity() {

    private var userId: Int = -1
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: ActivityAdapter
    private lateinit var tvNoActivities: TextView
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        dbHelper = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)

        recyclerView = findViewById(R.id.recyclerViewActivities)
        tvNoActivities = findViewById(R.id.tvNoActivities)

        recyclerView.layoutManager = LinearLayoutManager(this)

        loadActivities()
    }

    override fun onResume(){
        super.onResume()
        loadActivities()
    }

    private fun loadActivities(){
        val cursor = dbHelper.getActivitiesForUser(userId)
        Log.d("HistoryActivity", "Trovate ${cursor.count} attività")

        if(cursor.count == 0){
            tvNoActivities.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
        else{
            tvNoActivities.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            if(::adapter.isInitialized){
                adapter.updateCursor(cursor)
            } else{
                adapter = ActivityAdapter(cursor)
                recyclerView.adapter = adapter
                adapter.setOnItemClickListener { activityId ->
                    val intent = Intent(this, DetailActivity::class.java).apply {
                        putExtra("ACTIVITY_ID", activityId)
                    }
                    startActivity(intent)
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        adapter.cursor.close()
    }
}