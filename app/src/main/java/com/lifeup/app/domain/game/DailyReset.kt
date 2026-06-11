package com.lifeup.app.domain.game

import com.lifeup.app.data.preferences.SettingsPrefs
import com.lifeup.app.domain.model.DailyState
import com.lifeup.app.domain.model.Todo
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.TodoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DailyReset {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun performReset(
        dailyStateRepository: DailyStateRepository,
        todoRepository: TodoRepository,
        settingsPrefs: SettingsPrefs
    ) {
        val today = LocalDate.now().format(dateFormatter)
        val yesterday = LocalDate.now().minusDays(1).format(dateFormatter)

        // a. Reset energy to 0 for new day - create new DailyState with fresh energy
        // b. Reset habits (uncheck all habits for the new day)
        val yesterdayHabits = try {
            withTimeout(5000) { todoRepository.getHabitsByDate(yesterday).first() }
        } catch (_: Exception) { emptyList() }
        for (habit in yesterdayHabits) {
            val resetHabit = habit.copy(
                isCompleted = false,
                completedAt = null,
                date = today
            )
            todoRepository.insertTodo(resetHabit)
        }

        // c. Create new DailyState for today
        val existingToday = try {
            withTimeout(5000) { dailyStateRepository.getStateByDate(today).first() }
        } catch (_: Exception) { null }
        if (existingToday == null) {
            // d. Calculate streak (check if yesterday had completions)
            val streakCount = calculateStreak(dailyStateRepository, yesterday)

            val newDailyState = DailyState(
                date = today,
                energy = 100f,
                energyCap = 100f,
                investmentMinutes = 0,
                consumptionMinutes = 0,
                streakCount = streakCount,
                isFirstTimerUsed = false,
                todosCompleted = 0,
                habitsCompleted = 0,
                lastUpdated = System.currentTimeMillis()
            )
            dailyStateRepository.insertOrUpdateState(newDailyState)
        }

        // Reset SettingsPrefs first timer flag
        settingsPrefs.resetFirstTimerUsedToday()
    }

    private suspend fun calculateStreak(
        dailyStateRepository: DailyStateRepository,
        yesterdayDate: String
    ): Int {
        val yesterdayState = try {
            withTimeout(5000) { dailyStateRepository.getStateByDate(yesterdayDate).first() }
        } catch (_: Exception) { null }

        if (yesterdayState != null && yesterdayState.investmentMinutes > 0) {
            return yesterdayState.streakCount + 1
        }

        return 0
    }
}
