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

    override suspend fun getTotalGoldEarned(): Int {
        val records = timeRecordDao.getAll()
        var totalGold = 0
        for (record in records) {
            val isInvestment = record.recordType == "INVESTMENT"
            val baseRate = if (isInvestment) 1 else 0.2
            totalGold += (record.durationMinutes * baseRate).toInt()
        }
        val dailyStates = dailyStateDao.getAll()
        totalGold += dailyStates.size * 50
        return totalGold
    }

    override suspend fun getTotalGoldSpent(): Int {
        val dailyStates = dailyStateDao.getAll()
        return dailyStates.sumOf { it.goldSpent }
    }

    override fun getGoldBalance(): Flow<Int> {
        return flow {
            val earned = getTotalGoldEarned()
            val spent = getTotalGoldSpent()
            emit((earned - spent).coerceAtLeast(0))
        }
    }

    override suspend fun spendGold(amount: Int): Boolean {
        if (amount <= 0) return false
        val currentBalance = getTotalGoldEarned() - getTotalGoldSpent()
        if (amount > currentBalance) return false

        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val todayState = dailyStateDao.getByDateSync(today)

        if (todayState != null) {
            dailyStateDao.update(
                todayState.copy(goldSpent = todayState.goldSpent + amount)
            )
        } else {
            dailyStateDao.insert(
                DailyStateEntity(
                    date = today,
                    goldSpent = amount
                )
            )
        }
        return true
    }
}
