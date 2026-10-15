package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.entity.Song
import com.example.ui.theme.NoirBackground
import com.example.ui.theme.NoirBorder
import com.example.ui.theme.NoirCyan
import com.example.ui.theme.NoirPlatinum
import com.example.ui.theme.NoirSurface
import com.example.ui.theme.NoirSurfaceCard
import com.example.ui.theme.NoirSurfaceElevated
import com.example.ui.theme.NoirTextPrimary
import com.example.ui.theme.NoirTextSecondary
import com.example.ui.theme.NoirTextTertiary
import com.example.util.Formatters

@Composable
fun AlbumArtImage(
    artUri: String?,
    modifier: Modifier = Modifier,
    size: Dp? = null,
    cornerRadius: Dp = 8.dp,
    fallbackIcon: ImageVector = Icons.Default.MusicNote
) {
    val sizeModifier = if (size != null) modifier.size(size) else modifier
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = sizeModifier
            .clip(shape)
            .background(NoirSurfaceCard)
            .border(1.dp, NoirBorder.copy(alpha = 0.6f), shape),
        contentAlignment = Alignment.Center
    ) {
        if (!artUri.isNullOrBlank()) {
            AsyncImage(
                model = artUri,
                contentDescription = "Carátula del álbum",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
            )
        } else {
            // Elegant geometric vinyl groove placeholder
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                NoirSurfaceElevated,
                                NoirSurface,
                                Color(0xFF0A0A0C)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = null,
                    tint = NoirTextTertiary.copy(alpha = 0.7f),
                    modifier = Modifier.size(if (size != null) size * 0.45f else 32.dp)
                )
            }
        }
    }
}

@Composable
fun QualityBadge(
    badgeText: String,
    modifier: Modifier = Modifier
) {
    val isHiRes = badgeText.contains("FLAC") || badgeText.contains("HI-RES") || badgeText.contains("LOSSLESS")
    val badgeColor = if (isHiRes) NoirCyan else NoirTextSecondary
    val bgColor = if (isHiRes) NoirCyan.copy(alpha = 0.12f) else NoirSurfaceElevated

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, badgeColor.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Text(
            text = badgeText,
            color = badgeColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun SongListItem(
    song: Song,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    isSelectMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectToggle: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = if (isSelectMode && onSelectToggle != null) onSelectToggle else onClick)
            .background(if (isSelected) NoirCyan.copy(alpha = 0.08f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectMode) {
            androidx.compose.material3.Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectToggle?.invoke() },
                colors = androidx.compose.material3.CheckboxDefaults.colors(
                    checkedColor = NoirCyan,
                    checkmarkColor = NoirBackground,
                    uncheckedColor = NoirTextTertiary
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Box(modifier = Modifier.size(52.dp)) {
            AlbumArtImage(
                artUri = song.albumArtUri,
                size = 52.dp,
                cornerRadius = 8.dp
            )
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Reproduciendo",
                        tint = NoirCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isPlaying) NoirCyan else NoirTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NoirTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = " • ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NoirTextTertiary
                )

                Text(
                    text = Formatters.formatDuration(song.durationMs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = NoirTextTertiary
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        val badge = Formatters.getAudioQualityBadge(song.mimeType, song.bitrate, song.sampleRate)
        QualityBadge(badgeText = badge)

        if (!isSelectMode) {
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("song_more_${song.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opciones",
                    tint = NoirTextSecondary
                )
            }
        }
    }
}

@Composable
fun SongGridCard(
    song: Song,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    isSelectMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectToggle: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = if (isSelectMode && onSelectToggle != null) onSelectToggle else onClick)
            .background(if (isSelected) NoirCyan.copy(alpha = 0.12f) else NoirSurfaceCard)
            .border(
                1.dp,
                if (isSelected) NoirCyan else NoirBorder.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            AlbumArtImage(
                artUri = song.albumArtUri,
                cornerRadius = 8.dp,
                modifier = Modifier.matchParentSize()
            )

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Reproduciendo",
                        tint = NoirCyan,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            val badge = Formatters.getAudioQualityBadge(song.mimeType, song.bitrate, song.sampleRate)
            QualityBadge(
                badgeText = badge,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = song.title,
            style = MaterialTheme.typography.titleMedium,
            color = if (isPlaying) NoirCyan else NoirTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = NoirTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (!isSelectMode) {
                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = NoirTextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SubCategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCircular: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val shape = if (isCircular) CircleShape else RoundedCornerShape(10.dp)
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(NoirSurfaceElevated, NoirSurfaceCard)
                    )
                )
                .border(1.dp, NoirBorder, shape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NoirCyan,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = NoirTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = NoirTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun NoirEmptyState(
    title: String,
    description: String,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.GraphicEq
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(NoirSurfaceElevated)
                .border(1.dp, NoirCyan.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NoirCyan,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = NoirTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = NoirTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        if (actionButtonText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NoirCyan,
                    contentColor = NoirBackground
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("empty_state_action_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PlaylistAdd,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = actionButtonText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}

@Composable
fun ImportProgressDialog(
    current: Int,
    total: Int,
    currentName: String,
    isCompleted: Boolean,
    addedCount: Int = 0,
    duplicateCount: Int = 0,
    failedCount: Int = 0,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (isCompleted) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NoirSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NoirBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NoirCyan,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Importación completada",
                        style = MaterialTheme.typography.titleLarge,
                        color = NoirTextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NoirSurfaceElevated, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "✓ $addedCount canciones añadidas a la biblioteca",
                            color = NoirTextPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (duplicateCount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• $duplicateCount duplicados omitidos",
                                color = NoirTextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (failedCount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "! $failedCount archivos no decodificables",
                                color = NoirPlatinum,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NoirCyan,
                            contentColor = NoirBackground
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("import_done_btn")
                    ) {
                        Text("Explorar biblioteca", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = stringResource(R.string.importing_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = NoirTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Procesando $current de $total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NoirTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val progressFraction = if (total > 0) current.toFloat() / total.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = NoirCyan,
                        trackColor = NoirSurfaceElevated
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentName,
                        style = MaterialTheme.typography.bodySmall,
                        color = NoirTextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun SubCategoryHeader(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NoirBackground)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = NoirTextPrimary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = NoirTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NoirTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
