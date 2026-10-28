package com.example.caso_semestral.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios WHERE alias = :alias LIMIT 1")
    suspend fun buscarPorAlias(alias: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): UsuarioEntity?

    // Flow: la pantalla se actualiza sola cuando cambia la foto
    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    fun observarPorId(id: Int): Flow<UsuarioEntity?>

    @Query("UPDATE usuarios SET fotoRuta = :ruta WHERE id = :id")
    suspend fun actualizarFoto(id: Int, ruta: String?)

    // devuelve el id generado, o -1 si el alias ya existía
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(usuario: UsuarioEntity): Long
}