package com.lifeup.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lifeup.app.domain.game.DailyReset
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DailyResetReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dailyReset: DailyReset

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.LOCKED_BOOT_COMPLETED" -> {
                // 设备重启后重新设置每日重置
                scheduleDailyReset(context)
            }
        }
        // 执行每日重置
        scope.launch {
            try {
                dailyReset.performDailyReset()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun scheduleDailyReset(context: Context) {
        // 实际项目中应使用 WorkManager 或 AlarmManager 设置每日定时任务
        // 这里简化处理
    }
}
