package com.lifeup.app.domain.repository

import com.lifeup.app.domain.model.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {

    fun getEquippedItems(): Flow<List<Item>>

    fun getUnlockedItems(): Flow<List<Item>>

    suspend fun insertItem(item: Item): Long

    suspend fun updateItem(item: Item)
}
