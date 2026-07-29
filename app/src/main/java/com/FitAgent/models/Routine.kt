package com.FitAgent.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
    val lastUsedAt: Long? = null,
    val isPrebuilt: Boolean = false
)
