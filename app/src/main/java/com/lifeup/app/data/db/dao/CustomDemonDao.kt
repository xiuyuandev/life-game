package com.lifeup.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeup.app.data.db.entity.CustomDemonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomDemonDao {

    @Query("SELECT * FROM custom_demons ORDER BY createdAt DESC")
    fun getAll(): Flow<List<CustomDemonEntity>>

    @Query("SELECT * FROM custom_demons WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CustomDemonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(demon: CustomDemonEntity): Long

    @Update
    suspend fun update(demon: CustomDemonEntity)

    @Query("DELETE FROM custom_demons WHERE id = :id")
    suspend fun deleteById(id: Long)
}
