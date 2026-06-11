package com.lifeup.app.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.lifeup.app.domain.game.DailyReset
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.TodoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class DailyResetWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dailyStateRepository: DailyStateRepository,
    private val todoRepository: TodoRepository,
    private val settingsPrefs: com.lifeup.app.data.preferences.SettingsPrefs
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            DailyReset.performReset(dailyStateRepository, todoRepository, settingsPrefs)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "daily_reset_work"

        fun schedule(context: Context) {
            val now = java.time.LocalDateTime.now()
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
            val initialDelay = java.time.Duration.between(now, nextMidnight).toMinutes()

            val workRequest = PeriodicWorkRequestBuilder<DailyResetWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(initialDelay.coerceAtLeast(0), TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
        }
    }
}
