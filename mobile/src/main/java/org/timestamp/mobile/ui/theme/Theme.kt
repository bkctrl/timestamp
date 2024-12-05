package org.timestamp.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

@Composable
fun TimestampTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColors(
            primary = Color(0xFF2A2B2E),
            secondary = Color(0xFFFFFFFF),
            background = Color.DarkGray
        )
    } else {
        lightColors(
            secondary = Color(0xFF2A2B2E),
            primary = Color(0xFFFFFFFF),
            background = Color.LightGray
        )
    }
    MaterialTheme(
        colors = colorScheme,
        content = content
    )
}