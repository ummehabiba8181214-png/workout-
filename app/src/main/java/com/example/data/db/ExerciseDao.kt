package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise_completions")
    fun getAllCompletions(): Flow<List<ExerciseCompletionEntity>>

    @Query("SELECT * FROM exercise_completions WHERE phaseId = :phaseId AND dayNumber = :dayNumber")
    fun getCompletionsForDay(phaseId: Int, dayNumber: Int): Flow<List<ExerciseCompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: ExerciseCompletionEntity)

    @Query("DELETE FROM exercise_completions WHERE itemKey = :itemKey")
    suspend fun deleteByKey(itemKey: String)

    @Query("DELETE FROM exercise_completions WHERE phaseId = :phaseId AND dayNumber = :dayNumber")
    suspend fun clearDay(phaseId: Int, dayNumber: Int)

    @Query("DELETE FROM exercise_completions")
    suspend fun clearAll()
}
