package com.FitAgent

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

            session?.let {
                tvTitle.text = "Workout on ${it.date}\n${it.goal} • ${it.equipment} • ⏱ ${it.duration}"
            }

            recycler.adapter = HistoryDetailAdapter(logs)
        }
    }
}
