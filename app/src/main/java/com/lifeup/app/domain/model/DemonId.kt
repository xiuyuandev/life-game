package com.lifeup.app.domain.model

/**
 * The 12 built-in inner demons. Each one represents a real-world
 * psychological or behavioral obstacle to focus.
 *
 * The id is the stable primary key used across the database and UI.
 */
enum class DemonId(val key: String) {
    PROCRASTINATION_SERPENT("PROCRASTINATION_SERPENT"),
    LAST_MINUTE_SHADOW("LAST_MINUTE_SHADOW"),
    EXCUSE_FOG("EXCUSE_FOG"),
    PHONE_HORDE("PHONE_HORDE"),
    NOTIFICATION_TSUNAMI("NOTIFICATION_TSUNAMI"),
    VIDEO_QUAGMIRE("VIDEO_QUAGMIRE"),
    NIGHT_OWL_SHADOW("NIGHT_OWL_SHADOW"),
    MORNING_FOG("MORNING_FOG"),
    COUCH_GRAVITY("COUCH_GRAVITY"),
    PERFECTION_DRAGON("PERFECTION_DRAGON"),
    ANXIETY_CLOUD("ANXIETY_CLOUD"),
    NIHILISM_SHADOW("NIHILISM_SHADOW"),
    /** Final boss: only available after all 12 are defeated. */
    MIRROR_OF_SELF("MIRROR_OF_SELF");

    companion object {
        fun fromKey(key: String): DemonId? = values().firstOrNull { it.key == key }
    }
}
