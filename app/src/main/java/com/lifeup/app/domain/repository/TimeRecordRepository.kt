package com.lifeup.app.domain.repository

import com.lifeup.app.domain.model.TimeRecord
import kotlinx.coroutines.flow.Flow

interface TimeRecordRepository {

    fun getRecordsBySkill(skillId: Long): Flow<List<TimeRecord>>

    fun getRecordsByDateRange(startMs: Long, endMs: Long): Flow<List<TimeRecord>>

    suspend fun insertRecord(record: TimeRecord): Long

    suspend fun updateRecord(record: TimeRecord)

    suspend fun deleteRecord(record: TimeRecord)

    suspend fun getInvestmentMinutesByDate(date: String): Int
}
