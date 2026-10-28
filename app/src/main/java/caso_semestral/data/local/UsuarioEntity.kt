package com.example.caso_semestral.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["alias"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val alias: String,
    val passwordHash: String,
    val salt: String,
    val fechaRegistro: Long = System.currentTimeMillis(),
    val rol: String = "ESTUDIANTE",  // "ESTUDIANTE" o "DOCENTE" (nombre del enum Rol)
    val fotoRuta: String? = null     // ruta de la foto de perfil en filesDir (null = sin foto, se muestra la inicial)
)