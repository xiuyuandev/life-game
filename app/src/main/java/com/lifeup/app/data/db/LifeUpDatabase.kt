package com.lifeup.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lifeup.app.data.db.dao.AchievementDao
import com.lifeup.app.data.db.dao.CharacterDao
import com.lifeup.app.data.db.dao.EquipmentDao
import com.lifeup.app.data.db.dao.SkillDao
import com.lifeup.app.data.db.dao.TimeAssetDao
import com.lifeup.app.data.db.dao.TimeSessionDao
import com.lifeup.app.data.db.entity.AchievementEntity
import com.lifeup.app.data.db.entity.CharacterEntity
import com.lifeup.app.data.db.entity.EquipmentEntity
import com.lifeup.app.data.db.entity.SkillEntity
import com.lifeup.app.data.db.entity.TimeAssetEntity
import com.lifeup.app.data.db.entity.TimeSessionEntity

@Database(
    entities = [
        CharacterEntity::class,
        TimeSessionEntity::class,
        SkillEntity::class,
        EquipmentEntity::class,
        AchievementEntity::class,
        TimeAssetEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class LifeUpDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun timeSessionDao(): TimeSessionDao
    abstract fun skillDao(): SkillDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun achievementDao(): AchievementDao
    abstract fun timeAssetDao(): TimeAssetDao
}
