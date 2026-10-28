package com.example.caso_semestral.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documentos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId")]
)
data class DocumentoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val nombre: String,
    val rutaInterna: String,   // ruta del archivo copiado en filesDir
    val mime: String,
    val tamanoBytes: Long,
    val fecha: Long = System.currentTimeMillis(),
    val asignaturaId: Int = 0  // asignatura a la que pertenece (0 = sin asignatura; lo usa el docente)
)