package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Playlist
import com.example.data.local.entity.Song
import com.example.data.preferences.SortOption
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.ImportResult
import com.example.data.repository.LibraryStats
import com.example.data.repository.MusicRepository
import com.example.playback.EqualizerManager
import com.example.playback.MusicPlayerManager
import com.example.playback.NoirMediaSessionService
import com.example.ui.theme.AppThemeConfig
import com.example.util.Formatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ImportProgress(
    val isImporting: Boolean = false,
    val current: Int = 0,
    val total: Int = 0,
    val currentName: String = "",
    val result: ImportResult? = null
)

enum class SearchFilterType {
    ALL,
    SONGS,
    ARTISTS,
    ALBUMS
}

class MusicViewModel(
    application: Application,
    private val repository: MusicRepository,
    private val preferencesRepository: UserPreferencesRepository
) : AndroidViewModel(application) {

    // Player Engine Manager & Equalizer
    val playerManager = MusicPlayerManager(application, viewModelScope)
    val equalizerManager = EqualizerManager(application)

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    val rawSongs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val mostPlayedSongs: StateFlow<List<Song>> = repository.mostPlayedSongs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val playbackHistory: StateFlow<List<Song>> = repository.playbackHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Sorted songs reactive to UserPreferences
    val sortedSongs: StateFlow<List<Song>> = combine(rawSongs, userPreferences) { songs, prefs ->
        val sorted = when (prefs.sortOption) {
            SortOption.TITLE -> songs.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
            SortOption.ARTIST -> songs.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.artist })
            SortOption.ALBUM -> songs.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.album })
            SortOption.DURATION -> songs.sortedBy { it.durationMs }
            SortOption.DATE_ADDED -> songs.sortedBy { it.dateAdded }
        }
        if (prefs.sortAscending) sorted else sorted.reversed()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentlyAdded: StateFlow<List<Song>> = repository.recentlyAdded
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val playlists: StateFlow<List<Playlist>> = repository.allPlaylists
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val albumNames: StateFlow<List<String>> = repository.albumNames
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val artistNames: StateFlow<List<String>> = repository.artistNames
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val folderNames: StateFlow<List<String>> = repository.folderNames
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Playback state delegations
    val currentSong: StateFlow<Song?> = playerManager.currentSong
    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying
    val currentPositionMs: StateFlow<Long> = playerManager.currentPositionMs
    val durationMs: StateFlow<Long> = playerManager.durationMs
    val isShuffleEnabled: StateFlow<Boolean> = playerManager.isShuffleEnabled
    val repeatMode: StateFlow<Int> = playerManager.repeatMode
    val queue: StateFlow<List<Song>> = playerManager.queue
    val currentQueueIndex: StateFlow<Int> = playerManager.currentQueueIndex
    val playbackError: StateFlow<String?> = playerManager.playbackError

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow(SearchFilterType.ALL)
    val searchFilter: StateFlow<SearchFilterType> = _searchFilter.asStateFlow()

    val searchResults: StateFlow<List<Song>> = _searchQuery
        .debounce(150)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.searchSongs(query.trim())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Import State
    private val _importProgress = MutableStateFlow<ImportProgress?>(null)
    val importProgress: StateFlow<ImportProgress?> = _importProgress.asStateFlow()

    // Inspection / Detail State
    private val _selectedSongDetail = MutableStateFlow<Song?>(null)
    val selectedSongDetail: StateFlow<Song?> = _selectedSongDetail.asStateFlow()

    // Full Screen Player Sheet state
    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    // Queue Sheet state
    private val _isQueueSheetVisible = MutableStateFlow(false)
    val isQueueSheetVisible: StateFlow<Boolean> = _isQueueSheetVisible.asStateFlow()

    // Sub-screens: Settings, Equalizer, Appearance, ThemeEditor
    private val _isSettingsVisible = MutableStateFlow(false)
    val isSettingsVisible: StateFlow<Boolean> = _isSettingsVisible.asStateFlow()

    private val _isEqualizerVisible = MutableStateFlow(false)
    val isEqualizerVisible: StateFlow<Boolean> = _isEqualizerVisible.asStateFlow()

    private val _isAppearanceVisible = MutableStateFlow(false)
    val isAppearanceVisible: StateFlow<Boolean> = _isAppearanceVisible.asStateFlow()

    private val _isThemeEditorVisible = MutableStateFlow(false)
    val isThemeEditorVisible: StateFlow<Boolean> = _isThemeEditorVisible.asStateFlow()

    private val _editingTheme = MutableStateFlow<AppThemeConfig?>(null)
    val editingTheme: StateFlow<AppThemeConfig?> = _editingTheme.asStateFlow()

    val activeTheme: StateFlow<AppThemeConfig> = preferencesRepository.activeThemeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppThemeConfig.NOIR_ORIGINAL
        )

    val customThemes: StateFlow<List<AppThemeConfig>> = preferencesRepository.customThemesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Library Storage Stats
    private val _libraryStats = MutableStateFlow<LibraryStats?>(null)
    val libraryStats: StateFlow<LibraryStats?> = _libraryStats.asStateFlow()

    // Active subcategory view
    private val _subCategoryTitle = MutableStateFlow<String?>(null)
    val subCategoryTitle: StateFlow<String?> = _subCategoryTitle.asStateFlow()

    private val _subCategorySubtitle = MutableStateFlow<String?>(null)
    val subCategorySubtitle: StateFlow<String?> = _subCategorySubtitle.asStateFlow()

    private val _subCategorySongs = MutableStateFlow<List<Song>>(emptyList())
    val subCategorySongs: StateFlow<List<Song>> = _subCategorySongs.asStateFlow()

    private val _activePlaylist = MutableStateFlow<Playlist?>(null)
    val activePlaylist: StateFlow<Playlist?> = _activePlaylist.asStateFlow()

    // Status Message / Notification Banner
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        // Wire persistence from playerManager
        playerManager.onPlaybackStatePersist = { songId, pos ->
            viewModelScope.launch {
                preferencesRepository.saveLastPlaybackState(songId, pos)
            }
        }
        playerManager.onShufflePersist = { shuffle ->
            viewModelScope.launch {
                preferencesRepository.saveShuffleMode(shuffle)
            }
        }
        playerManager.onRepeatPersist = { mode ->
            viewModelScope.launch {
                preferencesRepository.saveRepeatMode(mode)
            }
        }
        playerManager.onSongStarted = { songId ->
            viewModelScope.launch {
                repository.recordSongPlayed(songId)
            }
        }

        // Restore last playback session without auto-playing
        viewModelScope.launch {
            val prefs = preferencesRepository.userPreferencesFlow.first()
            if (prefs.lastSongId > 0) {
                val songs = rawSongs.filter { it.isNotEmpty() }.first()
                val lastSong = songs.find { it.id == prefs.lastSongId }
                if (lastSong != null) {
                    playerManager.restoreSessionState(lastSong, prefs.lastPositionMs)
                }
            }
        }

        loadLibraryStats()
    }

    fun loadLibraryStats() {
        viewModelScope.launch {
            _libraryStats.value = repository.getLibraryStats(getApplication())
        }
    }

    fun cleanMissingFiles() {
        viewModelScope.launch {
            val (scanned, removed) = repository.auditAndCleanMissingSongs(getApplication())
            loadLibraryStats()
            if (removed > 0) {
                _userMessage.value = "Auditoría completa: $removed canciones inaccesibles retiradas de la biblioteca"
            } else {
                _userMessage.value = "Biblioteca en perfecto estado: $scanned archivos verificados"
            }
        }
    }

    fun clearCachedArt() {
        viewModelScope.launch {
            val freed = repository.clearCachedAlbumArt(getApplication())
            loadLibraryStats()
            _userMessage.value = "Caché liberada: ${Formatters.formatFileSize(freed)}"
        }
    }

    fun resetPreferences() {
        viewModelScope.launch {
            preferencesRepository.resetPreferences()
            _userMessage.value = "Preferencias restablecidas. Tu música y playlists están intactas."
        }
    }

    fun toggleAnimations(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAnimationsEnabled(enabled)
        }
    }

    fun showSettings(show: Boolean) {
        _isSettingsVisible.value = show
        if (show) loadLibraryStats()
    }

    fun showEqualizer(show: Boolean) {
        _isEqualizerVisible.value = show
    }

    // Playback actions
    fun playSong(song: Song, sourceQueue: List<Song> = rawSongs.value) {
        val targetQueue = if (sourceQueue.isEmpty()) listOf(song) else sourceQueue
        val index = targetQueue.indexOfFirst { it.id == song.id }.let { if (it >= 0) it else 0 }
        playerManager.playSongFromQueue(song, targetQueue, index, autoPlay = true)
    }

    fun playAlbum(albumName: String, startIndex: Int = 0) {
        viewModelScope.launch {
            val albumSongs = repository.getSongsByAlbum(albumName).first()
            if (albumSongs.isNotEmpty()) {
                val index = if (startIndex in albumSongs.indices) startIndex else 0
                playerManager.playSongFromQueue(albumSongs[index], albumSongs, index, autoPlay = true)
            }
        }
    }

    fun playPlaylist(playlist: Playlist, shuffle: Boolean = false) {
        viewModelScope.launch {
            val pSongs = repository.getSongsForPlaylist(playlist.id).first()
            if (pSongs.isNotEmpty()) {
                val targetList = if (shuffle) pSongs.shuffled() else pSongs
                playerManager.playSongFromQueue(targetList.first(), targetList, 0, autoPlay = true)
            } else {
                _userMessage.value = "La playlist \"${playlist.name}\" no tiene canciones aún"
            }
        }
    }

    fun playSelectedSongs(selectedSongs: List<Song>) {
        if (selectedSongs.isNotEmpty()) {
            playerManager.playSongFromQueue(selectedSongs.first(), selectedSongs, 0, autoPlay = true)
            _userMessage.value = "Reproduciendo ${selectedSongs.size} canciones seleccionadas"
        }
    }

    fun togglePlayPause() {
        if (currentSong.value == null && rawSongs.value.isNotEmpty()) {
            playSong(rawSongs.value.first())
        } else {
            playerManager.togglePlayPause()
        }
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun seekForward() {
        playerManager.seekForward()
    }

    fun seekBackward() {
        playerManager.seekBackward()
    }

    fun skipToNext() {
        playerManager.skipToNext()
    }

    fun skipToPrevious() {
        playerManager.skipToPrevious()
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    fun toggleRepeat() {
        playerManager.toggleRepeat()
    }

    fun addToPlayNext(song: Song) {
        playerManager.addToPlayNext(song)
        _userMessage.value = "Se reproducirá a continuación: \"${song.title}\""
    }

    fun addToEndOfQueue(song: Song) {
        playerManager.addToEndOfQueue(song)
        _userMessage.value = "Añadida al final de la cola: \"${song.title}\""
    }

    fun removeFromQueue(index: Int) {
        playerManager.removeFromQueue(index)
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        playerManager.moveQueueItem(fromIndex, toIndex)
    }

    fun clearQueue() {
        playerManager.clearQueueExceptCurrent()
        _userMessage.value = "Cola vaciada"
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song)
            if (currentSong.value?.id == song.id) {
                playerManager.updateCurrentSongFavorite(!song.isFavorite)
            }
            if (!song.isFavorite) {
                _userMessage.value = "Añadido a Favoritos"
            } else {
                _userMessage.value = "Eliminado de Favoritos"
            }
        }
    }

    fun expandPlayer(expanded: Boolean) {
        _isPlayerExpanded.value = expanded
    }

    fun showQueueSheet(show: Boolean) {
        _isQueueSheetVisible.value = show
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchFilter(filter: SearchFilterType) {
        _searchFilter.value = filter
    }

    fun setSortOption(sortOption: SortOption) {
        viewModelScope.launch {
            val currentAscending = userPreferences.value.sortAscending
            val newAscending = if (userPreferences.value.sortOption == sortOption) {
                !currentAscending
            } else {
                sortOption == SortOption.TITLE || sortOption == SortOption.ARTIST || sortOption == SortOption.ALBUM
            }
            preferencesRepository.updateSortOption(sortOption, newAscending)
        }
    }

    fun toggleGridView() {
        viewModelScope.launch {
            preferencesRepository.toggleGridView(!userPreferences.value.isGridView)
        }
    }

    fun setLibraryTab(tabIndex: Int) {
        viewModelScope.launch {
            preferencesRepository.setSelectedTab(tabIndex)
        }
    }

    fun importAudioFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return

        viewModelScope.launch {
            _importProgress.value = ImportProgress(
                isImporting = true,
                current = 0,
                total = uris.size,
                currentName = "Iniciando lectura de archivos…"
            )

            val result = repository.importAudioUris(
                context = getApplication(),
                uris = uris
            ) { current, total, name ->
                _importProgress.value = ImportProgress(
                    isImporting = true,
                    current = current,
                    total = total,
                    currentName = name
                )
            }

            _importProgress.value = ImportProgress(
                isImporting = false,
                current = uris.size,
                total = uris.size,
                result = result
            )

            loadLibraryStats()
            _userMessage.value = "Importadas: ${result.addedCount} | Omitidas: ${result.duplicateCount}${if (result.failedCount > 0) " | Fallidas: ${result.failedCount}" else ""}"
        }
    }

    fun dismissImportDialog() {
        _importProgress.value = null
    }

    fun selectSongForDetail(song: Song?) {
        _selectedSongDetail.value = song
    }

    fun openAlbum(albumName: String) {
        viewModelScope.launch {
            repository.getSongsByAlbum(albumName).collect { songs ->
                _subCategoryTitle.value = albumName
                _subCategorySubtitle.value = if (songs.isNotEmpty()) songs.first().artist else "Álbum"
                _subCategorySongs.value = songs
                _activePlaylist.value = null
            }
        }
    }

    fun openArtist(artistName: String) {
        viewModelScope.launch {
            repository.getSongsByArtist(artistName).collect { songs ->
                _subCategoryTitle.value = artistName
                val albumCount = songs.map { it.album }.distinct().size
                _subCategorySubtitle.value = "$albumCount álbumes • ${songs.size} canciones"
                _subCategorySongs.value = songs
                _activePlaylist.value = null
            }
        }
    }

    fun openFolder(folderName: String) {
        viewModelScope.launch {
            repository.getSongsByFolder(folderName).collect { songs ->
                _subCategoryTitle.value = folderName
                _subCategorySubtitle.value = "${songs.size} canciones"
                _subCategorySongs.value = songs
                _activePlaylist.value = null
            }
        }
    }

    fun openPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            _activePlaylist.value = playlist
            repository.getSongsForPlaylist(playlist.id).collect { songs ->
                _subCategoryTitle.value = playlist.name
                _subCategorySubtitle.value = "${songs.size} canciones"
                _subCategorySongs.value = songs
            }
        }
    }

    fun closeSubCategory() {
        _subCategoryTitle.value = null
        _subCategorySubtitle.value = null
        _subCategorySongs.value = emptyList()
        _activePlaylist.value = null
    }

    fun createPlaylist(name: String, description: String = "", coverUri: String? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repository.createPlaylist(name, description, coverUri)
            if (id > 0) {
                _userMessage.value = "Playlist \"$name\" creada"
            }
        }
    }

    fun editPlaylist(playlist: Playlist, name: String, desc: String, coverUri: String?) {
        viewModelScope.launch {
            repository.updatePlaylist(playlist.id, name, desc, coverUri)
            if (_activePlaylist.value?.id == playlist.id) {
                _activePlaylist.value = _activePlaylist.value?.copy(name = name, description = desc, coverUri = coverUri)
                _subCategoryTitle.value = name
            }
            _userMessage.value = "Playlist actualizada"
        }
    }

    fun duplicatePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            val newId = repository.duplicatePlaylist(playlist.id)
            if (newId > 0) {
                _userMessage.value = "Playlist \"${playlist.name}\" duplicada"
            }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
            if (_activePlaylist.value?.id == playlist.id) {
                closeSubCategory()
            }
            _userMessage.value = "Playlist eliminada"
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, song.id)
            _userMessage.value = "\"${song.title}\" añadida a la playlist"
        }
    }

    fun addSongsToPlaylist(playlistId: Long, songs: List<Song>) {
        viewModelScope.launch {
            repository.addSongsToPlaylist(playlistId, songs.map { it.id })
            _userMessage.value = "${songs.size} canciones añadidas a la playlist"
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
            _userMessage.value = "Canción retirada de la playlist"
        }
    }

    fun reorderPlaylistSong(playlistId: Long, songId: Long, newOrder: Int) {
        viewModelScope.launch {
            repository.reorderPlaylistSong(playlistId, songId, newOrder)
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            repository.deleteSong(song)
            if (_selectedSongDetail.value?.id == song.id) {
                _selectedSongDetail.value = null
            }
            if (currentSong.value?.id == song.id) {
                skipToNext()
            }
            loadLibraryStats()
            _userMessage.value = "Canción eliminada del registro de la biblioteca"
        }
    }

    fun deleteSongs(songs: List<Song>) {
        viewModelScope.launch {
            repository.deleteSongs(songs)
            if (songs.any { it.id == currentSong.value?.id }) {
                skipToNext()
            }
            loadLibraryStats()
            _userMessage.value = "${songs.size} canciones retiradas de la biblioteca"
        }
    }

    fun getActiveAudioSessionId(): Int {
        return NoirMediaSessionService.currentAudioSessionId
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun setAppearanceVisible(visible: Boolean) {
        _isAppearanceVisible.value = visible
    }

    fun openThemeEditor(themeToEdit: AppThemeConfig? = null) {
        _editingTheme.value = themeToEdit ?: activeTheme.value
        _isThemeEditorVisible.value = true
    }

    fun closeThemeEditor() {
        _isThemeEditorVisible.value = false
        _editingTheme.value = null
    }

    fun applyTheme(theme: AppThemeConfig) {
        viewModelScope.launch {
            preferencesRepository.updateActiveTheme(theme)
            _userMessage.value = "Tema \"${theme.name}\" aplicado"
        }
    }

    fun saveCustomTheme(theme: AppThemeConfig) {
        viewModelScope.launch {
            preferencesRepository.saveCustomTheme(theme)
            _userMessage.value = "Tema personalizado \"${theme.name}\" guardado"
        }
    }

    fun deleteCustomTheme(themeId: String) {
        viewModelScope.launch {
            preferencesRepository.deleteCustomTheme(themeId)
            _userMessage.value = "Tema personalizado eliminado"
        }
    }

    fun duplicateTheme(theme: AppThemeConfig, newName: String) {
        viewModelScope.launch {
            preferencesRepository.duplicateCustomTheme(theme, newName)
            _userMessage.value = "Tema duplicado como \"$newName\""
        }
    }

    fun resetTheme() {
        viewModelScope.launch {
            preferencesRepository.resetThemeToDefault()
            _userMessage.value = "Tema restablecido a NOIR Original"
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
        equalizerManager.release()
    }
}
