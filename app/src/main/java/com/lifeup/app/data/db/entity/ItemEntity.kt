package com.lifeup.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = SkillEntity::class,
            parentColumns = ["id"],
            childColumns = ["skill_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("skill_id")]
)
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    @ColumnInfo(name = "skill_id")
    val skillId: Long? = null,

    @ColumnInfo(name = "item_tier")
    val itemTier: String = "COMMON",

    @ColumnInfo(name = "attribute_bonus")
    val attributeBonus: Int = 0,

    @ColumnInfo(name = "exp_bonus_contribution")
    val expBonusContribution: Float = 0f,

    val description: String? = null,

    @ColumnInfo(name = "slot_type")
    val slotType: String,

    @ColumnInfo(name = "is_equipped")
    val isEquipped: Boolean = false,

    @ColumnInfo(name = "equipped_slot")
    val equippedSlot: String? = null,

    @ColumnInfo(name = "is_unlocked")
    val isUnlocked: Boolean = false,

    val price: Int = 0,

    @ColumnInfo(name = "custom_icon_key")
    val customIconKey: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
