package com.FitAgent.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val goal: String,
    val equipment: String,
    val duration: String,
    val totalVolume: Double,
    val exerciseCount: Int
)