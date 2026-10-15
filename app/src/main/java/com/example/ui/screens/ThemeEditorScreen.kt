package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppThemeConfig
import com.example.ui.theme.NoirTheme
import com.example.ui.theme.ThemeColorProperty
import java.util.UUID

/**
 * Pantalla de edición manual de temas.
 * Permite personalizar individualmente las 12 propiedades de color,
 * visualizando cambios reactivos en tiempo real en la vista previa superior.
 */
@Composable
fun ThemeEditorScreen(
    initialTheme: AppThemeConfig,
    onApplyTheme: (AppThemeConfig) -> Unit,
    onSaveAsNewTheme: (AppThemeConfig) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado local para los cambios no guardados
    var workingTheme by remember { mutableStateOf(initialTheme) }
    var activePropertyForPicker by remember { mutableStateOf<ThemeColorProperty?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var newThemeName by remember { mutableStateOf("${initialTheme.name} (Copia)") }

    val originalReference = remember {
        AppThemeConfig.findPresetById(initialTheme.id) ?: AppThemeConfig.NOIR_ORIGINAL
    }

    BackHandler {
        onClose()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NoirTheme.colors.background)
    ) {
        // Encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Cancelar y volver",
                    tint = NoirTheme.colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Editor de Tema",
                    style = MaterialTheme.typography.titleLarge,
                    color = NoirTheme.colors.textPrimary
                )
                Text(
                    text = "Personalización manual de colores • ${workingTheme.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NoirTheme.colors.textSecondary
                )
            }

            // Botón Restaurar valores predeterminados
            IconButton(
                onClick = { workingTheme = originalReference },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Restaurar valores predeterminados",
                    tint = NoirTheme.colors.accent
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 1. Vista previa interactiva en tiempo real
            item {
                Text(
                    text = "VISTA PREVIA EN TIEMPO REAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = NoirTheme.colors.textSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                RealtimeThemePreview(themeConfig = workingTheme)

                Spacer(modifier = Modifier.height(20.dp))
            }

            // 2. Encabezado de la lista de las 12 propiedades
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PALETA DE 12 COLORES INDIVIDUALES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = NoirTheme.colors.textSecondary
                    )
                    Text(
                        text = "Toca para editar",
                        fontSize = 11.sp,
                        color = NoirTheme.colors.accent
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // 3. Las 12 propiedades configurables individualmente
            items(ThemeColorProperty.values()) { property ->
                val colorLong = workingTheme.getColorForProperty(property)
                val color = Color(colorLong)
                val hexCode = String.format("#%06X", (colorLong and 0xFFFFFFL))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NoirTheme.colors.surfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NoirTheme.colors.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { activePropertyForPicker = property }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Muestra del color actual con borde contrastante
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (color.luminance() > 0.5f) Color.Black.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = property.displayName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NoirTheme.colors.textPrimary
                                )
                                Text(
                                    text = property.description,
                                    fontSize = 11.sp,
                                    color = NoirTheme.colors.textSecondary,
                                    maxLines = 1
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = hexCode,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = NoirTheme.colors.accent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = "Editar color",
                                tint = NoirTheme.colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Barra inferior fija con botones de acción solicitados:
        // - Aplicar tema
        // - Cancelar cambios
        // - Guardar como nuevo tema
        // - Restaurar valores predeterminados
        Surface(
            color = NoirTheme.colors.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NoirTheme.colors.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Cancelar cambios (revierte y cierra)
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NoirTheme.colors.border),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirTheme.colors.textSecondary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancelar", fontSize = 12.sp)
                    }

                    // Botón Guardar como nuevo tema
                    OutlinedButton(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.weight(1.3f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NoirTheme.colors.accent),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirTheme.colors.accent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardar nuevo", fontSize = 12.sp, maxLines = 1)
                    }

                    // Botón Aplicar tema
                    Button(
                        onClick = {
                            val applied = workingTheme.copy(
                                isPreset = AppThemeConfig.findPresetById(workingTheme.id) != null && workingTheme == AppThemeConfig.findPresetById(workingTheme.id)
                            )
                            onApplyTheme(applied)
                            onClose()
                        },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NoirTheme.colors.button,
                            contentColor = if (NoirTheme.colors.isDark) Color.Black else Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aplicar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal de Selector de color cuando se pulsa una propiedad
    activePropertyForPicker?.let { property ->
        val currentColorLong = workingTheme.getColorForProperty(property)
        val originalColorLong = originalReference.getColorForProperty(property)

        ColorPickerDialog(
            property = property,
            initialColorLong = currentColorLong,
            originalColorLong = originalColorLong,
            onColorSelected = { newColor ->
                workingTheme = workingTheme.copyWithColor(property, newColor)
            },
            onDismiss = { activePropertyForPicker = null }
        )
    }

    // Diálogo para guardar como nuevo tema personalizado
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "Guardar como tema personalizado",
                    color = NoirTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Ingresa un nombre para tu nuevo tema. Estará disponible en tu biblioteca de temas personalizados.",
                        color = NoirTheme.colors.textSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newThemeName,
                        onValueChange = { newThemeName = it },
                        label = { Text("Nombre del tema") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NoirTheme.colors.textPrimary,
                            unfocusedTextColor = NoirTheme.colors.textPrimary,
                            focusedBorderColor = NoirTheme.colors.accent,
                            unfocusedBorderColor = NoirTheme.colors.border
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newThemeName.isNotBlank()) {
                            val newCustomTheme = workingTheme.copy(
                                id = "custom_${UUID.randomUUID()}",
                                name = newThemeName.trim(),
                                isPreset = false
                            )
                            onSaveAsNewTheme(newCustomTheme)
                            showSaveDialog = false
                            onClose()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NoirTheme.colors.button,
                        contentColor = if (NoirTheme.colors.isDark) Color.Black else Color.White
                    )
                ) {
                    Text("Guardar y aplicar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancelar", color = NoirTheme.colors.textSecondary)
                }
            },
            containerColor = NoirTheme.colors.surface
        )
    }
}

// Extensión auxiliar para calcular luminancia
private fun Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}
