package com.example.caso_semestral.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta ESFOCAR: verde institucional y dorado.
// Se definen también los colores de contenedor y superficie, porque son los que usan
// la barra inferior, el menú lateral, las tarjetas y los campos de texto
// (con los valores de la plantilla se veían morados y rosados).
private val Verde = Color(0xFF174A35)
private val Azul = Color(0xFF244B70)

private val EsfocarColorScheme = lightColorScheme(
    primary = Verde,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC4E8D6),
    onPrimaryContainer = Color(0xFF0A2418),

    secondary = Azul,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3E4F7),      // indicador de la barra inferior y del menú
    onSecondaryContainer = Color(0xFF0B2238),

    tertiary = Azul,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD3E4F7),
    onTertiaryContainer = Color(0xFF0B2238),

    background = Color(0xFFF5F9F7),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFF5F9F7),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFDCE5E0),
    onSurfaceVariant = Color(0xFF3F4944),
    outline = Color(0xFF6F7974),
    outlineVariant = Color(0xFFBFC9C3),

    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFEFF5F2),
    surfaceContainer = Color(0xFFE9F0EC),
    surfaceContainerHigh = Color(0xFFE3EBE7),
    surfaceContainerHighest = Color(0xFFDDE6E1)   // color de fondo de las tarjetas
)

// Solo modo claro y sin color dinámico, para que la app siempre se vea con los colores institucionales
@Composable
fun Caso_semestralTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsfocarColorScheme,
        typography = Typography,
        content = content
    )
}
