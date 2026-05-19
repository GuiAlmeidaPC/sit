package com.sit.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.sit.domain.AppTheme

object ThemeCatalog {

    fun palette(theme: AppTheme): ThemePalette = when (theme) {
        AppTheme.CLASSIC -> classic
        AppTheme.CLASSIC_DARK -> classicDark
        AppTheme.NEON -> neon
        AppTheme.FOREST -> forest
        AppTheme.FOREST_DARK -> forestDark
        AppTheme.MONO -> mono
        AppTheme.MONO_DARK -> monoDark
        AppTheme.GLITTER_POP -> glitterPop
        AppTheme.GLITTER_POP_DARK -> glitterPopDark
    }

    private val classic = ThemePalette(
        material = lightColorScheme(
            primary = Color(0xFFE65100),
            onPrimary = Color.White,
            background = Color(0xFFFAFAFA),
            surface = Color(0xFFFFFFFF),
        ),
        rest = Color(0xFF1E88E5),
        run = Color(0xFFEF6C00),
        sprint = Color(0xFFD32F2F),
        sprintFlashAlt = Color(0xFF7F0000),
    )

    private val classicDark = ThemePalette(
        material = darkColorScheme(
            primary = Color(0xFFFF8A50),
            onPrimary = Color.Black,
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
        ),
        rest = Color(0xFF42A5F5),
        run = Color(0xFFFF9800),
        sprint = Color(0xFFFF5252),
        sprintFlashAlt = Color(0xFFB71C1C),
    )

    private val neon = ThemePalette(
        material = darkColorScheme(
            primary = Color(0xFF00E5FF),
            onPrimary = Color.Black,
            background = Color(0xFF050505),
            surface = Color(0xFF121212),
        ),
        rest = Color(0xFF00E5FF),
        run = Color(0xFFFFEB3B),
        sprint = Color(0xFFFF1493),
        sprintFlashAlt = Color(0xFF6A0072),
    )

    private val forest = ThemePalette(
        material = lightColorScheme(
            primary = Color(0xFF2E7D32),
            onPrimary = Color.White,
            background = Color(0xFFF1F8E9),
            surface = Color(0xFFFFFFFF),
        ),
        rest = Color(0xFF00897B),
        run = Color(0xFF9E9D24),
        sprint = Color(0xFFC62828),
        sprintFlashAlt = Color(0xFF6A1B1B),
    )

    private val forestDark = ThemePalette(
        material = darkColorScheme(
            primary = Color(0xFF66BB6A),
            onPrimary = Color.Black,
            background = Color(0xFF102019),
            surface = Color(0xFF1A2D24),
        ),
        rest = Color(0xFF26A69A),
        run = Color(0xFFD4E157),
        sprint = Color(0xFFEF5350),
        sprintFlashAlt = Color(0xFF8E2424),
    )

    private val mono = ThemePalette(
        material = lightColorScheme(
            primary = Color(0xFF000000),
            onPrimary = Color.White,
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFF5F5F5),
        ),
        rest = Color(0xFFBDBDBD),
        run = Color(0xFF616161),
        sprint = Color(0xFF000000),
        sprintFlashAlt = Color(0xFF9E9E9E),
    )

    private val monoDark = ThemePalette(
        material = darkColorScheme(
            primary = Color(0xFFE0E0E0),
            onPrimary = Color.Black,
            background = Color(0xFF111111),
            surface = Color(0xFF1A1A1A),
        ),
        rest = Color(0xFF757575),
        run = Color(0xFFBDBDBD),
        sprint = Color(0xFFF5F5F5),
        sprintFlashAlt = Color(0xFF616161),
    )

    private val glitterPop = ThemePalette(
        material = lightColorScheme(
            primary = Color(0xFFEC407A),
            onPrimary = Color.White,
            background = Color(0xFFFFF0F6),
            surface = Color(0xFFFFFFFF),
        ),
        rest = Color(0xFFB39DDB),
        run = Color(0xFF81D4FA),
        sprint = Color(0xFFFF1493),
        sprintFlashAlt = Color(0xFFF8BBD0),
        decorativeAccent = true,
    )

    private val glitterPopDark = ThemePalette(
        material = darkColorScheme(
            primary = Color(0xFFFF80AB),
            onPrimary = Color.Black,
            background = Color(0xFF1A1020),
            surface = Color(0xFF26162F),
        ),
        rest = Color(0xFFCE93D8),
        run = Color(0xFF81D4FA),
        sprint = Color(0xFFFF4081),
        sprintFlashAlt = Color(0xFF8E2456),
        decorativeAccent = true,
    )
}
