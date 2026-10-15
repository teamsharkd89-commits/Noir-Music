package com.example.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.data.local.entity.Song
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class MusicPlayerManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val TAG = "MusicPlayerManager"

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    // Reactive Playback States
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    // 0: OFF, 1: ONE, 2: ALL (following Player.REPEAT_MODE_*)
    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentQueueIndex = MutableStateFlow(0)
    val currentQueueIndex: StateFlow<Int> = _currentQueueIndex.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    // Internal mapping of songId -> Song to sync with MediaItems
    private val songsById = mutableMapOf<Long, Song>()

    // Polling job for progress update
    private var progressJob: Job? = null

    // Callback when song/position changes to persist state
    var onPlaybackStatePersist: ((songId: Long, positionMs: Long) -> Unit)? = null
    var onShufflePersist: ((enabled: Boolean) -> Unit)? = null
    var onRepeatPersist: ((mode: Int) -> Unit)? = null
    var onSongStarted: ((songId: Long) -> Unit)? = null

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, NoirMediaSessionService::class.java)
        )

        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                val controller = controllerFuture?.get()
                mediaController = controller
                setupPlayerListener(controller)
                syncStateFromController(controller)
            } catch (e: Exception) {
                Log.e(TAG, "Error inicializando MediaController: ${e.message}")
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener(controller: MediaController?) {
        controller ?: return

        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                    persistCurrentPosition()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val songId = mediaItem?.mediaId?.toLongOrNull()
                val song = songId?.let { songsById[it] }
                _currentSong.value = song
                _currentQueueIndex.value = controller.currentMediaItemIndex

                val dur = if (controller.duration > 0) controller.duration else (song?.durationMs ?: 0L)
                _durationMs.value = dur
                _currentPositionMs.value = 0L

                if (songId != null && songId > 0) {
                    onSongStarted?.invoke(songId)
                }

                persistCurrentPosition()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        _durationMs.value = if (controller.duration > 0) controller.duration else (_currentSong.value?.durationMs ?: 0L)
                        _currentPositionMs.value = controller.currentPosition
                    }
                    Player.STATE_ENDED -> {
                        _isPlaying.value = false
                        stopProgressTracker()
                    }
                    else -> {}
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _isShuffleEnabled.value = shuffleModeEnabled
                onShufflePersist?.invoke(shuffleModeEnabled)
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _repeatMode.value = repeatMode
                onRepeatPersist?.invoke(repeatMode)
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "Playback error: ${error.errorCodeName} - ${error.message}")
                _playbackError.value = "Error al reproducir pista: ${error.localizedMessage ?: "Archivo corrupto o no accesible"}"
                _isPlaying.value = false
            }
        })
    }

    private fun syncStateFromController(controller: MediaController?) {
        controller ?: return
        _isPlaying.value = controller.isPlaying
        _isShuffleEnabled.value = controller.shuffleModeEnabled
        _repeatMode.value = controller.repeatMode
        _currentPositionMs.value = controller.currentPosition
        if (controller.duration > 0) {
            _durationMs.value = controller.duration
        }

        val songId = controller.currentMediaItem?.mediaId?.toLongOrNull()
        songId?.let { id ->
            songsById[id]?.let { song ->
                _currentSong.value = song
            }
        }

        if (controller.isPlaying) {
            startProgressTracker()
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        _currentPositionMs.value = controller.currentPosition
                        if (controller.duration > 0) {
                            _durationMs.value = controller.duration
                        }
                    }
                }
                delay(300)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun persistCurrentPosition() {
        val songId = _currentSong.value?.id ?: -1L
        val pos = mediaController?.currentPosition ?: _currentPositionMs.value
        if (songId > 0) {
            onPlaybackStatePersist?.invoke(songId, pos)
        }
    }

    private fun songToMediaItem(song: Song): MediaItem {
        songsById[song.id] = song
        val artUri = song.albumArtUri?.let { path ->
            val file = File(path)
            if (file.exists()) Uri.fromFile(file) else null
        }

        return MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(Uri.parse(song.uriString))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(artUri)
                    .build()
            )
            .build()
    }

    fun playSongFromQueue(
        song: Song,
        fullList: List<Song>,
        startIndex: Int = 0,
        autoPlay: Boolean = true
    ) {
        val controller = mediaController ?: return

        // Register all songs in lookup map
        fullList.forEach { songsById[it.id] = it }
        _queue.value = fullList

        val mediaItems = fullList.map { songToMediaItem(it) }
        val targetIndex = if (startIndex in fullList.indices) startIndex else 0

        controller.setMediaItems(mediaItems, targetIndex, C.TIME_UNSET)
        controller.prepare()
        if (autoPlay) {
            controller.play()
        }

        _currentSong.value = fullList[targetIndex]
        _currentQueueIndex.value = targetIndex
        _durationMs.value = fullList[targetIndex].durationMs
        _currentPositionMs.value = 0L
    }

    fun updateCurrentSongFavorite(isFavorite: Boolean) {
        _currentSong.value = _currentSong.value?.copy(isFavorite = isFavorite)
    }

    fun restoreSessionState(song: Song, lastPositionMs: Long) {
        val controller = mediaController
        songsById[song.id] = song
        _currentSong.value = song
        _currentPositionMs.value = lastPositionMs
        _durationMs.value = song.durationMs
        _queue.value = listOf(song)

        if (controller != null) {
            val item = songToMediaItem(song)
            controller.setMediaItems(listOf(item), 0, lastPositionMs)
            controller.prepare()
            // Do NOT call controller.play() to satisfy requirement: "sin reproducir automáticamente de forma inesperada"
        }
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            if (controller.playbackState == Player.STATE_ENDED) {
                controller.seekTo(0, 0)
            }
            controller.play()
        }
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun seekTo(positionMs: Long) {
        val controller = mediaController ?: return
        val clamped = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(0L))
        _currentPositionMs.value = clamped
        controller.seekTo(clamped)
        persistCurrentPosition()
    }

    fun seekForward(offsetMs: Long = 10000L) {
        val pos = (_currentPositionMs.value + offsetMs).coerceAtMost(_durationMs.value)
        seekTo(pos)
    }

    fun seekBackward(offsetMs: Long = 10000L) {
        val pos = (_currentPositionMs.value - offsetMs).coerceAtLeast(0L)
        seekTo(pos)
    }

    fun skipToNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        } else if (_queue.value.isNotEmpty() && _repeatMode.value == Player.REPEAT_MODE_ALL) {
            controller.seekTo(0, 0)
        }
    }

    fun skipToPrevious() {
        val controller = mediaController ?: return
        if (controller.currentPosition > 3000L) {
            // If already played more than 3 seconds, restart current track
            controller.seekTo(0)
            _currentPositionMs.value = 0L
        } else if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        } else {
            controller.seekTo(0)
            _currentPositionMs.value = 0L
        }
    }

    fun toggleShuffle() {
        val controller = mediaController ?: return
        val newShuffle = !controller.shuffleModeEnabled
        controller.shuffleModeEnabled = newShuffle
        _isShuffleEnabled.value = newShuffle
        onShufflePersist?.invoke(newShuffle)
    }

    fun toggleRepeat() {
        val controller = mediaController ?: return
        val nextRepeat = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        controller.repeatMode = nextRepeat
        _repeatMode.value = nextRepeat
        onRepeatPersist?.invoke(nextRepeat)
    }

    fun addToPlayNext(song: Song) {
        val controller = mediaController ?: return
        val currentIdx = controller.currentMediaItemIndex
        val nextIdx = (currentIdx + 1).coerceAtMost(controller.mediaItemCount)

        val item = songToMediaItem(song)
        controller.addMediaItem(nextIdx, item)

        val updatedQueue = _queue.value.toMutableList()
        val insertIndex = (currentIdx + 1).coerceAtMost(updatedQueue.size)
        updatedQueue.add(insertIndex, song)
        _queue.value = updatedQueue
    }

    fun addToEndOfQueue(song: Song) {
        val controller = mediaController ?: return
        val item = songToMediaItem(song)
        controller.addMediaItem(item)

        val updatedQueue = _queue.value.toMutableList()
        updatedQueue.add(song)
        _queue.value = updatedQueue
    }

    fun removeFromQueue(index: Int) {
        val controller = mediaController ?: return
        if (index in 0 until controller.mediaItemCount) {
            controller.removeMediaItem(index)
            val updatedQueue = _queue.value.toMutableList()
            if (index in updatedQueue.indices) {
                updatedQueue.removeAt(index)
                _queue.value = updatedQueue
            }
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val controller = mediaController ?: return
        if (fromIndex in 0 until controller.mediaItemCount && toIndex in 0 until controller.mediaItemCount) {
            controller.moveMediaItem(fromIndex, toIndex)
            val updatedQueue = _queue.value.toMutableList()
            if (fromIndex in updatedQueue.indices && toIndex in updatedQueue.indices) {
                val item = updatedQueue.removeAt(fromIndex)
                updatedQueue.add(toIndex, item)
                _queue.value = updatedQueue
            }
        }
    }

    fun clearQueueExceptCurrent() {
        val controller = mediaController ?: return
        val currentIdx = controller.currentMediaItemIndex
        val currentItem = _currentSong.value

        if (currentItem != null) {
            // Keep only the current item
            val mediaItem = songToMediaItem(currentItem)
            controller.setMediaItems(listOf(mediaItem), 0, controller.currentPosition)
            _queue.value = listOf(currentItem)
            _currentQueueIndex.value = 0
        } else {
            controller.clearMediaItems()
            _queue.value = emptyList()
            _currentQueueIndex.value = 0
        }
    }

    fun clearPlaybackError() {
        _playbackError.value = null
    }

    fun release() {
        stopProgressTracker()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
