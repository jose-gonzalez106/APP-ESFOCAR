package com.example.caso_semestral.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caso_semestral.ColorDocente
import com.example.caso_semestral.data.local.DocumentoEntity
import com.example.caso_semestral.model.Asignatura
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------- Paleta del docente (basada en ColorDocente #244B70, azul) ----------
private val FondoDocente = Color(0xFF0D1319)      // fondo oscuro con un toque azul
private val AzulAcento = Color(0xFFA3C9EE)        // azul claro para etiquetas
private val AzulIcono = Color(0xFF4F82B3)         // azul para íconos sobre tarjetas
private val TextoClaro = Color(0xFFF1F1F1)
private val ColorSinInternetDocente = Color(0xFFFFB74D)

// ---------- Inicio del DOCENTE ----------
@Composable
fun InicioDocenteScreen(
    alias: String,
    asignaturas: List<Asignatura>,
    documentos: List<DocumentoEntity>,
    onIrDocumentos: () -> Unit,
    onAbrir: (Int) -> Unit
) {
    val conectado = rememberHayConexion()
    val colorEstado = if (conectado) AzulAcento else ColorSinInternetDocente

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoDocente)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        LogoInicio(AzulAcento)
        // Etiqueta de estado + saludo
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(colorEstado.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(colorEstado))
                Text(
                    if (conectado) "Conectado · Panel docente" else "Sin conexión · Tu material sigue disponible",
                    color = colorEstado,
                    fontSize = 12.sp
                )
            }
            Text("Panel docente", color = AzulAcento, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("Hola, $alias 👋", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(
                "Sube material para tus asignaturas y prepara actividades para tus estudiantes.",
                color = TextoClaro, fontSize = 15.sp
            )
        }

        // Asignaturas que imparte
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ColorDocente)
        ) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ASIGNATURAS QUE IMPARTES (${asignaturas.size})", color = AzulAcento, fontSize = 11.sp)
                    if (asignaturas.isEmpty()) {
                        Text("Aún no tienes asignaturas asignadas.", color = TextoClaro, fontSize = 14.sp)
                    } else {
                        asignaturas.forEach {
                            Text("· ${it.nombre}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                IconoCuadradoDocente(Icons.Default.School)
            }
        }

        // Subir material
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ColorDocente)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("MATERIAL DE ESTUDIO", color = AzulAcento, fontSize = 11.sp)
                        Text("Subir material", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Carga PDF, imágenes o texto y elige a qué asignatura pertenecen.", color = TextoClaro, fontSize = 14.sp)
                    }
                    IconoCuadradoDocente(Icons.Default.UploadFile)
                }
                Button(
                    onClick = onIrDocumentos,   // la carga se hace en la pestaña Material
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = ColorDocente)
                ) { Text("Subir archivo", fontWeight = FontWeight.Bold) }
            }
        }

        // Material reciente: los 2 más nuevos (la lista ya viene ordenada por fecha)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Material reciente", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (documentos.isNotEmpty()) {
                Text("Ver todo ›", color = AzulAcento, fontSize = 14.sp, modifier = Modifier.clickable { onIrDocumentos() })
            }
        }
        if (documentos.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ColorDocente)
            ) {
                Text(
                    "Aún no has subido material. Sube tu primer archivo para tus estudiantes.",
                    color = TextoClaro, modifier = Modifier.padding(20.dp)
                )
            }
        } else {
            documentos.take(2).forEach { doc ->
                val nombreAsignatura = asignaturas.find { it.id == doc.asignaturaId }?.nombre ?: "Sin asignatura"
                TarjetaMaterial(doc, nombreAsignatura) { onAbrir(doc.id) }
            }
        }

        // Funciones futuras del docente
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A222B)),
            border = BorderStroke(1.dp, AzulAcento.copy(alpha = 0.5f))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("PRÓXIMAMENTE", color = AzulAcento, fontSize = 11.sp)
                Text("Más herramientas para docentes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("· Crear preguntas", color = TextoClaro, fontSize = 14.sp)
                Text("· Crear casos simulados", color = TextoClaro, fontSize = 14.sp)
                Text("· Ver resultados de estudiantes", color = TextoClaro, fontSize = 14.sp)
            }
        }

        // Tip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ColorDocente)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconoCuadradoDocente(Icons.Default.Lightbulb, 40.dp)
            Text(
                "Tip: solo puedes subir material en las asignaturas que impartes.",
                color = TextoClaro, fontSize = 14.sp
            )
        }
    }
}

// Cuadrado azul con ícono
@Composable
private fun IconoCuadradoDocente(icono: ImageVector, tamano: Dp = 52.dp) {
    Box(
        modifier = Modifier.size(tamano).clip(RoundedCornerShape(14.dp)).background(AzulIcono),
        contentAlignment = Alignment.Center
    ) { Icon(icono, contentDescription = null, tint = Color.White) }
}

// Tarjeta de un material reciente con su asignatura y botón para abrirlo
@Composable
private fun TarjetaMaterial(doc: DocumentoEntity, asignatura: String, onAbrir: () -> Unit) {
    val fecha = SimpleDateFormat("d MMM yyyy", Locale("es", "CL")).format(Date(doc.fecha))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ColorDocente)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconoCuadradoDocente(Icons.Default.Description)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("$asignatura • $fecha", color = AzulAcento, fontSize = 12.sp)
                    Text(
                        doc.nombre.substringAfter("_"),
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 2
                    )
                }
            }
            Button(
                onClick = onAbrir,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = ColorDocente)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Abrir", fontWeight = FontWeight.Bold)
            }
        }
    }
}