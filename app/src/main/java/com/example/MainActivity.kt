package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.Song
import com.example.ui.components.ImportProgressDialog
import com.example.ui.screens.AddMultipleSongsToPlaylistDialog
import com.example.ui.screens.AddToPlaylistDialog
import com.example.ui.screens.CreatePlaylistDialog
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.AppearanceScreen
import com.example.ui.screens.FullScreenPlayer
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.MiniPlayer
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.QueueSheet
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SongDetailSheet
import com.example.ui.screens.ThemeEditorScreen
import com.example.ui.theme.AppThemeConfig
import com.example.ui.theme.NoirBackground
import com.example.ui.theme.NoirCyan
import com.example.ui.theme.NoirMusicTheme
import com.example.ui.theme.NoirSurface
import com.example.ui.theme.NoirSurfaceElevated
import com.example.ui.theme.NoirTextTertiary
import com.example.ui.theme.NoirTheme
import com.example.ui.viewmodel.MusicViewModel
import com.example.ui.viewmodel.MusicViewModelFactory

enum class NoirNavigationTab(
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME(
        R.string.nav_home,
        Icons.Filled.Home,
        Icons.Outlined.Home,
        "nav_home"
    ),
    LIBRARY(
        R.string.nav_library,
        Icons.Filled.LibraryMusic,
        Icons.Outlined.LibraryMusic,
        "nav_library"
    ),
    SEARCH(
        R.string.nav_search,
        Icons.Filled.Search,
        Icons.Outlined.Search,
        "nav_search"
    ),
    PLAYLISTS(
        R.string.nav_playlists,
        Icons.AutoMirrored.Filled.QueueMusic,
        Icons.AutoMirrored.Outlined.QueueMusic,
        "nav_playlists"
    )
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory = MusicViewModelFactory(application)
        val viewModel = ViewModelProvider(this, factory)[MusicViewModel::class.java]

        setContent {
            val activeTheme by viewModel.activeTheme.collectAsStateWithLifecycle()
            NoirMusicTheme(themeConfig = activeTheme) {
                NoirMusicApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun NoirMusicApp(viewModel: MusicViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Request Notification permission for Android 13+ (TIRAMISU)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Navigation state
    var currentNavIndex by rememberSaveable { mutableIntStateOf(0) }

    // Observables from ViewModel
    val rawSongs by viewModel.rawSongs.collectAsStateWithLifecycle()
    val sortedSongs by viewModel.sortedSongs.collectAsStateWithLifecycle()
    val favoriteSongs by viewModel.favoriteSongs.collectAsStateWithLifecycle()
    val playbackHistory by viewModel.playbackHistory.collectAsStateWithLifecycle()
    val mostPlayedSongs by viewModel.mostPlayedSongs.collectAsStateWithLifecycle()
    val recentlyAdded by viewModel.recentlyAdded.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val albumNames by viewModel.albumNames.collectAsStateWithLifecycle()
    val artistNames by viewModel.artistNames.collectAsStateWithLifecycle()
    val folderNames by viewModel.folderNames.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchFilter by viewModel.searchFilter.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val importProgress by viewModel.importProgress.collectAsStateWithLifecycle()
    val selectedSongDetail by viewModel.selectedSongDetail.collectAsStateWithLifecycle()

    val subCategoryTitle by viewModel.subCategoryTitle.collectAsStateWithLifecycle()
    val subCategorySubtitle by viewModel.subCategorySubtitle.collectAsStateWithLifecycle()
    val subCategorySongs by viewModel.subCategorySongs.collectAsStateWithLifecycle()
    val activePlaylist by viewModel.activePlaylist.collectAsStateWithLifecycle()

    val isSettingsVisible by viewModel.isSettingsVisible.collectAsStateWithLifecycle()
    val isEqualizerVisible by viewModel.isEqualizerVisible.collectAsStateWithLifecycle()
    val isAppearanceVisible by viewModel.isAppearanceVisible.collectAsStateWithLifecycle()
    val isThemeEditorVisible by viewModel.isThemeEditorVisible.collectAsStateWithLifecycle()
    val editingTheme by viewModel.editingTheme.collectAsStateWithLifecycle()
    val activeTheme by viewModel.activeTheme.collectAsStateWithLifecycle()
    val customThemes by viewModel.customThemes.collectAsStateWithLifecycle()
    val libraryStats by viewModel.libraryStats.collectAsStateWithLifecycle()

    // Playback observables
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val queue by viewModel.queue.collectAsStateWithLifecycle()
    val currentQueueIndex by viewModel.currentQueueIndex.collectAsStateWithLifecycle()
    val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsStateWithLifecycle()
    val isQueueSheetVisible by viewModel.isQueueSheetVisible.collectAsStateWithLifecycle()
    val playbackError by viewModel.playbackError.collectAsStateWithLifecycle()

    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    // Dialog states
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var songForAddToPlaylist by remember { mutableStateOf<Song?>(null) }
    var songsForAddToPlaylist by remember { mutableStateOf<List<Song>?>(null) }

    // Native multiple audio file picker (Storage Access Framework)
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importAudioFiles(uris)
        }
    }

    val triggerImport = {
        val audioMimeTypes = arrayOf(
            "audio/*",
            "application/ogg",
            "application/x-flac"
        )
        audioPickerLauncher.launch(audioMimeTypes)
    }

    // Handle user feedback messages
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    // Handle playback errors
    LaunchedEffect(playbackError) {
        playbackError?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            viewModel.playerManager.clearPlaybackError()
        }
    }

    Scaffold(
        containerColor = NoirTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Persistent MiniPlayer directly above the NavigationBar
                if (currentSong != null && !isPlayerExpanded) {
                    MiniPlayer(
                        song = currentSong,
                        isPlaying = isPlaying,
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onSkipNext = { viewModel.skipToNext() },
                        onClick = { viewModel.expandPlayer(true) }
                    )
                }

                NavigationBar(
                    containerColor = NoirTheme.colors.bottomNav,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("noir_bottom_navigation")
                ) {
                    NoirNavigationTab.values().forEachIndexed { index, tab ->
                        val selected = currentNavIndex == index
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentNavIndex = index },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = stringResource(tab.titleRes),
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(tab.titleRes),
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NoirTheme.colors.selectedItem,
                                selectedTextColor = NoirTheme.colors.selectedItem,
                                unselectedIconColor = NoirTheme.colors.icons.copy(alpha = 0.6f),
                                unselectedTextColor = NoirTheme.colors.textSecondary,
                                indicatorColor = NoirTheme.colors.surfaceElevated
                            ),
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentNavIndex,
                label = "nav_transition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> HomeScreen(
                        songs = rawSongs,
                        recentlyAdded = recentlyAdded,
                        playlists = playlists,
                        artistNames = artistNames,
                        onImportClick = triggerImport,
                        onSongClick = { song ->
                            viewModel.playSong(song, rawSongs)
                        },
                        onSongMoreClick = { song -> viewModel.selectSongForDetail(song) },
                        onArtistClick = { artist ->
                            viewModel.openArtist(artist)
                            currentNavIndex = 1
                        },
                        onPlaylistClick = { playlist ->
                            viewModel.openPlaylist(playlist)
                            currentNavIndex = 3
                        }
                    )

                    1 -> LibraryScreen(
                        songs = sortedSongs,
                        allSongs = rawSongs,
                        favoriteSongs = favoriteSongs,
                        playbackHistory = playbackHistory,
                        mostPlayedSongs = mostPlayedSongs,
                        albumNames = albumNames,
                        artistNames = artistNames,
                        folderNames = folderNames,
                        userPreferences = userPreferences,
                        currentPlayingSongId = currentSong?.id,
                        subCategoryTitle = subCategoryTitle,
                        subCategorySubtitle = subCategorySubtitle,
                        subCategorySongs = subCategorySongs,
                        onTabSelected = { tab -> viewModel.setLibraryTab(tab) },
                        onSortOptionSelected = { option -> viewModel.setSortOption(option) },
                        onToggleGridView = { viewModel.toggleGridView() },
                        onImportClick = triggerImport,
                        onOpenSettingsClick = { viewModel.showSettings(true) },
                        onSongClick = { song ->
                            val sourceList = when (userPreferences.selectedTab) {
                                1 -> favoriteSongs
                                2 -> playbackHistory
                                3 -> mostPlayedSongs
                                else -> sortedSongs
                            }
                            viewModel.playSong(song, sourceList)
                        },
                        onSongMoreClick = { song -> viewModel.selectSongForDetail(song) },
                        onOpenAlbum = { album -> viewModel.openAlbum(album) },
                        onOpenArtist = { artist -> viewModel.openArtist(artist) },
                        onOpenFolder = { folder -> viewModel.openFolder(folder) },
                        onCloseSubCategory = { viewModel.closeSubCategory() },
                        onPlayAllSubCategory = {
                            if (subCategorySongs.isNotEmpty()) {
                                viewModel.playSong(subCategorySongs.first(), subCategorySongs)
                            }
                        },
                        onPlaySelectedSongs = { selectedList ->
                            if (selectedList.isNotEmpty()) {
                                viewModel.playSong(selectedList.first(), selectedList)
                            }
                        },
                        onAddSelectedToPlaylist = { selectedList ->
                            songsForAddToPlaylist = selectedList
                        },
                        onDeleteSelectedSongs = { selectedList ->
                            viewModel.deleteSongs(selectedList)
                        }
                    )

                    2 -> SearchScreen(
                        query = searchQuery,
                        filter = searchFilter,
                        searchResults = searchResults,
                        allSongs = rawSongs,
                        onQueryChange = { q -> viewModel.updateSearchQuery(q) },
                        onFilterChange = { f -> viewModel.setSearchFilter(f) },
                        onSongClick = { song ->
                            viewModel.playSong(song, searchResults.ifEmpty { rawSongs })
                        },
                        onSongMoreClick = { song -> viewModel.selectSongForDetail(song) }
                    )

                    3 -> PlaylistsScreen(
                        playlists = playlists,
                        activePlaylist = activePlaylist,
                        playlistSongs = subCategorySongs,
                        currentPlayingSongId = currentSong?.id,
                        isPlaying = isPlaying,
                        onCreatePlaylistClick = { showCreatePlaylistDialog = true },
                        onPlaylistClick = { playlist -> viewModel.openPlaylist(playlist) },
                        onPlayPlaylist = { playlist, shuffle ->
                            viewModel.playPlaylist(playlist, shuffle)
                        },
                        onDuplicatePlaylist = { playlist ->
                            viewModel.duplicatePlaylist(playlist)
                        },
                        onEditPlaylist = { playlist, name, desc, coverUri ->
                            viewModel.editPlaylist(playlist, name, desc, coverUri)
                        },
                        onDeletePlaylistClick = { playlist -> viewModel.deletePlaylist(playlist) },
                        onRemoveSongFromPlaylist = { playlistId, songId ->
                            viewModel.removeSongFromPlaylist(playlistId, songId)
                        },
                        onReorderSong = { playlistId, songId, newOrder ->
                            viewModel.reorderPlaylistSong(playlistId, songId, newOrder)
                        },
                        onClosePlaylistView = { viewModel.closeSubCategory() },
                        onSongClick = { song ->
                            viewModel.playSong(song, subCategorySongs)
                        },
                        onSongMoreClick = { song -> viewModel.selectSongForDetail(song) }
                    )
                }
            }
        }
    }

    // Full-Screen TIDAL-inspired Player Overlay
    AnimatedVisibility(
        visible = isPlayerExpanded && currentSong != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        BackHandler {
            viewModel.expandPlayer(false)
        }

        FullScreenPlayer(
            song = currentSong,
            isPlaying = isPlaying,
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            isShuffleEnabled = isShuffleEnabled,
            repeatMode = repeatMode,
            queueSize = queue.size,
            onClose = { viewModel.expandPlayer(false) },
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onSeekTo = { pos -> viewModel.seekTo(pos) },
            onSkipNext = { viewModel.skipToNext() },
            onSkipPrevious = { viewModel.skipToPrevious() },
            onSeekForward = { viewModel.seekForward() },
            onSeekBackward = { viewModel.seekBackward() },
            onToggleShuffle = { viewModel.toggleShuffle() },
            onToggleRepeat = { viewModel.toggleRepeat() },
            onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
            onOpenQueue = { viewModel.showQueueSheet(true) },
            onOpenSongDetails = { song -> viewModel.selectSongForDetail(song) }
        )
    }

    // Queue Bottom Sheet
    if (isQueueSheetVisible) {
        QueueSheet(
            queue = queue,
            currentIndex = currentQueueIndex,
            isPlaying = isPlaying,
            onDismiss = { viewModel.showQueueSheet(false) },
            onSongSelected = { index ->
                if (index in queue.indices) {
                    viewModel.playSong(queue[index], queue)
                }
            },
            onRemoveFromQueue = { index ->
                viewModel.removeFromQueue(index)
            },
            onMoveItem = { from, to ->
                viewModel.moveQueueItem(from, to)
            },
            onClearQueue = {
                viewModel.clearQueue()
            }
        )
    }

    // Modal dialog for Import progress
    importProgress?.let { progress ->
        ImportProgressDialog(
            current = progress.current,
            total = progress.total,
            currentName = progress.currentName,
            isCompleted = !progress.isImporting,
            addedCount = progress.result?.addedCount ?: 0,
            duplicateCount = progress.result?.duplicateCount ?: 0,
            failedCount = progress.result?.failedCount ?: 0,
            onDismiss = { viewModel.dismissImportDialog() }
        )
    }

    // Technical Specs / Inspection Bottom Sheet
    selectedSongDetail?.let { song ->
        SongDetailSheet(
            song = song,
            onDismiss = { viewModel.selectSongForDetail(null) },
            onPlayNowClick = {
                viewModel.playSong(song, rawSongs)
                viewModel.selectSongForDetail(null)
            },
            onPlayNextClick = {
                viewModel.addToPlayNext(song)
                viewModel.selectSongForDetail(null)
            },
            onAddToQueueClick = {
                viewModel.addToEndOfQueue(song)
                viewModel.selectSongForDetail(null)
            },
            onToggleFavoriteClick = {
                viewModel.toggleFavorite(song)
            },
            onAddToPlaylistClick = {
                songForAddToPlaylist = song
                viewModel.selectSongForDetail(null)
            },
            onDeleteSongClick = {
                viewModel.deleteSong(song)
            }
        )
    }

    // Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { name, desc ->
                viewModel.createPlaylist(name, desc)
            }
        )
    }

    // Add Song to Playlist Dialog
    songForAddToPlaylist?.let { song ->
        AddToPlaylistDialog(
            song = song,
            playlists = playlists,
            onDismiss = { songForAddToPlaylist = null },
            onPlaylistSelected = { playlistId ->
                viewModel.addSongToPlaylist(playlistId, song)
            },
            onCreateNewPlaylist = {
                showCreatePlaylistDialog = true
            }
        )
    }

    // Add Multiple Songs to Playlist Dialog
    songsForAddToPlaylist?.let { songs ->
        AddMultipleSongsToPlaylistDialog(
            songs = songs,
            playlists = playlists,
            onDismiss = { songsForAddToPlaylist = null },
            onPlaylistSelected = { playlistId ->
                viewModel.addSongsToPlaylist(playlistId, songs)
            },
            onCreateNewPlaylist = {
                showCreatePlaylistDialog = true
            }
        )
    }

    // Settings Screen Overlay
    if (isSettingsVisible) {
        SettingsScreen(
            userPreferences = userPreferences,
            libraryStats = libraryStats,
            onBackClick = { viewModel.showSettings(false) },
            onOpenEqualizerClick = { viewModel.showEqualizer(true) },
            onOpenAppearanceClick = { viewModel.setAppearanceVisible(true) },
            onToggleAnimations = { enabled -> viewModel.toggleAnimations(enabled) },
            onCleanMissingFiles = { viewModel.cleanMissingFiles() },
            onClearCachedArt = { viewModel.clearCachedArt() },
            onResetPreferences = { viewModel.resetPreferences() }
        )
    }

    // Appearance Screen Overlay
    if (isAppearanceVisible) {
        AppearanceScreen(
            activeTheme = activeTheme,
            customThemes = customThemes,
            onSelectTheme = { viewModel.applyTheme(it) },
            onOpenEditor = { viewModel.openThemeEditor(it) },
            onDuplicateTheme = { theme, name -> viewModel.duplicateTheme(theme, name) },
            onDeleteCustomTheme = { viewModel.deleteCustomTheme(it) },
            onResetTheme = { viewModel.resetTheme() },
            onBackClick = { viewModel.setAppearanceVisible(false) }
        )
    }

    // Theme Editor Screen Overlay
    if (isThemeEditorVisible) {
        editingTheme?.let { themeToEdit ->
            ThemeEditorScreen(
                initialTheme = themeToEdit,
                onApplyTheme = { viewModel.applyTheme(it) },
                onSaveAsNewTheme = { viewModel.saveCustomTheme(it) },
                onClose = { viewModel.closeThemeEditor() }
            )
        }
    }

    // Equalizer Screen Overlay
    if (isEqualizerVisible) {
        EqualizerScreen(
            equalizerManager = viewModel.equalizerManager,
            audioSessionId = viewModel.getActiveAudioSessionId(),
            onBackClick = { viewModel.showEqualizer(false) }
        )
    }
}

