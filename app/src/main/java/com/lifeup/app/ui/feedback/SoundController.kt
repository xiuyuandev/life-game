package com.lifeup.app.ui.feedback

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.lifeup.app.data.preferences.SettingsPrefs

/**
 * Sound feedback contract. Use this for non-intrusive audio cues.
 */
interface SoundController {
    fun tap()
    fun success()
    fun levelUp()
    fun streak()
    fun warning()
    fun release()
}

/**
 * Provide a SoundController in a Composable that respects the user's settings.
 * When sound is disabled in settings, all calls become no-ops.
 */
@Composable
fun rememberSoundController(
    settingsPrefs: SettingsPrefs
): SoundController {
    val context = androidx.compose.ui.platform.LocalContext.current
    val enabled by settingsPrefs.isSoundEnabled().collectAsState(initial = true)
    return remember(context, enabled) {
        if (enabled) RealSoundController(context) else NoopSoundController
    }
}

class RealSoundController internal constructor(
    context: Context
) : SoundController {
    private val toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 50)
    } catch (_: Exception) {
        null
    }

    override fun tap() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
    }

    override fun success() {
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100)
    }

    override fun levelUp() {
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 200)
    }

    override fun streak() {
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 120)
    }

    override fun warning() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 80)
    }

    override fun release() {
        try {
            toneGenerator?.release()
        } catch (_: Exception) {}
    }
}

object NoopSoundController : SoundController {
    override fun tap() {}
    override fun success() {}
    override fun levelUp() {}
    override fun streak() {}
    override fun warning() {}
    override fun release() {}
}

enum class SoundType { TAP, SUCCESS, LEVEL_UP, STREAK, WARNING }
