package com.lifeup.app.data.db

import androidx.room.TypeConverter
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun fromJsonMap(value: String): Map<String, Float> {
        if (value.isEmpty() || value == "{}") return emptyMap()
        val json = JSONObject(value)
        val map = mutableMapOf<String, Float>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = json.getDouble(key).toFloat()
        }
        return map
    }

    @TypeConverter
    fun toJsonMap(map: Map<String, Float>): String {
        if (map.isEmpty()) return "{}"
        val json = JSONObject()
        for ((key, value) in map) {
            json.put(key, value.toDouble())
        }
        return json.toString()
    }

    @TypeConverter
    fun fromSkillCategory(value: String): SkillCategory = SkillCategory.valueOf(value)

    @TypeConverter
    fun toSkillCategory(category: SkillCategory): String = category.name

    @TypeConverter
    fun fromBoundAttribute(value: String): BoundAttribute = BoundAttribute.valueOf(value)

    @TypeConverter
    fun toBoundAttribute(attribute: BoundAttribute): String = attribute.name

    @TypeConverter
    fun fromSkillStatus(value: String): SkillStatus = SkillStatus.valueOf(value)

    @TypeConverter
    fun toSkillStatus(status: SkillStatus): String = status.name

    @TypeConverter
    fun fromPriority(value: String): Priority = Priority.valueOf(value)

    @TypeConverter
    fun toPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun fromRecordType(value: String): RecordType = RecordType.valueOf(value)

    @TypeConverter
    fun toRecordType(recordType: RecordType): String = recordType.name

    @TypeConverter
    fun fromFocusType(value: String): FocusType = FocusType.valueOf(value)

    @TypeConverter
    fun toFocusType(focusType: FocusType): String = focusType.name

    @TypeConverter
    fun fromItemTier(value: String): ItemTier = ItemTier.valueOf(value)

    @TypeConverter
    fun toItemTier(itemTier: ItemTier): String = itemTier.name

    @TypeConverter
    fun fromSlotType(value: String): SlotType = SlotType.valueOf(value)

    @TypeConverter
    fun toSlotType(slotType: SlotType): String = slotType.name
}

enum class SkillCategory {
    LIVELIHOOD, SOCIAL, LANGUAGE, LIFE, PHYSICAL, MENTAL, ART
}

enum class BoundAttribute {
    STRENGTH, INTELLIGENCE, CHARISMA, PERCEPTION, CREATIVITY, WILLPOWER, DEXTERITY
}

enum class SkillStatus {
    ACTIVE, PAUSED, ARCHIVED
}

enum class Priority {
    HIGH, MEDIUM, LOW, NONE
}

enum class RecordType {
    INVESTMENT, CONSUMPTION
}

enum class FocusType {
    FOCUSED, UNFOCUSED
}

enum class ItemTier {
    COMMON, FINE, RARE, EPIC, LEGENDARY
}

enum class SlotType {
    HEAD, BODY, HANDS, FEET, ACCESSORY
}
