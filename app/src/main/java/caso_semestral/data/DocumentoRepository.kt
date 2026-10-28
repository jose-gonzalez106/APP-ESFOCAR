package com.example.caso_semestral.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.caso_semestral.data.local.DocumentoDao
import com.example.caso_semestral.data.local.DocumentoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class DocumentoRepository(
    context: Context,
    private val documentoDao: DocumentoDao
) {
    private val appContext = context.applicationContext

    companion object {
        const val MAX_BYTES = 20L * 1024 * 1024   // límite de 20 MB por documento
    }

    fun listar(usuarioId: Int): Flow<List<DocumentoEntity>> = documentoDao.listar(usuarioId)

    suspend fun obtener(id: Int, usuarioId: Int): DocumentoEntity? = documentoDao.obtener(id, usuarioId)

    // copia el archivo elegido al almacenamiento interno y guarda sus metadatos
    // devuelve null si superó el tamaño máximo o no se pudo leer
    suspend fun cargar(usuarioId: Int, uri: Uri, asignaturaId: Int = 0): DocumentoEntity? = withContext(Dispatchers.IO) {        val resolver = appContext.contentResolver

        val nombreOriginal = resolver.query(uri, null, null, null, null)?.use { c ->
            val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (i >= 0 && c.moveToFirst()) c.getString(i) else null
        } ?: "documento"

        val nombreSeguro = File(nombreOriginal).name.replace(Regex("[^A-Za-z0-9._ -]"), "_")

        val carpeta = File(appContext.filesDir, "documentos/$usuarioId").apply { mkdirs() }
        val destino = File(carpeta, "${UUID.randomUUID()}_$nombreSeguro")

        try {
            val entrada = resolver.openInputStream(uri) ?: return@withContext null
            entrada.use { input ->
                destino.outputStream().use { output ->
                    var total = 0L
                    val buffer = ByteArray(8 * 1024)
                    while (true) {
                        val leidos = input.read(buffer)
                        if (leidos == -1) break
                        total += leidos
                        if (total > MAX_BYTES) {
                            destino.delete()
                            return@withContext null
                        }
                        output.write(buffer, 0, leidos)
                    }
                }
            }
        } catch (e: Exception) {
            destino.delete()
            return@withContext null
        }

        val documento = DocumentoEntity(
            usuarioId = usuarioId,
            nombre = nombreSeguro,
            rutaInterna = destino.absolutePath,
            mime = resolver.getType(uri) ?: "application/octet-stream",
            tamanoBytes = destino.length(),
            asignaturaId = asignaturaId
        )
        val id = documentoDao.insertar(documento)
        documento.copy(id = id.toInt())
    }

    // borra el archivo físico y el registro
    suspend fun eliminar(documento: DocumentoEntity) = withContext(Dispatchers.IO) {
        File(documento.rutaInterna).delete()
        documentoDao.eliminar(documento)
    }
}