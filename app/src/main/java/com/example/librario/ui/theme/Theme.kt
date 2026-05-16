package com.example.librario.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. Aplicamos tu paleta de colores oscuros
private val DarkColorScheme = darkColorScheme(
    primary = GradientPrimaryStart,
    secondary = GradientPrimaryEnd,
    background = BackgroundStart,
    surface = CardColor,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun LibrarioTheme(
    // Forzamos que siempre use el tema oscuro de tu diseño, sin importar cómo tenga el celu el usuario
    darkTheme: Boolean = true,
    // Apagamos los colores dinámicos de Android 12+ para que respete tus gradientes
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    // 2. Pintamos la barra de notificaciones de arriba (donde está la hora) con tu color de fondo
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundStart.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    // 3. Aplicamos el tema
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Toma las fuentes de Type.kt
        content = content
    )
}