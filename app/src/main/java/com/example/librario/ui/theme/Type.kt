package com.example.librario.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.librario.R

// 1. Tu fuente personalizada
val Montserrat = FontFamily(
    Font(R.font.montserratregular, FontWeight.Normal),
    Font(R.font.montserratbold, FontWeight.Bold)
)

// 2. El estilo para tu título gigante "Mi Biblioteca"
val TitleLargeSerif = TextStyle(
    fontFamily = Montserrat,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    color = TextPrimary // Toma el color blanco de tu Color.kt
)

// 3. ¡ESTO ES LO QUE FALTABA! El catálogo base de Android
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = TextPrimary
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = TextSecondary
    )
)