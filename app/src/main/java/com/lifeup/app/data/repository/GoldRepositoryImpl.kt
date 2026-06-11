package com.lifeup.app.data.repository

import com.lifeup.app.data.db.dao.DailyStateDao
import com.lifeup.app.data.db.dao.ItemDao
import com.lifeup.app.data.db.dao.TimeRecordDao
import com.lifeup.app.data.db.entity.DailyStateEntity
import com.lifeup.app.domain.repository.GoldRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoldRepositoryImpl @Inject constructor(
    private val timeRecordDao: TimeRecordDao,
    private val itemDao: ItemDao,
    private val dailyStateDao: DailyStateDao
) : GoldRepository {

    /**
     * Get total gold earned by aggregating dailyState.goldEarned.
     * This is the authoritative source of truth for earned gold, since
     * GameEngine records the actual calculated amount (including first-hit bonus,
     * level bonus, etc.) into each day's goldEarned.
     */
    override suspend fun getTotalGoldEarned(): Int {
        val dailyStates = dailyStateDao.getAll()
        return dailyStates.sumOf { it.goldEarned }
    }

    override suspend fun getTotalGoldSpent(): Int {
        val dailyStates = dailyStateDao.getAll()
        return dailyStates.sumOf { it.goldSpent }
    }

    override fun getGoldBalance(): Flow<Int> {
        return flow {
            // Single-pass aggregation: get all daily states once
            val dailyStates = dailyStateDao.getAll()
            val earned = dailyStates.sumOf { it.goldEarned }
            val spent = dailyStates.sumOf { it.goldSpent }
            emit((earned - spent).coerceAtLeast(0))
        }
    }

    override suspend fun spendGold(amount: Int): Boolean {
        if (amount <= 0) return false
        // Single-pass aggregation for balance check
        val dailyStates = dailyStateDao.getAll()
        val currentBalance = dailyStates.sumOf { it.goldEarned } - dailyStates.sumOf { it.goldSpent }
        if (amount > currentBalance) return false

        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val todayState = dailyStateDao.getByDateSync(today)

        if (todayState != null) {
            dailyStateDao.update(
                todayState.copy(
                    goldSpent = todayState.goldSpent + amount,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        } else {
            dailyStateDao.insert(
                DailyStateEntity(
                    date = today,
                    goldSpent = amount,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
        return true
    }
}
