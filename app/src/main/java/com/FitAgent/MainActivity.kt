package com.FitAgent

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.adapters.HistoryAdapter
import com.FitAgent.database.AppDatabase
import com.FitAgent.utils.ProfileManager
import com.FitAgent.utils.UnitConverter
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvGreetingSub: TextView
    private lateinit var tvBmiValue: TextView
    private lateinit var tvBmiCategory: TextView
    private lateinit var tvHeight: TextView
    private lateinit var tvWeight: TextView
    private lateinit var tvWorkoutPreview: TextView
    private lateinit var tvStatWorkouts: TextView
    private lateinit var tvStatVolume: TextView
    private lateinit var tvStatVolumeLabel: TextView
    private lateinit var tvNoRecent: TextView
    private lateinit var btnStartWorkout: LinearLayout
    private lateinit var btnHistory: MaterialButton
    private lateinit var btnProgress: MaterialButton
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var recyclerRecent: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Redirect to profile setup if profile isn't complete
        if (!ProfileManager.isProfileComplete(this)) {
            val intent = Intent(this, ProfileSetupActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        bindViews()
        setupClickListeners()
    }

    // Reload everything when returning from other screens
    override fun onResume() {
        super.onResume()
        loadProfileData()
        loadStats()
        loadRecentWorkouts()
    }

    private fun bindViews() {
        tvGreeting = findViewById(R.id.tv_greeting)
        tvGreetingSub = findViewById(R.id.tv_greeting_sub)
        tvBmiValue = findViewById(R.id.tv_home_bmi_value)
        tvBmiCategory = findViewById(R.id.tv_home_bmi_category)
        tvHeight = findViewById(R.id.tv_home_height)
        tvWeight = findViewById(R.id.tv_home_weight)
        tvWorkoutPreview = findViewById(R.id.tv_workout_preview)
        tvStatWorkouts = findViewById(R.id.tv_stat_workouts)
        tvStatVolume = findViewById(R.id.tv_stat_volume)
        tvStatVolumeLabel = findViewById(R.id.tv_stat_volume_label)
        tvNoRecent = findViewById(R.id.tv_no_recent)
        btnStartWorkout = findViewById(R.id.btn_start_workout)
        btnHistory = findViewById(R.id.btn_view_history)
        btnProgress = findViewById(R.id.btn_view_progress)
        btnEditProfile = findViewById(R.id.btn_edit_profile)
        recyclerRecent = findViewById(R.id.recycler_recent_workouts)
        recyclerRecent.layoutManager = LinearLayoutManager(this)
    }

    private fun setupClickListeners() {
        btnStartWorkout.setOnClickListener {
            startWorkoutFromProfile()
        }

        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        btnProgress.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
        }

        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
    }

    private fun loadProfileData() {
        val profile = ProfileManager.loadProfile(this)

        // Greeting
        val timeGreeting = getTimeBasedGreeting()
        tvGreeting.text = "$timeGreeting, ${profile.name}! 👋"
        tvGreetingSub.text = "Ready to crush today?"

        // BMI
        val bmi = profile.calculateBMI()
        tvBmiValue.text = String.format("%.1f", bmi)
        tvBmiCategory.text = profile.getBMICategory()

        // Height in user's preferred unit
        tvHeight.text = UnitConverter.formatHeight(profile.heightCm, profile.preferredHeightUnit)

        // Weight in user's preferred unit
        tvWeight.text = UnitConverter.formatWeight(profile.weightKg, profile.preferredWeightUnit)

        // Workout preview subtitle on start button
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
        val db = AppDatabase.getDatabase(this)
        val weightUnit = ProfileManager.getWeightUnit(this)

        lifecycleScope.launch {
            val totalWorkouts = db.workoutDao().getTotalWorkouts()
            val totalVolumeKg = db.workoutDao().getTotalVolumeLifted() ?: 0.0

            tvStatWorkouts.text = totalWorkouts.toString()

            // Convert volume to user's preferred unit
            val displayVolume = if (weightUnit == "lbs") {
                UnitConverter.kgToLbs(totalVolumeKg)
            } else {
                totalVolumeKg
            }
            tvStatVolume.text = String.format("%.0f", displayVolume)
            tvStatVolumeLabel.text = "Total $weightUnit"
        }
    }

    private fun loadRecentWorkouts() {
        val db = AppDatabase.getDatabase(this)
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
                    val intent = Intent(this@MainActivity, SessionDetailActivity::class.java)
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
        val profile = ProfileManager.loadProfile(this)
        val intent = Intent(this, WorkoutPlanActivity::class.java)
        intent.putExtra("GOAL", profile.goal)
        intent.putExtra("EQUIPMENT", profile.equipment)
        intent.putExtra("DAYS", profile.daysPerWeek)
        startActivity(intent)
    }
}