package com.example.caso_semestral.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UsuarioAsignaturaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(items: List<UsuarioAsignaturaEntity>)

    @Query("SELECT asignaturaId FROM usuario_asignaturas WHERE usuarioId = :usuarioId ORDER BY asignaturaId")
    suspend fun idsDe(usuarioId: Int): List<Int>
}