package com.lifeup.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.lifeup.app.R
import com.lifeup.app.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerService : Service() {

    companion object {
        const val CHANNEL_ID = NotificationHelper.CHANNEL_ID_TIMER
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.lifeup.app.action.TIMER_START"
        const val ACTION_STOP = "com.lifeup.app.action.TIMER_STOP"
        const val ACTION_PAUSE = "com.lifeup.app.action.TIMER_PAUSE"
        const val ACTION_RESUME = "com.lifeup.app.action.TIMER_RESUME"

        const val EXTRA_SKILL_ID = "skill_id"
        const val EXTRA_SKILL_NAME = "skill_name"
    }

    private val binder = TimerBinder()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private var _currentSkillId = MutableStateFlow(0L)
    val currentSkillId: StateFlow<Long> = _currentSkillId.asStateFlow()

    private var _currentSkillName = MutableStateFlow("")
    val currentSkillName: StateFlow<String> = _currentSkillName.asStateFlow()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var tickJob: Job? = null

    private var startTimeMs: Long = 0L
    private var accumulatedSeconds: Long = 0L

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val timerPrefs by lazy { getSharedPreferences("timer_state", Context.MODE_PRIVATE) }

    companion object {
        private const val PREFS_SKILL_ID = "skill_id"
        private const val PREFS_SKILL_NAME = "skill_name"
        private const val PREFS_START_TIME = "start_time"
        private const val PREFS_IS_RUNNING = "is_running"
        private const val PREFS_IS_PAUSED = "is_paused"
        private const val PREFS_ELAPSED_SECONDS = "elapsed_seconds"
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        restoreTimerState()
    }

    private fun restoreTimerState() {
        if (timerPrefs.getBoolean(PREFS_IS_RUNNING, false)) {
            val skillId = timerPrefs.getLong(PREFS_SKILL_ID, 0L)
            val skillName = timerPrefs.getString(PREFS_SKILL_NAME, "") ?: ""
            val savedStartTime = timerPrefs.getLong(PREFS_START_TIME, 0L)
            val savedElapsed = timerPrefs.getLong(PREFS_ELAPSED_SECONDS, 0L)
            val wasPaused = timerPrefs.getBoolean(PREFS_IS_PAUSED, false)
            if (skillId != 0L && savedStartTime > 0L) {
                _currentSkillId.value = skillId
                _currentSkillName.value = skillName
                _isRunning.value = true
                _isPaused.value = wasPaused
                accumulatedSeconds = savedElapsed
                startTimeMs = savedStartTime
                startForegroundWithNotification()
                tickJob = serviceScope.launch {
                    while (isActive) {
                        delay(1000L)
                        if (!_isPaused.value) {
                            val elapsed = (System.currentTimeMillis() - startTimeMs) / 1000L
                            _elapsedSeconds.value = accumulatedSeconds + elapsed
                            updateNotification()
                        }
                    }
                }
            }
        }
    }

    private fun saveTimerState() {
        timerPrefs.edit().apply {
            putLong(PREFS_SKILL_ID, _currentSkillId.value)
            putString(PREFS_SKILL_NAME, _currentSkillName.value)
            putLong(PREFS_START_TIME, startTimeMs)
            putBoolean(PREFS_IS_RUNNING, _isRunning.value)
            putBoolean(PREFS_IS_PAUSED, _isPaused.value)
            putLong(PREFS_ELAPSED_SECONDS, _elapsedSeconds.value)
            apply()
        }
    }

    private fun clearTimerState() {
        timerPrefs.edit().clear().apply()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val skillId = intent.getLongExtra(EXTRA_SKILL_ID, 0L)
                val skillName = intent.getStringExtra(EXTRA_SKILL_NAME) ?: ""
                startTimer(skillId, skillName)
            }
            ACTION_STOP -> {
                stopTimer()
            }
            ACTION_PAUSE -> {
                pauseTimer()
            }
            ACTION_RESUME -> {
                resumeTimer()
            }
        }
        return START_NOT_STICKY
    }

    fun startTimer(skillId: Long, skillName: String) {
        if (_isRunning.value) return

        _currentSkillId.value = skillId
        _currentSkillName.value = skillName
        _elapsedSeconds.value = 0L
        accumulatedSeconds = 0L
        _isPaused.value = false
        _isRunning.value = true

        startTimeMs = System.currentTimeMillis()

        startForegroundWithNotification()
        saveTimerState()

        tickJob = serviceScope.launch {
            while (isActive) {
                delay(1000L)
                if (!_isPaused.value) {
                    val elapsed = (System.currentTimeMillis() - startTimeMs) / 1000L
                    _elapsedSeconds.value = accumulatedSeconds + elapsed
                    updateNotification()
                }
            }
        }
    }

    fun stopTimer(): Long {
        val duration = _elapsedSeconds.value
        tickJob?.cancel()
        tickJob = null
        _isRunning.value = false
        _isPaused.value = false
        _elapsedSeconds.value = 0L
        accumulatedSeconds = 0L

        clearTimerState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        return duration
    }

    fun pauseTimer() {
        if (!_isRunning.value || _isPaused.value) return
        _isPaused.value = true
        accumulatedSeconds = _elapsedSeconds.value
        updateNotification()
    }

    fun resumeTimer() {
        if (!_isRunning.value || !_isPaused.value) return
        _isPaused.value = false
        startTimeMs = System.currentTimeMillis()
        updateNotification()
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_STOPWATCH
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val skillName = _currentSkillName.value.ifBlank { "技能练习" }
        val elapsed = _elapsedSeconds.value
        val timeText = formatElapsedTime(elapsed)
        val stateText = if (_isPaused.value) "已暂停" else "计时中"

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeAction = if (_isPaused.value) {
            NotificationCompat.Action.Builder(
                0, "继续",
                createActionPendingIntent(ACTION_RESUME)
            )
        } else {
            NotificationCompat.Action.Builder(
                0, "暂停",
                createActionPendingIntent(ACTION_PAUSE)
            )
        }

        val stopAction = NotificationCompat.Action.Builder(
            0, "停止",
            createActionPendingIntent(ACTION_STOP)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(skillName)
            .setContentText("$stateText - $timeText")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .addAction(pauseResumeAction.build())
            .addAction(stopAction.build())
            .setSilent(true)
            .build()
    }

    private fun createActionPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, TimerService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun updateNotification() {
        try {
            val notification = buildNotification()
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (_: Exception) {
            // Service may have been stopped
        }
    }

    private fun createNotificationChannel() {
        // Channels are created in LifeUpApplication via NotificationHelper
    }

    private fun formatElapsedTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    override fun onDestroy() {
        tickJob?.cancel()
        tickJob = null
        _isRunning.value = false
        serviceScope.cancel()
        super.onDestroy()
    }
}
