package com.FitAgent.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_exercises",
    foreignKeys = [ForeignKey(
        entity = Routine::class,
        parentColumns = ["id"],
        childColumns = ["routineId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("routineId")]
)
data class RoutineExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: Long,
    val exerciseName: String,
    val muscleGroup: String,
    val orderIndex: Int,
    val defaultSets: Int,
    val defaultReps: Int,
    val defaultWeight: Float,
    val unit: String = "kg",
    val notes: String = ""
)
