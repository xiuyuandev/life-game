package com.lifeup.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lifeup.app.R

object NotificationHelper {
    const val CHANNEL_ID_TIMER = "lifeup_timer_channel"
    const val CHANNEL_ID_DAILY = "lifeup_daily_channel"
    const val CHANNEL_ID_ACHIEVEMENT = "lifeup_achievement_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_TIMER,
                    "计时器",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "技能练习计时器前台服务通知"
                    setShowBadge(false)
                },
                NotificationChannel(
                    CHANNEL_ID_DAILY,
                    "每日提醒",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "每日习惯打卡和待办提醒"
                },
                NotificationChannel(
                    CHANNEL_ID_ACHIEVEMENT,
                    "成就解锁",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "成就解锁通知"
                }
            )

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannels(channels)
        }
    }
}
