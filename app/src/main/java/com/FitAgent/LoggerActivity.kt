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
import com.FitAgent.adapters.SetData
import com.FitAgent.database.AppDatabase
import com.FitAgent.models.Exercise
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
    private lateinit var adapter: LoggerAdapter

    private var routineId: Long = -1L
    private val SECONDS_KEY = "saved_seconds"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logger)

        tvTimer = findViewById(R.id.tv_timer)

        if (savedInstanceState != null) {
            seconds = savedInstanceState.getInt(SECONDS_KEY, 0)
        }

        routineId = intent.getLongExtra("ROUTINE_ID", -1L)
        val goal = intent.getStringExtra("GOAL") ?: "General"
        val equipment = intent.getStringExtra("EQUIPMENT") ?: "None"

        val recycler = findViewById<RecyclerView>(R.id.recycler_logger)
        recycler.layoutManager = LinearLayoutManager(this)

        val db = AppDatabase.getDatabase(this)

        if (routineId != -1L) {
            lifecycleScope.launch {
                val routineWithExercises = db.routineDao().getRoutineWithExercises(routineId)
                if (routineWithExercises != null && routineWithExercises.exercises.isNotEmpty()) {

                    val prefs = getSharedPreferences("fitagent_profile", MODE_PRIVATE)
                    val globalUnit = prefs.getString("unit", null)
                        ?: prefs.getString("weight_unit", null)
                        ?: prefs.getString("unit_preference", "kg")
                        ?: "kg"

                    loggedList = routineWithExercises.exercises.sortedBy { it.orderIndex }.map { re ->
                        val exType = when {
                            re.muscleGroup.equals("Cardio", ignoreCase = true) -> ExerciseType.TIMED
                            re.exerciseName.contains("Bodyweight", ignoreCase = true) -> ExerciseType.BODYWEIGHT
                            else -> ExerciseType.WEIGHTED
                        }

                        val exerciseModel = Exercise(
                            name = re.exerciseName,
                            muscleGroup = re.muscleGroup,
                            type = exType,
                            sets = re.defaultSets,
                            reps = re.defaultReps.toString(),
                            tip = ""
                        )

                        // Start with clean, EMPTY inputs (0.0 / 0) so user types fresh values
                        val initialSets = (0 until re.defaultSets).map {
                            SetData(
                                weight = 0.0,
                                reps = 0,
                                seconds = 0
                            )
                        }.toMutableList()

                        val selectedUnit = if (re.unit == "default" || re.unit.isEmpty()) globalUnit else re.unit

                        LoggedExercise(
                            exercise = exerciseModel,
                            sets = re.defaultSets,
                            unit = selectedUnit,
                            setDataList = initialSets
                        )
                    }.toMutableList()

                    adapter = LoggerAdapter(loggedList)
                    recycler.adapter = adapter
                    loadPreviousPerformance(db)
                } else {
                    setupDefaultGenerator(goal, equipment, recycler)
                }
            }
        } else {
            setupDefaultGenerator(goal, equipment, recycler)
        }

        startTimer()

        val btnSave = findViewById<MaterialButton>(R.id.btn_save_workout)
        btnSave.setOnClickListener {
            saveWorkoutToDatabase(goal, equipment)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmation()
            }
        })
    }

    private fun setupDefaultGenerator(goal: String, equipment: String, recycler: RecyclerView) {
        val exercises = WorkoutGenerator.generate(goal, equipment)
        loggedList = exercises.map { ex ->
            val initialSets = mutableListOf<SetData>()
            repeat(ex.sets) { initialSets.add(SetData()) }
            LoggedExercise(ex, sets = ex.sets, setDataList = initialSets)
        }.toMutableList()

        adapter = LoggerAdapter(loggedList)
        recycler.adapter = adapter

        val db = AppDatabase.getDatabase(this)
        loadPreviousPerformance(db)
    }

    private fun loadPreviousPerformance(db: AppDatabase) {
        lifecycleScope.launch {
            loggedList.forEach { logged ->
                val recentLogs = db.workoutDao().getRecentLogsForExercise("${logged.exercise.name}%")
                if (recentLogs.isNotEmpty()) {
                    adapter.setPreviousLogs(logged.exercise.name, recentLogs)
                }
            }
        }
    }

    private fun showExitConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Quit Workout? 🏃‍♂️")
            .setMessage("Your progress will be lost if you leave now.")
            .setPositiveButton("Keep Going 💪") { dialog, _ -> dialog.dismiss() }
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
            logged.setDataList.any { setData ->
                when (logged.exercise.type) {
                    ExerciseType.WEIGHTED -> setData.weight > 0 && setData.reps > 0
                    ExerciseType.BODYWEIGHT -> setData.reps > 0
                    ExerciseType.TIMED -> setData.seconds > 0
                }
            }
        }

        if (validLogged.isEmpty()) {
            Toast.makeText(this, "Please log at least one set", Toast.LENGTH_SHORT).show()
            return
        }

        var totalVolume = 0.0
        var totalExercises = 0

        val mins = seconds / 60
        val secs = seconds % 60
        val duration = String.format("%02d:%02d", mins, secs)
        val date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            val tempSession = WorkoutSession(
                date = date,
                goal = goal,
                equipment = equipment,
                duration = duration,
                totalVolume = 0.0,
                exerciseCount = 0,
                routineId = if (routineId != -1L) routineId else null
            )
            val sessionId = db.workoutDao().insertSession(tempSession)

            validLogged.forEach { logged ->
                val validSets = logged.setDataList.filter { sd ->
                    when (logged.exercise.type) {
                        ExerciseType.WEIGHTED -> sd.weight > 0 && sd.reps > 0
                        ExerciseType.BODYWEIGHT -> sd.reps > 0
                        ExerciseType.TIMED -> sd.seconds > 0
                    }
                }

                if (validSets.isNotEmpty()) {
                    totalExercises++
                    validSets.forEachIndexed { index, sd ->
                        val (weight, reps, unit) = when (logged.exercise.type) {
                            ExerciseType.WEIGHTED -> Triple(sd.weight, sd.reps, logged.unit)
                            ExerciseType.BODYWEIGHT -> Triple(0.0, sd.reps, "reps")
                            ExerciseType.TIMED -> Triple(0.0, sd.seconds, "sec")
                        }
                        val volume = when (logged.exercise.type) {
                            ExerciseType.WEIGHTED -> sd.weight * sd.reps
                            ExerciseType.BODYWEIGHT -> sd.reps.toDouble()
                            ExerciseType.TIMED -> sd.seconds.toDouble()
                        }
                        totalVolume += volume

                        val log = WorkoutLog(
                            sessionId = sessionId,
                            exerciseName = "${logged.exercise.name} (Set ${index + 1})",
                            muscleGroup = logged.exercise.muscleGroup,
                            sets = 1,
                            weight = weight,
                            reps = reps,
                            unit = unit,
                            volume = volume
                        )
                        db.workoutDao().insertLog(log)
                    }
                }
            }

            val updatedSession = tempSession.copy(
                id = sessionId,
                totalVolume = totalVolume,
                exerciseCount = totalExercises
            )
            db.workoutDao().deleteSession(tempSession.copy(id = sessionId))
            db.workoutDao().insertSession(updatedSession)

            // Update routine template without deleting unperformed exercises
            if (routineId != -1L) {
                val routineWith = db.routineDao().getRoutineWithExercises(routineId)
                if (routineWith != null) {
                    db.routineDao().deleteRoutineExercises(routineId)

                    // Keep ALL exercises in the routine (logged & unlogged)
                    val updatedRoutineExercises = loggedList.mapIndexed { index, logged ->
                        val firstValidSet = logged.setDataList.firstOrNull { sd -> sd.weight > 0 || sd.reps > 0 }

                        com.FitAgent.models.RoutineExercise(
                            routineId = routineId,
                            exerciseName = logged.exercise.name,
                            muscleGroup = logged.exercise.muscleGroup,
                            orderIndex = index,
                            defaultSets = logged.sets, // Preserves updated set count
                            defaultReps = firstValidSet?.reps ?: 0,
                            defaultWeight = (firstValidSet?.weight ?: 0.0).toFloat(),
                            unit = logged.unit,
                            notes = ""
                        )
                    }
                    db.routineDao().insertRoutineExercises(updatedRoutineExercises)
                    db.routineDao().updateLastUsed(routineId, System.currentTimeMillis())
                }
            }

            Toast.makeText(this@LoggerActivity, "✅ Workout Saved!", Toast.LENGTH_LONG).show()

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