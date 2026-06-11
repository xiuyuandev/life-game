package com.lifeup.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeup.app.data.db.entity.DemonPartDamageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DemonPartDamageDao {

    @Query("SELECT * FROM demon_part_damage WHERE demonId = :demonId ORDER BY dayOfWeek ASC")
    fun getByDemon(demonId: String): Flow<List<DemonPartDamageEntity>>

    @Query("SELECT * FROM demon_part_damage WHERE demonId = :demonId AND dayOfWeek = :dayOfWeek LIMIT 1")
    suspend fun getPart(demonId: String, dayOfWeek: Int): DemonPartDamageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(part: DemonPartDamageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(parts: List<DemonPartDamageEntity>)

    @Update
    suspend fun update(part: DemonPartDamageEntity)

    @Query("UPDATE demon_part_damage SET currentHp = MAX(0, currentHp - :damage), totalDamage = totalDamage + :damage, hitCount = hitCount + 1, lastHitAt = :timestamp, isBroken = CASE WHEN currentHp - :damage <= 0 THEN 1 ELSE 0 END WHERE demonId = :demonId AND dayOfWeek = :dayOfWeek")
    suspend fun applyDamage(demonId: String, dayOfWeek: Int, damage: Int, timestamp: Long = System.currentTimeMillis()): Int

    @Query("UPDATE demon_part_damage SET isBroken = 0, currentHp = maxHp WHERE demonId = :demonId")
    suspend fun resetAll(demonId: String)

    @Query("SELECT SUM(totalDamage) FROM demon_part_damage WHERE demonId = :demonId")
    suspend fun getTotalDamage(demonId: String): Int?

    @Query("DELETE FROM demon_part_damage WHERE demonId = :demonId")
    suspend fun deleteForDemon(demonId: String)
}
