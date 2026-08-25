package com.FitAgent

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.adapters.HistoryDetailAdapter
import com.FitAgent.database.AppDatabase
import kotlinx.coroutines.launch

class HistoryDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_detail)

        val sessionId = intent.getLongExtra("SESSION_ID", -1)
        val tvTitle = findViewById<TextView>(R.id.tv_detail_title)
        val recycler = findViewById<RecyclerView>(R.id.recycler_detail)

        recycler.layoutManager = LinearLayoutManager(this)

        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val session = db.workoutDao().getAllSessions().find { it.id == sessionId }
            val logs = if (sessionId != -1L) db.workoutDao().getLogsForSession(sessionId) else emptyList()

            session?.let { s ->
                val prefs = getSharedPreferences("fitagent_profile", Context.MODE_PRIVATE)
                val globalUnit = prefs.getString("unit", null) 
                    ?: prefs.getString("weight_unit", null) 
                    ?: prefs.getString("unit_preference", "kg") 
                    ?: "kg"

                var newVolume = 0.0
                for (log in logs) {
                    if (log.weight > 0) {
                        val convertedWeight = if (log.unit == "kg" && globalUnit == "lbs") {
                            log.weight * 2.20462
                        } else if (log.unit == "lbs" && globalUnit == "kg") {
                            log.weight / 2.20462
                        } else {
                            log.weight
                        }
                        newVolume += convertedWeight * log.reps
                    } else if (log.volume > 0) {
                        newVolume += log.volume
                    }
                }

                val formattedVolume = String.format("%.1f", newVolume)
                tvTitle.text = "Workout on ${s.date}\n${s.goal} • ${s.equipment} • ⏱ ${s.duration}\nTotal Volume: $formattedVolume $globalUnit"
            }

            recycler.adapter = HistoryDetailAdapter(logs)
        }
    }
}
