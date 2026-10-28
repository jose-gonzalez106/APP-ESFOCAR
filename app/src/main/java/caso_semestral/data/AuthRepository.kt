package com.example.caso_semestral.data

import com.example.caso_semestral.data.local.UsuarioAsignaturaDao
import com.example.caso_semestral.data.local.UsuarioAsignaturaEntity
import com.example.caso_semestral.data.local.UsuarioDao
import com.example.caso_semestral.data.local.UsuarioEntity
import com.example.caso_semestral.model.Asignatura
import com.example.caso_semestral.model.Rol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val usuarioDao: UsuarioDao,
    private val asignaturaDao: UsuarioAsignaturaDao
) {

    // paso 1 del registro: ¿el alias está libre?
    suspend fun aliasDisponible(alias: String): Boolean =
        usuarioDao.buscarPorAlias(alias.trim()) == null

    // crea la cuenta con su rol y guarda las asignaturas elegidas (paso 2)
    // devuelve el id del usuario creado, o null si el alias ya existe
    suspend fun registrar(
        alias: String,
        password: String,
        rol: Rol,
        asignaturaIds: List<Int>
    ): Int? = withContext(Dispatchers.Default) {
        val aliasLimpio = alias.trim()
        val salt = Seguridad.nuevoSalt()
        val usuario = UsuarioEntity(
            alias = aliasLimpio,
            passwordHash = Seguridad.hash(password, salt),
            salt = salt,
            rol = rol.name
        )
        val id = usuarioDao.insertar(usuario)
        if (id == -1L) {
            null
        } else {
            val usuarioId = id.toInt()
            asignaturaDao.insertar(
                asignaturaIds.distinct().map { UsuarioAsignaturaEntity(usuarioId, it) }
            )
            usuarioId
        }
    }

    // devuelve elid si las credenciales son correctas, o null
    suspend fun iniciarSesion(alias: String, password: String): Int? = withContext(Dispatchers.Default) {
        val usuario = usuarioDao.buscarPorAlias(alias.trim()) ?: return@withContext null
        if (Seguridad.coincide(password, usuario.salt, usuario.passwordHash)) usuario.id else null
    }

    suspend fun obtenerAlias(usuarioId: Int): String? = usuarioDao.buscarPorId(usuarioId)?.alias

    // las cuentas anteriores a esta versión quedan como ESTUDIANTE
    suspend fun obtenerRol(usuarioId: Int): Rol {
        val texto = usuarioDao.buscarPorId(usuarioId)?.rol ?: return Rol.ESTUDIANTE
        return runCatching { Rol.valueOf(texto) }.getOrDefault(Rol.ESTUDIANTE)
    }

    suspend fun obtenerAsignaturas(usuarioId: Int): List<Asignatura> {
        val ids = asignaturaDao.idsDe(usuarioId)
        return CatalogoAsignaturas.todas().filter { it.id in ids }
    }
}
