
package com.example.caso_semestral.data.local

import androidx.room.Entity
import androidx.room.ForeignKey

// Asignaturas de un usuario: las que cursa el estudiante o las que imparte el docente.
@Entity(
    tableName = "usuario_asignaturas",
    primaryKeys = ["usuarioId", "asignaturaId"],
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UsuarioAsignaturaEntity(
    val usuarioId: Int,
    val asignaturaId: Int
)