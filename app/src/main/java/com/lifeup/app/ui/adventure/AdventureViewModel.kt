package com.lifeup.app.ui.adventure

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.SeedData
import com.lifeup.app.data.db.entity.CharacterEntity
import com.lifeup.app.data.db.entity.SkillEntity
import com.lifeup.app.data.db.entity.TimeSessionEntity
import com.lifeup.app.data.repository.CharacterRepository
import com.lifeup.app.data.repository.EquipmentRepository
import com.lifeup.app.data.repository.SkillRepository
import com.lifeup.app.data.repository.TimeAssetRepository
import com.lifeup.app.data.repository.TimeSessionRepository
import com.lifeup.app.domain.game.GameEngine
import com.lifeup.app.domain.game.GameEvent
import com.lifeup.app.domain.game.SessionResult
import com.lifeup.app.service.TimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AdventureViewModel @Inject constructor(
    application: Application,
    private val characterRepository: CharacterRepository,
    private val skillRepository: SkillRepository,
    private val timeSessionRepository: TimeSessionRepository,
    private val timeAssetRepository: TimeAssetRepository,
    private val equipmentRepository: EquipmentRepository,
    private val gameEngine: GameEngine
) : AndroidViewModel(application) {

    private val _character = MutableStateFlow<CharacterEntity?>(null)
    val character: StateFlow<CharacterEntity?> = _character.asStateFlow()

    private val _skills = MutableStateFlow<List<SkillEntity>>(emptyList())
    val skills: StateFlow<List<SkillEntity>> = _skills.asStateFlow()

    private val _activeSession = MutableStateFlow<TimeSessionEntity?>(null)
    val activeSession: StateFlow<TimeSessionEntity?> = _activeSession.asStateFlow()

    private val _todaySessions = MutableStateFlow<List<TimeSessionEntity>>(emptyList())
    val todaySessions: StateFlow<List<TimeSessionEntity>> = _todaySessions.asStateFlow()

    private val _timeAsset = MutableStateFlow<com.lifeup.app.data.db.entity.TimeAssetEntity?>(null)
    val timeAsset: StateFlow<com.lifeup.app.data.db.entity.TimeAssetEntity?> = _timeAsset.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0L)
    val timerSeconds: StateFlow<Long> = _timerSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _sessionResult = MutableStateFlow<SessionResult?>(null)
    val sessionResult: StateFlow<SessionResult?> = _sessionResult.asStateFlow()

    private val _showLevelUp = MutableStateFlow<Int?>(null)
    val showLevelUp: StateFlow<Int?> = _showLevelUp.asStateFlow()

    private val _activeEquipment = MutableStateFlow<List<com.lifeup.app.data.db.entity.EquipmentEntity>>(emptyList())
    val activeEquipment: StateFlow<List<com.lifeup.app.data.db.entity.EquipmentEntity>> = _activeEquipment.asStateFlow()

    private val _lastSkillName = MutableStateFlow("")
    val lastSkillName: StateFlow<String> = _lastSkillName.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private var timerService: TimerService? = null
    private var timerJob: Job? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.LocalBinder
            timerService = binder.getService()
            serviceBound = true
            observeTimerService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            serviceBound = false
        }
    }

    init {
        loadData()
        bindTimerService()
        observeGameEvents()
    }

    private fun loadData() {
        viewModelScope.launch {
            characterRepository.getCharacterFlow()
                .onEach { _character.value = it }
                .launchIn(this)

            skillRepository.getAllSkillsFlow()
                .onEach { _skills.value = it }
                .launchIn(this)

            val today = LocalDate.now().format(dateFormatter)
            timeSessionRepository.getSessionsByDate(today)
                .onEach { _todaySessions.value = it }
                .launchIn(this)

            timeAssetRepository.getByDateFlow(today)
                .onEach { _timeAsset.value = it }
                .launchIn(this)

            // Check for active session from DB
            val dbSession = timeSessionRepository.getActiveSession()
            _activeSession.value = dbSession

            // Observe active equipment via Flow
            equipmentRepository.getAllEquipmentFlow()
                .onEach { allEquip ->
                    _activeEquipment.value = allEquip.filter { it.active }
                }
                .launchIn(this)
        }
    }

    private fun bindTimerService() {
        val intent = Intent(getApplication(), TimerService::class.java)
        getApplication<Application>().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun observeTimerService() {
        timerService?.let { service ->
            viewModelScope.launch {
                service.isRunning.collect { running ->
                    _isTimerRunning.value = running
                    if (running) {
                        startLocalTimer()
                    } else {
                        timerJob?.cancel()
                    }
                }
            }
            viewModelScope.launch {
                service.elapsedSeconds.collect { seconds ->
                    _timerSeconds.value = seconds
                }
            }
        }
    }

    private fun startLocalTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                timerService?.let { service ->
                    _timerSeconds.value = service.getAccumulatedSeconds()
                }
                delay(1000)
            }
        }
    }

    private fun observeGameEvents() {
        viewModelScope.launch {
            gameEngine.events.collect { event ->
                when (event) {
                    is GameEvent.CharacterLevelUp -> {
                        _showLevelUp.value = event.newLevel
                    }
                    is GameEvent.SkillLevelUp -> {
                        // Skill level up notification could be added here
                    }
                    is GameEvent.SkillUnlocked -> {
                        // Skill unlocked notification could be added here
                    }
                    is GameEvent.AchievementUnlocked -> {
                        // Achievement notification could be added here
                    }
                    is GameEvent.EquipmentBroken -> {
                        // Equipment broken notification could be added here
                    }
                }
            }
        }
    }

    fun startSession(title: String, category: String, skillId: Long?) {
        viewModelScope.launch {
            val isInvestment = SeedData.investmentActivities.contains(category)
            val today = LocalDate.now().format(dateFormatter)

            val session = TimeSessionEntity(
                title = title,
                category = category,
                linkedSkillId = skillId,
                startTime = System.currentTimeMillis(),
                isInvestment = isInvestment,
                date = today
            )

            val id = timeSessionRepository.insert(session)
            _activeSession.value = session.copy(id = id)

            // Start timer service
            TimerService.start(getApplication(), title, category)
        }
    }

    fun pauseTimer() {
        TimerService.pause(getApplication())
    }

    fun resumeTimer() {
        TimerService.resume(getApplication())
    }

    fun stopSession() {
        viewModelScope.launch {
            val session = _activeSession.value ?: return@launch
            val skillName = getSkillById(session.linkedSkillId)?.name ?: session.title
            val endTime = System.currentTimeMillis()
            val durationMinutes = (_timerSeconds.value / 60).coerceAtLeast(1)

            val updatedSession = session.copy(
                endTime = endTime,
                durationMinutes = durationMinutes
            )

            timeSessionRepository.update(updatedSession)

            // Settle the session via GameEngine
            val result = gameEngine.settleSession(updatedSession)
            _sessionResult.value = result
            _lastSkillName.value = skillName

            // Update active session
            _activeSession.value = null
            _timerSeconds.value = 0
            _isTimerRunning.value = false

            TimerService.stop(getApplication())
        }
    }

    fun dismissSessionResult() {
        _sessionResult.value = null
    }

    fun dismissLevelUp() {
        _showLevelUp.value = null
    }

    fun getSkillById(id: Long?): SkillEntity? {
        return _skills.value.find { it.id == id }
    }

    override fun onCleared() {
        super.onCleared()
        if (serviceBound) {
            getApplication<Application>().unbindService(serviceConnection)
        }
        timerJob?.cancel()
    }
}
