package com.lifeup.app.domain.game

import com.lifeup.app.data.preferences.SettingsPrefs
import com.lifeup.app.domain.model.DemonUnlockKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * "真实能力解锁"对外门面。
 *
 * 调用方（计时器、设置页、UI 主题）只通过这一层判断"我能不能用 X"。
 * 后端存储来自 [SettingsPrefs.getUnlockedFeatures]（按击败心魔的顺序累加）。
 */
@Singleton
class FeatureUnlocksService @Inject constructor(
    private val settingsPrefs: SettingsPrefs
) {

    /** 全部已解锁的 feature key 集合。 */
    val unlockedKeys: Flow<Set<String>> = settingsPrefs.getUnlockedFeatures()

    /** 是否开启了"1 秒极速启动"（跳过确认对话框）。 */
    fun observeInstantStartEnabled(): Flow<Boolean> =
        unlockedKeys.map { it.contains(DemonUnlockKey.INSTANT_START.key) }

    /** 是否开启了"夜间仪表盘"自动调暗。 */
    fun observeNightDashboardEnabled(): Flow<Boolean> =
        unlockedKeys.map { it.contains(DemonUnlockKey.NIGHT_MODE_DASHBOARD.key) }

    /** 是否开启了"自定义主题"。 */
    fun observeCustomThemesEnabled(): Flow<Boolean> =
        unlockedKeys.map { it.contains(DemonUnlockKey.CUSTOM_THEMES.key) }

    /** 是否开启了"深度专注盾"（长时段关闭通知）。 */
    fun observeDeepFocusShieldEnabled(): Flow<Boolean> =
        unlockedKeys.map { it.contains(DemonUnlockKey.DEEP_FOCUS_SHIELD.key) }

    /** 同步取值：是否已解锁指定 key。 */
    suspend fun isUnlocked(key: DemonUnlockKey): Boolean =
        unlockedKeys.first().contains(key.key)

    /** 同步取值：通用版本。 */
    suspend fun isFeatureUnlocked(key: String): Boolean =
        unlockedKeys.first().contains(key)
}
