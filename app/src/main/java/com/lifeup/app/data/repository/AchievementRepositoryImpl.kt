package com.lifeup.app.data.repository

import com.lifeup.app.data.db.dao.AchievementDao
import com.lifeup.app.data.db.entity.AchievementEntity
import com.lifeup.app.domain.model.Achievement
import com.lifeup.app.domain.model.AchievementCategory
import com.lifeup.app.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepositoryImpl @Inject constructor(
    private val achievementDao: AchievementDao
) : AchievementRepository {

    override fun getAllAchievements(): Flow<List<Achievement>> {
        return achievementDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getUnlockedAchievements(): Flow<List<Achievement>> {
        return achievementDao.getUnlocked().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun unlockAchievement(id: String) {
        achievementDao.unlock(id)
    }

    override suspend fun updateProgress(id: String, progress: Int) {
        achievementDao.updateProgress(id, progress)
    }

    override suspend fun initializeAchievements() {
        val existing = try {
            withTimeout(5000) { achievementDao.getAll().first() }
        } catch (_: Exception) { return }
        if (existing.isNotEmpty()) return

        val defaultAchievements = listOf(
            // Skill achievements
            AchievementEntity("first_skill", "初出茅庐", "创建第一个技能", "SKILL", target = 1),
            AchievementEntity("skill_level_5", "小有成就", "任意技能达到5级", "SKILL", target = 1),
            AchievementEntity("skill_level_10", "登峰造极", "任意技能达到10级", "SKILL", target = 1),
            AchievementEntity("five_skills", "多才多艺", "同时拥有5个技能", "SKILL", target = 5),
            AchievementEntity("master_skill", "大师之路", "一个技能达到大师级（LV5+3星）", "SKILL", target = 1),
            // Streak achievements
            AchievementEntity("first_streak", "坚持不懈", "连续打卡3天", "STREAK", target = 3),
            AchievementEntity("week_streak", "一周战士", "连续打卡7天", "STREAK", target = 7),
            AchievementEntity("month_streak", "月度达人", "连续打卡30天", "STREAK", target = 30),
            // Collection achievements
            AchievementEntity("first_item", "初次装备", "获得第一件道具", "COLLECTION", target = 1),
            AchievementEntity("rare_collector", "稀有收藏家", "拥有3件稀有品质道具", "COLLECTION", target = 3),
            AchievementEntity("legendary_owner", "传说持有者", "拥有1件传说品质道具", "COLLECTION", target = 1),
            // Combo achievements
            AchievementEntity("first_combo", "组合初探", "创建第一个技能组合", "COMBO", target = 1),
            AchievementEntity("combo_master", "组合大师", "同时拥有3个激活的组合", "COMBO", target = 3)
        )
        achievementDao.insertAll(defaultAchievements)
    }

    override suspend fun getUnlockedCount(): Int {
        return achievementDao.getUnlockedCount()
    }

    private fun AchievementEntity.toDomain(): Achievement {
        return Achievement(
            id = id,
            title = title,
            description = description,
            category = AchievementCategory.valueOf(category),
            isUnlocked = isUnlocked,
            unlockedAt = unlockedAt,
            progress = progress,
            target = target
        )
    }
}
