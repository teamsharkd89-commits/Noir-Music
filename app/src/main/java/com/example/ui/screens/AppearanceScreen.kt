package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppThemeConfig
import com.example.ui.theme.NoirTheme

enum class QuickThemeMode(val title: String, val subtitle: String) {
    DARK("Oscuro", "Fondo negro con acento Cyan"),
    LIGHT("Claro", "Fondo blanco polar con detalles oscuros"),
    AMOLED("AMOLED", "Negro puro (#000000)"),
    CUSTOM("Personalizado", "Edición manual de colores")
}

/**
 * Pantalla principal de "Apariencia y personalización".
 * Incluye:
 * - Selector rápido de Modo (Oscuro, Claro, AMOLED, Personalizado)
 * - Vista previa interactiva
 * - Temas prediseñados (8 presets con selección inmediata y opción de modificar)
 * - Temas personalizados (crear, duplicar, editar, eliminar con confirmación)
 * - Acceso directo al Editor de Tema
 * - Restablecer apariencia con confirmación
 */
@Composable
fun AppearanceScreen(
    activeTheme: AppThemeConfig,
    customThemes: List<AppThemeConfig>,
    onSelectTheme: (AppThemeConfig) -> Unit,
    onOpenEditor: (AppThemeConfig) -> Unit,
    onDuplicateTheme: (AppThemeConfig, String) -> Unit,
    onDeleteCustomTheme: (String) -> Unit,
    onResetTheme: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBackClick()
    }

    var themeToDelete by remember { mutableStateOf<AppThemeConfig?>(null) }
    var themeToDuplicate by remember { mutableStateOf<AppThemeConfig?>(null) }
    var duplicateName by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NoirTheme.colors.background)
    ) {
        // Encabezado
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
                    tint = NoirTheme.colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Apariencia y personalización",
                    style = MaterialTheme.typography.titleLarge,
                    color = NoirTheme.colors.textPrimary
                )
                Text(
                    text = "Personaliza temas, contrastes y paletas de NOIR",
                    style = MaterialTheme.typography.bodySmall,
                    color = NoirTheme.colors.textSecondary
                )
            }

            IconButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Restablecer apariencia",
                    tint = NoirTheme.colors.accent
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Selector de modo rápido (Oscuro, Claro, AMOLED, Personalizado)
            item {
                SectionLabel(title = "SELECTOR RÁPIDO DE TEMA")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val currentMode = when {
                        !activeTheme.isPreset -> QuickThemeMode.CUSTOM
                        activeTheme.id == AppThemeConfig.ARCTIC.id -> QuickThemeMode.LIGHT
                        activeTheme.id == AppThemeConfig.AMOLED.id -> QuickThemeMode.AMOLED
                        else -> QuickThemeMode.DARK
                    }

                    QuickModeCard(
                        title = "Oscuro",
                        isSelected = currentMode == QuickThemeMode.DARK,
                        accentColor = Color(0xFF00E5FF),
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectTheme(AppThemeConfig.NOIR_ORIGINAL) }
                    )

                    QuickModeCard(
                        title = "Claro",
                        isSelected = currentMode == QuickThemeMode.LIGHT,
                        accentColor = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectTheme(AppThemeConfig.ARCTIC) }
                    )

                    QuickModeCard(
                        title = "AMOLED",
                        isSelected = currentMode == QuickThemeMode.AMOLED,
                        accentColor = Color(0xFF00E5FF),
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectTheme(AppThemeConfig.AMOLED) }
                    )

                    QuickModeCard(
                        title = "Manual",
                        isSelected = currentMode == QuickThemeMode.CUSTOM,
                        accentColor = Color(activeTheme.accentColor),
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenEditor(activeTheme) }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // 2. Vista previa en tiempo real
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel(title = "VISTA PREVIA DEL TEMA ACTIVO")
                    Text(
                        text = activeTheme.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NoirTheme.colors.accent
                    )
                }

                RealtimeThemePreview(themeConfig = activeTheme)

                Spacer(modifier = Modifier.height(14.dp))

                // Botón destacado para abrir el Editor Manual de Colores
                Button(
                    onClick = { onOpenEditor(activeTheme) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NoirTheme.colors.button,
                        contentColor = if (NoirTheme.colors.isDark) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Abrir Editor Manual de Colores (12 Propiedades)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 3. Temas prediseñados (8 Temas oficiales)
            item {
                SectionLabel(title = "TEMAS PREDISEÑADOS (8 ESTILOS NOIR)")

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeConfig.PRESETS.forEach { preset ->
                        PresetThemeRow(
                            preset = preset,
                            isActive = activeTheme.id == preset.id,
                            onApply = { onSelectTheme(preset) },
                            onEdit = { onOpenEditor(preset) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 4. Temas personalizados guardados por el usuario
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel(title = "MIS TEMAS PERSONALIZADOS (${customThemes.size})")

                    TextButton(onClick = { onOpenEditor(activeTheme) }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = NoirTheme.colors.accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Crear nuevo", fontSize = 12.sp, color = NoirTheme.colors.accent)
                    }
                }

                if (customThemes.isEmpty()) {
                    Surface(
                        color = NoirTheme.colors.surfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NoirTheme.colors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Aún no has guardado temas personalizados",
                                fontSize = 13.sp,
                                color = NoirTheme.colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Abre el editor de tema o modifica cualquier preset para guardarlo con tu propio nombre.",
                                fontSize = 11.sp,
                                color = NoirTheme.colors.textTertiary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        customThemes.forEach { theme ->
                            CustomThemeRow(
                                theme = theme,
                                isActive = activeTheme.id == theme.id,
                                onApply = { onSelectTheme(theme) },
                                onEdit = { onOpenEditor(theme) },
                                onDuplicate = {
                                    themeToDuplicate = theme
                                    duplicateName = "${theme.name} (Copia)"
                                },
                                onDelete = { themeToDelete = theme }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 5. Restablecer apariencia
            item {
                SectionLabel(title = "RESTABLECER APARIENCIA")

                Surface(
                    color = NoirTheme.colors.surfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NoirTheme.colors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showResetDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Restablecer a valores de fábrica",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NoirTheme.colors.textPrimary
                            )
                            Text(
                                text = "Vuelve al tema NOIR Original por defecto",
                                fontSize = 11.sp,
                                color = NoirTheme.colors.textSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = NoirTheme.colors.accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar tema personalizado
    themeToDelete?.let { theme ->
        AlertDialog(
            onDismissRequest = { themeToDelete = null },
            title = {
                Text(
                    text = "¿Eliminar tema?",
                    color = NoirTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar el tema \"${theme.name}\"? Esta acción no se puede deshacer.",
                    color = NoirTheme.colors.textSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCustomTheme(theme.id)
                        themeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NoirTheme.colors.error,
                        contentColor = Color.White
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { themeToDelete = null }) {
                    Text("Cancelar", color = NoirTheme.colors.textSecondary)
                }
            },
            containerColor = NoirTheme.colors.surface
        )
    }

    // Diálogo para duplicar tema
    themeToDuplicate?.let { theme ->
        AlertDialog(
            onDismissRequest = { themeToDuplicate = null },
            title = {
                Text(
                    text = "Duplicar tema",
                    color = NoirTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Crea una copia editable de \"${theme.name}\":",
                        color = NoirTheme.colors.textSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = duplicateName,
                        onValueChange = { duplicateName = it },
                        label = { Text("Nombre de la copia") },
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
                        if (duplicateName.isNotBlank()) {
                            onDuplicateTheme(theme, duplicateName.trim())
                            themeToDuplicate = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NoirTheme.colors.button,
                        contentColor = if (NoirTheme.colors.isDark) Color.Black else Color.White
                    )
                ) {
                    Text("Duplicar")
                }
            },
            dismissButton = {
                TextButton(onClick = { themeToDuplicate = null }) {
                    Text("Cancelar", color = NoirTheme.colors.textSecondary)
                }
            },
            containerColor = NoirTheme.colors.surface
        )
    }

    // Diálogo de confirmación para restablecer apariencia
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Restablecer apariencia",
                    color = NoirTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Deseas restablecer el tema visual al NOIR Original por defecto? Tus temas personalizados guardados no serán eliminados.",
                    color = NoirTheme.colors.textSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetTheme()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NoirTheme.colors.button,
                        contentColor = if (NoirTheme.colors.isDark) Color.Black else Color.White
                    )
                ) {
                    Text("Restablecer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar", color = NoirTheme.colors.textSecondary)
                }
            },
            containerColor = NoirTheme.colors.surface
        )
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = NoirTheme.colors.textSecondary,
        modifier = Modifier.padding(vertical = 10.dp)
    )
}

@Composable
private fun QuickModeCard(
    title: String,
    isSelected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) NoirTheme.colors.surfaceElevated else NoirTheme.colors.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) accentColor else NoirTheme.colors.border
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(accentColor)
                    .border(1.dp, if (isSelected) Color.White else Color.Transparent, CircleShape)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) NoirTheme.colors.textPrimary else NoirTheme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun PresetThemeRow(
    preset: AppThemeConfig,
    isActive: Boolean,
    onApply: () -> Unit,
    onEdit: () -> Unit
) {
    val bg = Color(preset.backgroundColor)
    val accent = Color(preset.accentColor)
    val surf = Color(preset.surfaceColor)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = NoirTheme.colors.surfaceElevated,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) NoirTheme.colors.accent else NoirTheme.colors.border
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onApply)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Muestra de color (background + accent pill)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = preset.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NoirTheme.colors.textPrimary
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = NoirTheme.colors.accent.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "ACTIVO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NoirTheme.colors.accent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = if (preset.isDark) "Tema oscuro • ${preset.name}" else "Tema claro polar",
                        fontSize = 11.sp,
                        color = NoirTheme.colors.textSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Botón editar preset para personalizarlo
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar preset",
                        tint = NoirTheme.colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (!isActive) {
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(
                        onClick = onApply,
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text(
                            "Aplicar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NoirTheme.colors.accent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomThemeRow(
    theme: AppThemeConfig,
    isActive: Boolean,
    onApply: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val bg = Color(theme.backgroundColor)
    val accent = Color(theme.accentColor)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = NoirTheme.colors.surfaceElevated,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) NoirTheme.colors.accent else NoirTheme.colors.border
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onApply)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = theme.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NoirTheme.colors.textPrimary
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = NoirTheme.colors.accent.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "ACTIVO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NoirTheme.colors.accent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Tema personalizado",
                        fontSize = 11.sp,
                        color = NoirTheme.colors.textSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Duplicar
                IconButton(
                    onClick = onDuplicate,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicar tema",
                        tint = NoirTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Editar
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar tema",
                        tint = NoirTheme.colors.accent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Eliminar (solo para personalizados)
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar tema",
                        tint = NoirTheme.colors.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
