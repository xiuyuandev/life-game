package com.lifeup.app.di

import com.lifeup.app.data.repository.ComboRepositoryImpl
import com.lifeup.app.data.repository.DailyStateRepositoryImpl
import com.lifeup.app.data.repository.ItemRepositoryImpl
import com.lifeup.app.data.repository.SkillRepositoryImpl
import com.lifeup.app.data.repository.TimeRecordRepositoryImpl
import com.lifeup.app.data.repository.TodoRepositoryImpl
import com.lifeup.app.domain.repository.ComboRepository
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.ItemRepository
import com.lifeup.app.domain.repository.SkillRepository
import com.lifeup.app.domain.repository.TimeRecordRepository
import com.lifeup.app.domain.repository.TodoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSkillRepository(impl: SkillRepositoryImpl): SkillRepository

    @Binds
    @Singleton
    abstract fun bindTodoRepository(impl: TodoRepositoryImpl): TodoRepository

    @Binds
    @Singleton
    abstract fun bindTimeRecordRepository(impl: TimeRecordRepositoryImpl): TimeRecordRepository

    @Binds
    @Singleton
    abstract fun bindComboRepository(impl: ComboRepositoryImpl): ComboRepository

    @Binds
    @Singleton
    abstract fun bindItemRepository(impl: ItemRepositoryImpl): ItemRepository

    @Binds
    @Singleton
    abstract fun bindDailyStateRepository(impl: DailyStateRepositoryImpl): DailyStateRepository
}
