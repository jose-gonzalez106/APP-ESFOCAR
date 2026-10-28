package com.example.caso_semestral.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caso_semestral.ColorDocente
import com.example.caso_semestral.VerdeInstitucional

// Primera pantalla de la app (cuando no hay sesión iniciada):
// la persona elige si ingresa como estudiante o como docente y pasa al inicio de sesión.
@Composable
fun BienvenidaScreen(onEstudiante: () -> Unit, onDocente: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        EncabezadoLogo("Escuela de Formación de Carabineros", "Bienvenido")

        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // los elementos entran uno tras otro (cada Aparece espera un poco más)
            Aparece(retrasoMs = 150) {
                Text("¿Cómo quieres ingresar?", style = MaterialTheme.typography.titleMedium)
            }
            Aparece(retrasoMs = 300) {
                BotonRolGrande(
                    titulo = "Ingresar como estudiante",
                    detalle = "Estudia, repasa y guarda tus documentos",
                    icono = Icons.Default.School,
                    color = VerdeInstitucional,
                    onClick = onEstudiante
                )
            }
            Aparece(retrasoMs = 450) {
                BotonRolGrande(
                    titulo = "Ingresar como docente",
                    detalle = "Sube y organiza material por asignatura",
                    icono = Icons.Default.Person,
                    color = ColorDocente,
                    onClick = onDocente
                )
            }
        }
    }
}

// Botón grande con ícono, título y una línea de explicación
@Composable
private fun BotonRolGrande(
    titulo: String,
    detalle: String,
    icono: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color, contentColor = Color.White)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(detalle, fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
            }
        }
    }
}

