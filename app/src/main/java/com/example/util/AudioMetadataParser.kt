package com.example.util

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.data.local.entity.Song
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object AudioMetadataParser {

    private const val TAG = "AudioMetadataParser"

    fun parseSongFromUri(context: Context, uri: Uri): Song? {
        // 1. Persist URI permission via Storage Access Framework
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo tomar persistable permission para $uri: ${e.message}")
        }

        // 2. Query file name and size from content resolver
        var displayName = ""
        var fileSize = 0L
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        displayName = cursor.getString(nameIndex) ?: ""
                    }
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        fileSize = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error leyendo columnas de Openable: ${e.message}")
        }

        if (displayName.isBlank()) {
            displayName = uri.lastPathSegment?.substringAfterLast('/') ?: "audio_${System.currentTimeMillis()}"
        }

        val baseNameWithoutExt = displayName.substringBeforeLast('.')
        val extension = displayName.substringAfterLast('.', "").lowercase()

        // Derive folder name if available
        val pathSegments = uri.path?.split("/") ?: emptyList()
        val folderName = if (pathSegments.size > 2) {
            pathSegments[pathSegments.size - 2].takeIf { it.isNotBlank() } ?: "Música"
        } else {
            "Música"
        }

        // 3. Extract metadata with MediaMetadataRetriever
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)

            val rawTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val title = if (!rawTitle.isNullOrBlank()) rawTitle.trim() else baseNameWithoutExt

            val rawArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val artist = if (!rawArtist.isNullOrBlank()) rawArtist.trim() else "Artista desconocido"

            val rawAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val album = if (!rawAlbum.isNullOrBlank()) rawAlbum.trim() else "Álbum desconocido"

            val rawDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = rawDuration?.toLongOrNull() ?: 0L

            val rawTrack = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
            val trackNumber = rawTrack?.split("/")?.firstOrNull()?.trim()?.toIntOrNull() ?: 0

            val rawBitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            val bitrate = (rawBitrate?.toIntOrNull() ?: 0) / 1000 // Convert bps to kbps

            val rawSampleRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)
            val sampleRate = rawSampleRate?.toIntOrNull() ?: 0

            val rawMimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            val mimeType = when {
                !rawMimeType.isNullOrBlank() -> rawMimeType
                extension == "flac" -> "audio/flac"
                extension == "wav" -> "audio/wav"
                extension == "ogg" -> "audio/ogg"
                extension == "m4a" || extension == "aac" -> "audio/mp4"
                else -> "audio/mpeg"
            }

            // Save embedded artwork if present
            var albumArtPath: String? = null
            try {
                val artBytes = retriever.embeddedPicture
                if (artBytes != null && artBytes.isNotEmpty()) {
                    albumArtPath = saveAlbumArtToDisk(context, artist, album, artBytes)
                }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo extraer carátula: ${e.message}")
            }

            Song(
                uriString = uri.toString(),
                title = title,
                artist = artist,
                album = album,
                durationMs = durationMs,
                trackNumber = trackNumber,
                albumArtUri = albumArtPath,
                mimeType = mimeType,
                sizeBytes = fileSize,
                dateAdded = System.currentTimeMillis(),
                bitrate = bitrate,
                sampleRate = sampleRate,
                folderName = folderName,
                fileName = displayName
            )
        } catch (e: Exception) {
            Log.e(TAG, "Fallo al decodificar audio de $uri: ${e.message}")
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }

    private fun saveAlbumArtToDisk(
        context: Context,
        artist: String,
        album: String,
        artBytes: ByteArray
    ): String? {
        return try {
            val artDir = File(context.filesDir, "album_art").apply {
                if (!exists()) mkdirs()
            }
            val safeKey = "${artist.hashCode()}_${album.hashCode()}".replace("-", "n")
            val artFile = File(artDir, "art_$safeKey.jpg")
            if (!artFile.exists() || artFile.length() == 0L) {
                FileOutputStream(artFile).use { out ->
                    out.write(artBytes)
                    out.flush()
                }
            }
            artFile.absolutePath
        } catch (e: IOException) {
            Log.w(TAG, "Error guardando carátula en disco: ${e.message}")
            null
        }
    }

    fun isUriAccessible(context: Context, uriString: String): Boolean {
        return try {
            val uri = Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { true } ?: false
        } catch (_: Exception) {
            false
        }
    }
}
