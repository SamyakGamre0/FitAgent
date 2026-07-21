package com.FitAgent

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.adapters.LoggedExercise
import com.FitAgent.adapters.LoggerAdapter
import com.FitAgent.database.AppDatabase
import com.FitAgent.models.ExerciseType
import com.FitAgent.models.WorkoutLog
import com.FitAgent.models.WorkoutSession
import com.FitAgent.utils.WorkoutGenerator
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LoggerActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private var seconds = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timerRunnable: Runnable
    private lateinit var loggedList: MutableList<LoggedExercise>

    private val SECONDS_KEY = "saved_seconds"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logger)

        tvTimer = findViewById(R.id.tv_timer)

        if (savedInstanceState != null) {
            seconds = savedInstanceState.getInt(SECONDS_KEY, 0)
        }

        val goal = intent.getStringExtra("GOAL") ?: "General"
        val equipment = intent.getStringExtra("EQUIPMENT") ?: "None"

        val exercises = WorkoutGenerator.generate(goal, equipment)
        loggedList = exercises.map { LoggedExercise(it, sets = it.sets) }.toMutableList()

        val recycler = findViewById<RecyclerView>(R.id.recycler_logger)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = LoggerAdapter(loggedList)

        startTimer()

        val btnSave = findViewById<MaterialButton>(R.id.btn_save_workout)
        btnSave.setOnClickListener {
            saveWorkoutToDatabase(goal, equipment)
        }

        // NEW: Listen for when the user presses the back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmation()
            }
        })
    }

    // NEW: Show a pop-up asking if they really want to quit
    private fun showExitConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Quit Workout? 🏃‍♂️")
            .setMessage("Your progress will be lost if you leave now. Are you sure?")
            .setPositiveButton("Keep Going 💪") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Quit") { _, _ ->
                handler.removeCallbacks(timerRunnable)
                finish()
            }
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SECONDS_KEY, seconds)
    }

    private fun saveWorkoutToDatabase(goal: String, equipment: String) {
        val validLogged = loggedList.filter { logged ->
            when (logged.exercise.type) {
                ExerciseType.WEIGHTED -> logged.weight > 0 && logged.reps > 0
                ExerciseType.BODYWEIGHT -> logged.reps > 0
                ExerciseType.TIMED -> logged.seconds > 0
            }
        }

        if (validLogged.isEmpty()) {
            Toast.makeText(this,
                "Please log at least one exercise",
                Toast.LENGTH_SHORT).show()
            return
        }

        val totalVolume = validLogged.sumOf { logged ->
            when (logged.exercise.type) {
                ExerciseType.WEIGHTED -> logged.weight * logged.reps * logged.sets
                ExerciseType.BODYWEIGHT -> (logged.reps * logged.sets).toDouble()
                ExerciseType.TIMED -> (logged.seconds * logged.sets).toDouble()
            }
        }

        val mins = seconds / 60
        val secs = seconds % 60
        val duration = String.format("%02d:%02d", mins, secs)
        val date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            .format(Date())

        val session = WorkoutSession(
            date = date,
            goal = goal,
            equipment = equipment,
            duration = duration,
            totalVolume = totalVolume,
            exerciseCount = validLogged.size
        )

        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            val sessionId = db.workoutDao().insertSession(session)

            validLogged.forEach { logged ->
                val (weight, reps, unit) = when (logged.exercise.type) {
                    ExerciseType.WEIGHTED -> Triple(logged.weight, logged.reps, logged.unit)
                    ExerciseType.BODYWEIGHT -> Triple(0.0, logged.reps, "reps")
                    ExerciseType.TIMED -> Triple(0.0, logged.seconds, "sec")
                }

                val volume = when (logged.exercise.type) {
                    ExerciseType.WEIGHTED -> logged.weight * logged.reps * logged.sets
                    ExerciseType.BODYWEIGHT -> (logged.reps * logged.sets).toDouble()
                    ExerciseType.TIMED -> (logged.seconds * logged.sets).toDouble()
                }

                val log = WorkoutLog(
                    sessionId = sessionId,
                    exerciseName = logged.exercise.name,
                    muscleGroup = logged.exercise.muscleGroup,
                    sets = logged.sets,
                    weight = weight,
                    reps = reps,
                    unit = unit,
                    volume = volume
                )
                db.workoutDao().insertLog(log)
            }

            Toast.makeText(this@LoggerActivity,
                "✅ Workout Saved!",
                Toast.LENGTH_LONG).show()

            handler.removeCallbacks(timerRunnable)
            finish()
        }
    }

    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                seconds++
                val mins = seconds / 60
                val secs = seconds % 60
                tvTimer.text = String.format("⏱ %02d:%02d", mins, secs)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerRunnable)
    }
}