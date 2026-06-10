package com.lifeup.app.ui.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.entity.TimeAssetEntity
import com.lifeup.app.data.db.entity.TimeSessionEntity
import com.lifeup.app.data.repository.TimeAssetRepository
import com.lifeup.app.data.repository.TimeSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val timeAssetRepository: TimeAssetRepository,
    private val timeSessionRepository: TimeSessionRepository
) : ViewModel() {

    private val _todayAsset = MutableStateFlow<TimeAssetEntity?>(null)
    val todayAsset: StateFlow<TimeAssetEntity?> = _todayAsset.asStateFlow()

    private val _todaySessions = MutableStateFlow<List<TimeSessionEntity>>(emptyList())
    val todaySessions: StateFlow<List<TimeSessionEntity>> = _todaySessions.asStateFlow()

    private val _weeklyAssets = MutableStateFlow<List<TimeAssetEntity>>(emptyList())
    val weeklyAssets: StateFlow<List<TimeAssetEntity>> = _weeklyAssets.asStateFlow()

    private val _viewMode = MutableStateFlow<ViewMode>(ViewMode.DAILY)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        observeDailyData()
        loadWeeklyData()
    }

    private fun observeDailyData() {
        val today = LocalDate.now().format(dateFormatter)
        timeSessionRepository.getSessionsByDate(today)
            .onEach { _todaySessions.value = it }
            .launchIn(viewModelScope)
        timeAssetRepository.getByDateFlow(today)
            .onEach { _todayAsset.value = it }
            .launchIn(viewModelScope)
    }

    private fun loadWeeklyData() {
        val weekStart = LocalDate.now().minusDays(6).format(dateFormatter)
        val today = LocalDate.now().format(dateFormatter)
        timeAssetRepository.getByDateRangeFlow(weekStart, today)
            .onEach { _weeklyAssets.value = it }
            .launchIn(viewModelScope)
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    enum class ViewMode {
        DAILY, WEEKLY, MONTHLY
    }
}
