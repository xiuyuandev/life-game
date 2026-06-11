package com.lifeup.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lifeup.app.data.db.entity.DemonDiaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DemonDiaryDao {

    @Query("SELECT * FROM demon_diary ORDER BY createdAt DESC")
    fun getAll(): Flow<List<DemonDiaryEntity>>

    @Query("SELECT * FROM demon_diary WHERE demonId = :demonId ORDER BY createdAt DESC")
    fun getForDemon(demonId: String): Flow<List<DemonDiaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DemonDiaryEntity): Long

    @Query("DELETE FROM demon_diary WHERE id = :id")
    suspend fun deleteById(id: Long)
}
