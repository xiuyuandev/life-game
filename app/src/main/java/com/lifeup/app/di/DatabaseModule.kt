package com.lifeup.app.di

import android.content.Context
import androidx.room.Room
import com.lifeup.app.data.SeedData
import com.lifeup.app.data.backup.DataBackupManager
import com.lifeup.app.data.db.LifeUpDatabase
import com.lifeup.app.data.preferences.SettingsPreferences
import com.lifeup.app.data.repository.AchievementRepository
import com.lifeup.app.data.repository.CharacterRepository
import com.lifeup.app.data.repository.EquipmentRepository
import com.lifeup.app.data.repository.SkillRepository
import com.lifeup.app.data.repository.TimeAssetRepository
import com.lifeup.app.data.repository.TimeSessionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        skillRepositoryProvider: Provider<SkillRepository>,
        equipmentRepositoryProvider: Provider<EquipmentRepository>,
        achievementRepositoryProvider: Provider<AchievementRepository>
    ): LifeUpDatabase {
        return Room.databaseBuilder(
            context,
            LifeUpDatabase::class.java,
            "lifeup_database"
        )
            .fallbackToDestructiveMigration()
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Seed data
                    CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                        try {
                            skillRepositoryProvider.get().insertAll(SeedData.defaultSkills)
                            equipmentRepositoryProvider.get().insertAll(SeedData.defaultEquipment)
                            achievementRepositoryProvider.get().insertAll(SeedData.defaultAchievements)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideSettingsPreferences(@ApplicationContext context: Context): SettingsPreferences {
        return SettingsPreferences(context)
    }

    @Provides
    @Singleton
    fun provideDataBackupManager(
        @ApplicationContext context: Context,
        characterRepository: CharacterRepository,
        skillRepository: SkillRepository,
        timeSessionRepository: TimeSessionRepository,
        equipmentRepository: EquipmentRepository,
        achievementRepository: AchievementRepository,
        timeAssetRepository: TimeAssetRepository
    ): DataBackupManager {
        return DataBackupManager(
            context,
            characterRepository,
            skillRepository,
            timeSessionRepository,
            equipmentRepository,
            achievementRepository,
            timeAssetRepository
        )
    }

    @Provides
    fun provideCharacterDao(database: LifeUpDatabase) = database.characterDao()

    @Provides
    fun provideTimeSessionDao(database: LifeUpDatabase) = database.timeSessionDao()

    @Provides
    fun provideSkillDao(database: LifeUpDatabase) = database.skillDao()

    @Provides
    fun provideEquipmentDao(database: LifeUpDatabase) = database.equipmentDao()

    @Provides
    fun provideAchievementDao(database: LifeUpDatabase) = database.achievementDao()

    @Provides
    fun provideTimeAssetDao(database: LifeUpDatabase) = database.timeAssetDao()
}