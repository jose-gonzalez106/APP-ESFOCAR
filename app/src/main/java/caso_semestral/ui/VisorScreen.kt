package com.example.caso_semestral.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.caso_semestral.data.local.DocumentoEntity
import com.example.caso_semestral.viewmodel.DocumentosViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

private enum class TipoDoc { PDF, IMAGEN, TEXTO, OTRO }

// primero por MIME; si el proveedor no lo informa bien, por extensión
private fun tipoDe(doc: DocumentoEntity): TipoDoc {
    val ext = doc.nombre.substringAfterLast('.', "").lowercase()
    return when {
        doc.mime == "application/pdf" || ext == "pdf" -> TipoDoc.PDF
        doc.mime.startsWith("image/") || ext in listOf("png", "jpg", "jpeg", "webp", "gif") -> TipoDoc.IMAGEN
        doc.mime.startsWith("text/") || ext == "txt" -> TipoDoc.TEXTO
        else -> TipoDoc.OTRO
    }
}

@Composable
fun VisorScreen(vm: DocumentosViewModel, documentoId: Int) {
    val seleccionado by vm.seleccionado.collectAsState()

    LaunchedEffect(documentoId) { vm.seleccionar(documentoId) }

    // solo se muestra cuando el documento cargado es el pedido (evita ver el anterior un instante)
    val doc = seleccionado?.takeIf { it.id == documentoId }

    if (doc == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        val archivo = File(doc.rutaInterna)
        when {
            !archivo.exists() -> MensajeCentrado("El archivo ya no está disponible en el teléfono.")
            tipoDe(doc) == TipoDoc.PDF -> PdfVisor(archivo)
            tipoDe(doc) == TipoDoc.IMAGEN -> ImagenVisor(archivo)
            tipoDe(doc) == TipoDoc.TEXTO -> TextoVisor(archivo)
            else -> MensajeCentrado("Este tipo de archivo no se puede visualizar.")
        }
    }
}

@Composable
private fun MensajeCentrado(texto: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(texto, textAlign = TextAlign.Center)
    }
}

// ---------- PDF ----------

// PdfRenderer solo permite una página abierta a la vez y no es seguro entre hilos:
// el Mutex serializa los accesos
private class PdfDoc(archivo: File) {
    private val pfd = ParcelFileDescriptor.open(archivo, ParcelFileDescriptor.MODE_READ_ONLY)
    private val renderer = PdfRenderer(pfd)
    private val mutex = Mutex()
    private var cerrado = false

    val paginas: Int = renderer.pageCount

    suspend fun renderizar(indice: Int, anchoPx: Int): Bitmap? = mutex.withLock {
        if (cerrado) return@withLock null
        withContext(Dispatchers.IO) {
            val pagina = renderer.openPage(indice)
            try {
                val alto = (anchoPx.toFloat() * pagina.height / pagina.width).toInt().coerceAtLeast(1)
                val bmp = Bitmap.createBitmap(anchoPx, alto, Bitmap.Config.ARGB_8888)
                bmp.eraseColor(android.graphics.Color.WHITE)
                pagina.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bmp
            } finally {
                pagina.close()
            }
        }
    }

    fun cerrar() {
        runBlocking {
            mutex.withLock {
                if (!cerrado) {
                    cerrado = true
                    renderer.close()
                    pfd.close()
                }
            }
        }
    }
}

@Composable
private fun PdfVisor(archivo: File) {
    // si el PDF está dañado o protegido con contraseña, el constructor lanza excepción
    val pdf = remember(archivo) { runCatching { PdfDoc(archivo) }.getOrNull() }

    if (pdf == null) {
        MensajeCentrado("No se pudo abrir el PDF. Puede estar dañado o protegido con contraseña.")
        return
    }

    DisposableEffect(pdf) { onDispose { pdf.cerrar() } }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val anchoPx = constraints.maxWidth.coerceAtMost(1600)

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(count = pdf.paginas) { i ->
                val pagina by produceState<Bitmap?>(null, i) { value = pdf.renderizar(i, anchoPx) }

                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    val bmp = pagina
                    if (bmp == null) {
                        Box(Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Página ${i + 1} de ${pdf.paginas}",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text(
                        "Página ${i + 1} de ${pdf.paginas}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

// ---------- Imagen ----------

private fun decodificar(archivo: File, maximo: Int): Bitmap? {
    val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(archivo.absolutePath, medidas)
    var muestra = 1
    while (medidas.outWidth / muestra > maximo || medidas.outHeight / muestra > maximo) muestra *= 2
    val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
    return BitmapFactory.decodeFile(archivo.absolutePath, opciones)
}

@Composable
private fun ImagenVisor(archivo: File) {
    var cargada by remember(archivo) { mutableStateOf(false) }
    val imagen by produceState<Bitmap?>(null, archivo) {
        value = withContext(Dispatchers.IO) { decodificar(archivo, 2048) }
        cargada = true
    }

    var escala by remember { mutableFloatStateOf(1f) }
    var desplazamiento by remember { mutableStateOf(Offset.Zero) }
    val estadoZoom = rememberTransformableState { zoom, pan, _ ->
        val nueva = (escala * zoom).coerceIn(1f, 5f)
        desplazamiento = if (nueva == 1f) Offset.Zero else desplazamiento + pan
        escala = nueva
    }

    val bmp = imagen
    when {
        bmp != null -> Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "Imagen del documento",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = escala,
                    scaleY = escala,
                    translationX = desplazamiento.x,
                    translationY = desplazamiento.y
                )
                .transformable(estadoZoom)
        )
        cargada -> MensajeCentrado("No se pudo mostrar la imagen.")
        else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    }
}

// ---------- Texto ----------

@Composable
private fun TextoVisor(archivo: File) {
    val texto by produceState<String?>(null, archivo) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                archivo.bufferedReader().use { lector ->
                    val buffer = CharArray(200_000)   // se muestran hasta 200.000 caracteres
                    val leidos = lector.read(buffer)
                    if (leidos > 0) String(buffer, 0, leidos) else ""
                }
            }.getOrNull() ?: "No se pudo leer el archivo."
        }
    }

    val contenido = texto
    if (contenido == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        SelectionContainer {
            Text(
                contenido,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
            )
        }
    }
}