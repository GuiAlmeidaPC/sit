package com.sit.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

data class ThemePalette(
    val material: ColorScheme,
    val rest: Color,
    val run: Color,
    val sprint: Color,
    val sprintFlashAlt: Color,
    val decorativeAccent: Boolean = false,
)
