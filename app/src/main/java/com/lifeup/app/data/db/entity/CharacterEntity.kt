package com.lifeup.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "冒险者",
    val level: Int = 1,
    val exp: Long = 0,
    val expToNext: Long = 140,       // 100 * level * 1.4
    val hp: Int = 100,               // 生命值 = 活力
    val maxHp: Int = 100,
    val sp: Int = 10,                // 精力点
    val maxSp: Int = 10,
    val strength: Int = 1,           // 体质
    val intelligence: Int = 1,       // 智力
    val charm: Int = 1,              // 魅力
    val constitution: Int = 1,       // 耐力
    val agility: Int = 1,            // 敏捷
    val luck: Int = 1,               // 运气
    val gold: Long = 0,              // 金币
    val avatarStyle: String = "default",
    val lastActiveDate: String = "",  // yyyy-MM-dd
    val streakDays: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
