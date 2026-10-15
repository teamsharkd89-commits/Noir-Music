package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playback.EqualizerManager
import com.example.ui.theme.NoirBackground
import com.example.ui.theme.NoirBorder
import com.example.ui.theme.NoirCyan
import com.example.ui.theme.NoirSurface
import com.example.ui.theme.NoirSurfaceElevated
import com.example.ui.theme.NoirTextPrimary
import com.example.ui.theme.NoirTextSecondary
import com.example.ui.theme.NoirTextTertiary

@Composable
fun EqualizerScreen(
    equalizerManager: EqualizerManager,
    audioSessionId: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBackClick()
    }

    val isHardwareSupported = remember(audioSessionId) {
        equalizerManager.initEqualizer(audioSessionId)
    }

    var isEqEnabled by remember { mutableStateOf(equalizerManager.isEnabled) }
    var selectedPreset by remember { mutableStateOf(equalizerManager.currentPresetIndex) }
    var bassBoostVal by remember { mutableFloatStateOf(equalizerManager.bassBoostStrength.toFloat()) }

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
                    text = "Ajustes de Sonido",
                    style = MaterialTheme.typography.titleLarge,
                    color = NoirTextPrimary
                )
                Text(
                    text = "Ecualizador de audio nativo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NoirTextSecondary
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            if (!isHardwareSupported || equalizerManager.bands.isEmpty()) {
                item {
                    Surface(
                        color = NoirSurfaceElevated,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NoirBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Equalizer,
                                    contentDescription = null,
                                    tint = NoirCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Ecualizador del Sistema",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = NoirTextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "El chipset o la versión de Android de este dispositivo gestiona los efectos de audio a través del subsistema central de Android. Puedes abrir el panel nativo del sistema para calibrar el sonido en tiempo real sin simulación.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NoirTextSecondary
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = {
                                    equalizerManager.openSystemAudioEffectPanel(audioSessionId)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NoirCyan,
                                    contentColor = NoirBackground
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Abrir ecualizador del sistema", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Hardware Equalizer Controls
                item {
                    // Master Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NoirSurfaceElevated)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Activar Ecualizador",
                            style = MaterialTheme.typography.titleMedium,
                            color = NoirTextPrimary
                        )

                        Switch(
                            checked = isEqEnabled,
                            onCheckedChange = { checked ->
                                isEqEnabled = checked
                                equalizerManager.equalizer?.enabled = checked
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NoirBackground,
                                checkedTrackColor = NoirCyan,
                                uncheckedThumbColor = NoirTextTertiary,
                                uncheckedTrackColor = NoirSurface
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Presets
                    if (equalizerManager.presets.isNotEmpty()) {
                        Text(
                            text = "PERFILES DE SONIDO",
                            style = MaterialTheme.typography.labelSmall,
                            color = NoirTextTertiary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            itemsIndexed(equalizerManager.presets) { index, name ->
                                val isSelected = selectedPreset == index.toShort()
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedPreset = index.toShort()
                                        equalizerManager.usePreset(index.toShort())
                                    },
                                    label = { Text(name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = NoirSurface,
                                        labelColor = NoirTextSecondary,
                                        selectedContainerColor = NoirCyan.copy(alpha = 0.15f),
                                        selectedLabelColor = NoirCyan
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = NoirBorder,
                                        selectedBorderColor = NoirCyan
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Frequency Bands
                    Text(
                        text = "BANDAS DE FRECUENCIA",
                        style = MaterialTheme.typography.labelSmall,
                        color = NoirTextTertiary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(equalizerManager.bands) { band ->
                    var sliderVal by remember { mutableFloatStateOf(band.currentLevel.toFloat()) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NoirSurfaceElevated)
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val freqLabel = if (band.centerFreqHz >= 1000) {
                                "${band.centerFreqHz / 1000} kHz"
                            } else {
                                "${band.centerFreqHz} Hz"
                            }
                            Text(
                                text = freqLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = NoirTextPrimary
                            )
                            val db = (sliderVal / 100f)
                            Text(
                                text = String.format("%+.1f dB", db),
                                style = MaterialTheme.typography.labelSmall,
                                color = NoirCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = sliderVal,
                            onValueChange = {
                                sliderVal = it
                                selectedPreset = -1
                                equalizerManager.setBandLevel(band.index, it.toInt().toShort())
                            },
                            valueRange = band.minLevel.toFloat()..band.maxLevel.toFloat(),
                            enabled = isEqEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = NoirCyan,
                                activeTrackColor = NoirCyan,
                                inactiveTrackColor = NoirBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Bass Boost if supported
                if (equalizerManager.isBassBoostSupported) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "REALCE DE GRAVES (BASS BOOST)",
                            style = MaterialTheme.typography.labelSmall,
                            color = NoirTextTertiary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(NoirSurfaceElevated)
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Graves Profundos",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = NoirTextPrimary
                                )
                                Text(
                                    text = "${(bassBoostVal / 10).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NoirCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Slider(
                                value = bassBoostVal,
                                onValueChange = {
                                    bassBoostVal = it
                                    equalizerManager.setBassBoost(it.toInt().toShort())
                                },
                                valueRange = 0f..1000f,
                                enabled = isEqEnabled,
                                colors = SliderDefaults.colors(
                                    thumbColor = NoirCyan,
                                    activeTrackColor = NoirCyan,
                                    inactiveTrackColor = NoirBorder
                                )
                            )
                        }
                    }
                }

                // System Panel Shortcut
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            equalizerManager.openSystemAudioEffectPanel(audioSessionId)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NoirSurfaceElevated,
                            contentColor = NoirTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Launch, contentDescription = null, tint = NoirCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Abrir panel de sonido avanzado del sistema")
                    }
                }
            }
        }
    }
}
