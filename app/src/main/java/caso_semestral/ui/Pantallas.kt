package com.example.caso_semestral.ui

import android.content.Context
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.net.Uri
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caso_semestral.ColorDocente
import com.example.caso_semestral.VerdeInstitucional
import com.example.caso_semestral.data.local.DocumentoEntity
import com.example.caso_semestral.model.Asignatura
import com.example.caso_semestral.model.Rol
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------- Paleta (basada en VerdeInstitucional #174A35) ----------
private val FondoInicio = Color(0xFF0E1512)       // fondo oscuro con un toque verde
private val TarjetaInicio = VerdeInstitucional    // tarjetas con el verde de la app
private val VerdeAcento = Color(0xFF8FD6B4)       // verde claro para etiquetas e íconos
private val VerdeBoton = Color(0xFF3E8F6E)        // verde para íconos sobre tarjetas
private val TextoSecundario = Color(0xFFF1F1F1)
private val ColorSinInternet = Color(0xFFFFB74D)  // ámbar para el aviso sin conexión

// ---------- Inicio (estudiante) ----------
@Composable
fun InicioScreen(
    alias: String,
    documentos: List<DocumentoEntity>,
    onIrDocumentos: () -> Unit,
    onAbrir: (Int) -> Unit
) {
    // true si hay internet real; cambia sola cuando se pierde o vuelve la conexión
    val conectado = rememberHayConexion()
    val colorEstado = if (conectado) VerdeAcento else ColorSinInternet

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoInicio)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        LogoInicio(VerdeAcento)
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
                    if (conectado) "Conectado · Modo sin conexión disponible"
                    else "Sin conexión · Tus documentos siguen disponibles",
                    color = colorEstado,
                    fontSize = 12.sp
                )
            }
            Text("¡Hola de nuevo, $alias! 👋", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(
                "Tienes tus materiales listos para consultar sin conexión en cualquier momento.",
                color = TextoSecundario, fontSize = 15.sp
            )
        }

        // Tarjeta grande: cargar documento
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = TarjetaInicio)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("SUBIR MATERIAL DE ESTUDIO", color = VerdeAcento, fontSize = 11.sp)
                        Text("Cargar nuevo documento", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Sube PDFs para estudiar sin conexión.", color = TextoSecundario, fontSize = 14.sp)
                    }
                    IconoCuadrado(Icons.Default.UploadFile)
                }
                // botón blanco con texto verde para que resalte sobre la tarjeta
                Button(
                    onClick = onIrDocumentos,   // la carga se hace en la pestaña Documentos
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = VerdeInstitucional)
                ) { Text("Subir archivo", fontWeight = FontWeight.Bold) }
            }
        }

        // Continuar estudiando: los 2 documentos más recientes (la lista ya viene ordenada por fecha)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Continuar estudiando", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (documentos.isNotEmpty()) {
                Text("Ver todos ›", color = VerdeAcento, fontSize = 14.sp, modifier = Modifier.clickable { onIrDocumentos() })
            }
        }
        if (documentos.isEmpty()) {
            // estado vacío
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TarjetaInicio)
            ) {
                Text(
                    "Aún no tienes documentos. Sube tu primer PDF para empezar a estudiar.",
                    color = TextoSecundario, modifier = Modifier.padding(20.dp)
                )
            }
        } else {
            documentos.take(2).forEach { doc -> TarjetaDocumento(doc) { onAbrir(doc.id) } }
        }

        // Aviso de función futura: quizzes a partir de los documentos
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2420)),
            border = BorderStroke(1.dp, VerdeAcento.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconoCuadrado(Icons.Default.Quiz)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("PRÓXIMAMENTE", color = VerdeAcento, fontSize = 11.sp)
                    Text("Crear quizzes con tus documentos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "En el futuro se implementará la función de crear quizzes a partir de tus documentos para practicar y autoevaluarte.",
                        color = TextoSecundario, fontSize = 14.sp
                    )
                }
            }
        }

        // Tip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(TarjetaInicio)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconoCuadrado(Icons.Default.Lightbulb, 40.dp)
            Text(
                "Tip: tus documentos guardados no consumen datos móviles y funcionan sin internet.",
                color = TextoSecundario, fontSize = 14.sp
            )
        }
    }
}

// true solo si el teléfono tiene internet real; se actualiza sola cuando cambia la conexión
// (internal: también la usa la pantalla de Inicio del docente)
@Composable
internal fun rememberHayConexion(): Boolean {
    val context = LocalContext.current
    val cm = remember { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }

    // INTERNET = la red declara salida a internet, VALIDATED = Android comprobó que realmente funciona
    fun conInternet(caps: NetworkCapabilities?): Boolean =
        caps != null &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

    // estado inicial: ¿hay internet ahora mismo?
    var hayConexion by remember { mutableStateOf(conInternet(cm.getNetworkCapabilities(cm.activeNetwork))) }

    // escucha los cambios mientras la pantalla está visible
    DisposableEffect(cm) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            // se llama cuando la red cambia o termina de validarse (WiFi sin salida queda en false)
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                hayConexion = conInternet(caps)
            }
            override fun onLost(network: Network) { hayConexion = false }
        }
        cm.registerDefaultNetworkCallback(callback)
        onDispose { cm.unregisterNetworkCallback(callback) }   // se desregistra al salir
    }
    return hayConexion
}

// Cuadrado verde con ícono
@Composable
private fun IconoCuadrado(icono: ImageVector, tamano: Dp = 52.dp) {
    Box(
        modifier = Modifier.size(tamano).clip(RoundedCornerShape(14.dp)).background(VerdeBoton),
        contentAlignment = Alignment.Center
    ) { Icon(icono, contentDescription = null, tint = Color.White) }
}

// Tarjeta de un documento reciente con botón para abrirlo
@Composable
private fun TarjetaDocumento(doc: DocumentoEntity, onAbrir: () -> Unit) {
    val fecha = SimpleDateFormat("d MMM yyyy", Locale("es", "CL")).format(Date(doc.fecha))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = TarjetaInicio)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconoCuadrado(Icons.Default.Description)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Offline • $fecha", color = VerdeAcento, fontSize = 12.sp)
                    Text(
                        doc.nombre.substringAfter("_"),   // igual que el título del visor
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 2
                    )
                    Text(formatearTamano(doc.tamanoBytes), color = TextoSecundario, fontSize = 13.sp)
                }
            }
            Button(
                onClick = onAbrir,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = VerdeInstitucional)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Continuar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------- Perfil ----------
// Sirve para los dos roles: el color, la etiqueta y el título de las asignaturas cambian según el rol.
@Composable
fun PerfilScreen(
    alias: String,
    rol: Rol,
    asignaturas: List<Asignatura>,
    fotoRuta: String? = null,                 // NUEVO: foto actual (null = sin foto)
    onCambiarFoto: (Uri) -> Unit = {},        // NUEVO: el usuario eligió una foto
    onQuitarFoto: () -> Unit = {},            // NUEVO: volver a la inicial
    onCerrarSesion: () -> Unit
) {
    val esDocente = rol == Rol.DOCENTE
    val colorRol = if (esDocente) ColorDocente else VerdeInstitucional

    // abre la galería (selector del sistema: no necesita permisos)
    val selectorFoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onCambiarFoto(uri)
    }
    val abrirGaleria = {
        selectorFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        // Cabecera con avatar (inicial del alias) y etiqueta de rol
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorRol)
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // avatar tocable con una cámara chiquita en la esquina
            Box(contentAlignment = Alignment.BottomEnd) {
                AvatarPerfil(
                    fotoRuta = fotoRuta,
                    inicial = alias.take(1).uppercase().ifEmpty { "?" },
                    color = colorRol,
                    tamano = 88.dp,
                    modifier = Modifier.clickable { abrirGaleria() }
                )
                Box(
                    modifier = Modifier.size(30.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Cambiar foto", tint = colorRol, modifier = Modifier.size(18.dp))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { abrirGaleria() }) { Text("Cambiar foto", color = Color.White) }
                if (fotoRuta != null) {
                    TextButton(onClick = onQuitarFoto) { Text("Quitar foto", color = Color.White.copy(alpha = 0.8f)) }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(alias, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            AssistChip(
                onClick = {},
                label = { Text(if (esDocente) "Docente" else "Estudiante", color = Color.White) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.15f)),
                border = null
            )
        }

        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Tarjeta con el nombre y las asignaturas
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Mi cuenta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    FilaInfo(Icons.Default.Person, "Nombre", alias, colorRol)
                    FilaInfo(
                        Icons.Default.School,
                        if (esDocente) "Asignaturas que imparte" else "Mis asignaturas",
                        if (asignaturas.isEmpty()) "Ninguna" else asignaturas.joinToString("\n") { it.nombre },
                        colorRol
                    )
                }
            }

            Button(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorRol, contentColor = Color.White)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
        }
    }
}

// Círculo con la foto del usuario; si no tiene, muestra la inicial de su alias.
// Lo usan los dos roles (estudiante y docente).
@Composable
fun AvatarPerfil(
    fotoRuta: String?,
    inicial: String,
    color: Color,
    tamano: Dp,
    modifier: Modifier = Modifier
) {
    // carga la imagen fuera del hilo principal; se recarga sola si cambia la ruta
    val imagen by produceState<ImageBitmap?>(initialValue = null, fotoRuta) {
        value = if (fotoRuta == null) null else withContext(Dispatchers.IO) {
            BitmapFactory.decodeFile(fotoRuta)?.asImageBitmap()
        }
    }

    Box(
        modifier = modifier.size(tamano).clip(CircleShape).background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        val img = imagen
        if (img != null) {
            Image(
                bitmap = img,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,   // llena el círculo sin deformar
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(text = inicial, color = color, fontSize = (tamano.value * 0.45f).sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Fila de la tarjeta: ícono + título pequeño + valor
@Composable
private fun FilaInfo(icono: ImageVector, titulo: String, valor: String, color: Color = VerdeInstitucional) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icono, contentDescription = null, tint = color)
        Column {
            Text(titulo, style = MaterialTheme.typography.labelMedium)
            Text(valor, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun formatearTamano(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
}