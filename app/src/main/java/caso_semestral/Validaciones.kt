package com.example.caso_semestral

import com.example.caso_semestral.model.Rol

// Validaciones de los formularios (misma idea que validarCampos de la guía)

fun validarLogin(usuario: String, password: String): Boolean {
    return usuario.isNotBlank() && password.isNotBlank()
}

fun validarRegistro(alias: String, password: String, repetir: String): Boolean {
    return alias.length >= 4 &&      // alias mínimo 4 caracteres
            password.length >= 8 &&  // contraseña mínimo 8 caracteres
            password == repetir      // las dos contraseñas deben coincidir
}

// Los docentes crean su cuenta con un alias del tipo Docente_[nombre], por ejemplo Docente_Maria.
const val PREFIJO_DOCENTE = "Docente_"

// Docente: el alias debe empezar con "Docente_" y tener un nombre después.
// Estudiante: no puede usar ese prefijo (es solo para docentes).
fun validarAliasPorRol(alias: String, rol: Rol): Boolean {
    val limpio = alias.trim()
    val empiezaConPrefijo = limpio.startsWith(PREFIJO_DOCENTE, ignoreCase = true)
    return if (rol == Rol.DOCENTE) {
        empiezaConPrefijo && limpio.length > PREFIJO_DOCENTE.length
    } else {
        !empiezaConPrefijo
    }
}