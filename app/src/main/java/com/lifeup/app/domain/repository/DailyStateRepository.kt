package com.lifeup.app.domain.repository

import com.lifeup.app.domain.model.DailyState
import kotlinx.coroutines.flow.Flow

interface DailyStateRepository {

    fun getStateByDate(date: String): Flow<DailyState?>

    suspend fun insertOrUpdateState(state: DailyState): Long

    suspend fun getLatestStreak(): Int?
}
