package com.FitAgent.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.FitAgent.R
import com.FitAgent.database.AppDatabase
import com.FitAgent.utils.ProfileManager
import com.FitAgent.utils.UnitConverter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProgressFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.activity_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvStreak = view.findViewById<TextView>(R.id.tv_streak_count)
        val tvTotalWorkouts = view.findViewById<TextView>(R.id.tv_total_workouts)
        val tvTotalVolume = view.findViewById<TextView>(R.id.tv_total_volume)
        val tvWeekWorkouts = view.findViewById<TextView>(R.id.tv_week_workouts)
        val tvWeekVolume = view.findViewById<TextView>(R.id.tv_week_volume)
        val tvTopMuscle = view.findViewById<TextView>(R.id.tv_top_muscle)

        val db = AppDatabase.getDatabase(requireContext())
        val weightUnit = ProfileManager.getWeightUnit(requireContext())

        lifecycleScope.launch {
            val sessions = db.workoutDao().getAllSessions()
            val totalWorkouts = sessions.size
            val totalVolumeKg = sessions.sumOf { it.totalVolume }

            tvTotalWorkouts.text = totalWorkouts.toString()

            val displayVolume = if (weightUnit == "lbs") {
                UnitConverter.kgToLbs(totalVolumeKg)
            } else {
                totalVolumeKg
            }
            tvTotalVolume.text = displayVolume.toInt().toString()

            val streak = calculateStreak(sessions.map { it.date })
            tvStreak.text = streak.toString()

            val weekSessions = sessions.filter { isThisWeek(it.date) }
            tvWeekWorkouts.text = "Workouts: ${weekSessions.size}"

            val weekVolumeKg = weekSessions.sumOf { it.totalVolume }
            val weekDisplayVolume = if (weightUnit == "lbs") {
                UnitConverter.kgToLbs(weekVolumeKg)
            } else {
                weekVolumeKg
            }
            tvWeekVolume.text = "Volume Lifted: ${weekDisplayVolume.toInt()} $weightUnit"

            val topMuscle = db.workoutDao().getMostTrainedMuscle()
            tvTopMuscle.text = topMuscle ?: "None yet"
        }
    }

    private fun calculateStreak(dates: List<String>): Int {
        if (dates.isEmpty()) return 0
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

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
