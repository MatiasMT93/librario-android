package com.example.librario.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Fondo degradado principal
val BackgroundStart = Color(0xFF121636) // Azul noche oscuro
val BackgroundEnd = Color(0xFF351B40) // Morado/Magenta oscuro

// Color de las tarjetas (glassmorphism)
val CardColor = Color(0xFF1E254A) // Azul ligeramente más claro con algo de transparencia
val CardBorder = Color(0xFFFFFFFF).copy(alpha = 0.2f) // Borde blanco muy sutil

// Colores de texto
val TextPrimary = Color(0xFFFFFFFF) // Blanco puro
val TextSecondary = Color(0xFFFFFFFF).copy(alpha = 0.7f) // Blanco/Grisáceo

// Colores de los stats (con el degradado)
val StatTotal = Color(0xFFFFFFFF)
val StatReading = Color(0xFFA1E4DF) // Verde/Cian
val StatRead = Color(0xFFDF9F97) // Naranja/Durazno

// Botones y FAB (Degradado principal)
val GradientPrimaryStart = Color(0xFFA1C9EE)
val GradientPrimaryEnd = Color(0xFFDF9F97)

// Barra de búsqueda y elementos oscuros
val SearchBarBg = Color(0xFF181D3E)

// Pincel de degradado de fondo para reutilizar
val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(BackgroundStart, BackgroundEnd)
)