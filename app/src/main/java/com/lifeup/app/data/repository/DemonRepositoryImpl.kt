package com.lifeup.app.data.repository

import com.lifeup.app.data.db.dao.CustomDemonDao
import com.lifeup.app.data.db.dao.DemonDiaryDao
import com.lifeup.app.data.db.dao.DemonPartDamageDao
import com.lifeup.app.data.db.dao.DemonProgressDao
import com.lifeup.app.data.db.entity.CustomDemonEntity
import com.lifeup.app.data.db.entity.DemonDiaryEntity
import com.lifeup.app.data.db.entity.DemonPartDamageEntity
import com.lifeup.app.data.db.entity.DemonProgressEntity
import com.lifeup.app.domain.model.DemonId
import com.lifeup.app.domain.repository.DemonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemonRepositoryImpl @Inject constructor(
    private val progressDao: DemonProgressDao,
    private val partDamageDao: DemonPartDamageDao,
    private val diaryDao: DemonDiaryDao,
    private val customDemonDao: CustomDemonDao
) : DemonRepository {

    // ---- progress ----
    override fun observeAllProgress(): Flow<List<DemonProgressEntity>> = progressDao.getAll()

    override fun observeProgress(demonId: DemonId): Flow<DemonProgressEntity?> =
        progressDao.getById(demonId.key)

    override fun observeUndefeated(): Flow<List<DemonProgressEntity>> = progressDao.getUndefeated()

    override fun observeDefeated(): Flow<List<DemonProgressEntity>> = progressDao.getDefeated()

    override suspend fun getProgressOnce(demonId: DemonId): DemonProgressEntity? =
        progressDao.getByIdOnce(demonId.key)

    override suspend fun upsertProgress(progress: DemonProgressEntity) =
        progressDao.upsert(progress)

    override suspend fun setActive(demonId: DemonId, active: Boolean) =
        progressDao.setActive(demonId.key, active)

    override suspend fun updateProgressFraction(demonId: DemonId, fraction: Float) =
        progressDao.updateProgressFraction(demonId.key, fraction)

    override suspend fun getDefeatedCount(): Int = progressDao.getDefeatedCount()

    // ---- parts ----
    override fun observeParts(demonId: DemonId): Flow<List<DemonPartDamageEntity>> =
        partDamageDao.getByDemon(demonId.key)

    override suspend fun getPart(demonId: DemonId, dayOfWeek: Int): DemonPartDamageEntity? =
        partDamageDao.getPart(demonId.key, dayOfWeek)

    override suspend fun upsertPart(part: DemonPartDamageEntity) = partDamageDao.upsert(part)

    override suspend fun upsertParts(parts: List<DemonPartDamageEntity>) =
        partDamageDao.upsertAll(parts)

    override suspend fun deletePartsFor(demonId: DemonId) =
        partDamageDao.deleteForDemon(demonId.key)

    // ---- diary ----
    override fun observeAllDiaries(): Flow<List<DemonDiaryEntity>> = diaryDao.getAll()
    override fun observeDiariesFor(demonId: DemonId): Flow<List<DemonDiaryEntity>> =
        diaryDao.getForDemon(demonId.key)

    override suspend fun insertDiary(entry: DemonDiaryEntity): Long = diaryDao.insert(entry)
    override suspend fun deleteDiary(id: Long) = diaryDao.deleteById(id)

    // ---- custom demons ----
    override fun observeAllCustomDemons(): Flow<List<CustomDemonEntity>> = customDemonDao.getAll()
    override suspend fun getCustomDemonById(id: Long): CustomDemonEntity? =
        customDemonDao.getById(id)
    override suspend fun insertCustomDemon(entity: CustomDemonEntity): Long =
        customDemonDao.insert(entity)
    override suspend fun updateCustomDemon(entity: CustomDemonEntity) = customDemonDao.update(entity)
    override suspend fun deleteCustomDemon(id: Long) = customDemonDao.deleteById(id)
}
