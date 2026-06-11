package com.lifeup.app.data.repository

import com.lifeup.app.data.db.dao.DailyStateDao
import com.lifeup.app.data.db.dao.ItemDao
import com.lifeup.app.data.db.dao.TimeRecordDao
import com.lifeup.app.domain.repository.GoldRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
        val items = itemDao.getAllList()
        return items.filter { it.price > 0 }.sumOf { it.price }
    }

    override fun getGoldBalance(): Flow<Int> {
        return flow {
            val earned = getTotalGoldEarned()
            val spent = getTotalGoldSpent()
            emit((earned - spent).coerceAtLeast(0))
        }
    }
}
