package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.Song
import com.example.data.preferences.SortOption
import com.example.data.preferences.UserPreferences
import com.example.ui.components.NoirEmptyState
import com.example.ui.components.SongGridCard
import com.example.ui.components.SongListItem
import com.example.ui.components.SubCategoryCard
import com.example.ui.components.SubCategoryHeader
import com.example.ui.theme.NoirBackground
import com.example.ui.theme.NoirBorder
import com.example.ui.theme.NoirCyan
import com.example.ui.theme.NoirError
import com.example.ui.theme.NoirSurface
import com.example.ui.theme.NoirSurfaceElevated
import com.example.ui.theme.NoirTextPrimary
import com.example.ui.theme.NoirTextSecondary
import com.example.ui.theme.NoirTextTertiary
import com.example.util.Formatters

@Composable
fun LibraryScreen(
    songs: List<Song>,
    allSongs: List<Song>,
    favoriteSongs: List<Song>,
    playbackHistory: List<Song>,
    mostPlayedSongs: List<Song>,
    albumNames: List<String>,
    artistNames: List<String>,
    folderNames: List<String>,
    userPreferences: UserPreferences,
    currentPlayingSongId: Long?,
    subCategoryTitle: String?,
    subCategorySubtitle: String?,
    subCategorySongs: List<Song>,
    onTabSelected: (Int) -> Unit,
    onSortOptionSelected: (SortOption) -> Unit,
    onToggleGridView: () -> Unit,
    onImportClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    onOpenAlbum: (String) -> Unit,
    onOpenArtist: (String) -> Unit,
    onOpenFolder: (String) -> Unit,
    onCloseSubCategory: () -> Unit,
    onPlayAllSubCategory: () -> Unit,
    onPlaySelectedSongs: (List<Song>) -> Unit,
    onAddSelectedToPlaylist: (List<Song>) -> Unit,
    onDeleteSelectedSongs: (List<Song>) -> Unit,
    modifier: Modifier = Modifier
) {
    // Multi-selection state
    var isSelectMode by remember { mutableStateOf(false) }
    var selectedSongIds by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // If in multi-select mode, BackHandler exits multi-select
    if (isSelectMode) {
        BackHandler {
            isSelectMode = false
            selectedSongIds = emptySet()
        }
    }

    // If user is currently inspecting an Album, Artist or Folder
    if (subCategoryTitle != null) {
        BackHandler {
            onCloseSubCategory()
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(NoirBackground)
        ) {
            SubCategoryHeader(
                title = subCategoryTitle,
                subtitle = subCategorySubtitle ?: "",
                onBackClick = onCloseSubCategory
            )

            // Play all button in subcategory header
            if (subCategorySongs.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onPlayAllSubCategory,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NoirCyan,
                            contentColor = NoirBackground
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reproducir todo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(subCategorySongs) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = song.id == currentPlayingSongId,
                        onClick = { onSongClick(song) },
                        onMoreClick = { onSongMoreClick(song) }
                    )
                }
            }
        }
        return
    }

    var showSortMenu by remember { mutableStateOf(false) }
    val tabs = listOf("Canciones", "Me gusta", "Historial", "Más escuchadas", "Álbumes", "Artistas", "Carpetas")
    val currentTab = userPreferences.selectedTab.coerceIn(0, tabs.size - 1)

    val activeList = when (currentTab) {
        0 -> songs
        1 -> favoriteSongs
        2 -> playbackHistory
        3 -> mostPlayedSongs
        else -> songs
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NoirBackground)
    ) {
        // Multi-selection Top Bar OR Standard Top Bar
        if (isSelectMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NoirSurfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        isSelectMode = false
                        selectedSongIds = emptySet()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = NoirTextPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${selectedSongIds.size} seleccionadas",
                        style = MaterialTheme.typography.titleMedium,
                        color = NoirTextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Select All / Deselect All
                    IconButton(onClick = {
                        if (selectedSongIds.size == activeList.size) {
                            selectedSongIds = emptySet()
                        } else {
                            selectedSongIds = activeList.map { it.id }.toSet()
                        }
                    }) {
                        Icon(Icons.Default.SelectAll, contentDescription = "Seleccionar todo", tint = NoirCyan)
                    }

                    if (selectedSongIds.isNotEmpty()) {
                        // Play Selected
                        IconButton(onClick = {
                            val selected = activeList.filter { selectedSongIds.contains(it.id) }
                            onPlaySelectedSongs(selected)
                            isSelectMode = false
                            selectedSongIds = emptySet()
                        }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir selección", tint = NoirCyan)
                        }

                        // Add to Playlist
                        IconButton(onClick = {
                            val selected = activeList.filter { selectedSongIds.contains(it.id) }
                            onAddSelectedToPlaylist(selected)
                            isSelectMode = false
                            selectedSongIds = emptySet()
                        }) {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = "Añadir a playlist", tint = NoirTextPrimary)
                        }

                        // Remove from Library
                        IconButton(onClick = {
                            showDeleteConfirmDialog = true
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Quitar de la biblioteca", tint = NoirError)
                        }
                    }
                }
            }
        } else {
            // Standard Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Biblioteca",
                        style = MaterialTheme.typography.headlineMedium,
                        color = NoirTextPrimary
                    )
                    if (allSongs.isNotEmpty()) {
                        val totalDurationMs = allSongs.sumOf { it.durationMs }
                        Text(
                            text = "${allSongs.size} canciones • ${Formatters.formatTotalDuration(totalDurationMs)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NoirTextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Multi-select toggle button
                    if (currentTab in 0..3 && activeList.isNotEmpty()) {
                        IconButton(onClick = { isSelectMode = true }) {
                            Icon(
                                imageVector = Icons.Default.SelectAll,
                                contentDescription = "Selección múltiple",
                                tint = NoirTextSecondary
                            )
                        }
                    }

                    // View toggle (List / Grid)
                    IconButton(
                        onClick = onToggleGridView,
                        modifier = Modifier.testTag("toggle_grid_view_btn")
                    ) {
                        Icon(
                            imageVector = if (userPreferences.isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Cambiar vista",
                            tint = NoirTextPrimary
                        )
                    }

                    // Sort Button
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("sort_menu_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Ordenar",
                                tint = NoirTextPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier
                                .background(NoirSurface)
                                .border(1.dp, NoirBorder, RoundedCornerShape(8.dp))
                        ) {
                            SortOption.values().forEach { option ->
                                val isSelected = userPreferences.sortOption == option
                                val label = when (option) {
                                    SortOption.TITLE -> "Título"
                                    SortOption.ARTIST -> "Artista"
                                    SortOption.ALBUM -> "Álbum"
                                    SortOption.DURATION -> "Duración"
                                    SortOption.DATE_ADDED -> "Añadido recientemente"
                                }

                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSelected) NoirCyan else NoirTextPrimary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = if (userPreferences.sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                                    contentDescription = null,
                                                    tint = NoirCyan,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onSortOptionSelected(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Import Button
                    IconButton(
                        onClick = onImportClick,
                        modifier = Modifier.testTag("library_import_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Importar música",
                            tint = NoirCyan
                        )
                    }

                    // Settings Button
                    IconButton(
                        onClick = onOpenSettingsClick,
                        modifier = Modifier.testTag("library_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes",
                            tint = NoirTextPrimary
                        )
                    }
                }
            }
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = currentTab,
            containerColor = NoirBackground,
            contentColor = NoirCyan,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                if (currentTab in tabPositions.indices) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab]),
                        color = NoirCyan,
                        height = 2.dp
                    )
                }
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, tabTitle ->
                val isSelected = currentTab == index
                Tab(
                    selected = isSelected,
                    onClick = {
                        isSelectMode = false
                        selectedSongIds = emptySet()
                        onTabSelected(index)
                    },
                    text = {
                        Text(
                            text = tabTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) NoirTextPrimary else NoirTextTertiary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        // Tab Content
        if (allSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                NoirEmptyState(
                    title = stringResource(R.string.empty_library_title),
                    description = stringResource(R.string.empty_library_desc),
                    actionButtonText = stringResource(R.string.btn_import_music),
                    onActionClick = onImportClick,
                    icon = Icons.Default.LibraryMusic
                )
            }
        } else {
            Crossfade(targetState = currentTab, label = "library_tab") { tabIndex ->
                when (tabIndex) {
                    0, 1, 2, 3 -> {
                        // Song list tabs: 0 (All), 1 (Favorites), 2 (History), 3 (Most Played)
                        val currentList = when (tabIndex) {
                            0 -> songs
                            1 -> favoriteSongs
                            2 -> playbackHistory
                            3 -> mostPlayedSongs
                            else -> songs
                        }

                        if (currentList.isEmpty()) {
                            val (emptyTitle, emptyDesc, emptyIcon) = when (tabIndex) {
                                1 -> Triple("Sin favoritos", "Toca el corazón en cualquier pista para marcarla como favorita.", Icons.Default.Favorite)
                                2 -> Triple("Sin historial de reproducción", "Las canciones que escuches aparecerán aquí.", Icons.Default.History)
                                3 -> Triple("Sin pistas reproducidas", "Tus pistas más escuchadas se ordenarán aquí.", Icons.Default.TrendingUp)
                                else -> Triple("Sin pistas", "Importa música a la biblioteca.", Icons.Default.LibraryMusic)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                NoirEmptyState(
                                    title = emptyTitle,
                                    description = emptyDesc,
                                    icon = emptyIcon
                                )
                            }
                        } else {
                            if (userPreferences.isGridView) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 80.dp)
                                ) {
                                    items(currentList) { song ->
                                        val isSelected = selectedSongIds.contains(song.id)
                                        SongGridCard(
                                            song = song,
                                            isPlaying = song.id == currentPlayingSongId,
                                            isSelectMode = isSelectMode,
                                            isSelected = isSelected,
                                            onSelectToggle = {
                                                selectedSongIds = if (isSelected) {
                                                    selectedSongIds - song.id
                                                } else {
                                                    selectedSongIds + song.id
                                                }
                                            },
                                            onClick = { onSongClick(song) },
                                            onMoreClick = { onSongMoreClick(song) }
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 80.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    items(currentList) { song ->
                                        val isSelected = selectedSongIds.contains(song.id)
                                        SongListItem(
                                            song = song,
                                            isPlaying = song.id == currentPlayingSongId,
                                            isSelectMode = isSelectMode,
                                            isSelected = isSelected,
                                            onSelectToggle = {
                                                selectedSongIds = if (isSelected) {
                                                    selectedSongIds - song.id
                                                } else {
                                                    selectedSongIds + song.id
                                                }
                                            },
                                            onClick = { onSongClick(song) },
                                            onMoreClick = { onSongMoreClick(song) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    4 -> {
                        // Álbumes
                        val albums = remember(allSongs) {
                            allSongs.groupBy { it.album }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 80.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(albums.keys.toList()) { albumName ->
                                val albumSongs = albums[albumName] ?: emptyList()
                                val artistName = albumSongs.firstOrNull()?.artist ?: "Varios artistas"
                                SubCategoryCard(
                                    title = albumName,
                                    subtitle = "$artistName • ${albumSongs.size} canciones",
                                    icon = Icons.Default.Album,
                                    onClick = { onOpenAlbum(albumName) }
                                )
                            }
                        }
                    }

                    5 -> {
                        // Artistas
                        val artists = remember(allSongs) {
                            allSongs.groupBy { it.artist }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 80.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(artists.keys.toList()) { artistName ->
                                val artistSongs = artists[artistName] ?: emptyList()
                                val albumCount = artistSongs.map { it.album }.distinct().size
                                SubCategoryCard(
                                    title = artistName,
                                    subtitle = "$albumCount álbumes • ${artistSongs.size} canciones",
                                    icon = Icons.Default.Person,
                                    isCircular = true,
                                    onClick = { onOpenArtist(artistName) }
                                )
                            }
                        }
                    }

                    6 -> {
                        // Carpetas
                        val folders = remember(allSongs) {
                            allSongs.groupBy { it.folderName }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 80.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(folders.keys.toList()) { folderName ->
                                val folderSongs = folders[folderName] ?: emptyList()
                                SubCategoryCard(
                                    title = folderName,
                                    subtitle = "${folderSongs.size} pistas de audio",
                                    icon = Icons.Default.Folder,
                                    onClick = { onOpenFolder(folderName) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation for multi-selection
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("¿Quitar de la biblioteca?", color = NoirTextPrimary) },
            text = {
                Text(
                    "Se quitarán ${selectedSongIds.size} canciones del registro de NOIR Music. Los archivos originales en el almacenamiento de tu teléfono permanecerán intactos.",
                    color = NoirTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val selected = activeList.filter { selectedSongIds.contains(it.id) }
                        onDeleteSelectedSongs(selected)
                        showDeleteConfirmDialog = false
                        isSelectMode = false
                        selectedSongIds = emptySet()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NoirError)
                ) {
                    Text("Quitar", color = NoirTextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar", color = NoirTextSecondary)
                }
            },
            containerColor = NoirSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
