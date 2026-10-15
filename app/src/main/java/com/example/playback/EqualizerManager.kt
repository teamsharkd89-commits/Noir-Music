package com.example.playback

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.util.Log

data class EqualizerBand(
    val index: Short,
    val centerFreqHz: Int,
    val minLevel: Short,
    val maxLevel: Short,
    var currentLevel: Short
)

class EqualizerManager(private val context: Context) {
    private val TAG = "EqualizerManager"

    var equalizer: Equalizer? = null
        private set
    var bassBoost: BassBoost? = null
        private set

    var isSupported: Boolean = false
        private set
    var isEnabled: Boolean = false
        private set

    var bands: List<EqualizerBand> = emptyList()
        private set
    var presets: List<String> = emptyList()
        private set
    var currentPresetIndex: Short = -1
        private set

    var isBassBoostSupported: Boolean = false
        private set
    var bassBoostStrength: Short = 0
        private set

    fun initEqualizer(audioSessionId: Int): Boolean {
        if (audioSessionId <= 0) {
            isSupported = false
            return false
        }

        release()

        return try {
            val eq = Equalizer(0, audioSessionId)
            eq.enabled = true
            equalizer = eq
            isEnabled = eq.enabled

            val numBands = eq.numberOfBands
            val levelRange = eq.bandLevelRange
            val bandList = mutableListOf<EqualizerBand>()

            for (i in 0 until numBands) {
                val bandIndex = i.toShort()
                val freqMilliHz = eq.getCenterFreq(bandIndex)
                val freqHz = freqMilliHz / 1000
                val cur = eq.getBandLevel(bandIndex)
                bandList.add(
                    EqualizerBand(
                        index = bandIndex,
                        centerFreqHz = freqHz,
                        minLevel = levelRange[0],
                        maxLevel = levelRange[1],
                        currentLevel = cur
                    )
                )
            }
            bands = bandList

            val numPresets = eq.numberOfPresets
            val presetList = mutableListOf<String>()
            for (i in 0 until numPresets) {
                presetList.add(eq.getPresetName(i.toShort()))
            }
            presets = presetList
            currentPresetIndex = eq.currentPreset

            // Try BassBoost
            try {
                val bb = BassBoost(0, audioSessionId)
                if (bb.strengthSupported) {
                    bb.enabled = true
                    bassBoost = bb
                    isBassBoostSupported = true
                    bassBoostStrength = bb.roundedStrength
                }
            } catch (e: Exception) {
                isBassBoostSupported = false
            }

            isSupported = true
            true
        } catch (e: Exception) {
            Log.w(TAG, "Hardware Equalizer no soportado o inaccesible: ${e.message}")
            isSupported = false
            false
        }
    }

    fun setBandLevel(bandIndex: Short, level: Short) {
        try {
            equalizer?.setBandLevel(bandIndex, level)
            currentPresetIndex = -1
            bands.find { it.index == bandIndex }?.currentLevel = level
        } catch (e: Exception) {
            Log.e(TAG, "Error setting band level: ${e.message}")
        }
    }

    fun usePreset(presetIndex: Short) {
        try {
            equalizer?.usePreset(presetIndex)
            currentPresetIndex = presetIndex
            bands.forEach { band ->
                equalizer?.let {
                    band.currentLevel = it.getBandLevel(band.index)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting preset: ${e.message}")
        }
    }

    fun setBassBoost(strength: Short) {
        try {
            if (isBassBoostSupported) {
                bassBoost?.setStrength(strength)
                bassBoostStrength = strength
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting bass boost: ${e.message}")
        }
    }

    fun openSystemAudioEffectPanel(audioSessionId: Int): Boolean {
        return try {
            val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.w(TAG, "No system audio effects panel found: ${e.message}")
            false
        }
    }

    fun release() {
        try {
            equalizer?.release()
            equalizer = null
            bassBoost?.release()
            bassBoost = null
        } catch (_: Exception) {}
    }
}
