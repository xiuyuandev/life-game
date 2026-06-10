package com.lifeup.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipment")
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val icon: String = "🛡️",
    val slot: String,
    val effectType: String,
    val effectValue: Double,
    val effectTarget: String = "",
    val maxDurability: Int = 30,
    val currentDurability: Int = 30,
    val maintenanceActivity: String = "",
    val source: String = "shop",
    val sourceId: Long? = null,
    val price: Long = 0,
    val owned: Boolean = false,
    val active: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
