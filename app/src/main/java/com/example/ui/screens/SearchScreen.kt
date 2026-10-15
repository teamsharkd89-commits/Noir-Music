package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.ui.components.NoirEmptyState
import com.example.ui.components.SongListItem
import com.example.ui.theme.NoirBackground
import com.example.ui.theme.NoirBorder
import com.example.ui.theme.NoirCyan
import com.example.ui.theme.NoirSurface
import com.example.ui.theme.NoirSurfaceElevated
import com.example.ui.theme.NoirTextPrimary
import com.example.ui.theme.NoirTextSecondary
import com.example.ui.theme.NoirTextTertiary
import com.example.ui.viewmodel.SearchFilterType

@Composable
fun SearchScreen(
    query: String,
    filter: SearchFilterType,
    searchResults: List<Song>,
    allSongs: List<Song>,
    onQueryChange: (String) -> Unit,
    onFilterChange: (SearchFilterType) -> Unit,
    onSongClick: (Song) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredResults = remember(searchResults, filter) {
        when (filter) {
            SearchFilterType.ALL -> searchResults
            SearchFilterType.SONGS -> searchResults
            SearchFilterType.ARTISTS -> searchResults.filter { it.artist.contains(query, ignoreCase = true) }
            SearchFilterType.ALBUMS -> searchResults.filter { it.album.contains(query, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NoirBackground)
    ) {
        // Search Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Buscar",
                style = MaterialTheme.typography.headlineMedium,
                color = NoirTextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar Input
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        "Canción, artista o álbum…",
                        color = NoirTextTertiary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = if (query.isNotBlank()) NoirCyan else NoirTextTertiary
                    )
                },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar",
                                tint = NoirTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = NoirSurfaceElevated,
                    unfocusedContainerColor = NoirSurface,
                    focusedBorderColor = NoirCyan,
                    unfocusedBorderColor = NoirBorder,
                    focusedTextColor = NoirTextPrimary,
                    unfocusedTextColor = NoirTextPrimary,
                    cursorColor = NoirCyan
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_text_input")
            )

            if (query.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                // Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        SearchChip(
                            label = "Todos",
                            selected = filter == SearchFilterType.ALL,
                            onClick = { onFilterChange(SearchFilterType.ALL) }
                        )
                    }
                    item {
                        SearchChip(
                            label = "Canciones",
                            selected = filter == SearchFilterType.SONGS,
                            onClick = { onFilterChange(SearchFilterType.SONGS) }
                        )
                    }
                    item {
                        SearchChip(
                            label = "Artistas",
                            selected = filter == SearchFilterType.ARTISTS,
                            onClick = { onFilterChange(SearchFilterType.ARTISTS) }
                        )
                    }
                    item {
                        SearchChip(
                            label = "Álbumes",
                            selected = filter == SearchFilterType.ALBUMS,
                            onClick = { onFilterChange(SearchFilterType.ALBUMS) }
                        )
                    }
                }
            }
        }

        // Search Content
        if (query.isBlank()) {
            // Empty Search State
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
                        icon = Icons.Default.Search
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Exploración rápida",
                        style = MaterialTheme.typography.titleMedium,
                        color = NoirTextSecondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    val sampleArtists = remember(allSongs) {
                        allSongs.map { it.artist }.distinct().take(6)
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(sampleArtists) { artist ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = NoirSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, NoirBorder),
                                modifier = Modifier.clickable { onQueryChange(artist) }
                            ) {
                                Text(
                                    text = artist,
                                    color = NoirTextPrimary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Recientes en tu colección",
                        style = MaterialTheme.typography.titleMedium,
                        color = NoirTextSecondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        items(allSongs.take(5)) { song ->
                            SongListItem(
                                song = song,
                                onClick = { onSongClick(song) },
                                onMoreClick = { onSongMoreClick(song) }
                            )
                        }
                    }
                }
            }
        } else if (filteredResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                NoirEmptyState(
                    title = "Sin resultados",
                    description = stringResource(R.string.no_search_results, query),
                    icon = Icons.Default.Search
                )
            }
        } else {
            // Results list
            Text(
                text = "${filteredResults.size} coincidencias",
                style = MaterialTheme.typography.labelSmall,
                color = NoirCyan,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(filteredResults) { song ->
                    SongListItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        onMoreClick = { onSongMoreClick(song) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = NoirSurface,
            labelColor = NoirTextSecondary,
            selectedContainerColor = NoirCyan.copy(alpha = 0.15f),
            selectedLabelColor = NoirCyan
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = NoirBorder,
            selectedBorderColor = NoirCyan.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    )
}
