package com.lifeup.app.data.repository

import com.lifeup.app.data.db.Priority
import com.lifeup.app.data.db.dao.TodoDao
import com.lifeup.app.data.db.entity.TodoEntity
import com.lifeup.app.domain.model.Todo
import com.lifeup.app.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepositoryImpl @Inject constructor(
    private val todoDao: TodoDao
) : TodoRepository {

    override fun getTodosByDate(date: String): Flow<List<Todo>> {
        return todoDao.getTodosByDate(date).map { list -> list.map { it.toDomain() } }
    }

    override fun getHabitsByDate(date: String): Flow<List<Todo>> {
        return todoDao.getHabitsByDate(date).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertTodo(todo: Todo): Long {
        return todoDao.insert(todo.toEntity())
    }

    override suspend fun updateTodo(todo: Todo) {
        todoDao.update(todo.toEntity())
    }

    override suspend fun deleteTodo(todo: Todo) {
        todoDao.delete(todo.toEntity())
    }

    override suspend fun getCompletedCountByDate(date: String): Int {
        return todoDao.getCompletedCountByDate(date)
    }
}

fun TodoEntity.toDomain(): Todo {
    return Todo(
        id = id,
        title = title,
        isHabit = isHabit,
        priority = Priority.valueOf(priority),
        linkedSkillId = linkedSkillId,
        isCompleted = isCompleted,
        completedAt = completedAt,
        createdAt = createdAt,
        date = date,
        sortOrder = sortOrder
    )
}

fun Todo.toEntity(): TodoEntity {
    return TodoEntity(
        id = id,
        title = title,
        isHabit = isHabit,
        priority = priority.name,
        linkedSkillId = linkedSkillId,
        isCompleted = isCompleted,
        completedAt = completedAt,
        createdAt = createdAt,
        date = date,
        sortOrder = sortOrder
    )
}
