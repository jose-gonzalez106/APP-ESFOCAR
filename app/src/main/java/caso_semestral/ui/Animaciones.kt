package com.example.caso_semestral.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

// Muestra un mensaje (error o ayuda) que aparece y desaparece con animación.
// Guarda el último texto para que no quede vacío mientras se desvanece.
@Composable
fun MensajeAnimado(
    mensaje: String?,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    var ultimo by remember { mutableStateOf("") }
    LaunchedEffect(mensaje) { if (mensaje != null) ultimo = mensaje }

    AnimatedVisibility(visible = mensaje != null, modifier = modifier) {
        Text(text = mensaje ?: ultimo, color = color)
    }
}

// Hace que el contenido entre con un desvanecimiento y un leve movimiento hacia arriba.
// Solo se anima la primera vez que se abre la pantalla.
@Composable
fun Aparece(retrasoMs: Long = 0, content: @Composable () -> Unit) {
    var visible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(retrasoMs)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 8 }
    ) {
        content()
    }
}
