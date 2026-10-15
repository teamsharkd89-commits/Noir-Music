package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.NoirTheme
import com.example.ui.theme.ThemeColorProperty
import kotlin.math.roundToInt

/**
 * Paleta cromática rápida con tonos populares para selección inmediata.
 */
private val QuickPalette = listOf(
    Color(0xFF080808), // Noir Negro
    Color(0xFF000000), // AMOLED
    Color(0xFF141414), // Gris carbón
    Color(0xFF1E293B), // Slate oscuro
    Color(0xFFFFFFFF), // Blanco puro
    Color(0xFFE2E8F0), // Hielo
    Color(0xFF00E5FF), // Cyan TIDAL
    Color(0xFF38BDF8), // Azul cielo
    Color(0xFF3B82F6), // Azul real
    Color(0xFF6366F1), // Índigo
    Color(0xFFA855F7), // Púrpura real
    Color(0xFFEC4899), // Rosa neón
    Color(0xFFEF4444), // Crimson rojo
    Color(0xFFF97316), // Naranja
    Color(0xFFF59E0B), // Dorado / Ámbar
    Color(0xFF10B981), // Esmeralda
    Color(0xFF14B8A6), // Turquesa
    Color(0xFF84CC16)  // Lima
)

/**
 * Diálogo de selector de color visual completamente funcional.
 * Proporciona:
 * - Muestra de color actual vs original
 * - Control de Tonalidad (Hue: 0° - 360°)
 * - Control de Saturación (0% - 100%)
 * - Control de Brillo / Valor (0% - 100%)
 * - Paleta cromática interactiva
 * - Entrada hexadecimal editable ("#RRGGBB")
 * - Botón para restaurar valor original
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    property: ThemeColorProperty,
    initialColorLong: Long,
    originalColorLong: Long,
    onColorSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val initialColor = remember(initialColorLong) { Color(initialColorLong) }
    val originalColor = remember(originalColorLong) { Color(originalColorLong) }

    // Convert initial color to HSV
    val initialHsv = remember(initialColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)
        hsv
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }

    fun currentColor(): Color {
        val argb = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))
        return Color(argb)
    }

    fun formatHex(c: Color): String {
        val argb = c.toArgb()
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF
        return String.format("#%02X%02X%02X", r, g, b)
    }

    var hexText by remember { mutableStateOf(formatHex(initialColor)) }
    var hexError by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    fun updateFromColor(color: Color) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color.toArgb(), hsv)
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
        hexText = formatHex(color)
        hexError = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, NoirTheme.colors.border, RoundedCornerShape(20.dp)),
            color = NoirTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = property.displayName,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NoirTheme.colors.textPrimary
                        )
                        Text(
                            text = property.description,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = NoirTheme.colors.textSecondary,
                            maxLines = 1
                        )
                    }

                    // Botón Restaurar original
                    TextButton(
                        onClick = { updateFromColor(originalColor) },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Restaurar",
                            tint = NoirTheme.colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Restaurar",
                            fontSize = 12.sp,
                            color = NoirTheme.colors.accent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Muestra de color actual vs original
                val selectedColor = currentColor()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NoirTheme.colors.surfaceElevated)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Swatch nuevo
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(selectedColor)
                                .border(1.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Color elegido",
                                fontSize = 11.sp,
                                color = NoirTheme.colors.textSecondary
                            )
                            Text(
                                text = formatHex(selectedColor),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = NoirTheme.colors.textPrimary
                            )
                        }
                    }

                    // Comparativa con original
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { updateFromColor(originalColor) }
                            .padding(4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Original",
                                fontSize = 10.sp,
                                color = NoirTheme.colors.textSecondary
                            )
                            Text(
                                text = formatHex(originalColor),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = NoirTheme.colors.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(originalColor)
                                .border(1.dp, NoirTheme.colors.border, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Control 1: Tonalidad (Hue 0 - 360)
                Text(
                    text = "Tonalidad (Matiz): ${hue.roundToInt()}°",
                    fontSize = 12.sp,
                    color = NoirTheme.colors.textSecondary
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Red, Color.Yellow, Color.Green,
                                    Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                                )
                            )
                        )
                )
                Slider(
                    value = hue,
                    onValueChange = {
                        hue = it
                        hexText = formatHex(currentColor())
                        hexError = false
                    },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )

                // Control 2: Saturación (0% - 100%)
                Text(
                    text = "Saturación: ${(saturation * 100).roundToInt()}%",
                    fontSize = 12.sp,
                    color = NoirTheme.colors.textSecondary
                )
                val satPureHue = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.White, satPureHue)
                            )
                        )
                )
                Slider(
                    value = saturation,
                    onValueChange = {
                        saturation = it
                        hexText = formatHex(currentColor())
                        hexError = false
                    },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )

                // Control 3: Brillo / Luminosidad (0% - 100%)
                Text(
                    text = "Brillo: ${(value * 100).roundToInt()}%",
                    fontSize = 12.sp,
                    color = NoirTheme.colors.textSecondary
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Black, satPureHue)
                            )
                        )
                )
                Slider(
                    value = value,
                    onValueChange = {
                        value = it
                        hexText = formatHex(currentColor())
                        hexError = false
                    },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Campo Hexadecimal Editable
                OutlinedTextField(
                    value = hexText,
                    onValueChange = { input ->
                        hexText = input
                        val cleaned = input.removePrefix("#").trim()
                        if (cleaned.length == 6) {
                            try {
                                val parsed = android.graphics.Color.parseColor("#$cleaned")
                                updateFromColor(Color(parsed))
                                hexError = false
                            } catch (_: Exception) {
                                hexError = true
                            }
                        } else {
                            hexError = cleaned.isNotEmpty()
                        }
                    },
                    label = { Text("Código Hexadecimal", fontSize = 12.sp) },
                    singleLine = true,
                    isError = hexError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NoirTheme.colors.textPrimary,
                        unfocusedTextColor = NoirTheme.colors.textPrimary,
                        focusedBorderColor = NoirTheme.colors.accent,
                        unfocusedBorderColor = NoirTheme.colors.border,
                        errorBorderColor = NoirTheme.colors.error
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Paleta rápida de muestras
                Text(
                    text = "Paleta sugerida",
                    fontSize = 11.sp,
                    color = NoirTheme.colors.textSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickPalette.forEach { quickColor ->
                        val isSelected = quickColor.toArgb() == selectedColor.toArgb()
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(quickColor)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) NoirTheme.colors.accent else NoirTheme.colors.border,
                                    shape = CircleShape
                                )
                                .clickable { updateFromColor(quickColor) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (quickColor == Color.White) Color.Black else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NoirTheme.colors.border),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirTheme.colors.textSecondary)
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val colorLong = (selectedColor.toArgb().toLong() and 0xFFFFFFFFL)
                            onColorSelected(colorLong)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NoirTheme.colors.button,
                            contentColor = if (NoirTheme.colors.isDark) Color.Black else Color.White
                        )
                    ) {
                        Text("Guardar color", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
