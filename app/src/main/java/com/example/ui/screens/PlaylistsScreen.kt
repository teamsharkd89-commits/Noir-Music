package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.entity.Playlist
import com.example.data.local.entity.Song
import com.example.ui.components.AlbumArtImage
import com.example.ui.components.NoirEmptyState
import com.example.ui.components.QualityBadge
import com.example.ui.theme.NoirBackground
import com.example.ui.theme.NoirBorder
import com.example.ui.theme.NoirCyan
import com.example.ui.theme.NoirError
import com.example.ui.theme.NoirSurface
import com.example.ui.theme.NoirSurfaceCard
import com.example.ui.theme.NoirSurfaceElevated
import com.example.ui.theme.NoirTextPrimary
import com.example.ui.theme.NoirTextSecondary
import com.example.ui.theme.NoirTextTertiary
import com.example.util.Formatters

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    activePlaylist: Playlist?,
    playlistSongs: List<Song>,
    currentPlayingSongId: Long?,
    isPlaying: Boolean,
    onCreatePlaylistClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onPlayPlaylist: (Playlist, Boolean) -> Unit, // (playlist, shuffle)
    onDuplicatePlaylist: (Playlist) -> Unit,
    onEditPlaylist: (Playlist, String, String, String?) -> Unit,
    onDeletePlaylistClick: (Playlist) -> Unit,
    onRemoveSongFromPlaylist: (playlistId: Long, songId: Long) -> Unit,
    onReorderSong: (playlistId: Long, songId: Long, newOrder: Int) -> Unit,
    onClosePlaylistView: () -> Unit,
    onSongClick: (Song) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }
    var showPlaylistMenu by remember { mutableStateOf(false) }

    // Playlist Details Screen (TIDAL-inspired)
    if (activePlaylist != null) {
        BackHandler {
            onClosePlaylistView()
        }

        // Determine cover art: custom coverUri, or first song's album art, or null
        val heroCover = activePlaylist.coverUri
            ?: playlistSongs.firstOrNull { !it.albumArtUri.isNullOrBlank() }?.albumArtUri

        val totalDurationMs = playlistSongs.sumOf { it.durationMs }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(NoirBackground)
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClosePlaylistView,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = NoirTextPrimary
                    )
                }

                Box {
                    IconButton(
                        onClick = { showPlaylistMenu = true },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones de playlist",
                            tint = NoirTextPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = showPlaylistMenu,
                        onDismissRequest = { showPlaylistMenu = false },
                        modifier = Modifier
                            .background(NoirSurface)
                            .border(1.dp, NoirBorder, RoundedCornerShape(8.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar detalles", color = NoirTextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = NoirCyan)
                            },
                            onClick = {
                                showPlaylistMenu = false
                                showEditDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicar playlist", color = NoirTextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NoirTextSecondary)
                            },
                            onClick = {
                                showPlaylistMenu = false
                                onDuplicatePlaylist(activePlaylist)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar playlist", color = NoirError) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = NoirError)
                            },
                            onClick = {
                                showPlaylistMenu = false
                                playlistToDelete = activePlaylist
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                // Large Hero Artwork and Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(NoirSurfaceElevated)
                                .border(1.dp, NoirBorder, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!heroCover.isNullOrBlank()) {
                                AsyncImage(
                                    model = heroCover,
                                    contentDescription = "Carátula de playlist",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = null,
                                    tint = NoirCyan,
                                    modifier = Modifier.size(80.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = activePlaylist.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = NoirTextPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (activePlaylist.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activePlaylist.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = NoirTextSecondary,
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${playlistSongs.size} canciones • ${Formatters.formatTotalDuration(totalDurationMs)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NoirTextTertiary,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Action Buttons: Play in order & Shuffle
                        if (playlistSongs.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = { onPlayPlaylist(activePlaylist, false) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NoirCyan,
                                        contentColor = NoirBackground
                                    ),
                                    shape = RoundedCornerShape(22.dp),
                                    modifier = Modifier
                                        .height(44.dp)
                                        .testTag("play_playlist_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Reproducir", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Button(
                                    onClick = { onPlayPlaylist(activePlaylist, true) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NoirSurfaceElevated,
                                        contentColor = NoirTextPrimary
                                    ),
                                    shape = RoundedCornerShape(22.dp),
                                    modifier = Modifier
                                        .height(44.dp)
                                        .testTag("shuffle_playlist_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = null,
                                        tint = NoirCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Aleatorio", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (playlistSongs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            NoirEmptyState(
                                title = "Playlist vacía",
                                description = "Explora tu biblioteca o canciones de inicio y añade canciones a esta lista con el menú de opciones (⋮).",
                                icon = Icons.Default.QueueMusic
                            )
                        }
                    }
                } else {
                    itemsIndexed(playlistSongs) { index, song ->
                        val isPlayingCurrent = song.id == currentPlayingSongId

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSongClick(song) }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Track number or playing equalizer
                            if (isPlayingCurrent) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Reproduciendo",
                                    tint = NoirCyan,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(start = 2.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NoirTextTertiary,
                                    modifier = Modifier.width(22.dp),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            AlbumArtImage(
                                artUri = song.albumArtUri,
                                size = 48.dp,
                                cornerRadius = 8.dp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isPlayingCurrent) NoirCyan else NoirTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = song.artist,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = NoirTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Text(
                                        text = " • ${Formatters.formatDuration(song.durationMs)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NoirTextTertiary
                                    )
                                }
                            }

                            // Reorder buttons (Move Up / Down)
                            if (index > 0) {
                                IconButton(
                                    onClick = { onReorderSong(activePlaylist.id, song.id, index - 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Subir",
                                        tint = NoirTextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (index < playlistSongs.size - 1) {
                                IconButton(
                                    onClick = { onReorderSong(activePlaylist.id, song.id, index + 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Bajar",
                                        tint = NoirTextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Song more options
                            IconButton(
                                onClick = { onSongMoreClick(song) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Opciones",
                                    tint = NoirTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Edit Playlist Dialog
        if (showEditDialog) {
            EditPlaylistDialog(
                playlist = activePlaylist,
                onDismiss = { showEditDialog = false },
                onSave = { name, desc, coverUri ->
                    onEditPlaylist(activePlaylist, name, desc, coverUri)
                    showEditDialog = false
                }
            )
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmDialog && playlistToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmDialog = false
                    playlistToDelete = null
                },
                title = { Text("¿Eliminar playlist?", color = NoirTextPrimary) },
                text = {
                    Text(
                        "Se eliminará la playlist \"${playlistToDelete?.name}\". Las canciones originales permanecerán seguras en tu biblioteca y en tu almacenamiento.",
                        color = NoirTextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            playlistToDelete?.let { onDeletePlaylistClick(it) }
                            showDeleteConfirmDialog = false
                            playlistToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NoirError)
                    ) {
                        Text("Eliminar", color = NoirTextPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteConfirmDialog = false
                        playlistToDelete = null
                    }) {
                        Text("Cancelar", color = NoirTextSecondary)
                    }
                },
                containerColor = NoirSurface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        return
    }

    // Playlists Main List Screen
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NoirBackground)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Playlists",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NoirTextPrimary
                )
                Text(
                    text = "${playlists.size} listas de reproducción",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NoirTextSecondary
                )
            }

            Button(
                onClick = onCreatePlaylistClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NoirCyan,
                    contentColor = NoirBackground
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .height(40.dp)
                    .testTag("create_playlist_header_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Nueva", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        if (playlists.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                NoirEmptyState(
                    title = "Sin playlists creadas",
                    description = "Crea listas de reproducción ilimitadas para organizar tus temas favoritos, álbumes o sesiones de audio.",
                    actionButtonText = "Crear primera playlist",
                    onActionClick = onCreatePlaylistClick,
                    icon = Icons.Default.QueueMusic
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp, top = 8.dp)
            ) {
                itemsIndexed(playlists) { _, playlist ->
                    PlaylistItemRow(
                        playlist = playlist,
                        onClick = { onPlaylistClick(playlist) },
                        onDeleteClick = {
                            playlistToDelete = playlist
                            showDeleteConfirmDialog = true
                        }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog for Main List
    if (showDeleteConfirmDialog && playlistToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                playlistToDelete = null
            },
            title = { Text("¿Eliminar playlist?", color = NoirTextPrimary) },
            text = {
                Text(
                    "Se eliminará la playlist \"${playlistToDelete?.name}\". Las canciones originales permanecerán seguras en tu biblioteca y almacenamiento.",
                    color = NoirTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        playlistToDelete?.let { onDeletePlaylistClick(it) }
                        showDeleteConfirmDialog = false
                        playlistToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NoirError)
                ) {
                    Text("Eliminar", color = NoirTextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog = false
                    playlistToDelete = null
                }) {
                    Text("Cancelar", color = NoirTextSecondary)
                }
            },
            containerColor = NoirSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun PlaylistItemRow(
    playlist: Playlist,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NoirSurfaceElevated)
                .border(1.dp, NoirBorder, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (!playlist.coverUri.isNullOrBlank()) {
                AsyncImage(
                    model = playlist.coverUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = null,
                    tint = NoirCyan,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                color = NoirTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (playlist.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = playlist.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NoirTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar",
                tint = NoirTextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun EditPlaylistDialog(
    playlist: Playlist,
    onDismiss: () -> Unit,
    onSave: (name: String, desc: String, coverUri: String?) -> Unit
) {
    var name by remember { mutableStateOf(playlist.name) }
    var description by remember { mutableStateOf(playlist.description) }
    var coverUri by remember { mutableStateOf(playlist.coverUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { coverUri = it.toString() }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NoirSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NoirBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Editar Playlist",
                    style = MaterialTheme.typography.titleLarge,
                    color = NoirTextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Cover Picker Box
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NoirSurfaceElevated)
                        .border(1.dp, NoirBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (!coverUri.isNullOrBlank()) {
                        AsyncImage(
                            model = coverUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = NoirCyan,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Foto", fontSize = 11.sp, color = NoirTextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la lista") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoirCyan,
                        unfocusedBorderColor = NoirBorder,
                        focusedTextColor = NoirTextPrimary,
                        unfocusedTextColor = NoirTextPrimary,
                        focusedLabelColor = NoirCyan,
                        unfocusedLabelColor = NoirTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoirCyan,
                        unfocusedBorderColor = NoirBorder,
                        focusedTextColor = NoirTextPrimary,
                        unfocusedTextColor = NoirTextPrimary,
                        focusedLabelColor = NoirCyan,
                        unfocusedLabelColor = NoirTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = NoirTextSecondary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name, description, coverUri)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NoirCyan,
                            contentColor = NoirBackground
                        ),
                        shape = RoundedCornerShape(20.dp),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
