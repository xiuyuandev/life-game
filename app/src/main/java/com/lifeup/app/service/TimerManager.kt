package com.lifeup.app.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

object TimerManager {

    private var timerService: TimerService? = null
    private var isBound = false

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _currentSkillId = MutableStateFlow(0L)
    val currentSkillId: StateFlow<Long> = _currentSkillId.asStateFlow()

    private val _currentSkillName = MutableStateFlow("")
    val currentSkillName: StateFlow<String> = _currentSkillName.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            isBound = true

            // Sync state from service
            _isRunning.value = timerService!!.isRunning.value
            _elapsedSeconds.value = timerService!!.elapsedSeconds.value
            _currentSkillId.value = timerService!!.currentSkillId.value
            _currentSkillName.value = timerService!!.currentSkillName.value
            _isPaused.value = timerService!!.isPaused.value

            // Start collecting service state
            startCollecting()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            isBound = false
        }
    }

    private var collectJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private fun startCollecting() {
        collectJob?.cancel()
        collectJob = scope.launch {
            combine(
                timerService!!.isRunning,
                timerService!!.elapsedSeconds,
                timerService!!.currentSkillId,
                timerService!!.currentSkillName,
                timerService!!.isPaused
            ) { running, elapsed, skillId, skillName, paused ->
                _isRunning.value = running
                _elapsedSeconds.value = elapsed
                _currentSkillId.value = skillId
                _currentSkillName.value = skillName
                _isPaused.value = paused
            }.collect {}
        }
    }

    fun bindService(context: Context) {
        if (isBound) return
        val intent = Intent(context, TimerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService(context: Context) {
        if (!isBound) return
        try {
            collectJob?.cancel()
            context.unbindService(serviceConnection)
        } catch (_: Exception) {
            // Already unbound
        }
        isBound = false
        timerService = null
    }

    fun startTimer(context: Context, skillId: Long, skillName: String) {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_SKILL_ID, skillId)
            putExtra(TimerService.EXTRA_SKILL_NAME, skillName)
        }
        context.startForegroundService(intent)

        // Bind if not already bound
        if (!isBound) {
            bindService(context)
        }
    }

    fun stopTimer(): Long {
        val duration = timerService?.stopTimer() ?: 0L
        _isRunning.value = false
        _elapsedSeconds.value = 0L
        _isPaused.value = false
        return duration
    }

    fun pauseTimer() {
        timerService?.pauseTimer()
    }

    fun resumeTimer() {
        timerService?.resumeTimer()
    }

    fun formatElapsedTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    fun toDurationMinutes(seconds: Long): Int {
        return (seconds / 60).toInt().coerceAtLeast(1)
    }
}
