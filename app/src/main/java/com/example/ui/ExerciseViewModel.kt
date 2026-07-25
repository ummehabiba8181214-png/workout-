package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ExerciseData
import com.example.data.db.AppDatabase
import com.example.data.repository.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExerciseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExerciseRepository
    private val prefs = application.getSharedPreferences("exercise_tracker_prefs", Context.MODE_PRIVATE)

    // Default Day 1 = July 27, 2026
    private val defaultStartMillis: Long by lazy {
        Calendar.getInstance().apply {
            set(2026, Calendar.JULY, 27, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private val _startDateMillis = MutableStateFlow(
        prefs.getLong("start_date_millis", defaultStartMillis)
    )
    val startDateMillis: StateFlow<Long> = _startDateMillis.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).exerciseDao()
        repository = ExerciseRepository(dao)
    }

    private val _activePhaseId = MutableStateFlow(1)
    val activePhaseId: StateFlow<Int> = _activePhaseId.asStateFlow()

    private val _activeDay = MutableStateFlow(1)
    val activeDay: StateFlow<Int> = _activeDay.asStateFlow()

    fun setStartDate(year: Int, month: Int, dayOfMonth: Int) {
        val cal = Calendar.getInstance().apply {
            set(year, month, dayOfMonth, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val newMillis = cal.timeInMillis
        _startDateMillis.value = newMillis
        prefs.edit().putLong("start_date_millis", newMillis).apply()
    }

    fun getDateForDay(dayNumber: Int, pattern: String = "dd/MM/yyyy"): String {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _startDateMillis.value
            add(Calendar.DAY_OF_YEAR, dayNumber - 1)
        }
        return SimpleDateFormat(pattern, Locale.getDefault()).format(cal.time)
    }

    val completionsMap: StateFlow<Map<String, Boolean>> = repository.allCompletions
        .map { list ->
            list.filter { it.isCompleted }.associate { it.itemKey to true }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    fun selectPhase(phaseId: Int) {
        _activePhaseId.value = phaseId
        _activeDay.value = 1
    }

    fun selectDay(day: Int) {
        _activeDay.value = day
    }

    fun toggleExercise(sectionIndex: Int, exerciseIndex: Int) {
        val currentPhaseId = activePhaseId.value
        val currentDay = activeDay.value
        val key = ExerciseData.makeKey(currentPhaseId, currentDay, sectionIndex, exerciseIndex)
        val isCompleted = completionsMap.value[key] == true

        viewModelScope.launch {
            repository.toggleCompletion(
                itemKey = key,
                phaseId = currentPhaseId,
                dayNumber = currentDay,
                sectionIndex = sectionIndex,
                exerciseIndex = exerciseIndex,
                isCurrentlyCompleted = isCompleted
            )
        }
    }

    fun resetActiveDay() {
        val currentPhaseId = activePhaseId.value
        val currentDay = activeDay.value
        viewModelScope.launch {
            repository.clearDay(currentPhaseId, currentDay)
        }
    }
}
