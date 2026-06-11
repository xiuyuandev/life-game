package com.lifeup.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.Priority
import com.lifeup.app.data.preferences.SettingsPrefs
import com.lifeup.app.domain.model.DailyState
import com.lifeup.app.domain.model.Todo
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.SkillRepository
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.withTimeout
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable
data class TodayUiState(
    val habits: List<Todo> = emptyList(),
    val todos: List<Todo> = emptyList(),
    val energy: Float = 0f,
    val energyCap: Float = 100f,
    val streakCount: Int = 0,
    val todayDate: String = "",
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val tip: TipContent? = null,
    // Game stats for today
    val investmentMinutes: Int = 0,
    val consumptionMinutes: Int = 0,
    val goldEarned: Int = 0,
    val goldSpent: Int = 0,
    val habitsCompleted: Int = 0,
    val todosCompleted: Int = 0,
    val characterLevel: Int = 1,
    val totalExp: Long = 0L,
    val expToNextLevel: Long = 1000L
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val todoRepository: TodoRepository,
    private val dailyStateRepository: DailyStateRepository,
    private val timeRecordRepository: TimeRecordRepository,
    private val skillRepository: SkillRepository,
    private val characterStateRepository: com.lifeup.app.domain.repository.CharacterStateRepository,
    val settingsPrefs: SettingsPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINESE)

    private var loadJob: Job? = null

    init {
        loadTodayData()
        loadTip()
    }

    private fun loadTip() {
        viewModelScope.launch {
            try {
                val earliestCreatedAt = skillRepository.getEarliestCreatedAt()
                if (earliestCreatedAt == null || earliestCreatedAt == 0L) {
                    _uiState.update { it.copy(tip = null) }
                    return@launch
                }

                val daysSinceStart = ((System.currentTimeMillis() - earliestCreatedAt) / (24 * 60 * 60 * 1000)).toInt() + 1
                val dismissedTips = try {
                    withTimeout(5000) { settingsPrefs.getDismissedTips().first() }
                } catch (_: Exception) {
                    emptySet()
                }

                val tip = when {
                    daysSinceStart in 1..2 -> TipContent(
                        id = "tip_day_1_2",
                        title = "新手提示",
                        message = "试试开始计时吧！投资性时间会为你带来经验和金币"
                    )
                    daysSinceStart in 3..4 -> TipContent(
                        id = "tip_day_3_4",
                        title = "技能组合",
                        message = "创建更多技能来解锁组合加成！跨分类技能组合可获得额外经验"
                    )
                    daysSinceStart in 5..7 -> TipContent(
                        id = "tip_day_5_7",
                        title = "坚持就是胜利",
                        message = "坚持打卡可以获得连续天数加成，每天首次计时还有额外奖励"
                    )
                    else -> null
                }

                val finalTip = if (tip != null && tip.id !in dismissedTips) tip else null
                _uiState.update { it.copy(tip = finalTip) }
            } catch (_: Exception) {
                _uiState.update { it.copy(tip = null) }
            }
        }
    }

    fun dismissTip() {
        val tipId = _uiState.value.tip?.id ?: return
        viewModelScope.launch {
            try {
                settingsPrefs.dismissTip(tipId)
                _uiState.update { it.copy(tip = null) }
            } catch (_: Exception) {
            }
        }
    }

    fun refresh() {
        loadTodayData(isRefresh = true)
    }

    private fun loadTodayData(isRefresh: Boolean = false) {
        val today = LocalDate.now()
        val todayStr = today.format(dateFormat)
        val displayDate = today.format(displayDateFormat)

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    todayDate = displayDate,
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    error = null
                )
            }

            try {
                val characterState = try {
                    withTimeout(5000) { characterStateRepository.getCharacterState().first() }
                } catch (_: Exception) {
                    null
                }
                val charLevel = characterState?.let {
                    com.lifeup.app.domain.calculator.AttributeCalculator.calculateCharacterLevel(it.totalExp)
                } ?: 1
                val expToNext = characterState?.let {
                    val currentLevel = com.lifeup.app.domain.calculator.AttributeCalculator.calculateCharacterLevel(it.totalExp)
                    val nextThreshold = com.lifeup.app.domain.calculator.AttributeCalculator.getLevelThreshold(currentLevel + 1)
                    nextThreshold - it.totalExp
                } ?: 1000L

                // Pre-fetch streak (suspend call must be outside collect lambda)
                val latestStreak = try {
                    withTimeout(5000) { dailyStateRepository.getLatestStreak() }
                } catch (_: Exception) {
                    null
                }

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
                    val streak = latestStreak ?: dailyState.streakCount
                    val habitsCompleted = habits.count { it.isCompleted }
                    val todosCompleted = todos.count { it.isCompleted }

                    _uiState.update { currentState ->
                        currentState.copy(
                            habits = habits,
                            todos = todos,
                            energy = dailyState.energy,
                            energyCap = dailyState.energyCap,
                            streakCount = streak,
                            isLoading = false,
                            isRefreshing = false,
                            error = null,
                            investmentMinutes = dailyState.investmentMinutes,
                            consumptionMinutes = dailyState.consumptionMinutes,
                            goldEarned = dailyState.goldEarned,
                            goldSpent = dailyState.goldSpent,
                            habitsCompleted = habitsCompleted,
                            todosCompleted = todosCompleted,
                            characterLevel = charLevel,
                            totalExp = characterState?.totalExp ?: 0L,
                            expToNextLevel = expToNext.coerceAtLeast(0L)
                        )
                    }

                    // Persist counts to DailyState if out of sync
                    if (dailyState.habitsCompleted != habitsCompleted || dailyState.todosCompleted != todosCompleted || dailyState.streakCount != streak) {
                        dailyStateRepository.insertOrUpdateState(
                            dailyState.copy(
                                streakCount = streak,
                                habitsCompleted = habitsCompleted,
                                todosCompleted = todosCompleted
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "加载失败"
                    )
                }
            }
        }
    }

    /**
     * Calculate energy regeneration based on elapsed time since last update.
     * Energy regenerates at 5 points per hour, up to the energy cap.
     */
    private fun calculateRegeneratedEnergy(currentEnergy: Float, energyCap: Float, lastUpdatedMs: Long): Float {
        if (currentEnergy >= energyCap) return currentEnergy
        val hoursElapsed = ((System.currentTimeMillis() - lastUpdatedMs) / (60 * 60 * 1000.0)).coerceAtLeast(0.0)
        val regenerated = (hoursElapsed * 5).toFloat()
        return (currentEnergy + regenerated).coerceAtMost(energyCap)
    }

    fun toggleTodo(id: Long) {
        viewModelScope.launch {
            try {
                val currentHabits = _uiState.value.habits
                val currentTodos = _uiState.value.todos
                val allItems = currentHabits + currentTodos
                val item = allItems.find { it.id == id } ?: return@launch

                val updated = item.copy(
                    isCompleted = !item.isCompleted,
                    completedAt = if (!item.isCompleted) System.currentTimeMillis() else null
                )
                todoRepository.updateTodo(updated)

                // Apply gold reward on completion (energy is managed by Timer/GameEngine only)
                if (!item.isCompleted) {
                    val todayStr = LocalDate.now().format(dateFormat)
                    val dailyState = try {
                        withTimeout(5000) { dailyStateRepository.getStateByDate(todayStr).first() }
                    } catch (_: Exception) {
                        null
                    }
                    val state = dailyState ?: DailyState(date = todayStr)

                    val goldReward = if (item.isHabit) 5 else 2

                    dailyStateRepository.insertOrUpdateState(
                        state.copy(
                            goldEarned = state.goldEarned + goldReward,
                            habitsCompleted = state.habitsCompleted + if (item.isHabit) 1 else 0,
                            todosCompleted = state.todosCompleted + if (!item.isHabit) 1 else 0,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                }
            } catch (_: Exception) {
            }
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
            try {
                val todayStr = LocalDate.now().format(dateFormat)
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
            } catch (_: Exception) {
            }
        }
    }

    fun deleteTodo(id: Long) {
        viewModelScope.launch {
            try {
                val currentHabits = _uiState.value.habits
                val currentTodos = _uiState.value.todos
                val allItems = currentHabits + currentTodos
                val item = allItems.find { it.id == id } ?: return@launch
                todoRepository.deleteTodo(item)
            } catch (_: Exception) {
            }
        }
    }

    fun updateEnergy() {
        viewModelScope.launch {
            try {
                val todayStr = LocalDate.now().format(dateFormat)
                val dailyState = try {
                    withTimeout(5000) { dailyStateRepository.getStateByDate(todayStr).first() }
                } catch (_: Exception) {
                    null
                }
                val state = dailyState ?: DailyState(date = todayStr)

                // Regenerate energy based on elapsed time since last update
                val lastUpdated = state.lastUpdated.takeIf { it > 0 } ?: System.currentTimeMillis()
                val regeneratedEnergy = calculateRegeneratedEnergy(state.energy, state.energyCap, lastUpdated)

                _uiState.update { it.copy(energy = regeneratedEnergy, energyCap = state.energyCap) }

                if (regeneratedEnergy != state.energy) {
                    dailyStateRepository.insertOrUpdateState(
                        state.copy(energy = regeneratedEnergy, lastUpdated = System.currentTimeMillis())
                    )
                }
            } catch (_: Exception) {
            }
        }
    }
}
