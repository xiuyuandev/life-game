package com.lifeup.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================
// LifeUp — 精致暗色主题配色系统
// ============================================
// 设计灵感：RPG游戏HUD + 赛博朋克 + 玻璃拟态

object PixelColors {
    // ---- 基础背景层 ----
    val DeepSpace = Color(0xFF0B0E17)      // 最深层背景
    val Background = Color(0xFF121521)     // 主背景
    val Surface = Color(0xFF1A1E2E)        // 卡片/面板背景
    val SurfaceElevated = Color(0xFF22263A) // 提升层
    val SurfaceVariant = Color(0xFF2A3047)  // 变体表面

    // ---- 主色调（魔法/力量） ----
    val Primary = Color(0xFFFF6B6B)        // 珊瑚红 — 强调色
    val PrimaryVariant = Color(0xFFFF8E8E)  // 浅珊瑚
    val PrimaryDark = Color(0xFFE84848)     // 深珊瑚
    val PrimaryGlow = Color(0x40FF6B6B)     // 发光色

    // ---- 次色调（智慧/科技） ----
    val Secondary = Color(0xFF6C5CE7)       // 紫罗兰
    val SecondaryVariant = Color(0xFF8B7CF0) // 浅紫罗兰
    val SecondaryGlow = Color(0x406C5CE7)    // 紫发光

    // ---- 第三色（自然/生命） ----
    val Tertiary = Color(0xFF00D4AA)        // 翡翠绿
    val TertiaryVariant = Color(0xFF4CE8C5)  // 浅翡翠
    val TertiaryGlow = Color(0x4000D4AA)     // 绿发光

    // ---- 强调色系统 ----
    val AccentGold = Color(0xFFFFD93D)       // 金币/经验
    val AccentGoldDim = Color(0xFFC9A227)
    val AccentGoldGlow = Color(0x40FFD93D)

    val AccentOrange = Color(0xFFFF8C42)     // 连续天数/警告
    val AccentRed = Color(0xFFFF4757)        // 消耗/错误
    val AccentGreen = Color(0xFF2ED573)      // 成功/投资
    val AccentBlue = Color(0xFF54A0FF)       // 信息/技能
    val AccentCyan = Color(0xFF00D2D3)       // 计时器/活力
    val AccentPurple = Color(0xFFA55EEA)     // 装备/魔法
    val AccentPink = Color(0xFFFF6B9D)       // 魅力/艺术

    // ---- 状态条专用色 ----
    val ExpBar = AccentGold
    val ExpBarTrack = Color(0x30FFD93D)
    val HpBar = Color(0xFFFF4757)
    val HpBarTrack = Color(0x30FF4757)
    val SpBar = AccentBlue
    val SpBarTrack = Color(0x3054A0FF)

    // ---- 文字色 ----
    val TextPrimary = Color(0xFFF0F2F5)      // 主要文字
    val TextSecondary = Color(0xFFB0B5C3)    // 次要文字
    val TextMuted = Color(0xFF6B7280)        // 弱化文字
    val TextDisabled = Color(0xFF4A4F5C)     // 禁用文字

    // ---- 投资/消耗 ----
    val Investment = AccentGreen
    val InvestmentGlow = Color(0x402ED573)
    val Consumption = AccentRed
    val ConsumptionGlow = Color(0x40FF4757)

    // ---- 边框/分割线 ----
    val Border = Color(0x20FFFFFF)           // 微妙边框
    val BorderStrong = Color(0x35FFFFFF)     // 强边框
    val BorderHighlight = AccentGold.copy(alpha = 0.3f)
    val Divider = Color(0x15FFFFFF)

    // ---- 渐变定义 ----
    val GradientPrimary = Brush.horizontalGradient(
        colors = listOf(Primary, PrimaryVariant)
    )
    val GradientExp = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFB700), AccentGold, Color(0xFFFFE066))
    )
    val GradientHp = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF2E63), HpBar, Color(0xFFFF6B81))
    )
    val GradientSurface = Brush.verticalGradient(
        colors = listOf(Surface, Surface.copy(alpha = 0.95f))
    )
    val GradientHero = Brush.verticalGradient(
        colors = listOf(
            Secondary.copy(alpha = 0.15f),
            Primary.copy(alpha = 0.08f),
            Background
        )
    )

    // ---- 装备槽位色 ----
    val SlotHabit = Color(0xFFFF6B6B)
    val SlotTool = Color(0xFF54A0FF)
    val SlotMindset = Color(0xFFA55EEA)
    val SlotEnvironment = Color(0xFF2ED573)

    // ---- 难度/等级色 ----
    val LevelCommon = Color(0xFFB0B5C3)
    val LevelUncommon = AccentGreen
    val LevelRare = AccentBlue
    val LevelEpic = AccentPurple
    val LevelLegendary = AccentGold
}
