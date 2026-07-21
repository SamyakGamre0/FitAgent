package com.FitAgent.models

enum class ExerciseType {
    WEIGHTED,     // needs weight + reps (bench press)
    BODYWEIGHT,   // needs only reps (pushups)
    TIMED         // needs only seconds (plank)
}

data class Exercise(
    val name: String,
    val sets: Int,
    val reps: String,
    val muscleGroup: String,
    val tip: String,
    val type: ExerciseType = ExerciseType.WEIGHTED
)
