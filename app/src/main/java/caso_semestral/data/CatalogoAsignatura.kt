package com.example.caso_semestral.data

import com.example.caso_semestral.model.Asignatura

// Lista de asignaturas disponibles para elegir al registrarse (estudiante: las que cursa,
// docente: las que imparte).
object CatalogoAsignaturas {
    private val asignaturas = listOf(
        Asignatura(id = 1, nombre = "Asignatura demo A"),
        Asignatura(id = 2, nombre = "Asignatura demo B"),
        Asignatura(id = 3, nombre = "Asignatura demo C")
    )

    fun todas(): List<Asignatura> = asignaturas
}
