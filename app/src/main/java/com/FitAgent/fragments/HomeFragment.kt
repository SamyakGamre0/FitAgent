package com.FitAgent.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.HistoryActivity
import com.FitAgent.R
import com.FitAgent.SessionDetailActivity
import com.FitAgent.WorkoutPlanActivity
import com.FitAgent.adapters.HistoryAdapter
import com.FitAgent.adapters.TopLiftAdapter
import com.FitAgent.database.AppDatabase
import com.FitAgent.utils.ProfileManager
import com.FitAgent.utils.UnitConverter
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import com.FitAgent.MainActivity

class HomeFragment : Fragment() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvGreetingSub: TextView
    private lateinit var tvWorkoutPreview: TextView
    private lateinit var tvStatWorkouts: TextView
    private lateinit var tvStatVolume: TextView
    private lateinit var tvStatVolumeLabel: TextView
    private lateinit var tvNoRecent: TextView
    private lateinit var tvNoLifts: TextView
    private lateinit var btnStartWorkout: LinearLayout
    private lateinit var btnFullHistory: MaterialButton
    private lateinit var recyclerRecent: RecyclerView
    private lateinit var recyclerTopLifts: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
        loadStats()
        loadTopLifts()
        loadRecentWorkouts()
    }

    private fun bindViews(view: View) {
        tvGreeting = view.findViewById(R.id.tv_greeting)
        tvGreetingSub = view.findViewById(R.id.tv_greeting_sub)
        tvWorkoutPreview = view.findViewById(R.id.tv_workout_preview)
        tvStatWorkouts = view.findViewById(R.id.tv_stat_workouts)
        tvStatVolume = view.findViewById(R.id.tv_stat_volume)
        tvStatVolumeLabel = view.findViewById(R.id.tv_stat_volume_label)
        tvNoRecent = view.findViewById(R.id.tv_no_recent)
        tvNoLifts = view.findViewById(R.id.tv_no_lifts)
        btnStartWorkout = view.findViewById(R.id.btn_start_workout)
        btnFullHistory = view.findViewById(R.id.btn_view_full_history)
        recyclerRecent = view.findViewById(R.id.recycler_recent_workouts)
        recyclerTopLifts = view.findViewById(R.id.recycler_top_lifts)
        recyclerRecent.layoutManager = LinearLayoutManager(requireContext())
        recyclerTopLifts.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupClickListeners() {
        btnStartWorkout.setOnClickListener {
            startWorkoutFromProfile()
        }
        btnFullHistory.setOnClickListener {
            startActivity(Intent(requireContext(), HistoryActivity::class.java))
        }
    }

    private fun loadProfileData() {
        val profile = ProfileManager.loadProfile(requireContext())

        val timeGreeting = getTimeBasedGreeting()
        tvGreeting.text = "$timeGreeting, ${profile.name}! 👋"
        tvGreetingSub.text = "Ready to crush today?"

        tvWorkoutPreview.text = "${profile.goal} • ${profile.equipment}"
    }

    private fun getTimeBasedGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Hi"
        }
    }

    private fun loadStats() {
        val db = AppDatabase.getDatabase(requireContext())
        val weightUnit = ProfileManager.getWeightUnit(requireContext())

        lifecycleScope.launch {
            val totalWorkouts = db.workoutDao().getTotalWorkouts()
            val totalVolumeKg = db.workoutDao().getTotalVolumeLifted() ?: 0.0

            tvStatWorkouts.text = totalWorkouts.toString()

            val displayVolume = if (weightUnit == "lbs") {
                UnitConverter.kgToLbs(totalVolumeKg)
            } else {
                totalVolumeKg
            }
            tvStatVolume.text = String.format("%.0f", displayVolume)
            tvStatVolumeLabel.text = "Total $weightUnit"
        }
    }

    private fun loadTopLifts() {
        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            val topLifts = db.workoutDao().getTopLifts()
            if (topLifts.isEmpty()) {
                tvNoLifts.visibility = View.VISIBLE
                recyclerTopLifts.visibility = View.GONE
            } else {
                tvNoLifts.visibility = View.GONE
                recyclerTopLifts.visibility = View.VISIBLE
                recyclerTopLifts.adapter = TopLiftAdapter(topLifts)
            }
        }
    }

    private fun loadRecentWorkouts() {
        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            val allSessions = db.workoutDao().getAllSessions()
            val recent = allSessions.take(3).toMutableList()

            if (recent.isEmpty()) {
                tvNoRecent.visibility = View.VISIBLE
                recyclerRecent.visibility = View.GONE
            } else {
                tvNoRecent.visibility = View.GONE
                recyclerRecent.visibility = View.VISIBLE
                recyclerRecent.adapter = HistoryAdapter(recent) { session ->
                    val intent = Intent(requireContext(), SessionDetailActivity::class.java)
                    intent.putExtra("SESSION_ID", session.id)
                    intent.putExtra("SESSION_DATE", session.date)
                    intent.putExtra("SESSION_GOAL", session.goal)
                    intent.putExtra("SESSION_EQUIPMENT", session.equipment)
                    intent.putExtra("SESSION_DURATION", session.duration)
                    intent.putExtra("SESSION_VOLUME", session.totalVolume)
                    startActivity(intent)
                }
            }
        }
    }

    private fun startWorkoutFromProfile() {
        (activity as? MainActivity)?.switchToWorkoutTab()
    }
}
