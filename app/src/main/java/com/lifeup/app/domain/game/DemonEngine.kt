package com.lifeup.app.domain.game

import com.lifeup.app.data.db.SkillCategory
import com.lifeup.app.data.db.entity.DemonPartDamageEntity
import com.lifeup.app.data.db.entity.DemonProgressEntity
import com.lifeup.app.data.preferences.SettingsPrefs
import com.lifeup.app.domain.model.DemonChapter
import com.lifeup.app.domain.model.DemonId
import com.lifeup.app.domain.model.DemonTemplate
import com.lifeup.app.domain.model.DemonTypeMultiplier
import com.lifeup.app.domain.model.DemonUnlockKey
import com.lifeup.app.domain.model.InnerDemon
import com.lifeup.app.domain.repository.DemonRepository
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 战斗引擎：把"玩家在一次计时中投资的时间"折算成"对心魔的伤害"。
 *
 * 公式：
 *   damage = 时长(分钟) × 等级系数 × 分类克制系数 × 部位当日激活系数
 *
 * - 时长系数：基础 1.0；超出推荐时长 1.5 倍的部分按 0.7 折扣。
 * - 分类克制：弱 1.6 / 中 1.0 / 抗 0.55。
 * - 部位当日：今天星期 N，仅当日对应部位受 1.0；其它 6 个部位受 0.15 干扰伤害（不归零）。
 *
 * 击败条件：所有 7 个部位 HP 全部归零。
 */
@Singleton
class DemonEngine @Inject constructor(
    private val demonRepository: DemonRepository,
    private val settingsPrefs: SettingsPrefs
) {

    // ---------- 公式 ----------

    /**
     * 计算单次攻击对心魔的伤害（同时返回命中详情）。
     *
     * @param demon 静态模板
     * @param focusMinutes 本次专注的实际时长（分钟）
     * @param skillCategory 训练该技能时所属的分类
     * @param dayOfWeek 今日是星期几
     */
    fun calculateDamage(
        demon: InnerDemon,
        focusMinutes: Int,
        skillCategory: SkillCategory?,
        dayOfWeek: Int
    ): DamageBreakdown {
        if (focusMinutes < demon.minFocusMinutes) {
            return DamageBreakdown(
                demon = demon,
                dayOfWeek = dayOfWeek,
                focusMinutes = focusMinutes,
                baseDamage = 0,
                typeMultiplier = DemonTypeMultiplier.MULTIPLIER_NEUTRAL,
                partMultiplier = 0f,
                totalDamage = 0,
                hitPart = false,
                reason = "专注时长不足（需要 ≥ ${demon.minFocusMinutes} 分钟）"
            )
        }

        val recommended = demon.recommendedCategories.firstOrNull()
        val base = focusMinutes.toFloat()
        val levelMult = difficultyMultiplier(demon.difficulty)
        val typeMult = categoryMultiplier(skillCategory, demon, recommended)
        val (partMult, hitPart) = partMultiplier(demon, dayOfWeek)
        val total = (base * levelMult * typeMult * partMult).toInt().coerceAtLeast(0)
        return DamageBreakdown(
            demon = demon,
            dayOfWeek = dayOfWeek,
            focusMinutes = focusMinutes,
            baseDamage = base.toInt(),
            typeMultiplier = typeMult,
            partMultiplier = partMult,
            totalDamage = total,
            hitPart = hitPart,
            reason = if (hitPart) null else "今日对应部位未激活，仅造成干扰伤害"
        )
    }

    /**
     * 难度系数：1 星 1.0，2 星 1.15，3 星 1.3，4 星 1.5，5 星 1.75。
     */
    fun difficultyMultiplier(difficulty: Int): Float = when (difficulty) {
        1 -> 1.0f
        2 -> 1.15f
        3 -> 1.3f
        4 -> 1.5f
        else -> 1.75f
    }

    /**
     * 分类克制系数：弱 1.6 / 中 1.0 / 抗 0.55。
     * 推荐分类视作"弱"的强化版本。
     */
    fun categoryMultiplier(
        skillCategory: SkillCategory?,
        demon: InnerDemon,
        recommended: SkillCategory?
    ): Float {
        if (skillCategory == null) return DemonTypeMultiplier.MULTIPLIER_NEUTRAL
        if (recommended != null && skillCategory == recommended) return 1.8f
        if (skillCategory in demon.weakCategories) return DemonTypeMultiplier.MULTIPLIER_WEAK
        if (skillCategory in demon.resistCategories) return DemonTypeMultiplier.MULTIPLIER_RESIST
        return DemonTypeMultiplier.MULTIPLIER_NEUTRAL
    }

    /**
     * 部位系数：今日对应部位 1.0；其他 6 个部位 0.15。
     */
    fun partMultiplier(demon: InnerDemon, dayOfWeek: Int): Pair<Float, Boolean> {
        val normalized = ((dayOfWeek - 1) % 7) + 1
        return if (normalized in 1..7) 1.0f to true else 0.15f to false
    }

    // ---------- 写入 ----------

    /**
     * 玩家完成一次专注后调用：
     *   - 找到/创建该心魔的进度行；
     *   - 对应当日部位扣血（不会重置其它部位）；
     *   - 如果所有部位归零，标记为已击败并写入解锁。
     */
    suspend fun applySessionResult(
        demon: InnerDemon,
        focusMinutes: Int,
        skillCategory: SkillCategory?,
        date: LocalDate = LocalDate.now(),
        dayOfWeek: Int = date.dayOfWeek.value
    ): DemonBattleOutcome {
        val breakdown = calculateDamage(demon, focusMinutes, skillCategory, dayOfWeek)
        val timestamp = System.currentTimeMillis()

        // 1) 读取 / 初始化进度
        var progress = demonRepository.getProgressOnce(demon.id)
        if (progress == null) {
            progress = DemonProgressEntity(
                demonId = demon.id.key,
                totalHp = demon.totalHp
            )
            demonRepository.upsertProgress(progress)
            // 初始化 7 个部位
            val initialParts = demon.basePartHps.mapIndexed { idx, hp ->
                DemonPartDamageEntity(
                    demonId = demon.id.key,
                    dayOfWeek = idx + 1,
                    maxHp = hp,
                    currentHp = hp
                )
            }
            demonRepository.upsertParts(initialParts)
        }
        if (progress.isDefeated) {
            return DemonBattleOutcome.AlreadyDefeated(demon, breakdown)
        }

        // 2) 对当日部位造成伤害
        if (breakdown.hitPart && breakdown.totalDamage > 0) {
            demonRepository.getPart(demon.id, dayOfWeek)?.let { existing ->
                demonRepository.upsertPart(
                    existing.copy(
                        currentHp = (existing.currentHp - breakdown.totalDamage).coerceAtLeast(0),
                        totalDamage = existing.totalDamage + breakdown.totalDamage,
                        hitCount = existing.hitCount + 1,
                        lastHitAt = timestamp,
                        isBroken = (existing.currentHp - breakdown.totalDamage) <= 0
                    )
                )
            }
        }

        // 3) 重新计算总 HP = 7 个部位 currentHp 之和
        val parts = mutableListOf<DemonPartDamageEntity>()
        for (d in 1..7) {
            val p = demonRepository.getPart(demon.id, d) ?: continue
            parts += p
        }
        val sumCurrent = parts.sumOf { it.currentHp }
        val fraction = if (demon.totalHp <= 0) 0f else 1f - (sumCurrent.toFloat() / demon.totalHp)

        val newProgress = progress.copy(
            currentHp = sumCurrent,
            progressFraction = fraction.coerceIn(0f, 1f),
            attemptCount = progress.attemptCount + 1,
            lastUpdated = timestamp
        )
        demonRepository.upsertProgress(newProgress)

        // 4) 检测是否击败
        val allBroken = parts.size == 7 && parts.all { it.isBroken }
        return if (allBroken) {
            val finalProgress = newProgress.copy(
                isDefeated = true,
                defeatedAt = timestamp,
                progressFraction = 1f,
                lastUpdated = timestamp
            )
            demonRepository.upsertProgress(finalProgress)
            settingsPrefs.unlockFeature(demon.unlock)
            DemonBattleOutcome.Defeated(demon, finalProgress, breakdown, demon.unlock)
        } else {
            DemonBattleOutcome.Damaged(demon, newProgress, breakdown)
        }
    }

    /**
     * 初始化 12 只内置心魔（首次进入心魔页面时调用）。
     * 已存在的不覆盖。
     */
    suspend fun seedStandardDemonsIfNeeded() {
        for (demon in DemonTemplate.STANDARD) {
            val existing = demonRepository.getProgressOnce(demon.id)
            if (existing == null) {
                val progress = DemonProgressEntity(
                    demonId = demon.id.key,
                    totalHp = demon.totalHp
                )
                demonRepository.upsertProgress(progress)
                val parts = demon.basePartHps.mapIndexed { idx, hp ->
                    DemonPartDamageEntity(
                        demonId = demon.id.key,
                        dayOfWeek = idx + 1,
                        maxHp = hp,
                        currentHp = hp
                    )
                }
                demonRepository.upsertParts(parts)
            }
        }
    }

    /**
     * 当 12 只标准心魔全部击败后，初始化镜像。
     */
    suspend fun ensureMirrorIfUnlocked() {
        val defeated = demonRepository.getDefeatedCount()
        if (defeated < 12) return
        val mirror = DemonTemplate.MIRROR_OF_SELF
        val existing = demonRepository.getProgressOnce(mirror.id)
        if (existing == null) {
            demonRepository.upsertProgress(
                DemonProgressEntity(
                    demonId = mirror.id.key,
                    totalHp = mirror.totalHp
                )
            )
            val parts = mirror.basePartHps.mapIndexed { idx, hp ->
                DemonPartDamageEntity(
                    demonId = mirror.id.key,
                    dayOfWeek = idx + 1,
                    maxHp = hp,
                    currentHp = hp
                )
            }
            demonRepository.upsertParts(parts)
        }
    }

    /**
     * 计算"周开始日期"（周一）。用于战记 / 击杀时间线。
     */
    fun startOfWeek(date: LocalDate = LocalDate.now()): LocalDate {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        return date.with(java.time.temporal.TemporalAdjusters.previousOrSame(firstDayOfWeek))
    }

    /**
     * 章节完成度。
     */
    suspend fun chapterProgress(chapter: DemonChapter): Pair<Int, Int> {
        val defeated = demonRepository.observeDefeated().first()
        val defeatedIds = defeated.map { DemonId.fromKey(it.demonId) }.filterNotNull().toSet()
        val list = chapter.demons
        val done = list.count { it in defeatedIds }
        return done to list.size
    }
}

/** 单次伤害的拆分，用于 UI 展示 */
data class DamageBreakdown(
    val demon: InnerDemon,
    val dayOfWeek: Int,
    val focusMinutes: Int,
    val baseDamage: Int,
    val typeMultiplier: Float,
    val partMultiplier: Float,
    val totalDamage: Int,
    val hitPart: Boolean,
    val reason: String?
) {
    val dayLabel: String get() = DayOfWeek.of(((dayOfWeek - 1) % 7) + 1).getDisplayName(java.time.format.TextStyle.SHORT, Locale.SIMPLIFIED_CHINESE)
}

/** 单次攻击的结果 */
sealed class DemonBattleOutcome {
    abstract val demon: InnerDemon
    abstract val breakdown: DamageBreakdown

    data class Damaged(
        override val demon: InnerDemon,
        val progress: DemonProgressEntity,
        override val breakdown: DamageBreakdown
    ) : DemonBattleOutcome()

    data class Defeated(
        override val demon: InnerDemon,
        val progress: DemonProgressEntity,
        override val breakdown: DamageBreakdown,
        val unlockedFeature: DemonUnlockKey
    ) : DemonBattleOutcome()

    data class AlreadyDefeated(
        override val demon: InnerDemon,
        override val breakdown: DamageBreakdown
    ) : DemonBattleOutcome()
}
