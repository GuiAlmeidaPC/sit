package com.sit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.sit.domain.AppTheme

val LocalSitPalette = staticCompositionLocalOf<ThemePalette> {
    error("No SitPalette provided; wrap content in SitTheme { ... }")
}

@Composable
fun SitTheme(
    theme: AppTheme = AppTheme.CLASSIC,
    content: @Composable () -> Unit,
) {
    val palette = ThemeCatalog.palette(theme)
    CompositionLocalProvider(LocalSitPalette provides palette) {
        MaterialTheme(colorScheme = palette.material, content = content)
    }
}
