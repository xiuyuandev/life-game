package com.lifeup.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.Priority
import com.lifeup.app.domain.model.DailyState
import com.lifeup.app.domain.model.Todo
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.TimeRecordRepository
import com.lifeup.app.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class TodayUiState(
    val habits: List<Todo> = emptyList(),
    val todos: List<Todo> = emptyList(),
    val energy: Float = 0f,
    val energyCap: Float = 100f,
    val streakCount: Int = 0,
    val todayDate: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val todoRepository: TodoRepository,
    private val dailyStateRepository: DailyStateRepository,
    private val timeRecordRepository: TimeRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("M月d日 EEEE", Locale.CHINESE)

    init {
        loadTodayData()
    }

    private fun loadTodayData() {
        val todayStr = dateFormat.format(Date())
        val displayDate = displayDateFormat.format(Date())

        viewModelScope.launch {
            _uiState.update { it.copy(todayDate = displayDate, isLoading = true) }

            combine(
                todoRepository.getHabitsByDate(todayStr),
                todoRepository.getTodosByDate(todayStr),
                dailyStateRepository.getStateByDate(todayStr)
            ) { habits, todos, dailyState ->
                val state = dailyState ?: DailyState(
                    date = todayStr,
                    energy = 0f,
                    energyCap = 100f,
                    streakCount = 0
                )
                Triple(habits, todos, state)
            }.collect { (habits, todos, dailyState) ->
                val calculatedEnergy = calculateEnergy(habits, todos)
                val streak = dailyStateRepository.getLatestStreak() ?: dailyState.streakCount

                _uiState.update { currentState ->
                    currentState.copy(
                        habits = habits,
                        todos = todos,
                        energy = calculatedEnergy,
                        energyCap = dailyState.energyCap,
                        streakCount = streak,
                        isLoading = false
                    )
                }

                // Persist updated energy to DailyState
                if (dailyState.energy != calculatedEnergy || dailyState.streakCount != streak) {
                    dailyStateRepository.insertOrUpdateState(
                        dailyState.copy(
                            energy = calculatedEnergy,
                            streakCount = streak,
                            habitsCompleted = habits.count { it.isCompleted },
                            todosCompleted = todos.count { it.isCompleted }
                        )
                    )
                }
            }
        }
    }

    private fun calculateEnergy(habits: List<Todo>, todos: List<Todo>): Float {
        var energy = 0f
        val allItems = habits + todos
        for (item in allItems) {
            if (item.isCompleted) {
                energy += when (item.priority) {
                    Priority.HIGH -> 1.5f
                    else -> 1f
                }
            }
        }
        return energy
    }

    fun toggleTodo(id: Long) {
        viewModelScope.launch {
            val currentHabits = _uiState.value.habits
            val currentTodos = _uiState.value.todos
            val allItems = currentHabits + currentTodos
            val item = allItems.find { it.id == id } ?: return@launch

            val updated = item.copy(
                isCompleted = !item.isCompleted,
                completedAt = if (!item.isCompleted) System.currentTimeMillis() else null
            )
            todoRepository.updateTodo(updated)
        }
    }

    fun addTodo(
        title: String,
        isHabit: Boolean,
        priority: Priority = Priority.NONE,
        linkedSkillId: Long? = null
    ) {
        if (title.isBlank()) return

        viewModelScope.launch {
            val todayStr = dateFormat.format(Date())
            val todo = Todo(
                title = title.trim(),
                isHabit = isHabit,
                priority = priority,
                linkedSkillId = linkedSkillId,
                date = todayStr,
                sortOrder = if (isHabit) {
                    _uiState.value.habits.size
                } else {
                    _uiState.value.todos.size
                }
            )
            todoRepository.insertTodo(todo)
        }
    }

    fun deleteTodo(id: Long) {
        viewModelScope.launch {
            val currentHabits = _uiState.value.habits
            val currentTodos = _uiState.value.todos
            val allItems = currentHabits + currentTodos
            val item = allItems.find { it.id == id } ?: return@launch
            todoRepository.deleteTodo(item)
        }
    }

    fun updateEnergy() {
        viewModelScope.launch {
            val todayStr = dateFormat.format(Date())
            val dailyState = dailyStateRepository.getStateByDate(todayStr).first()
            val state = dailyState ?: DailyState(date = todayStr)
            val calculatedEnergy = calculateEnergy(_uiState.value.habits, _uiState.value.todos)

            _uiState.update { it.copy(energy = calculatedEnergy, energyCap = state.energyCap) }

            dailyStateRepository.insertOrUpdateState(
                state.copy(energy = calculatedEnergy)
            )
        }
    }
}
