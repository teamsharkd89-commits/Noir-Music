package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Representa la configuración completa de un tema visual en NOIR Music.
 * Permite personalizar 12 propiedades de color individuales y persistirlas.
 */
data class AppThemeConfig(
    val id: String = "preset_noir_original",
    val name: String = "NOIR Original",
    val isPreset: Boolean = true,
    val isDark: Boolean = true,
    // Las 12 propiedades solicitadas:
    val backgroundColor: Long = 0xFF080808,
    val surfaceColor: Long = 0xFF141414,
    val accentColor: Long = 0xFF00E5FF,
    val buttonColor: Long = 0xFF00E5FF,
    val textPrimaryColor: Long = 0xFFFFFFFF,
    val textSecondaryColor: Long = 0xFFA1A1AA,
    val iconsColor: Long = 0xFFFFFFFF,
    val bottomNavColor: Long = 0xFF0F0F12,
    val miniPlayerColor: Long = 0xFF16161A,
    val progressBarColor: Long = 0xFF00E5FF,
    val playerControlsColor: Long = 0xFF00E5FF,
    val selectedItemColor: Long = 0xFF00E5FF
) {
    fun toComposeColors(): NoirColors {
        val bg = Color(backgroundColor)
        val surf = Color(surfaceColor)
        val acc = Color(accentColor)
        val btn = Color(buttonColor)
        val txt1 = Color(textPrimaryColor)
        val txt2 = Color(textSecondaryColor)
        val icn = Color(iconsColor)
        val bNav = Color(bottomNavColor)
        val mPlay = Color(miniPlayerColor)
        val pBar = Color(progressBarColor)
        val pCtrl = Color(playerControlsColor)
        val sItem = Color(selectedItemColor)

        // Derivados armoniosos para elevación y bordes
        val border = if (isDark) {
            Color(0xFF26262A)
        } else {
            Color(0xFFE2E8F0)
        }
        val surfElevated = if (isDark) {
            Color(0xFF1C1C1E)
        } else {
            Color(0xFFF1F5F9)
        }
        val surfCard = if (isDark) {
            Color(0xFF18181A)
        } else {
            Color(0xFFFFFFFF)
        }
        val txt3 = if (isDark) {
            Color(0xFF71717A)
        } else {
            Color(0xFF94A3B8)
        }

        return NoirColors(
            background = bg,
            surface = surf,
            surfaceElevated = surfElevated,
            surfaceCard = surfCard,
            border = border,
            textPrimary = txt1,
            textSecondary = txt2,
            textTertiary = txt3,
            accent = acc,
            button = btn,
            icons = icn,
            bottomNav = bNav,
            miniPlayer = mPlay,
            progressBar = pBar,
            playerControls = pCtrl,
            selectedItem = sItem,
            isDark = isDark
        )
    }

    fun toJson(): String {
        val json = JSONObject()
        json.put("id", id)
        json.put("name", name)
        json.put("isPreset", isPreset)
        json.put("isDark", isDark)
        json.put("backgroundColor", backgroundColor)
        json.put("surfaceColor", surfaceColor)
        json.put("accentColor", accentColor)
        json.put("buttonColor", buttonColor)
        json.put("textPrimaryColor", textPrimaryColor)
        json.put("textSecondaryColor", textSecondaryColor)
        json.put("iconsColor", iconsColor)
        json.put("bottomNavColor", bottomNavColor)
        json.put("miniPlayerColor", miniPlayerColor)
        json.put("progressBarColor", progressBarColor)
        json.put("playerControlsColor", playerControlsColor)
        json.put("selectedItemColor", selectedItemColor)
        return json.toString()
    }

    fun copyWithColor(property: ThemeColorProperty, newColor: Long): AppThemeConfig {
        return when (property) {
            ThemeColorProperty.BACKGROUND -> copy(backgroundColor = newColor)
            ThemeColorProperty.SURFACE -> copy(surfaceColor = newColor)
            ThemeColorProperty.ACCENT -> copy(accentColor = newColor)
            ThemeColorProperty.BUTTON -> copy(buttonColor = newColor)
            ThemeColorProperty.TEXT_PRIMARY -> copy(textPrimaryColor = newColor)
            ThemeColorProperty.TEXT_SECONDARY -> copy(textSecondaryColor = newColor)
            ThemeColorProperty.ICONS -> copy(iconsColor = newColor)
            ThemeColorProperty.BOTTOM_NAV -> copy(bottomNavColor = newColor)
            ThemeColorProperty.MINI_PLAYER -> copy(miniPlayerColor = newColor)
            ThemeColorProperty.PROGRESS_BAR -> copy(progressBarColor = newColor)
            ThemeColorProperty.PLAYER_CONTROLS -> copy(playerControlsColor = newColor)
            ThemeColorProperty.SELECTED_ITEM -> copy(selectedItemColor = newColor)
        }
    }

    fun getColorForProperty(property: ThemeColorProperty): Long {
        return when (property) {
            ThemeColorProperty.BACKGROUND -> backgroundColor
            ThemeColorProperty.SURFACE -> surfaceColor
            ThemeColorProperty.ACCENT -> accentColor
            ThemeColorProperty.BUTTON -> buttonColor
            ThemeColorProperty.TEXT_PRIMARY -> textPrimaryColor
            ThemeColorProperty.TEXT_SECONDARY -> textSecondaryColor
            ThemeColorProperty.ICONS -> iconsColor
            ThemeColorProperty.BOTTOM_NAV -> bottomNavColor
            ThemeColorProperty.MINI_PLAYER -> miniPlayerColor
            ThemeColorProperty.PROGRESS_BAR -> progressBarColor
            ThemeColorProperty.PLAYER_CONTROLS -> playerControlsColor
            ThemeColorProperty.SELECTED_ITEM -> selectedItemColor
        }
    }

    companion object {
        fun fromJson(jsonStr: String): AppThemeConfig? {
            return try {
                val json = JSONObject(jsonStr)
                AppThemeConfig(
                    id = json.optString("id", UUID.randomUUID().toString()),
                    name = json.optString("name", "Tema"),
                    isPreset = json.optBoolean("isPreset", false),
                    isDark = json.optBoolean("isDark", true),
                    backgroundColor = json.optLong("backgroundColor", 0xFF080808),
                    surfaceColor = json.optLong("surfaceColor", 0xFF141414),
                    accentColor = json.optLong("accentColor", 0xFF00E5FF),
                    buttonColor = json.optLong("buttonColor", 0xFF00E5FF),
                    textPrimaryColor = json.optLong("textPrimaryColor", 0xFFFFFFFF),
                    textSecondaryColor = json.optLong("textSecondaryColor", 0xFFA1A1AA),
                    iconsColor = json.optLong("iconsColor", 0xFFFFFFFF),
                    bottomNavColor = json.optLong("bottomNavColor", 0xFF0F0F12),
                    miniPlayerColor = json.optLong("miniPlayerColor", 0xFF16161A),
                    progressBarColor = json.optLong("progressBarColor", 0xFF00E5FF),
                    playerControlsColor = json.optLong("playerControlsColor", 0xFF00E5FF),
                    selectedItemColor = json.optLong("selectedItemColor", 0xFF00E5FF)
                )
            } catch (_: Exception) {
                null
            }
        }

        fun listToJson(list: List<AppThemeConfig>): String {
            val array = JSONArray()
            for (item in list) {
                array.put(JSONObject(item.toJson()))
            }
            return array.toString()
        }

        fun listFromJson(jsonStr: String): List<AppThemeConfig> {
            val list = mutableListOf<AppThemeConfig>()
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    fromJson(obj.toString())?.let { list.add(it) }
                }
            } catch (_: Exception) {
            }
            return list
        }

        // --- Temas Prediseñados Oficiales ---

        val NOIR_ORIGINAL = AppThemeConfig(
            id = "preset_noir_original",
            name = "NOIR Original",
            isPreset = true,
            isDark = true,
            backgroundColor = 0xFF080808,
            surfaceColor = 0xFF141414,
            accentColor = 0xFF00E5FF,
            buttonColor = 0xFF00E5FF,
            textPrimaryColor = 0xFFFFFFFF,
            textSecondaryColor = 0xFFA1A1AA,
            iconsColor = 0xFFFFFFFF,
            bottomNavColor = 0xFF0F0F12,
            miniPlayerColor = 0xFF16161A,
            progressBarColor = 0xFF00E5FF,
            playerControlsColor = 0xFF00E5FF,
            selectedItemColor = 0xFF00E5FF
        )

        val CRIMSON = AppThemeConfig(
            id = "preset_crimson",
            name = "Crimson",
            isPreset = true,
            isDark = true,
            backgroundColor = 0xFF0B0B0C,
            surfaceColor = 0xFF181214,
            accentColor = 0xFFEF4444,
            buttonColor = 0xFFEF4444,
            textPrimaryColor = 0xFFFFFFFF,
            textSecondaryColor = 0xFFA1A1AA,
            iconsColor = 0xFFFFFFFF,
            bottomNavColor = 0xFF130E10,
            miniPlayerColor = 0xFF1F1417,
            progressBarColor = 0xFFEF4444,
            playerControlsColor = 0xFFEF4444,
            selectedItemColor = 0xFFEF4444
        )

        val ROYAL_PURPLE = AppThemeConfig(
            id = "preset_royal_purple",
            name = "Royal Purple",
            isPreset = true,
            isDark = true,
            backgroundColor = 0xFF090611,
            surfaceColor = 0xFF161026,
            accentColor = 0xFFA855F7,
            buttonColor = 0xFFA855F7,
            textPrimaryColor = 0xFFFFFFFF,
            textSecondaryColor = 0xFFA1A1AA,
            iconsColor = 0xFFFFFFFF,
            bottomNavColor = 0xFF110C1D,
            miniPlayerColor = 0xFF1D1432,
            progressBarColor = 0xFFA855F7,
            playerControlsColor = 0xFFA855F7,
            selectedItemColor = 0xFFA855F7
        )

        val MIDNIGHT_BLUE = AppThemeConfig(
            id = "preset_midnight_blue",
            name = "Midnight Blue",
            isPreset = true,
            isDark = true,
            backgroundColor = 0xFF050B14,
            surfaceColor = 0xFF0D1929,
            accentColor = 0xFF3B82F6,
            buttonColor = 0xFF3B82F6,
            textPrimaryColor = 0xFFFFFFFF,
            textSecondaryColor = 0xFFA1A1AA,
            iconsColor = 0xFFFFFFFF,
            bottomNavColor = 0xFF0A1220,
            miniPlayerColor = 0xFF132238,
            progressBarColor = 0xFF3B82F6,
            playerControlsColor = 0xFF3B82F6,
            selectedItemColor = 0xFF3B82F6
        )

        val EMERALD = AppThemeConfig(
            id = "preset_emerald",
            name = "Emerald",
            isPreset = true,
            isDark = true,
            backgroundColor = 0xFF06120D,
            surfaceColor = 0xFF0E2319,
            accentColor = 0xFF10B981,
            buttonColor = 0xFF10B981,
            textPrimaryColor = 0xFFFFFFFF,
            textSecondaryColor = 0xFFA1A1AA,
            iconsColor = 0xFFFFFFFF,
            bottomNavColor = 0xFF0A1A13,
            miniPlayerColor = 0xFF133023,
            progressBarColor = 0xFF10B981,
            playerControlsColor = 0xFF10B981,
            selectedItemColor = 0xFF10B981
        )

        val GOLD_EDITION = AppThemeConfig(
            id = "preset_gold_edition",
            name = "Gold Edition",
            isPreset = true,
            isDark = true,
            backgroundColor = 0xFF0D0B05,
            surfaceColor = 0xFF1F1A0F,
            accentColor = 0xFFF59E0B,
            buttonColor = 0xFFF59E0B,
            textPrimaryColor = 0xFFFFFFFF,
            textSecondaryColor = 0xFFA1A1AA,
            iconsColor = 0xFFFFFFFF,
            bottomNavColor = 0xFF17130A,
            miniPlayerColor = 0xFF282113,
            progressBarColor = 0xFFF59E0B,
            playerControlsColor = 0xFFF59E0B,
            selectedItemColor = 0xFFF59E0B
        )

        val ARCTIC = AppThemeConfig(
            id = "preset_arctic",
            name = "Arctic",
            isPreset = true,
            isDark = false,
            backgroundColor = 0xFFF8FAFC,
            surfaceColor = 0xFFFFFFFF,
            accentColor = 0xFF0284C7,
            buttonColor = 0xFF0284C7,
            textPrimaryColor = 0xFF0F172A,
            textSecondaryColor = 0xFF64748B,
            iconsColor = 0xFF0F172A,
            bottomNavColor = 0xFFFFFFFF,
            miniPlayerColor = 0xFFF1F5F9,
            progressBarColor = 0xFF0284C7,
            playerControlsColor = 0xFF0284C7,
            selectedItemColor = 0xFF0284C7
        )

        val AMOLED = AppThemeConfig(
            id = "preset_amoled",
            name = "AMOLED",
            isPreset = true,
            isDark = true,
            backgroundColor = 0xFF000000,
            surfaceColor = 0xFF0A0A0A,
            accentColor = 0xFF00E5FF,
            buttonColor = 0xFF00E5FF,
            textPrimaryColor = 0xFFFFFFFF,
            textSecondaryColor = 0xFFA1A1AA,
            iconsColor = 0xFFFFFFFF,
            bottomNavColor = 0xFF000000,
            miniPlayerColor = 0xFF0A0A0A,
            progressBarColor = 0xFF00E5FF,
            playerControlsColor = 0xFF00E5FF,
            selectedItemColor = 0xFF00E5FF
        )

        val PRESETS = listOf(
            NOIR_ORIGINAL,
            CRIMSON,
            ROYAL_PURPLE,
            MIDNIGHT_BLUE,
            EMERALD,
            GOLD_EDITION,
            ARCTIC,
            AMOLED
        )

        fun findPresetById(id: String): AppThemeConfig? {
            return PRESETS.find { it.id == id }
        }
    }
}

/**
 * Las 12 propiedades de color configurables individualmente.
 */
enum class ThemeColorProperty(val displayName: String, val description: String) {
    BACKGROUND("Fondo principal", "Color de fondo de las pantallas principales"),
    SURFACE("Superficies y tarjetas", "Color de tarjetas, paneles y contenedores"),
    ACCENT("Acento principal", "Color de acentos, insignias y detalles destacados"),
    BUTTON("Botones de acción", "Color de fondo de los botones principales"),
    TEXT_PRIMARY("Texto principal", "Títulos, nombres de canciones y elementos destacados"),
    TEXT_SECONDARY("Texto secundario", "Artistas, duraciones, subtítulos y metadatos"),
    ICONS("Iconos", "Iconos de navegación, opciones y herramientas"),
    BOTTOM_NAV("Barra de navegación", "Fondo de la barra inferior de navegación"),
    MINI_PLAYER("Mini reproductor", "Fondo del reproductor acoplado sobre la barra"),
    PROGRESS_BAR("Barra de progreso", "Línea indicadora de avance de reproducción"),
    PLAYER_CONTROLS("Controles de reproducción", "Botón reproducir/pausar, saltar y aleatorio"),
    SELECTED_ITEM("Elementos seleccionados", "Indicadores de pistas activas o pestañas elegidas")
}
