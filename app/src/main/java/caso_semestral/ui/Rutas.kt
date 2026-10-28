package com.example.caso_semestral.ui

import com.example.caso_semestral.model.Rol

object Rutas {
    const val BIENVENIDA = "bienvenida"       // elegir: estudiante o docente
    const val LOGIN = "login/{rol}"           // el rol elegido viaja en la ruta
    const val REGISTRO = "registro/{rol}"
    const val INICIO = "inicio"
    const val DOCUMENTOS = "documentos"
    const val PERFIL = "perfil"
    const val VISOR = "visor/{id}"

    fun visor(id: Int) = "visor/$id"
    fun login(rol: Rol) = "login/${rol.name.lowercase()}"
    fun registro(rol: Rol) = "registro/${rol.name.lowercase()}"

    // convierte el texto de la ruta ("docente" / "estudiante") otra vez en Rol
    fun rolDe(texto: String?): Rol = if (texto == "docente") Rol.DOCENTE else Rol.ESTUDIANTE

    val publicas = listOf(BIENVENIDA, LOGIN, REGISTRO)
    val pestanas = listOf(INICIO, DOCUMENTOS, PERFIL)
}
