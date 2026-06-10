package com.lifeup.app.domain.game

import com.lifeup.app.data.repository.CharacterRepository
import com.lifeup.app.data.repository.EquipmentRepository
import com.lifeup.app.data.repository.TimeAssetRepository
import com.lifeup.app.data.repository.TimeSessionRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyReset @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val equipmentRepository: EquipmentRepository,
    private val timeAssetRepository: TimeAssetRepository,
    private val timeSessionRepository: TimeSessionRepository,
    private val achievementChecker: AchievementChecker
) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun performDailyReset() {
        val today = LocalDate.now().format(dateFormatter)
        val yesterday = LocalDate.now().minusDays(1).format(dateFormatter)

        val character = characterRepository.getCharacter() ?: return

        // 1. 检查昨日是否有投资性时间
        val yesterdaySessions = timeSessionRepository.getSessionsByDateSync(yesterday)
        val hadInvestmentYesterday = yesterdaySessions.any { it.isInvestment }

        // 2. 更新连续天数
        val newStreak = if (hadInvestmentYesterday) {
            character.streakDays + 1
        } else {
            0
        }
        characterRepository.updateStreak(newStreak, today)

        // 3. 恢复SP
        characterRepository.updateHpSp(character.maxHp, character.maxSp)

        // 4. 装备耐久度检查
        val activeEquipment = equipmentRepository.getActiveEquipment()
        for (equip in activeEquipment) {
            val hadMaintenance = yesterdaySessions.any { it.category == equip.maintenanceActivity }
            if (!hadMaintenance) {
                val newDurability = equip.currentDurability - 1
                equipmentRepository.updateDurability(equip.id, newDurability)
                if (newDurability <= 0) {
                    equipmentRepository.unequip(equip.id)
                }
            }
        }

        // 5. 检查成就
        achievementChecker.checkAll()
    }
}
