package com.lifeup.app.data.repository

import com.lifeup.app.data.db.FocusType
import com.lifeup.app.data.db.RecordType
import com.lifeup.app.data.db.dao.TimeRecordDao
import com.lifeup.app.data.db.entity.TimeRecordEntity
import com.lifeup.app.domain.model.TimeRecord
import com.lifeup.app.domain.repository.TimeRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeRecordRepositoryImpl @Inject constructor(
    private val timeRecordDao: TimeRecordDao
) : TimeRecordRepository {

    override fun getRecordsBySkill(skillId: Long): Flow<List<TimeRecord>> {
        return timeRecordDao.getBySkillId(skillId).map { list -> list.map { it.toDomain() } }
    }

    override fun getRecordsByDateRange(startMs: Long, endMs: Long): Flow<List<TimeRecord>> {
        return timeRecordDao.getByDateRange(startMs, endMs).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertRecord(record: TimeRecord): Long {
        return timeRecordDao.insert(record.toEntity())
    }

    override suspend fun updateRecord(record: TimeRecord) {
        timeRecordDao.update(record.toEntity())
    }

    override suspend fun deleteRecord(record: TimeRecord) {
        timeRecordDao.delete(record.toEntity())
    }

    override suspend fun getInvestmentMinutesByDate(date: String): Int {
        return timeRecordDao.getInvestmentMinutesByDate(date)
    }
}

fun TimeRecordEntity.toDomain(): TimeRecord {
    return TimeRecord(
        id = id,
        skillId = skillId,
        startTime = startTime,
        endTime = endTime,
        durationMinutes = durationMinutes,
        recordType = RecordType.valueOf(recordType),
        focusType = FocusType.valueOf(focusType),
        note = note,
        createdAt = createdAt,
        isLocked = isLocked
    )
}

fun TimeRecord.toEntity(): TimeRecordEntity {
    return TimeRecordEntity(
        id = id,
        skillId = skillId,
        startTime = startTime,
        endTime = endTime,
        durationMinutes = durationMinutes,
        recordType = recordType.name,
        focusType = focusType.name,
        note = note,
        createdAt = createdAt,
        isLocked = isLocked
    )
}
