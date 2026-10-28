package com.example.caso_semestral.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.caso_semestral.ColorDocente
import com.example.caso_semestral.VerdeInstitucional
import com.example.caso_semestral.data.local.DocumentoEntity
import com.example.caso_semestral.model.Asignatura
import com.example.caso_semestral.viewmodel.DocumentosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// tipos que la app sabe visualizar
private val TIPOS_PERMITIDOS = arrayOf("application/pdf", "image/*", "text/plain")

// Pestaña Documentos (estudiante) / Material (docente).
// El docente elige la asignatura al subir y puede filtrar su material por asignatura.
@Composable
fun DocumentosScreen(
    vm: DocumentosViewModel,
    esDocente: Boolean = false,
    asignaturas: List<Asignatura> = emptyList(),
    onAbrir: (Int) -> Unit
) {
    val documentos by vm.documentos.collectAsState()
    val ocupado by vm.ocupado.collectAsState()
    var porEliminar by remember { mutableStateOf<DocumentoEntity?>(null) }
    var uriPendiente by remember { mutableStateOf<Uri?>(null) }   // archivo elegido, falta la asignatura
    var filtro by remember { mutableStateOf<Int?>(null) }          // null = todas las asignaturas

    val colorRol = if (esDocente) ColorDocente else VerdeInstitucional

    // el botón "Cargar" se expande solo cuando la lista está arriba del todo
    val listaEstado = rememberLazyListState()
    val botonExpandido by remember { derivedStateOf { listaEstado.firstVisibleItemIndex == 0 } }

    val selector = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            // el docente primero elige la asignatura; el estudiante carga directo
            if (esDocente && asignaturas.isNotEmpty()) uriPendiente = uri else vm.cargar(uri)
        }
    }

    val visibles = if (filtro == null) documentos else documentos.filter { it.asignaturaId == filtro }

    Box(Modifier.fillMaxSize()) {

        Column(Modifier.fillMaxSize()) {

            // filtro por asignatura (solo docente)
            if (esDocente && asignaturas.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filtro == null,
                            onClick = { filtro = null },
                            label = { Text("Todas") }
                        )
                    }
                    items(asignaturas, key = { it.id }) { a ->
                        FilterChip(
                            selected = filtro == a.id,
                            onClick = { filtro = a.id },
                            label = { Text(a.nombre) }
                        )
                    }
                }
            }

            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (visibles.isEmpty()) {
                    Column(
                        Modifier.fillMaxSize().padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (esDocente) "Aún no hay material aquí" else "Aún no tienes documentos",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (esDocente) "Sube PDF, imágenes o archivos de texto para tus asignaturas."
                            else "Carga PDF, imágenes o archivos de texto para estudiar sin conexión.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        state = listaEstado,
                        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(visibles, key = { it.id }) { doc ->
                            Card(onClick = { onAbrir(doc.id) }, modifier = Modifier.fillMaxWidth().animateItem()) {
                                Row(
                                    Modifier.padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(iconoPara(doc.mime), contentDescription = null, tint = colorRol)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            doc.nombre.substringAfter("_"),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        if (esDocente) {
                                            Text(
                                                asignaturas.find { it.id == doc.asignaturaId }?.nombre ?: "Sin asignatura",
                                                color = colorRol,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                        Text(
                                            "${formatearTamano(doc.tamanoBytes)} · ${formatearFecha(doc.fecha)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    IconButton(onClick = { porEliminar = doc }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = ocupado, modifier = Modifier.align(Alignment.TopCenter)) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        ExtendedFloatingActionButton(
            onClick = { selector.launch(TIPOS_PERMITIDOS) },
            icon = { Icon(Icons.Default.UploadFile, contentDescription = null) },
            text = { Text(if (esDocente) "Subir material" else "Cargar") },
            expanded = botonExpandido,
            containerColor = colorRol,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }

    // docente: ¿a qué asignatura pertenece el archivo?
    uriPendiente?.let { uri ->
        var elegida by remember(uri) { mutableStateOf(filtro ?: asignaturas.first().id) }
        AlertDialog(
            onDismissRequest = { uriPendiente = null },
            title = { Text("¿A qué asignatura pertenece?") },
            text = {
                Column {
                    asignaturas.forEach { a ->
                        Row(
                            Modifier.fillMaxWidth().clickable { elegida = a.id },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = elegida == a.id, onClick = { elegida = a.id })
                            Text(a.nombre)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.cargar(uri, elegida)
                    uriPendiente = null
                }) { Text("Subir") }
            },
            dismissButton = { TextButton(onClick = { uriPendiente = null }) { Text("Cancelar") } }
        )
    }

    porEliminar?.let { doc ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar documento") },
            text = { Text("¿Eliminar \"${doc.nombre.substringAfter("_")}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.eliminar(doc)
                    porEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}

private fun iconoPara(mime: String): ImageVector = when {
    mime == "application/pdf" -> Icons.Default.PictureAsPdf
    mime.startsWith("image/") -> Icons.Default.Image
    else -> Icons.Default.Description
}

private fun formatearTamano(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
}

private fun formatearFecha(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(millis))