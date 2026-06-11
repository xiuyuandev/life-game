package com.lifeup.app.data.export

import android.content.Context
import com.lifeup.app.data.db.dao.AchievementDao
import com.lifeup.app.data.db.dao.CharacterStateDao
import com.lifeup.app.data.db.dao.ComboDao
import com.lifeup.app.data.db.dao.DailyStateDao
import com.lifeup.app.data.db.dao.ItemDao
import com.lifeup.app.data.db.dao.SkillDao
import com.lifeup.app.data.db.dao.TimeRecordDao
import com.lifeup.app.data.db.dao.TodoDao
import com.lifeup.app.data.db.entity.AchievementEntity
import com.lifeup.app.data.db.entity.CharacterStateEntity
import com.lifeup.app.data.db.entity.ComboEntity
import com.lifeup.app.data.db.entity.DailyStateEntity
import com.lifeup.app.data.db.entity.ItemEntity
import com.lifeup.app.data.db.entity.SkillEntity
import com.lifeup.app.data.db.entity.TimeRecordEntity
import com.lifeup.app.data.db.entity.TodoEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExporter @Inject constructor(
    private val skillDao: SkillDao,
    private val todoDao: TodoDao,
    private val timeRecordDao: TimeRecordDao,
    private val comboDao: ComboDao,
    private val itemDao: ItemDao,
    private val dailyStateDao: DailyStateDao,
    private val achievementDao: AchievementDao,
    private val characterStateDao: CharacterStateDao,
    @ApplicationContext private val context: Context
) {

    suspend fun exportToJson(): File {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        root.put("appVersion", getAppVersion())

        val data = JSONObject()
        data.put("skills", skillDao.getAll().toJsonArray { it.toJson() })
        data.put("todos", todoDao.getAll().toJsonArray { it.toJson() })
        data.put("timeRecords", timeRecordDao.getAll().toJsonArray { it.toJson() })
        data.put("combos", comboDao.getAllList().toJsonArray { it.toJson() })
        data.put("items", itemDao.getAllList().toJsonArray { it.toJson() })
        data.put("dailyStates", dailyStateDao.getAll().toJsonArray { it.toJson() })
        data.put("achievements", try {
            withTimeout(5000) { achievementDao.getAll().first() }.toJsonArray { it.toJson() }
        } catch (_: Exception) { JSONArray() })
        data.put("characterState", try {
            withTimeout(5000) { characterStateDao.getState().first() }?.toJson() ?: JSONObject()
        } catch (_: Exception) { JSONObject() })
        root.put("data", data)

        val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
            android.os.Environment.DIRECTORY_DOWNLOADS
        )
        val lifeUpDir = File(downloadsDir, "LifeUp")
        if (!lifeUpDir.exists()) {
            lifeUpDir.mkdirs()
        }

        val fileName = "lifeup_backup_${System.currentTimeMillis()}.json"
        val file = File(lifeUpDir, fileName)
        file.writeText(root.toString(2))

        return file
    }

    suspend fun importFromJson(file: File): Boolean {
        return try {
            val jsonString = file.readText()
            val root = JSONObject(jsonString)

            val version = root.optInt("version", -1)
            if (version != 1) {
                return false
            }

            val data = root.getJSONObject("data")

            // Clear existing data (order matters due to foreign keys)
            timeRecordDao.deleteAll()
            todoDao.deleteAll()
            comboDao.deleteAll()
            itemDao.deleteAll()
            dailyStateDao.deleteAll()
            achievementDao.deleteAll()
            skillDao.deleteAll()

            // Insert imported data (skills first due to foreign key dependencies)
            data.optJSONArray("skills")?.let { arr ->
                val entities = (0 until arr.length()).map { arr.getJSONObject(it).toSkillEntity() }
                if (entities.isNotEmpty()) skillDao.insertAll(entities)
            }
            data.optJSONArray("todos")?.let { arr ->
                val entities = (0 until arr.length()).map { arr.getJSONObject(it).toTodoEntity() }
                if (entities.isNotEmpty()) todoDao.insertAll(entities)
            }
            data.optJSONArray("timeRecords")?.let { arr ->
                val entities = (0 until arr.length()).map { arr.getJSONObject(it).toTimeRecordEntity() }
                if (entities.isNotEmpty()) timeRecordDao.insertAll(entities)
            }
            data.optJSONArray("combos")?.let { arr ->
                val entities = (0 until arr.length()).map { arr.getJSONObject(it).toComboEntity() }
                if (entities.isNotEmpty()) comboDao.insertAll(entities)
            }
            data.optJSONArray("items")?.let { arr ->
                val entities = (0 until arr.length()).map { arr.getJSONObject(it).toItemEntity() }
                if (entities.isNotEmpty()) itemDao.insertAll(entities)
            }
            data.optJSONArray("dailyStates")?.let { arr ->
                val entities = (0 until arr.length()).map { arr.getJSONObject(it).toDailyStateEntity() }
                if (entities.isNotEmpty()) dailyStateDao.insertAll(entities)
            }
            data.optJSONArray("achievements")?.let { arr ->
                val entities = (0 until arr.length()).map { arr.getJSONObject(it).toAchievementEntity() }
                if (entities.isNotEmpty()) achievementDao.insertAll(entities)
            }
            data.optJSONObject("characterState")?.let { obj ->
                if (obj.has("id")) {
                    characterStateDao.insert(obj.toCharacterStateEntity())
                }
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getTotalRecordsCount(): Int {
        return skillDao.getAll().size +
                todoDao.getAll().size +
                timeRecordDao.getAll().size +
                comboDao.getAllList().size +
                itemDao.getAllList().size +
                dailyStateDao.getAll().size
    }

    fun getDatabaseSize(): Long {
        val dbFile = context.getDatabasePath("lifeup_database")
        return if (dbFile.exists()) dbFile.length() else 0L
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    // --- JSON serialization helpers ---

    private inline fun <T> List<T>.toJsonArray(transform: (T) -> JSONObject): JSONArray {
        val arr = JSONArray()
        for (item in this) {
            arr.put(transform(item))
        }
        return arr
    }

    private fun SkillEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("category", category)
        put("boundAttribute", boundAttribute)
        put("totalMinutes", totalMinutes)
        put("level", level)
        put("masteryStars", masteryStars)
        put("customThresholds", customThresholds)
        put("iconKey", iconKey)
        put("color", color)
        put("status", status)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
        put("sortOrder", sortOrder)
        put("displayInShowcase", displayInShowcase)
    }

    private fun JSONObject.toSkillEntity() = SkillEntity(
        id = optLong("id", 0),
        name = getString("name"),
        category = getString("category"),
        boundAttribute = getString("boundAttribute"),
        totalMinutes = optLong("totalMinutes", 0L),
        level = optInt("level", 1),
        masteryStars = optInt("masteryStars", 0),
        customThresholds = optString("customThresholds", "{}"),
        iconKey = optString("iconKey", null),
        color = optString("color", null),
        status = optString("status", "ACTIVE"),
        createdAt = optLong("createdAt", System.currentTimeMillis()),
        updatedAt = optLong("updatedAt", System.currentTimeMillis()),
        sortOrder = optInt("sortOrder", 0),
        displayInShowcase = optBoolean("displayInShowcase", true)
    )

    private fun TodoEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("isHabit", isHabit)
        put("priority", priority)
        put("linkedSkillId", linkedSkillId)
        put("isCompleted", isCompleted)
        put("completedAt", completedAt)
        put("createdAt", createdAt)
        put("date", date)
        put("sortOrder", sortOrder)
    }

    private fun JSONObject.toTodoEntity() = TodoEntity(
        id = optLong("id", 0),
        title = getString("title"),
        isHabit = optBoolean("isHabit", false),
        priority = optString("priority", "NONE"),
        linkedSkillId = if (has("linkedSkillId") && !isNull("linkedSkillId")) getLong("linkedSkillId") else null,
        isCompleted = optBoolean("isCompleted", false),
        completedAt = if (has("completedAt") && !isNull("completedAt")) getLong("completedAt") else null,
        createdAt = optLong("createdAt", System.currentTimeMillis()),
        date = getString("date"),
        sortOrder = optInt("sortOrder", 0)
    )

    private fun TimeRecordEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("skillId", skillId)
        put("startTime", startTime)
        put("endTime", endTime)
        put("durationMinutes", durationMinutes)
        put("recordType", recordType)
        put("focusType", focusType)
        put("note", note)
        put("createdAt", createdAt)
        put("isLocked", isLocked)
    }

    private fun JSONObject.toTimeRecordEntity() = TimeRecordEntity(
        id = optLong("id", 0),
        skillId = getLong("skillId"),
        startTime = getLong("startTime"),
        endTime = getLong("endTime"),
        durationMinutes = optInt("durationMinutes", 0),
        recordType = optString("recordType", "INVESTMENT"),
        focusType = optString("focusType", "FOCUSED"),
        note = optString("note", null),
        createdAt = optLong("createdAt", System.currentTimeMillis()),
        isLocked = optBoolean("isLocked", false)
    )

    private fun ComboEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("primarySkillId", primarySkillId)
        put("secondarySkillId", secondarySkillId)
        put("requiredLevel", requiredLevel)
        put("expBonus", expBonus.toDouble())
        put("suggestion", suggestion)
        put("connectionColor", connectionColor)
        put("isActive", isActive)
        put("createdAt", createdAt)
    }

    private fun JSONObject.toComboEntity() = ComboEntity(
        id = optLong("id", 0),
        name = getString("name"),
        primarySkillId = getLong("primarySkillId"),
        secondarySkillId = getLong("secondarySkillId"),
        requiredLevel = optInt("requiredLevel", 2),
        expBonus = optDouble("expBonus", 1.05).toFloat(),
        suggestion = optString("suggestion", null),
        connectionColor = optString("connectionColor", null),
        isActive = optBoolean("isActive", true),
        createdAt = optLong("createdAt", System.currentTimeMillis())
    )

    private fun ItemEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("skillId", skillId)
        put("itemTier", itemTier)
        put("attributeBonus", attributeBonus)
        put("expBonusContribution", expBonusContribution.toDouble())
        put("description", description)
        put("slotType", slotType)
        put("isEquipped", isEquipped)
        put("equippedSlot", equippedSlot)
        put("isUnlocked", isUnlocked)
        put("price", price)
        put("customIconKey", customIconKey)
        put("createdAt", createdAt)
    }

    private fun JSONObject.toItemEntity() = ItemEntity(
        id = optLong("id", 0),
        name = getString("name"),
        skillId = getLong("skillId"),
        itemTier = optString("itemTier", "COMMON"),
        attributeBonus = optInt("attributeBonus", 0),
        expBonusContribution = optDouble("expBonusContribution", 0.0).toFloat(),
        description = optString("description", null),
        slotType = getString("slotType"),
        isEquipped = optBoolean("isEquipped", false),
        equippedSlot = optString("equippedSlot", null),
        isUnlocked = optBoolean("isUnlocked", false),
        price = optInt("price", 0),
        customIconKey = optString("customIconKey", null),
        createdAt = optLong("createdAt", System.currentTimeMillis())
    )

    private fun DailyStateEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("date", date)
        put("energy", energy.toDouble())
        put("energyCap", energyCap.toDouble())
        put("investmentMinutes", investmentMinutes)
        put("consumptionMinutes", consumptionMinutes)
        put("streakCount", streakCount)
        put("isFirstTimerUsed", isFirstTimerUsed)
        put("todosCompleted", todosCompleted)
        put("habitsCompleted", habitsCompleted)
        put("goldEarned", goldEarned)
        put("goldSpent", goldSpent)
    }

    private fun JSONObject.toDailyStateEntity() = DailyStateEntity(
        id = optLong("id", 0),
        date = getString("date"),
        energy = optDouble("energy", 100.0).toFloat(),
        energyCap = optDouble("energyCap", 100.0).toFloat(),
        investmentMinutes = optInt("investmentMinutes", 0),
        consumptionMinutes = optInt("consumptionMinutes", 0),
        streakCount = optInt("streakCount", 0),
        isFirstTimerUsed = optBoolean("isFirstTimerUsed", false),
        todosCompleted = optInt("todosCompleted", 0),
        habitsCompleted = optInt("habitsCompleted", 0),
        goldEarned = optInt("goldEarned", 0),
        goldSpent = optInt("goldSpent", 0)
    )

    private fun AchievementEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("description", description)
        put("category", category)
        put("isUnlocked", isUnlocked)
        put("unlockedAt", unlockedAt)
        put("progress", progress)
        put("target", target)
    }

    private fun JSONObject.toAchievementEntity() = AchievementEntity(
        id = getString("id"),
        title = getString("title"),
        description = getString("description"),
        category = getString("category"),
        isUnlocked = optBoolean("isUnlocked", false),
        unlockedAt = if (has("unlockedAt") && !isNull("unlockedAt")) getLong("unlockedAt") else null,
        progress = optInt("progress", 0),
        target = optInt("target", 1)
    )

    private fun CharacterStateEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("totalExp", totalExp)
        put("characterLevel", characterLevel)
        put("title", title)
        put("totalTimeMinutes", totalTimeMinutes)
        put("skillCount", skillCount)
        put("maxSkillLevel", maxSkillLevel)
        put("achievementsUnlocked", achievementsUnlocked)
        put("lastUpdated", lastUpdated)
    }

    private fun JSONObject.toCharacterStateEntity() = CharacterStateEntity(
        id = optInt("id", 1),
        totalExp = optLong("totalExp", 0L),
        characterLevel = optInt("characterLevel", 1),
        title = optString("title", "初学者"),
        totalTimeMinutes = optLong("totalTimeMinutes", 0L),
        skillCount = optInt("skillCount", 0),
        maxSkillLevel = optInt("maxSkillLevel", 1),
        achievementsUnlocked = optInt("achievementsUnlocked", 0),
        lastUpdated = optLong("lastUpdated", System.currentTimeMillis())
    )
}
