package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["uriString"], unique = true),
        Index(value = ["artist"]),
        Index(value = ["album"]),
        Index(value = ["folderName"])
    ]
)
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uriString: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val trackNumber: Int = 0,
    val albumArtUri: String? = null,
    val mimeType: String = "audio/mpeg",
    val sizeBytes: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
    val bitrate: Int = 0, // kbps
    val sampleRate: Int = 0, // Hz
    val folderName: String = "Música",
    val fileName: String = "",
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayed: Long = 0L
)
