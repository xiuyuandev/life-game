package com.lifeup.app.domain.repository

import com.lifeup.app.domain.model.Todo
import kotlinx.coroutines.flow.Flow

interface TodoRepository {

    fun getTodosByDate(date: String): Flow<List<Todo>>

    fun getHabitsByDate(date: String): Flow<List<Todo>>

    suspend fun insertTodo(todo: Todo): Long

    suspend fun updateTodo(todo: Todo)

    suspend fun deleteTodo(todo: Todo)

    suspend fun getCompletedCountByDate(date: String): Int
}
