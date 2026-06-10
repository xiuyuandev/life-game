package com.lifeup.app.data.repository

import com.lifeup.app.data.db.dao.TimeSessionDao
import com.lifeup.app.data.db.entity.TimeSessionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeSessionRepository @Inject constructor(
    private val timeSessionDao: TimeSessionDao
) {
    suspend fun getActiveSession(): TimeSessionEntity? = timeSessionDao.getActiveSession()

    fun getActiveSessionFlow(): Flow<TimeSessionEntity?> = timeSessionDao.getActiveSessionFlow()

    fun getSessionsByDate(date: String): Flow<List<TimeSessionEntity>> = timeSessionDao.getSessionsByDate(date)

    suspend fun getSessionsByDateSync(date: String): List<TimeSessionEntity> = timeSessionDao.getSessionsByDateSync(date)

    suspend fun getSessionsBySkill(skillId: Long): List<TimeSessionEntity> = timeSessionDao.getSessionsBySkill(skillId)

    suspend fun getRecentSessions(limit: Int = 20): List<TimeSessionEntity> = timeSessionDao.getRecentSessions(limit)

    fun getAllSessionsFlow(): Flow<List<TimeSessionEntity>> = timeSessionDao.getAllSessionsFlow()

    suspend fun insert(session: TimeSessionEntity): Long = timeSessionDao.insert(session)

    suspend fun update(session: TimeSessionEntity) = timeSessionDao.update(session)

    suspend fun deleteById(id: Long) = timeSessionDao.deleteById(id)

    suspend fun deleteAll() = timeSessionDao.deleteAll()

    suspend fun getSessionCountByDate(date: String): Int = timeSessionDao.getSessionCountByDate(date)
}
