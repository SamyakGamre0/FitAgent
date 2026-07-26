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

    // NEW: Get top exercises by frequency, with their max weight/reps/seconds
    // Cleans "(Set 1)", "(Set 2)" suffixes so all sets of same exercise group together
    @Query("""
        SELECT 
            TRIM(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(exerciseName,
                ' (Set 1)', ''), ' (Set 2)', ''), ' (Set 3)', ''), ' (Set 4)', ''), 
                ' (Set 5)', ''), ' (Set 6)', ''), ' (Set 7)', ''), ' (Set 8)', ''),
                ' (Set 9)', ''), ' (Set 10)', '')) as cleanName,
            muscleGroup,
            MAX(weight) as maxWeight,
            MAX(reps) as maxReps,
            unit,
            COUNT(*) as frequency
        FROM workout_logs
        GROUP BY cleanName
        ORDER BY frequency DESC
        LIMIT 3
    """)
    suspend fun getTopLifts(): List<TopLiftResult>
}

data class TopLiftResult(
    val cleanName: String,
    val muscleGroup: String,
    val maxWeight: Double,
    val maxReps: Int,
    val unit: String,
    val frequency: Int
)