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
    version = 8,
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

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add last_updated column to daily_states for energy regeneration tracking
                val cursor = db.query("PRAGMA table_info(daily_states)")
                val columnNames = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    columnNames.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
                }
                cursor.close()

                if (!columnNames.contains("last_updated")) {
                    db.execSQL("ALTER TABLE daily_states ADD COLUMN last_updated INTEGER NOT NULL DEFAULT 0")
                }
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Rebuild items table to make skill_id nullable (for shop items that aren't bound to a skill)
                // SQLite doesn't support ALTER COLUMN, so we need to:
                // 1. Create a new table with nullable skill_id
                // 2. Copy data from old table
                // 3. Drop old table
                // 4. Rename new table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS items_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        skill_id INTEGER,
                        item_tier TEXT NOT NULL,
                        attribute_bonus INTEGER NOT NULL,
                        exp_bonus_contribution REAL NOT NULL,
                        description TEXT,
                        slot_type TEXT NOT NULL,
                        is_equipped INTEGER NOT NULL,
                        equipped_slot TEXT,
                        is_unlocked INTEGER NOT NULL,
                        price INTEGER NOT NULL,
                        custom_icon_key TEXT,
                        created_at INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO items_new (id, name, skill_id, item_tier, attribute_bonus, exp_bonus_contribution, description, slot_type, is_equipped, equipped_slot, is_unlocked, price, custom_icon_key, created_at)
                    SELECT id, name, skill_id, item_tier, attribute_bonus, exp_bonus_contribution, description, slot_type, is_equipped, equipped_slot, is_unlocked, price, custom_icon_key, created_at FROM items
                """.trimIndent())
                db.execSQL("DROP TABLE items")
                db.execSQL("ALTER TABLE items_new RENAME TO items")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_items_skill_id ON items(skill_id)")
            }
        }

        fun getDatabase(context: Context): LifeUpDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeUpDatabase::class.java,
                    "lifeup_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
