package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_completions")
data class ExerciseCompletionEntity(
    @PrimaryKey val itemKey: String,
    val phaseId: Int,
    val dayNumber: Int,
    val sectionIndex: Int,
    val exerciseIndex: Int,
    val isCompleted: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
