package com.FitAgent.models

import com.FitAgent.data.MasterExercise

// In-memory only, not a Room entity. Used during routine creation.
data class DraftExercise(
    val masterExercise: MasterExercise,
    var notes: String = "",
    val sets: MutableList<DraftSet> = mutableListOf(DraftSet())
)
