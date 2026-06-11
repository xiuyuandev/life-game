package com.lifeup.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lifeup.app.data.db.dao.CharacterStateDao
import com.lifeup.app.data.db.dao.ComboDao
import com.lifeup.app.data.db.dao.DailyStateDao
import com.lifeup.app.data.db.dao.ItemDao
import com.lifeup.app.data.db.dao.SkillDao
import com.lifeup.app.data.db.dao.TimeRecordDao
import com.lifeup.app.data.db.dao.TodoDao
import com.lifeup.app.data.db.entity.CharacterStateEntity
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
        DailyStateEntity::class,
        CharacterStateEntity::class,
        AchievementEntity::class
    ],
    version = 6,
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
    abstract fun achievementDao(): AchievementDao
    abstract fun characterStateDao(): CharacterStateDao

    companion object {
        @Volatile
        private var INSTANCE: LifeUpDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_states (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL DEFAULT 0,
                        date TEXT NOT NULL,
                        energy REAL NOT NULL DEFAULT 100.0,
                        energy_cap REAL NOT NULL DEFAULT 100.0,
                        investment_minutes INTEGER NOT NULL DEFAULT 0,
                        consumption_minutes INTEGER NOT NULL DEFAULT 0,
                        streak_count INTEGER NOT NULL DEFAULT 0,
                        is_first_timer_used INTEGER NOT NULL DEFAULT 0,
                        todos_completed INTEGER NOT NULL DEFAULT 0,
                        habits_completed INTEGER NOT NULL DEFAULT 0,
                        gold_earned INTEGER NOT NULL DEFAULT 0,
                        gold_spent INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_daily_states_date ON daily_states(date)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS character_state (
                        id INTEGER PRIMARY KEY NOT NULL DEFAULT 1,
                        totalExp INTEGER NOT NULL DEFAULT 0,
                        characterLevel INTEGER NOT NULL DEFAULT 1,
                        title TEXT NOT NULL DEFAULT '初学者',
                        totalTimeMinutes INTEGER NOT NULL DEFAULT 0,
                        skillCount INTEGER NOT NULL DEFAULT 0,
                        maxSkillLevel INTEGER NOT NULL DEFAULT 1,
                        achievementsUnlocked INTEGER NOT NULL DEFAULT 0,
                        lastUpdated INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS achievements (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        category TEXT NOT NULL,
                        isUnlocked INTEGER NOT NULL DEFAULT 0,
                        unlockedAt INTEGER,
                        progress INTEGER NOT NULL DEFAULT 0,
                        target INTEGER NOT NULL DEFAULT 1
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS character_state (
                        id INTEGER PRIMARY KEY NOT NULL DEFAULT 1,
                        totalExp INTEGER NOT NULL DEFAULT 0,
                        characterLevel INTEGER NOT NULL DEFAULT 1,
                        title TEXT NOT NULL DEFAULT '初学者',
                        totalTimeMinutes INTEGER NOT NULL DEFAULT 0,
                        skillCount INTEGER NOT NULL DEFAULT 0,
                        maxSkillLevel INTEGER NOT NULL DEFAULT 1,
                        achievementsUnlocked INTEGER NOT NULL DEFAULT 0,
                        lastUpdated INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add goldEarned and goldSpent columns to daily_states if they don't exist
                val cursor = db.query("PRAGMA table_info(daily_states)")
                val columnNames = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    columnNames.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
                }
                cursor.close()

                if (!columnNames.contains("gold_earned")) {
                    db.execSQL("ALTER TABLE daily_states ADD COLUMN gold_earned INTEGER NOT NULL DEFAULT 0")
                }
                if (!columnNames.contains("gold_spent")) {
                    db.execSQL("ALTER TABLE daily_states ADD COLUMN gold_spent INTEGER NOT NULL DEFAULT 0")
                }
            }
        }

        fun getDatabase(context: Context): LifeUpDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeUpDatabase::class.java,
                    "lifeup_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
