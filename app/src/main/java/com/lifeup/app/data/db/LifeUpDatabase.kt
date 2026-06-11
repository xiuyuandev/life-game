package com.lifeup.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lifeup.app.data.db.dao.ComboDao
import com.lifeup.app.data.db.dao.DailyStateDao
import com.lifeup.app.data.db.dao.ItemDao
import com.lifeup.app.data.db.dao.SkillDao
import com.lifeup.app.data.db.dao.TimeRecordDao
import com.lifeup.app.data.db.dao.TodoDao
import com.lifeup.app.data.db.entity.ComboEntity
import com.lifeup.app.data.db.entity.DailyStateEntity
import com.lifeup.app.data.db.entity.ItemEntity
import com.lifeup.app.data.db.entity.SkillEntity
import com.lifeup.app.data.db.entity.TimeRecordEntity
import com.lifeup.app.data.db.entity.TodoEntity

@Database(
    entities = [
        SkillEntity::class,
        TodoEntity::class,
        TimeRecordEntity::class,
        ComboEntity::class,
        ItemEntity::class,
        DailyStateEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LifeUpDatabase : RoomDatabase() {

    abstract fun skillDao(): SkillDao
    abstract fun todoDao(): TodoDao
    abstract fun timeRecordDao(): TimeRecordDao
    abstract fun comboDao(): ComboDao
    abstract fun itemDao(): ItemDao
    abstract fun dailyStateDao(): DailyStateDao

    companion object {
        @Volatile
        private var INSTANCE: LifeUpDatabase? = null

        fun getDatabase(context: Context): LifeUpDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeUpDatabase::class.java,
                    "lifeup_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
