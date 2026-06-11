package com.lifeup.app.domain.model

/**
 * The 4 chapters demons are organized into. Each chapter contains 3
 * demons and represents a different facet of focus.
 */
enum class DemonChapter(val displayName: String, val description: String) {
    PROCRASTINATION("第一章 拖延", "对'推迟'的恐惧，是它最锋利的獠牙。"),
    DISTRACTION("第二章 分心", "屏幕亮起的瞬间，是它最得意的时刻。"),
    RHYTHM("第三章 作息", "节律一旦失守，它便趁虚而入。"),
    PSYCHE("第四章 心理", "看不见的敌人，往往最为致命。"),
    FINAL("终章 镜像", "当你凝视深渊时，深渊也在凝视你。");

    val demons: List<DemonId>
        get() = when (this) {
            PROCRASTINATION -> listOf(
                DemonId.PROCRASTINATION_SERPENT,
                DemonId.LAST_MINUTE_SHADOW,
                DemonId.EXCUSE_FOG
            )
            DISTRACTION -> listOf(
                DemonId.PHONE_HORDE,
                DemonId.NOTIFICATION_TSUNAMI,
                DemonId.VIDEO_QUAGMIRE
            )
            RHYTHM -> listOf(
                DemonId.NIGHT_OWL_SHADOW,
                DemonId.MORNING_FOG,
                DemonId.COUCH_GRAVITY
            )
            PSYCHE -> listOf(
                DemonId.PERFECTION_DRAGON,
                DemonId.ANXIETY_CLOUD,
                DemonId.NIHILISM_SHADOW
            )
            FINAL -> listOf(DemonId.MIRROR_OF_SELF)
        }
}
