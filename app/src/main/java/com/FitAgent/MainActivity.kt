package com.FitAgent

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.FitAgent.fragments.HomeFragment
import com.FitAgent.fragments.ProfileFragment
import com.FitAgent.fragments.ProgressFragment
import com.FitAgent.fragments.WorkoutFragment
import android.content.Context
import androidx.lifecycle.lifecycleScope
import com.FitAgent.database.AppDatabase
import kotlinx.coroutines.launch
import com.FitAgent.utils.ProfileManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

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

        bottomNav = findViewById(R.id.bottom_nav)

        // Load Home tab by default when app opens
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        lifecycleScope.launch {
            val prefs = getSharedPreferences("fitagent_profile", Context.MODE_PRIVATE)
            val migrated = prefs.getBoolean("prebuilt_routine_migrated", false)
            val routineDao = AppDatabase.getDatabase(this@MainActivity).routineDao()
            
            if (!migrated) {
                if (routineDao.countRoutines() == 0) {
                    val profile = ProfileManager.loadProfile(this@MainActivity)
                    val goal = profile.goal
                    val equipment = profile.equipment
                    val exercises = com.FitAgent.utils.WorkoutGenerator.generate(goal, equipment)
                    
                    val routineId = routineDao.insertRoutine(
                        com.FitAgent.models.Routine(
                            name = "My $goal Plan",
                            createdAt = System.currentTimeMillis(),
                            lastUsedAt = null,
                            isPrebuilt = true
                        )
                    )
                    
                    val rows = exercises.mapIndexed { index, ex ->
                        val defaultReps = ex.reps.split("-").firstOrNull()?.toIntOrNull() ?: 10
                        com.FitAgent.models.RoutineExercise(
                            routineId = routineId,
                            exerciseName = ex.name,
                            muscleGroup = ex.muscleGroup,
                            orderIndex = index,
                            defaultSets = ex.sets,
                            defaultReps = defaultReps,
                            defaultWeight = 0f,
                            unit = ProfileManager.getWeightUnit(this@MainActivity).ifEmpty { "kg" },
                            notes = ex.tip
                        )
                    }
                    routineDao.insertRoutineExercises(rows)
                }
                prefs.edit().putBoolean("prebuilt_routine_migrated", true).apply()
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_workout -> WorkoutFragment()
                R.id.nav_progress -> ProgressFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun switchToWorkoutTab() {
        bottomNav.selectedItemId = R.id.nav_workout
    }
}