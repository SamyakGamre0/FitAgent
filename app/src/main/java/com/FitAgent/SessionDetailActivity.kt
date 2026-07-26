package com.FitAgent

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.adapters.LogDetailAdapter
import com.FitAgent.database.AppDatabase
import kotlinx.coroutines.launch

class SessionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_detail)

        val sessionId = intent.getLongExtra("SESSION_ID", -1L)
        val sessionDate = intent.getStringExtra("SESSION_DATE") ?: ""
        val sessionGoal = intent.getStringExtra("SESSION_GOAL") ?: ""
        val sessionEquipment = intent.getStringExtra("SESSION_EQUIPMENT") ?: ""
        val sessionDuration = intent.getStringExtra("SESSION_DURATION") ?: ""
        val sessionVolume = intent.getDoubleExtra("SESSION_VOLUME", 0.0)

        findViewById<TextView>(R.id.tv_detail_date).text = sessionDate
        findViewById<TextView>(R.id.tv_detail_goal).text = "$sessionGoal • $sessionEquipment"
        findViewById<TextView>(R.id.tv_detail_duration).text = "⏱ $sessionDuration"
        findViewById<TextView>(R.id.tv_detail_volume).text =
            "Total Volume: ${String.format("%.1f", sessionVolume)}"

        val recycler = findViewById<RecyclerView>(R.id.recycler_detail_logs)
        recycler.layoutManager = LinearLayoutManager(this)

        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val logs = db.workoutDao().getLogsForSession(sessionId)
            recycler.adapter = LogDetailAdapter(logs)
        }
    }
}