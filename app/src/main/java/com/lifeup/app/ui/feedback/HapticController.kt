package com.lifeup.app.ui.feedback

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.lifeup.app.data.preferences.SettingsPrefs
import kotlinx.coroutines.flow.flowOf

/**
 * Haptic feedback contract. Use this for all user-facing tactile feedback.
 */
interface HapticController {
    fun light()
    fun medium()
    fun heavy()
    fun tick()
    fun success()
    fun warning()
    fun levelUp()
    fun streak()
}

/**
 * Provide a HapticController in a Composable that respects the user's settings.
 * When haptics are disabled in settings, all calls become no-ops.
 */
@Composable
fun rememberHapticController(
    settingsPrefs: SettingsPrefs
): HapticController {
    val context = LocalContext.current
    val enabled by settingsPrefs.isHapticEnabled().collectAsState(initial = true)
    return remember(context, enabled) {
        if (enabled) RealHapticController(context) else NoopHapticController
    }
}

/**
 * Real implementation that triggers device vibration.
 */
class RealHapticController internal constructor(
    private val context: Context
) : HapticController {
    override fun light() = perform(HapticType.LIGHT)
    override fun medium() = perform(HapticType.MEDIUM)
    override fun heavy() = perform(HapticType.HEAVY)
    override fun tick() = perform(HapticType.TICK)
    override fun success() = perform(HapticType.SUCCESS)
    override fun warning() = perform(HapticType.WARNING)
    override fun levelUp() = perform(HapticType.LEVEL_UP)
    override fun streak() = perform(HapticType.STREAK)

    private fun perform(type: HapticType) {
        val vibrator = getVibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (type) {
                HapticType.LIGHT -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                HapticType.MEDIUM -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                HapticType.HEAVY -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                HapticType.TICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                HapticType.SUCCESS -> VibrationEffect.createWaveform(longArrayOf(0, 50, 30, 50), -1)
                HapticType.WARNING -> VibrationEffect.createWaveform(longArrayOf(0, 30, 50, 30), -1)
                HapticType.LEVEL_UP -> VibrationEffect.createWaveform(longArrayOf(0, 80, 40, 80, 40, 120), -1)
                HapticType.STREAK -> VibrationEffect.createWaveform(longArrayOf(0, 60, 30, 60), -1)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            when (type) {
                HapticType.LIGHT, HapticType.TICK -> vibrator.vibrate(10)
                HapticType.MEDIUM, HapticType.HEAVY -> vibrator.vibrate(30)
                HapticType.SUCCESS -> vibrator.vibrate(longArrayOf(0, 50, 30, 50), -1)
                HapticType.WARNING -> vibrator.vibrate(longArrayOf(0, 30, 50, 30), -1)
                HapticType.LEVEL_UP -> vibrator.vibrate(longArrayOf(0, 80, 40, 80, 40, 120), -1)
                HapticType.STREAK -> vibrator.vibrate(longArrayOf(0, 60, 30, 60), -1)
            }
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}

object NoopHapticController : HapticController {
    override fun light() {}
    override fun medium() {}
    override fun heavy() {}
    override fun tick() {}
    override fun success() {}
    override fun warning() {}
    override fun levelUp() {}
    override fun streak() {}
}

enum class HapticType { LIGHT, MEDIUM, HEAVY, TICK, SUCCESS, WARNING, LEVEL_UP, STREAK }
