package com.lifeup.app.domain.repository

import com.lifeup.app.domain.model.Combo
import kotlinx.coroutines.flow.Flow

interface ComboRepository {

    fun getActiveCombos(): Flow<List<Combo>>

    suspend fun insertCombo(combo: Combo): Long

    suspend fun updateCombo(combo: Combo)

    suspend fun deleteCombo(combo: Combo)
}
