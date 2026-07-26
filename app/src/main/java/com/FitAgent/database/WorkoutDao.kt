package com.FitAgent.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.FitAgent.models.WorkoutLog
import com.FitAgent.models.WorkoutSession

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

    @Query("SELECT muscleGroup FROM workout_logs GROUP BY muscleGroup ORDER BY COUNT(*) DESC LIMIT 1")
    suspend fun getMostTrainedMuscle(): String?

    @Delete
    suspend fun deleteSession(session: WorkoutSession)

    @Query("DELETE FROM workout_logs WHERE sessionId = :sessionId")
    suspend fun deleteLogsForSession(sessionId: Long)
}