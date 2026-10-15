package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferences
import com.example.data.repository.LibraryStats
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
fun SettingsScreen(
    userPreferences: UserPreferences,
    libraryStats: LibraryStats?,
    onBackClick: () -> Unit,
    onOpenEqualizerClick: () -> Unit,
    onOpenAppearanceClick: () -> Unit,
    onToggleAnimations: (Boolean) -> Unit,
    onCleanMissingFiles: () -> Unit,
    onClearCachedArt: () -> Unit,
    onResetPreferences: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBackClick()
    }

    var showResetDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NoirBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
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

            Column {
                Text(
                    text = "Ajustes",
                    style = MaterialTheme.typography.titleLarge,
                    color = NoirTextPrimary
                )
                Text(
                    text = "Configuración y almacenamiento de NOIR Music",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NoirTextSecondary
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Sección Apariencia y Personalización
            item {
                SectionHeader(title = "APARIENCIA Y PERSONALIZACIÓN")

                SettingsNavRow(
                    title = "Apariencia y personalización",
                    subtitle = "Temas prediseñados, modo AMOLED, colores y editor manual",
                    icon = Icons.Default.Palette,
                    onClick = onOpenAppearanceClick
                )

                Spacer(modifier = Modifier.height(18.dp))
            }

            // Sección Sonido y Audio
            item {
                SectionHeader(title = "AUDIO Y PROCESAMIENTO")

                SettingsNavRow(
                    title = "Ecualizador y Ajustes de Sonido",
                    subtitle = "Perfiles, bandas de frecuencia y panel de efectos",
                    icon = Icons.Default.Equalizer,
                    onClick = onOpenEqualizerClick
                )

                Spacer(modifier = Modifier.height(18.dp))
            }

            // Sección Experiencia y Animaciones
            item {
                SectionHeader(title = "EXPERIENCIA DE USUARIO")

                SettingsSwitchRow(
                    title = "Animaciones fluidas",
                    subtitle = "Transiciones elásticas en mini reproductor y carátulas",
                    checked = userPreferences.animationsEnabled,
                    onCheckedChange = onToggleAnimations
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsInfoRow(
                    title = "Recordar última pista",
                    subtitle = "Restaura la sesión al abrir la app sin reproducir automáticamente",
                    value = "Activado"
                )

                Spacer(modifier = Modifier.height(18.dp))
            }

            // Sección Almacenamiento y Mantenimiento
            item {
                SectionHeader(title = "GESTIÓN DE ALMACENAMIENTO")

                if (libraryStats != null) {
                    Surface(
                        color = NoirSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NoirBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pistas registradas", color = NoirTextSecondary, fontSize = 13.sp)
                                Text("${libraryStats.songCount}", color = NoirTextPrimary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Álbumes / Artistas", color = NoirTextSecondary, fontSize = 13.sp)
                                Text("${libraryStats.albumCount} / ${libraryStats.artistCount}", color = NoirTextPrimary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Duración total de colección", color = NoirTextSecondary, fontSize = 13.sp)
                                Text(Formatters.formatTotalDuration(libraryStats.totalDurationMs), color = NoirCyan, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Carátulas en caché local", color = NoirTextSecondary, fontSize = 13.sp)
                                Text(Formatters.formatFileSize(libraryStats.cachedArtSizeBytes), color = NoirTextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                SettingsActionRow(
                    title = "Comprobar biblioteca y limpiar inaccesibles",
                    subtitle = "Detecta archivos eliminados del almacenamiento del teléfono",
                    icon = Icons.Default.SdCard,
                    onClick = onCleanMissingFiles
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsActionRow(
                    title = "Limpiar carátulas en caché",
                    subtitle = "Libera espacio en la memoria interna de la aplicación",
                    icon = Icons.Default.CleaningServices,
                    onClick = onClearCachedArt
                )

                Spacer(modifier = Modifier.height(18.dp))
            }

            // Sección Restablecer y Sistema
            item {
                SectionHeader(title = "PREFERENCIAS Y RESTABLECIMIENTO")

                SettingsActionRow(
                    title = "Restablecer preferencias predeterminadas",
                    subtitle = "Restaura ordenación y vista (No borra tu música ni tus playlists)",
                    icon = Icons.Default.RestartAlt,
                    onClick = { showResetDialog = true },
                    isDestructive = true
                )

                Spacer(modifier = Modifier.height(18.dp))
            }

            // Sección Acerca de
            item {
                SectionHeader(title = "ACERCA DE LA APLICACIÓN")

                SettingsInfoRow(
                    title = "NOIR Music",
                    subtitle = "Reproductor local nativo para audiófilos",
                    value = "v1.0"
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsActionRow(
                    title = "Licencias de código abierto",
                    subtitle = "Media3 ExoPlayer, Jetpack Compose, Room, Coil",
                    icon = Icons.Default.Info,
                    onClick = { showLicensesDialog = true }
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Reset Preferences Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("¿Restablecer preferencias?", color = NoirTextPrimary) },
            text = {
                Text(
                    "Se restaurarán las opciones de vista, ordenación y animaciones a los valores iniciales de fábrica. Tu música importada y tus playlists permanecerán totalmente intactas.",
                    color = NoirTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetPreferences()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NoirCyan, contentColor = NoirBackground)
                ) {
                    Text("Restablecer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar", color = NoirTextSecondary)
                }
            },
            containerColor = NoirSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Open Source Licenses Dialog
    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = { Text("Licencias de Código Abierto", color = NoirTextPrimary) },
            text = {
                Column {
                    Text(
                        "NOIR Music está construido con tecnologías nativas de código abierto líderes de la industria:",
                        color = NoirTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("• AndroidX Media3 ExoPlayer (Apache 2.0)", color = NoirTextPrimary, fontSize = 12.sp)
                    Text("• Jetpack Compose & Material 3 (Apache 2.0)", color = NoirTextPrimary, fontSize = 12.sp)
                    Text("• Room Persistence Library (Apache 2.0)", color = NoirTextPrimary, fontSize = 12.sp)
                    Text("• Coil Image Loading (Apache 2.0)", color = NoirTextPrimary, fontSize = 12.sp)
                    Text("• Kotlinx Coroutines & Flow (Apache 2.0)", color = NoirTextPrimary, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLicensesDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NoirCyan, contentColor = NoirBackground)
                ) {
                    Text("Aceptar", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = NoirSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = NoirTextTertiary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsNavRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NoirSurfaceElevated)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = NoirCyan, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = NoirTextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = NoirTextSecondary)
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NoirSurfaceElevated)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = NoirTextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = NoirTextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NoirBackground,
                checkedTrackColor = NoirCyan,
                uncheckedThumbColor = NoirTextTertiary,
                uncheckedTrackColor = NoirSurface
            )
        )
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NoirSurfaceElevated)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isDestructive) NoirError else NoirTextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isDestructive) NoirError else NoirTextPrimary
            )
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = NoirTextSecondary)
        }
    }
}

@Composable
private fun SettingsInfoRow(
    title: String,
    subtitle: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NoirSurfaceElevated)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = NoirTextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = NoirTextSecondary)
        }
        Text(value, style = MaterialTheme.typography.labelLarge, color = NoirCyan)
    }
}
