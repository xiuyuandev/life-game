package com.lifeup.app

import android.app.Application
import com.lifeup.app.service.DailyResetWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LifeUpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        DailyResetWorker.schedule(this)
    }
}
