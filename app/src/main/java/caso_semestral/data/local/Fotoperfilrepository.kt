package com.example.caso_semestral.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.example.caso_semestral.data.local.UsuarioDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

// Guarda la foto de perfil de cada usuario dentro del almacenamiento interno de la app
// (igual que los documentos): se reduce a 512x512 para que pese poco.
class FotoPerfilRepository(
    context: Context,
    private val usuarioDao: UsuarioDao
) {
    private val appContext = context.applicationContext

    companion object {
        private const val LADO = 512          // la foto final mide 512 x 512 px
        private const val LADO_MAX_LECTURA = 1024
    }

    // ruta de la foto del usuario; emite de nuevo cada vez que cambia
    fun observar(usuarioId: Int): Flow<String?> =
        usuarioDao.observarPorId(usuarioId).map { it?.fotoRuta }

    // true si se guardó bien, false si no se pudo leer la imagen
    suspend fun guardar(usuarioId: Int, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val resolver = appContext.contentResolver

            // 1) leer solo las medidas (sin cargar la imagen entera en memoria)
            val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, medidas) }
                ?: return@withContext false

            // 2) cargarla ya reducida (si es gigante, se lee a la mitad, cuarta parte, etc.)
            var muestra = 1
            while (medidas.outWidth / muestra > LADO_MAX_LECTURA || medidas.outHeight / muestra > LADO_MAX_LECTURA) {
                muestra *= 2
            }
            val original = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = muestra })
            } ?: return@withContext false

            // 3) enderezarla: muchas fotos del celular vienen "acostadas" y traen la rotación en EXIF
            val rotacion = resolver.openInputStream(uri)?.use {
                when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
            val derecha = if (rotacion == 0f) original else Bitmap.createBitmap(
                original, 0, 0, original.width, original.height,
                Matrix().apply { postRotate(rotacion) }, true
            )

            // 4) recortar un cuadrado desde el centro y dejarlo en 512 x 512
            val lado = minOf(derecha.width, derecha.height)
            val cuadrada = Bitmap.createBitmap(derecha, (derecha.width - lado) / 2, (derecha.height - lado) / 2, lado, lado)
            val final = Bitmap.createScaledBitmap(cuadrada, LADO, LADO, true)

            // 5) guardar como JPEG con nombre nuevo (así la pantalla nota el cambio) y borrar la anterior
            val carpeta = File(appContext.filesDir, "fotos").apply { mkdirs() }
            val destino = File(carpeta, "${usuarioId}_${System.currentTimeMillis()}.jpg")
            destino.outputStream().use { final.compress(Bitmap.CompressFormat.JPEG, 90, it) }

            borrarArchivoActual(usuarioId)
            usuarioDao.actualizarFoto(usuarioId, destino.absolutePath)
            true
        } catch (e: Exception) {
            false
        }
    }

    // vuelve a mostrar la inicial del alias
    suspend fun quitar(usuarioId: Int) = withContext(Dispatchers.IO) {
        borrarArchivoActual(usuarioId)
        usuarioDao.actualizarFoto(usuarioId, null)
    }

    private suspend fun borrarArchivoActual(usuarioId: Int) {
        // lee la ruta actual desde la base de datos y borra ese archivo
        val anterior = usuarioDao.buscarPorId(usuarioId)?.fotoRuta ?: return
        File(anterior).delete()
    }
}