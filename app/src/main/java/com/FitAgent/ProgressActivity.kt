package com.FitAgent

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.FitAgent.database.AppDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProgressActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        val tvStreak = findViewById<TextView>(R.id.tv_streak_count)
        val tvTotalWorkouts = findViewById<TextView>(R.id.tv_total_workouts)
        val tvTotalVolume = findViewById<TextView>(R.id.tv_total_volume)
        val tvWeekWorkouts = findViewById<TextView>(R.id.tv_week_workouts)
        val tvWeekVolume = findViewById<TextView>(R.id.tv_week_volume)
        val tvTopMuscle = findViewById<TextView>(R.id.tv_top_muscle)

        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            val sessions = db.workoutDao().getAllSessions()
            val totalWorkouts = sessions.size
            val totalVolume = sessions.sumOf { it.totalVolume }

            tvTotalWorkouts.text = totalWorkouts.toString()
            tvTotalVolume.text = totalVolume.toInt().toString()

            // Streak calculation
            val streak = calculateStreak(sessions.map { it.date })
            tvStreak.text = streak.toString()

            // This week stats
            val weekSessions = sessions.filter { isThisWeek(it.date) }
            tvWeekWorkouts.text = "Workouts: ${weekSessions.size}"
            tvWeekVolume.text = "Volume Lifted: ${weekSessions.sumOf { it.totalVolume }.toInt()}"

            // Most trained muscle
            val topMuscle = db.workoutDao().getMostTrainedMuscle()
            tvTopMuscle.text = topMuscle ?: "None yet"
        }
    }

    private fun calculateStreak(dates: List<String>): Int {
        if (dates.isEmpty()) return 0
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

        // FIXED: Convert to List first, then sort descending (newest first)
        val uniqueDays = dates.mapNotNull {
            try {
                val d = format.parse(it) ?: return@mapNotNull null
                val cal = Calendar.getInstance()
                cal.time = d
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            } catch (e: Exception) { null }
        }.toSet().toList().sortedDescending()

        if (uniqueDays.isEmpty()) return 0

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val yesterday = today - 86400000L

        if (uniqueDays.first() != today && uniqueDays.first() != yesterday) return 0

        var streak = 1
        var previous = uniqueDays.first()
        for (day in uniqueDays.drop(1)) {
            if (previous - day == 86400000L) {
                streak++
                previous = day
            } else break
        }
        return streak
    }

    private fun isThisWeek(dateString: String): Boolean {
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return try {
            val d = format.parse(dateString) ?: return false
            val cal = Calendar.getInstance()
            cal.time = d
            val weekOfYear = cal.get(Calendar.WEEK_OF_YEAR)
            val year = cal.get(Calendar.YEAR)

            val now = Calendar.getInstance()
            weekOfYear == now.get(Calendar.WEEK_OF_YEAR) && year == now.get(Calendar.YEAR)
        } catch (e: Exception) { false }
    }
}