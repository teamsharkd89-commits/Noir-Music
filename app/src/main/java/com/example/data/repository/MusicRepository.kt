package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.dao.PlaylistDao
import com.example.data.local.dao.SongDao
import com.example.data.local.entity.Playlist
import com.example.data.local.entity.PlaylistSong
import com.example.data.local.entity.Song
import com.example.util.AudioMetadataParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

data class ImportResult(
    val addedCount: Int,
    val duplicateCount: Int,
    val failedCount: Int
)

data class LibraryStats(
    val songCount: Int,
    val albumCount: Int,
    val artistCount: Int,
    val totalDurationMs: Long,
    val totalSizeBytes: Long,
    val cachedArtSizeBytes: Long
)

class MusicRepository(
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao
) {

    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val recentlyAdded: Flow<List<Song>> = songDao.getRecentlyAdded(12)
    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    val albumNames: Flow<List<String>> = songDao.getAllAlbumNames()
    val artistNames: Flow<List<String>> = songDao.getAllArtistNames()
    val folderNames: Flow<List<String>> = songDao.getAllFolderNames()
    val favoriteSongs: Flow<List<Song>> = songDao.getFavoriteSongs()
    val mostPlayedSongs: Flow<List<Song>> = songDao.getMostPlayedSongs(30)
    val playbackHistory: Flow<List<Song>> = songDao.getPlaybackHistory(30)
    val songCount: Flow<Int> = songDao.getSongCount()

    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)

    fun getSongsByAlbum(album: String): Flow<List<Song>> = songDao.getSongsByAlbum(album)

    fun getSongsByArtist(artist: String): Flow<List<Song>> = songDao.getSongsByArtist(artist)

    fun getSongsByFolder(folder: String): Flow<List<Song>> = songDao.getSongsByFolder(folder)

    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> = playlistDao.getSongsForPlaylist(playlistId)

    suspend fun recordSongPlayed(songId: Long) = withContext(Dispatchers.IO) {
        songDao.incrementPlayCount(songId)
    }

    suspend fun importAudioUris(
        context: Context,
        uris: List<Uri>,
        onProgress: suspend (current: Int, total: Int, name: String) -> Unit
    ): ImportResult = withContext(Dispatchers.IO) {
        var added = 0
        var duplicates = 0
        var failed = 0
        val total = uris.size

        for (i in uris.indices) {
            val uri = uris[i]
            val song = AudioMetadataParser.parseSongFromUri(context, uri)

            if (song == null) {
                failed++
                onProgress(i + 1, total, "Archivo no decodificable")
                continue
            }

            onProgress(i + 1, total, song.title)

            // Check duplicate by URI or by (title + artist + duration)
            val existingByUri = songDao.getSongByUri(song.uriString)
            val existingDuplicate = if (existingByUri != null) {
                existingByUri
            } else {
                songDao.findDuplicate(song.title, song.artist, song.durationMs)
            }

            if (existingDuplicate != null) {
                duplicates++
            } else {
                val insertedId = songDao.insertSong(song)
                if (insertedId > 0) {
                    added++
                } else {
                    duplicates++
                }
            }
        }

        ImportResult(addedCount = added, duplicateCount = duplicates, failedCount = failed)
    }

    suspend fun deleteSong(song: Song) = withContext(Dispatchers.IO) {
        songDao.deleteSong(song)
    }

    suspend fun deleteSongs(songs: List<Song>) = withContext(Dispatchers.IO) {
        val ids = songs.map { it.id }
        songDao.deleteSongsByIds(ids)
    }

    suspend fun toggleFavorite(song: Song) = withContext(Dispatchers.IO) {
        val newFav = !song.isFavorite
        songDao.updateFavorite(song.id, newFav)
    }

    suspend fun createPlaylist(name: String, description: String = "", coverUri: String? = null): Long = withContext(Dispatchers.IO) {
        val playlist = Playlist(
            name = name.trim(),
            description = description.trim(),
            coverUri = coverUri,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        playlistDao.insertPlaylist(playlist)
    }

    suspend fun updatePlaylist(id: Long, name: String, description: String, coverUri: String?) = withContext(Dispatchers.IO) {
        playlistDao.updatePlaylistDetails(id, name.trim(), description.trim(), coverUri, System.currentTimeMillis())
    }

    suspend fun duplicatePlaylist(playlistId: Long): Long = withContext(Dispatchers.IO) {
        val original = playlistDao.getPlaylistByIdDirect(playlistId) ?: return@withContext -1L
        val originalSongs = playlistDao.getSongsForPlaylist(playlistId).first()

        val copyId = playlistDao.insertPlaylist(
            Playlist(
                name = "${original.name} (Copia)",
                description = original.description,
                coverUri = original.coverUri,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        if (copyId > 0 && originalSongs.isNotEmpty()) {
            val copyEntries = originalSongs.mapIndexed { index, song ->
                PlaylistSong(
                    playlistId = copyId,
                    songId = song.id,
                    orderIndex = index,
                    addedAt = System.currentTimeMillis()
                )
            }
            playlistDao.insertPlaylistSongs(copyEntries)
        }

        copyId
    }

    suspend fun deletePlaylist(playlist: Playlist) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(playlist)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        val crossRef = PlaylistSong(
            playlistId = playlistId,
            songId = songId,
            addedAt = System.currentTimeMillis()
        )
        playlistDao.insertPlaylistSong(crossRef)
    }

    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) = withContext(Dispatchers.IO) {
        val entries = songIds.mapIndexed { index, id ->
            PlaylistSong(
                playlistId = playlistId,
                songId = id,
                orderIndex = index,
                addedAt = System.currentTimeMillis()
            )
        }
        playlistDao.insertPlaylistSongs(entries)
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    suspend fun reorderPlaylistSong(playlistId: Long, songId: Long, newOrder: Int) = withContext(Dispatchers.IO) {
        playlistDao.updateSongOrder(playlistId, songId, newOrder)
    }

    suspend fun auditAndCleanMissingSongs(context: Context): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val all = songDao.getAllSongsList()
        val missingIds = mutableListOf<Long>()

        for (song in all) {
            if (!AudioMetadataParser.isUriAccessible(context, song.uriString)) {
                missingIds.add(song.id)
            }
        }

        if (missingIds.isNotEmpty()) {
            songDao.deleteSongsByIds(missingIds)
        }

        Pair(all.size, missingIds.size)
    }

    suspend fun getLibraryStats(context: Context): LibraryStats = withContext(Dispatchers.IO) {
        val all = songDao.getAllSongsList()
        val uniqueAlbums = all.map { it.album }.distinct().size
        val uniqueArtists = all.map { it.artist }.distinct().size
        val totalDur = all.sumOf { it.durationMs }
        val totalSize = all.sumOf { it.sizeBytes }

        var cachedArtSize = 0L
        try {
            val artDir = File(context.filesDir, "album_art")
            if (artDir.exists()) {
                cachedArtSize = artDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            }
        } catch (_: Exception) {}

        LibraryStats(
            songCount = all.size,
            albumCount = uniqueAlbums,
            artistCount = uniqueArtists,
            totalDurationMs = totalDur,
            totalSizeBytes = totalSize,
            cachedArtSizeBytes = cachedArtSize
        )
    }

    suspend fun clearCachedAlbumArt(context: Context): Long = withContext(Dispatchers.IO) {
        var freed = 0L
        try {
            val artDir = File(context.filesDir, "album_art")
            if (artDir.exists()) {
                artDir.listFiles()?.forEach { file ->
                    freed += file.length()
                    file.delete()
                }
            }
        } catch (_: Exception) {}
        freed
    }
}
