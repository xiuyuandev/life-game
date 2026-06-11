package com.lifeup.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import com.lifeup.app.ui.MainActivity

class TimerWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_OPEN_TIMER = "com.lifeup.app.action.OPEN_TIMER"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgets: IntArray
    ) {
        WidgetUpdateService.updateTimerWidget(context, appWidgetManager, appWidgets)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_OPEN_TIMER -> {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(launchIntent)
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateService.updateTimerWidget(context)
    }
}
