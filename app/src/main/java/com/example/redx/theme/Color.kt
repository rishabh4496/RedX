package com.example.redx.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateOf

/**
 * Colors used by the custom feed components. The property names are kept for
 * source compatibility, but their values now follow the active app palette.
 */
data class RedXPalette(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val accent: Color,
    val accentVariant: Color,
    val upvote: Color,
    val downvote: Color,
    val saved: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val chipBackground: Color,
    val chipSelected: Color,
    val chipTextSelected: Color,
    val flairBackground: Color,
    val flairText: Color,
    val nsfw: Color,
    val spoiler: Color
)

internal object RedXPaletteState {
    private val state = mutableStateOf(
        RedXPalette(
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
    )

    /**
     * Backed by Compose snapshot state so that changing the app theme recomposes every
     * component that reads the palette convenience properties below.
     */
    var current: RedXPalette
        get() = state.value
        set(value) {
            if (state.value != value) state.value = value
        }
}

val AmoledBackground: Color get() = RedXPaletteState.current.background
val AmoledSurface: Color get() = RedXPaletteState.current.surface
val AmoledSurfaceElevated: Color get() = RedXPaletteState.current.surfaceElevated
val AmoledBorder: Color get() = RedXPaletteState.current.border

val RedditOrange: Color get() = RedXPaletteState.current.accent
val RedditOrangeVariant: Color get() = RedXPaletteState.current.accentVariant
val UpvoteOrange: Color get() = RedXPaletteState.current.upvote
val DownvotePeriwinkle: Color get() = RedXPaletteState.current.downvote
val SavedGold: Color get() = RedXPaletteState.current.saved

val TextPrimary: Color get() = RedXPaletteState.current.textPrimary
val TextSecondary: Color get() = RedXPaletteState.current.textSecondary
val TextTertiary: Color get() = RedXPaletteState.current.textTertiary

val ChipBackground: Color get() = RedXPaletteState.current.chipBackground
val ChipSelected: Color get() = RedXPaletteState.current.chipSelected
val ChipTextSelected: Color get() = RedXPaletteState.current.chipTextSelected

val FlairBackground: Color get() = RedXPaletteState.current.flairBackground
val FlairText: Color get() = RedXPaletteState.current.flairText

val NsfwRed: Color get() = RedXPaletteState.current.nsfw
val SpoilerGrey: Color get() = RedXPaletteState.current.spoiler
