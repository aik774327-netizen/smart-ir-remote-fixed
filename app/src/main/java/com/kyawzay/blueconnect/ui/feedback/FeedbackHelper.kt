package com.kyawzay.blueconnect.ui.feedback

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class FeedbackHelper(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerClickFeedback(enableHaptic: Boolean = true, enableSound: Boolean = true) {
        if (enableHaptic) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(40)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (enableSound) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
