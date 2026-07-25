package com.example.data.repository

import com.example.data.db.ExerciseCompletionEntity
import com.example.data.db.ExerciseDao
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(private val dao: ExerciseDao) {
    val allCompletions: Flow<List<ExerciseCompletionEntity>> = dao.getAllCompletions()

    suspend fun toggleCompletion(
        itemKey: String,
        phaseId: Int,
        dayNumber: Int,
        sectionIndex: Int,
        exerciseIndex: Int,
        isCurrentlyCompleted: Boolean
    ) {
        if (isCurrentlyCompleted) {
            dao.deleteByKey(itemKey)
        } else {
            dao.insertOrUpdate(
                ExerciseCompletionEntity(
                    itemKey = itemKey,
                    phaseId = phaseId,
                    dayNumber = dayNumber,
                    sectionIndex = sectionIndex,
                    exerciseIndex = exerciseIndex,
                    isCompleted = true
                )
            )
        }
    }

    suspend fun clearDay(phaseId: Int, dayNumber: Int) {
        dao.clearDay(phaseId, dayNumber)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
