package com.lifeup.app.di

import android.content.Context
import com.lifeup.app.data.db.LifeUpDatabase
import com.lifeup.app.data.db.dao.CharacterStateDao
import com.lifeup.app.data.db.dao.AchievementDao
import com.lifeup.app.data.db.dao.ComboDao
import com.lifeup.app.data.db.dao.DailyStateDao
import com.lifeup.app.data.db.dao.ItemDao
import com.lifeup.app.data.db.dao.SkillDao
import com.lifeup.app.data.db.dao.TimeRecordDao
import com.lifeup.app.data.db.dao.TodoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LifeUpDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            LifeUpDatabase::class.java,
            "lifeup_database"
        )
            .addMigrations(
                LifeUpDatabase.MIGRATION_1_2,
                LifeUpDatabase.MIGRATION_3_4,
                LifeUpDatabase.MIGRATION_4_5,
                LifeUpDatabase.MIGRATION_5_6
            )
            .build()
    }

    @Provides
    fun provideSkillDao(database: LifeUpDatabase): SkillDao {
        return database.skillDao()
    }

    @Provides
    fun provideTodoDao(database: LifeUpDatabase): TodoDao {
        return database.todoDao()
    }

    @Provides
    fun provideTimeRecordDao(database: LifeUpDatabase): TimeRecordDao {
        return database.timeRecordDao()
    }

    @Provides
    fun provideComboDao(database: LifeUpDatabase): ComboDao {
        return database.comboDao()
    }

    @Provides
    fun provideItemDao(database: LifeUpDatabase): ItemDao {
        return database.itemDao()
    }

    @Provides
    fun provideDailyStateDao(database: LifeUpDatabase): DailyStateDao {
        return database.dailyStateDao()
    }

    @Provides
    fun provideAchievementDao(database: LifeUpDatabase): AchievementDao {
        return database.achievementDao()
    }

    @Provides
    fun provideCharacterStateDao(database: LifeUpDatabase): CharacterStateDao {
        return database.characterStateDao()
    }
}
