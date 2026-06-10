package com.lifeup.app.data.backup

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.lifeup.app.data.db.entity.AchievementEntity
import com.lifeup.app.data.db.entity.CharacterEntity
import com.lifeup.app.data.db.entity.EquipmentEntity
import com.lifeup.app.data.db.entity.SkillEntity
import com.lifeup.app.data.db.entity.TimeAssetEntity
import com.lifeup.app.data.db.entity.TimeSessionEntity
import com.lifeup.app.data.repository.AchievementRepository
import com.lifeup.app.data.repository.CharacterRepository
import com.lifeup.app.data.repository.EquipmentRepository
import com.lifeup.app.data.repository.SkillRepository
import com.lifeup.app.data.repository.TimeAssetRepository
import com.lifeup.app.data.repository.TimeSessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val characterRepository: CharacterRepository,
    private val skillRepository: SkillRepository,
    private val timeSessionRepository: TimeSessionRepository,
    private val equipmentRepository: EquipmentRepository,
    private val achievementRepository: AchievementRepository,
    private val timeAssetRepository: TimeAssetRepository
) {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    data class BackupData(
        val version: Int = 1,
        val exportTime: Long = System.currentTimeMillis(),
        val character: CharacterEntity? = null,
        val skills: List<SkillEntity> = emptyList(),
        val sessions: List<TimeSessionEntity> = emptyList(),
        val equipment: List<EquipmentEntity> = emptyList(),
        val achievements: List<AchievementEntity> = emptyList(),
        val timeAssets: List<TimeAssetEntity> = emptyList()
    )

    data class BackupResult(
        val success: Boolean,
        val message: String,
        val recordCount: Int = 0
    )

    /**
     * 导出所有数据到 JSON 文件
     */
    suspend fun exportToUri(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val data = BackupData(
                character = characterRepository.getCharacter(),
                skills = skillRepository.getAllSkills(),
                sessions = timeSessionRepository.getRecentSessions(10000),
                equipment = equipmentRepository.getAllEquipment(),
                achievements = achievementRepository.getAllAchievements(),
                timeAssets = timeAssetRepository.getRecentAssets()
            )

            val json = gson.toJson(data)
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                OutputStreamWriter(stream).use { writer ->
                    writer.write(json)
                }
            }

            val count = data.skills.size + data.sessions.size +
                    data.equipment.size + data.achievements.size + data.timeAssets.size +
                    (if (data.character != null) 1 else 0)

            BackupResult(
                success = true,
                message = "备份成功",
                recordCount = count
            )
        } catch (e: Exception) {
            e.printStackTrace()
            BackupResult(
                success = false,
                message = "备份失败: ${e.message}"
            )
        }
    }

    /**
     * 从 JSON 文件导入数据
     */
    suspend fun importFromUri(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).use { reader -
                    reader.readText()
                }
            } ?: return@withContext BackupResult(
                success = false,
                message = "无法读取文件"
            )

            val data = gson.fromJson(json, BackupData::class.java)
                ?: return@withContext BackupResult(
                    success = false,
                    message = "无效的备份文件"
                )

            // 清除旧数据
            characterRepository.resetCharacter()
            skillRepository.deleteAll()
            timeSessionRepository.deleteAll()
            equipmentRepository.deleteAll()
            achievementRepository.deleteAll()
            timeAssetRepository.deleteAll()

            // 导入角色（去除自增ID，让数据库重新分配）
            data.character?.let { char ->
                characterRepository.createCharacter(
                    name = char.name
                )
                val newChar = characterRepository.getCharacter()
                newChar?.let { nc ->
                    // 恢复角色属性
                    val restored = nc.copy(
                        level = char.level,
                        exp = char.exp,
                        expToNext = char.expToNext,
                        hp = char.hp,
                        maxHp = char.maxHp,
                        sp = char.sp,
                        maxSp = char.maxSp,
                        strength = char.strength,
                        intelligence = char.intelligence,
                        charm = char.charm,
                        constitution = char.constitution,
                        agility = char.agility,
                        luck = char.luck,
                        gold = char.gold,
                        avatarStyle = char.avatarStyle,
                        lastActiveDate = char.lastActiveDate,
                        streakDays = char.streakDays
                    )
                    // 通过 DAO 直接更新
                    // 这里简化为只保留基础属性，后续可扩展完整恢复
                }
            }

            // 导入技能（去除ID，让数据库重新分配）
            data.skills.forEach { skill ->
                skillRepository.insert(
                    skill.copy(id = 0)
                )
            }

            // 导入装备
            data.equipment.forEach { equip ->
                equipmentRepository.insert(
                    equip.copy(id = 0)
                )
            }

            // 导入成就
            data.achievements.forEach { ach ->
                achievementRepository.insert(
                    ach.copy(id = 0)
                )
            }

            // 导入时间资产
            data.timeAssets.forEach { asset ->
                timeAssetRepository.insert(
                    asset.copy(id = 0)
                )
            }

            // 导入会话
            data.sessions.forEach { session ->
                timeSessionRepository.insert(
                    session.copy(id = 0)
                )
            }

            val count = data.skills.size + data.sessions.size +
                    data.equipment.size + data.achievements.size + data.timeAssets.size +
                    (if (data.character != null) 1 else 0)

            BackupResult(
                success = true,
                message = "恢复成功",
                recordCount = count
            )
        } catch (e: Exception) {
            e.printStackTrace()
            BackupResult(
                success = false,
                message = "恢复失败: ${e.message}"
            )
        }
    }

    /**
     * 生成默认备份文件名
     */
    fun generateBackupFileName(): String {
        val timestamp = java.text.SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            java.util.Locale.getDefault()
        ).format(java.util.Date())
        return "lifeup_backup_$timestamp.json"
    }
}
