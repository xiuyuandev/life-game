package com.lifeup.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeup.app.data.db.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Query("SELECT * FROM todos WHERE date = :date ORDER BY sort_order ASC")
    fun getByDate(date: String): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE date = :date AND is_habit = 1 ORDER BY sort_order ASC")
    fun getHabitsByDate(date: String): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE date = :date AND is_habit = 0 ORDER BY sort_order ASC")
    fun getTodosByDate(date: String): Flow<List<TodoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoEntity): Long

    @Update
    suspend fun update(todo: TodoEntity)

    @Delete
    suspend fun delete(todo: TodoEntity)

    @Query("SELECT COUNT(*) FROM todos WHERE date = :date AND is_completed = 1")
    suspend fun getCompletedCountByDate(date: String): Int

    @Query("SELECT * FROM todos")
    suspend fun getAll(): List<TodoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<TodoEntity>)

    @Query("DELETE FROM todos")
    suspend fun deleteAll()
}
