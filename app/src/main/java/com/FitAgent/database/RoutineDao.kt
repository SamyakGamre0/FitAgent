package com.FitAgent.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.FitAgent.models.Routine
import com.FitAgent.models.RoutineExercise
import com.FitAgent.models.RoutineWithExercises

@Dao
interface RoutineDao {
    @Insert
    suspend fun insertRoutine(routine: Routine): Long

    @Insert
    suspend fun insertRoutineExercises(exercises: List<RoutineExercise>)

    @Update
    suspend fun updateRoutine(routine: Routine)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutine(id: Long)

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteRoutineExercises(routineId: Long)

    @Transaction
    @Query("SELECT * FROM routines ORDER BY lastUsedAt DESC, createdAt DESC")
    fun getAllRoutines(): kotlinx.coroutines.flow.Flow<List<RoutineWithExercises>>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :routineId")
    suspend fun getRoutineWithExercises(routineId: Long): RoutineWithExercises?

    @Query("UPDATE routines SET lastUsedAt = :timestamp WHERE id = :routineId")
    suspend fun updateLastUsed(routineId: Long, timestamp: Long)

    @Query("SELECT COUNT(*) FROM routines")
    suspend fun countRoutines(): Int

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    suspend fun getExercisesForRoutine(routineId: Long): List<RoutineExercise>
}
