package com.example.caso_semestral.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentoDao {

    // Flow: la lista en pantalla se actualiza sola al cargar o eliminar
    @Query("SELECT * FROM documentos WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun listar(usuarioId: Int): Flow<List<DocumentoEntity>>

    @Query("SELECT * FROM documentos WHERE id = :id AND usuarioId = :usuarioId LIMIT 1")
    suspend fun obtener(id: Int, usuarioId: Int): DocumentoEntity?

    @Insert
    suspend fun insertar(documento: DocumentoEntity): Long

    @Delete
    suspend fun eliminar(documento: DocumentoEntity)
}