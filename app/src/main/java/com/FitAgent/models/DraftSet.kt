package com.FitAgent.models

// In-memory only, not a Room entity. Used during routine creation.
data class DraftSet(
    var weight: Float = 0f,
    var reps: Int = 0
)
