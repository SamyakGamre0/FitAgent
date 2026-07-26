package com.FitAgent.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.FitAgent.models.WorkoutLog
import com.FitAgent.models.WorkoutSession

data class MuscleCount(
    val muscleGroup: String,
    val cnt: Int
)

@Dao
interface WorkoutDao {

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Insert
    suspend fun insertLog(log: WorkoutLog)

    @Query("SELECT * FROM workout_sessions ORDER BY id DESC")
    suspend fun getAllSessions(): List<WorkoutSession>

    @Query("SELECT * FROM workout_logs WHERE sessionId = :sessionId")
    suspend fun getLogsForSession(sessionId: Long): List<WorkoutLog>

    @Query("SELECT COUNT(*) FROM workout_sessions")
    suspend fun getTotalWorkouts(): Int

    @Query("SELECT SUM(totalVolume) FROM workout_sessions")
    suspend fun getTotalVolumeLifted(): Double?

    @Query("SELECT date FROM workout_sessions ORDER BY id DESC")
    suspend fun getAllDates(): List<String>

    @Query("SELECT muscleGroup, COUNT(*) as cnt FROM workout_logs GROUP BY muscleGroup ORDER BY cnt DESC LIMIT 1")
    suspend fun getMostTrainedMuscle(): MuscleCount?
}