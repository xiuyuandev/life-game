package com.lifeup.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.lifeup.app.R
import com.lifeup.app.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class TimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private val binder = LocalBinder()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private var startTimeMillis: Long = 0
    private var accumulatedSeconds: Long = 0
    private var timerJob: Job? = null

    private var sessionTitle: String = "计时中"
    private var sessionCategory: String = ""

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                sessionTitle = intent.getStringExtra(EXTRA_TITLE) ?: "计时中"
                sessionCategory = intent.getStringExtra(EXTRA_CATEGORY) ?: ""
                startTimer()
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_NOT_STICKY
    }

    private fun startTimer() {
        if (_isRunning.value) return
        startTimeMillis = System.currentTimeMillis()
        accumulatedSeconds = 0
        _elapsedSeconds.value = 0
        _isRunning.value = true

        startForeground(NOTIFICATION_ID, buildNotification(0))

        timerJob = serviceScope.launch {
            while (isActive) {
                val elapsed = accumulatedSeconds + ((System.currentTimeMillis() - startTimeMillis) / 1000)
                _elapsedSeconds.value = elapsed
                updateNotification(elapsed)
                delay(1000)
            }
        }
    }

    private fun pauseTimer() {
        if (!_isRunning.value) return
        timerJob?.cancel()
        accumulatedSeconds += (System.currentTimeMillis() - startTimeMillis) / 1000
        _isRunning.value = false
        updateNotification(accumulatedSeconds)
    }

    private fun resumeTimer() {
        if (_isRunning.value) return
        startTimeMillis = System.currentTimeMillis()
        _isRunning.value = true

        timerJob = serviceScope.launch {
            while (isActive) {
                val elapsed = accumulatedSeconds + ((System.currentTimeMillis() - startTimeMillis) / 1000)
                _elapsedSeconds.value = elapsed
                updateNotification(elapsed)
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        _isRunning.value = false
        _elapsedSeconds.value = 0
        accumulatedSeconds = 0
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun getStartTime(): Long = startTimeMillis

    fun getAccumulatedSeconds(): Long = accumulatedSeconds +
            if (_isRunning.value) ((System.currentTimeMillis() - startTimeMillis) / 1000) else 0

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "计时器",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "LifeUp 计时器通知"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(elapsedSeconds: Long): Notification {
        val timeStr = formatTime(elapsedSeconds)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(sessionTitle)
            .setContentText("⏱️ $timeStr")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(elapsedSeconds: Long) {
        val notification = buildNotification(elapsedSeconds)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    inner class LocalBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    companion object {
        const val CHANNEL_ID = "lifeup_timer_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.lifeup.app.ACTION_START"
        const val ACTION_PAUSE = "com.lifeup.app.ACTION_PAUSE"
        const val ACTION_RESUME = "com.lifeup.app.ACTION_RESUME"
        const val ACTION_STOP = "com.lifeup.app.ACTION_STOP"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_CATEGORY = "extra_category"

        fun start(context: Context, title: String, category: String) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_CATEGORY, category)
            }
            context.startForegroundService(intent)
        }

        fun pause(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
