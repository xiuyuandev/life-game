package com.lifeup.app.domain.repository

import com.lifeup.app.data.db.entity.CustomDemonEntity
import com.lifeup.app.data.db.entity.DemonDiaryEntity
import com.lifeup.app.data.db.entity.DemonPartDamageEntity
import com.lifeup.app.data.db.entity.DemonProgressEntity
import com.lifeup.app.domain.model.DemonId
import kotlinx.coroutines.flow.Flow

/**
 * 心魔进度 / 部位伤害 / 战记的存储仓库。
 *
 * 实现位于 data 层；
 * 调用方（[com.lifeup.app.domain.game.DemonEngine]、UI ViewModel）只依赖此接口。
 */
interface DemonRepository {

    // ---- 进度 ----

    fun observeAllProgress(): Flow<List<DemonProgressEntity>>
    fun observeProgress(demonId: DemonId): Flow<DemonProgressEntity?>
    fun observeUndefeated(): Flow<List<DemonProgressEntity>>
    fun observeDefeated(): Flow<List<DemonProgressEntity>>

    suspend fun getProgressOnce(demonId: DemonId): DemonProgressEntity?
    suspend fun upsertProgress(progress: DemonProgressEntity)
    suspend fun setActive(demonId: DemonId, active: Boolean)
    suspend fun updateProgressFraction(demonId: DemonId, fraction: Float)
    suspend fun getDefeatedCount(): Int

    // ---- 部位 ----

    fun observeParts(demonId: DemonId): Flow<List<DemonPartDamageEntity>>
    suspend fun getPart(demonId: DemonId, dayOfWeek: Int): DemonPartDamageEntity?
    suspend fun upsertPart(part: DemonPartDamageEntity)
    suspend fun upsertParts(parts: List<DemonPartDamageEntity>)
    suspend fun deletePartsFor(demonId: DemonId)

    // ---- 战记 ----

    fun observeAllDiaries(): Flow<List<DemonDiaryEntity>>
    fun observeDiariesFor(demonId: DemonId): Flow<List<DemonDiaryEntity>>
    suspend fun insertDiary(entry: DemonDiaryEntity): Long
    suspend fun deleteDiary(id: Long)

    // ---- 自定义心魔 ----

    fun observeAllCustomDemons(): Flow<List<CustomDemonEntity>>
    suspend fun getCustomDemonById(id: Long): CustomDemonEntity?
    suspend fun insertCustomDemon(entity: CustomDemonEntity): Long
    suspend fun updateCustomDemon(entity: CustomDemonEntity)
    suspend fun deleteCustomDemon(id: Long)
}
