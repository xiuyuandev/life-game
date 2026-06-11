package com.lifeup.app.data.repository

import com.lifeup.app.data.db.dao.DailyStateDao
import com.lifeup.app.data.db.entity.DailyStateEntity
import com.lifeup.app.domain.model.DailyState
import com.lifeup.app.domain.repository.DailyStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyStateRepositoryImpl @Inject constructor(
    private val dailyStateDao: DailyStateDao
) : DailyStateRepository {

    override fun getStateByDate(date: String): Flow<DailyState?> {
        return dailyStateDao.getByDate(date).map { it?.toDomain() }
    }

    override suspend fun insertOrUpdateState(state: DailyState): Long {
        return dailyStateDao.insert(state.toEntity())
    }

    override suspend fun getLatestStreak(): Int? {
        return dailyStateDao.getLatestStreak()
    }
}

fun DailyStateEntity.toDomain(): DailyState {
    return DailyState(
        id = id,
        date = date,
        energy = energy,
        energyCap = energyCap,
        investmentMinutes = investmentMinutes,
        consumptionMinutes = consumptionMinutes,
        streakCount = streakCount,
        isFirstTimerUsed = isFirstTimerUsed,
        todosCompleted = todosCompleted,
        habitsCompleted = habitsCompleted,
        goldEarned = goldEarned,
        goldSpent = goldSpent,
        lastUpdated = lastUpdated
    )
}

fun DailyState.toEntity(): DailyStateEntity {
    return DailyStateEntity(
        id = id,
        date = date,
        energy = energy,
        energyCap = energyCap,
        investmentMinutes = investmentMinutes,
        consumptionMinutes = consumptionMinutes,
        streakCount = streakCount,
        isFirstTimerUsed = isFirstTimerUsed,
        todosCompleted = todosCompleted,
        habitsCompleted = habitsCompleted,
        goldEarned = goldEarned,
        goldSpent = goldSpent,
        lastUpdated = lastUpdated
    )
}
