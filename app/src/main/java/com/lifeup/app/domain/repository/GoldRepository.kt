package com.lifeup.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface GoldRepository {
    suspend fun getTotalGoldEarned(): Int
    suspend fun getTotalGoldSpent(): Int
    fun getGoldBalance(): Flow<Int>
    suspend fun spendGold(amount: Int): Boolean
}
