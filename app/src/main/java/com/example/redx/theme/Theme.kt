package com.example.redx.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.redx.model.AppTheme

private val AmoledPalette = RedXPalette(
    background = Color(0xFF090B0E),
    surface = Color(0xFF13171C),
    surfaceElevated = Color(0xFF1B2129),
    border = Color(0xFF262D37),
    accent = Color(0xFFFF4500),
    accentVariant = Color(0xFFFF5722),
    upvote = Color(0xFFFF4500),
    downvote = Color(0xFF7193FF),
    saved = Color(0xFFFFB300),
    textPrimary = Color(0xFFF3F5F7),
    textSecondary = Color(0xFF9EA7B3),
    textTertiary = Color(0xFF6C7684),
    chipBackground = Color(0xFF1D232C),
    chipSelected = Color(0xFFFF4500),
    chipTextSelected = Color.White,
    flairBackground = Color(0xFF263238),
    flairText = Color(0xFF80CBC4),
    nsfw = Color(0xFFE53935),
    spoiler = Color(0xFF546E7A)
)

private val MidnightPalette = RedXPalette(
    background = Color(0xFF070D18),
    surface = Color(0xFF0F1A2E),
    surfaceElevated = Color(0xFF172642),
    border = Color(0xFF1F3358),
    accent = Color(0xFF38B6FF),
    accentVariant = Color(0xFF0084D1),
    upvote = Color(0xFF38B6FF),
    downvote = Color(0xFF8FAEFF),
    saved = Color(0xFFFFC857),
    textPrimary = Color(0xFFE8EEF5),
    textSecondary = Color(0xFFA5B4C7),
    textTertiary = Color(0xFF71849E),
    chipBackground = Color(0xFF14233D),
    chipSelected = Color(0xFF38B6FF),
    chipTextSelected = Color.White,
    flairBackground = Color(0xFF1C2E47),
    flairText = Color(0xFF88D7FF),
    nsfw = Color(0xFFF05C66),
    spoiler = Color(0xFF526A87)
)

private val SunsetPalette = RedXPalette(
    background = Color(0xFF0E0907),
    surface = Color(0xFF1A120D),
    surfaceElevated = Color(0xFF291D16),
    border = Color(0xFF3D2A1F),
    accent = Color(0xFFFF6B35),
    accentVariant = Color(0xFFE0531B),
    upvote = Color(0xFFFF6B35),
    downvote = Color(0xFFF7B05B),
    saved = Color(0xFFFFC857),
    textPrimary = Color(0xFFF6ECE8),
    textSecondary = Color(0xFFC7B1A5),
    textTertiary = Color(0xFF977B6B),
    chipBackground = Color(0xFF2A1B13),
    chipSelected = Color(0xFFFF6B35),
    chipTextSelected = Color.White,
    flairBackground = Color(0xFF3A241C),
    flairText = Color(0xFFFFD0A8),
    nsfw = Color(0xFFFF6B6B),
    spoiler = Color(0xFF6B5145)
)

private val CyberpunkPalette = RedXPalette(
    background = Color(0xFF09060B),
    surface = Color(0xFF150D1B),
    surfaceElevated = Color(0xFF22132A),
    border = Color(0xFF381B47),
    accent = Color(0xFFFF1744),
    accentVariant = Color(0xFFD500F9),
    upvote = Color(0xFFFF1744),
    downvote = Color(0xFF651FFF),
    saved = Color(0xFFFFEA00),
    textPrimary = Color(0xFFFDF4FF),
    textSecondary = Color(0xFFC0A6CD),
    textTertiary = Color(0xFF886A96),
    chipBackground = Color(0xFF1E1026),
    chipSelected = Color(0xFFFF1744),
    chipTextSelected = Color.White,
    flairBackground = Color(0xFF2C1538),
    flairText = Color(0xFFFF80AB),
    nsfw = Color(0xFFFF1744),
    spoiler = Color(0xFF5E2B6D)
)

private val MatrixPalette = RedXPalette(
    background = Color(0xFF060B08),
    surface = Color(0xFF0D1711),
    surfaceElevated = Color(0xFF14241B),
    border = Color(0xFF1C3828),
    accent = Color(0xFF00E676),
    accentVariant = Color(0xFF00B0FF),
    upvote = Color(0xFF00E676),
    downvote = Color(0xFF80D8FF),
    saved = Color(0xFFFFD600),
    textPrimary = Color(0xFFEDFFF5),
    textSecondary = Color(0xFFA6CBBB),
    textTertiary = Color(0xFF6C9784),
    chipBackground = Color(0xFF112117),
    chipSelected = Color(0xFF00E676),
    chipTextSelected = Color(0xFF060B08),
    flairBackground = Color(0xFF162D20),
    flairText = Color(0xFF69F0AE),
    nsfw = Color(0xFFFF5252),
    spoiler = Color(0xFF456B57)
)

internal fun paletteFor(appTheme: AppTheme): RedXPalette = when (appTheme) {
    AppTheme.AMOLED_BLACK -> AmoledPalette
    AppTheme.MIDNIGHT_BLUE -> MidnightPalette
    AppTheme.SUNSET_ORANGE -> SunsetPalette
    AppTheme.CYBERPUNK_CRIMSON -> CyberpunkPalette
    AppTheme.MATRIX_EMERALD -> MatrixPalette
}

private fun colorSchemeFor(palette: RedXPalette): ColorScheme = darkColorScheme(
    primary = palette.accent,
    onPrimary = Color.White,
    primaryContainer = palette.accentVariant,
    onPrimaryContainer = Color.White,
    secondary = palette.upvote,
    onSecondary = Color.White,
    tertiary = palette.downvote,
    onTertiary = if (palette == SunsetPalette) Color.Black else Color.White,
    background = palette.background,
    onBackground = palette.textPrimary,
    surface = palette.surface,
    onSurface = palette.textPrimary,
    surfaceVariant = palette.surfaceElevated,
    onSurfaceVariant = palette.textSecondary,
    outline = palette.border,
    outlineVariant = palette.border
)

@Composable
fun RedXTheme(
    appTheme: AppTheme = AppTheme.AMOLED_BLACK,
    fontScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val palette = paletteFor(appTheme)
    // Custom feed components use the same palette as Material 3 components.
    // Applied in a SideEffect so composition stays side-effect free while the
    // snapshot-backed palette still recomposes dependent components on change.
    SideEffect { RedXPaletteState.current = palette }

    val baseDensity = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(baseDensity.density, baseDensity.fontScale * fontScale)
    ) {
        MaterialTheme(
            colorScheme = colorSchemeFor(palette),
            typography = Typography,
            content = content
        )
    }
}
