package com.FitAgent.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseName: String,
    val muscleGroup: String,
    val sets: Int,
    val weight: Double,
    val reps: Int,
    val unit: String,
    val volume: Double
)
