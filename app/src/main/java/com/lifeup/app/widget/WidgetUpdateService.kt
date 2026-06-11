package com.lifeup.app.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.lifeup.app.R
import com.lifeup.app.data.db.LifeUpDatabase
import com.lifeup.app.service.TimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class WidgetUpdateService : android.app.Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun getTodayDate(): String {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault()))
    }

    fun updateTodayWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, TodayWidget::class.java)
        )
        if (widgetIds.isNotEmpty()) {
            updateTodayWidget(context, appWidgetManager, widgetIds)
        }
    }

    fun updateTodayWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        serviceScope.launch {
            val db = LifeUpDatabase.getDatabase(context)
            val today = getTodayDate()

            val dailyState = try {
                db.dailyStateDao().getAll().find { it.date == today }
            } catch (_: Exception) {
                null
            }

            val investmentMinutes = try {
                db.timeRecordDao().getInvestmentMinutesByDate(today)
            } catch (_: Exception) {
                0
            }

            val streakCount = try {
                db.dailyStateDao().getLatestStreak() ?: 0
            } catch (_: Exception) {
                0
            }

            val energy = dailyState?.energy?.toInt() ?: 100
            val energyCap = dailyState?.energyCap?.toInt() ?: 100

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_today)

                // Energy progress
                views.setProgressBar(
                    R.id.widget_energy_progress,
                    energyCap,
                    energy,
                    false
                )
                views.setTextViewText(
                    R.id.widget_energy_value,
                    "$energy/$energyCap"
                )

                // Investment time
                val hours = investmentMinutes / 60
                val minutes = investmentMinutes % 60
                val investmentText = if (hours > 0) {
                    "投资时间: ${hours}小时${minutes}分钟"
                } else {
                    "投资时间: ${minutes}分钟"
                }
                views.setTextViewText(R.id.widget_investment_time, investmentText)

                // Streak
                views.setTextViewText(R.id.widget_streak, "🔥 ${streakCount}天")

                // Click action - open app
                val openAppIntent = Intent(context, TodayWidget::class.java).apply {
                    action = TodayWidget.ACTION_OPEN_APP
                }
                val openAppPendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_open_app, openAppPendingIntent)

                // Click on entire widget also opens app
                val mainIntent = Intent(context, com.lifeup.app.ui.MainActivity::class.java)
                val mainPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_title, mainPendingIntent)

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    fun updateTimerWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, TimerWidget::class.java)
        )
        if (widgetIds.isNotEmpty()) {
            updateTimerWidget(context, appWidgetManager, widgetIds)
        }
    }

    fun updateTimerWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val isRunning = TimerManager.isRunning.value
        val isPaused = TimerManager.isPaused.value
        val skillName = TimerManager.currentSkillName.value
        val elapsedSeconds = TimerManager.elapsedSeconds.value

        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_timer)

            if (isRunning) {
                views.setTextViewText(R.id.widget_timer_skill, skillName)
                views.setTextViewText(
                    R.id.widget_timer_elapsed,
                    TimerManager.formatElapsedTime(elapsedSeconds)
                )
                views.setViewVisibility(R.id.widget_timer_indicator, View.VISIBLE)

                // Change indicator color based on paused state
                views.setInt(
                    R.id.widget_timer_indicator,
                    "setBackgroundResource",
                    if (isPaused) R.drawable.widget_indicator_dot_paused
                    else R.drawable.widget_indicator_dot
                )
                views.setTextViewText(
                    R.id.widget_timer_hint,
                    if (isPaused) "已暂停 - 点击继续" else "计时中 - 点击打开"
                )
            } else {
                views.setTextViewText(R.id.widget_timer_skill, "未在计时")
                views.setTextViewText(R.id.widget_timer_elapsed, "00:00:00")
                views.setViewVisibility(R.id.widget_timer_indicator, View.GONE)
                views.setTextViewText(R.id.widget_timer_hint, "点击打开计时器")
            }

            // Click action - open timer
            val openTimerIntent = Intent(context, TimerWidget::class.java).apply {
                action = TimerWidget.ACTION_OPEN_TIMER
            }
            val openTimerPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                openTimerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_timer_elapsed, openTimerPendingIntent)

            // Click on entire widget opens app
            val mainIntent = Intent(context, com.lifeup.app.ui.MainActivity::class.java)
            val mainPendingIntent = PendingIntent.getActivity(
                context,
                1,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_timer_skill, mainPendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    override fun onBind(intent: android.content.Intent?): android.os.IBinder? = null

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        fun scheduleTimerWidgetUpdates(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                2,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Update every second
            val intervalMillis = 1000L

            alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME,
                android.os.SystemClock.elapsedRealtime() + intervalMillis,
                intervalMillis,
                pendingIntent
            )
        }

        fun cancelTimerWidgetUpdates(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                2,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
