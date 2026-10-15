package com.example.util

import java.util.Locale
import java.util.concurrent.TimeUnit

object Formatters {

    fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "--:--"
        val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
        }
    }

    fun formatTotalDuration(durationMs: Long): String {
        if (durationMs <= 0) return "0 min"
        val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
        return if (hours > 0) {
            "${hours} h ${minutes} min"
        } else {
            "${minutes} min"
        }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format(Locale.getDefault(), "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.getDefault(), "%.0f KB", kb)
            else -> "$bytes B"
        }
    }

    fun getAudioQualityBadge(mimeType: String, bitrate: Int, sampleRate: Int): String {
        val lower = mimeType.lowercase(Locale.ROOT)
        return when {
            lower.contains("flac") -> "FLAC LOSSLESS"
            lower.contains("wav") -> "WAV HI-RES"
            lower.contains("ogg") || lower.contains("vorbis") -> "OGG"
            lower.contains("mp4") || lower.contains("m4a") || lower.contains("aac") -> "AAC"
            bitrate >= 320 -> "MP3 320K"
            bitrate >= 256 -> "MP3 256K"
            bitrate > 0 -> "MP3 ${bitrate}K"
            else -> "STEREO"
        }
    }
}
