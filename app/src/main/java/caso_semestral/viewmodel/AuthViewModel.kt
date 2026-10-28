package com.example.caso_semestral.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.caso_semestral.data.AuthRepository
import com.example.caso_semestral.data.FotoPerfilRepository
import com.example.caso_semestral.data.SesionManager
import com.example.caso_semestral.data.local.AppDatabase
import com.example.caso_semestral.model.Asignatura
import com.example.caso_semestral.model.Rol
import com.example.caso_semestral.validarAliasPorRol
import com.example.caso_semestral.validarLogin
import com.example.caso_semestral.validarRegistro
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Estado de la sesión: evita mostrar el login un instante mientras se lee DataStore
sealed interface EstadoSesion {
    data object Cargando : EstadoSesion
    data object SinSesion : EstadoSesion
    data class ConSesion(val usuarioId: Int) : EstadoSesion
}

data class AuthUiState(
    val cargando: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.obtener(app)
    private val repo = AuthRepository(db.usuarioDao(), db.usuarioAsignaturaDao())
    private val fotoRepo = FotoPerfilRepository(app, db.usuarioDao())
    private val sesion = SesionManager(app)

    val estadoSesion: StateFlow<EstadoSesion> = sesion.usuarioId
        .map { id -> if (id == null) EstadoSesion.SinSesion else EstadoSesion.ConSesion(id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EstadoSesion.Cargando)

    // alias del usuario activo (para mostrarlo en el menú o el perfil)
    val alias: StateFlow<String> = sesion.usuarioId
        .flatMapLatest { id -> if (id == null) flowOf("") else flowOf(repo.obtenerAlias(id) ?: "") }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    // rol del usuario activo; null mientras se lee (así no se muestra por un instante la pantalla equivocada)
    val rol: StateFlow<Rol?> = sesion.usuarioId
        .flatMapLatest { id -> if (id == null) flowOf<Rol?>(null) else flowOf<Rol?>(repo.obtenerRol(id)) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // asignaturas del usuario activo (las que cursa o las que imparte)
    val asignaturas: StateFlow<List<Asignatura>> = sesion.usuarioId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList<Asignatura>()) else flowOf(repo.obtenerAsignaturas(id)) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ruta de la foto de perfil del usuario activo (null = sin foto)
    val foto: StateFlow<String?> = sesion.usuarioId
        .flatMapLatest { id -> if (id == null) flowOf<String?>(null) else fotoRepo.observar(id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    fun iniciarSesion(alias: String, password: String) {
        if (!validarLogin(alias, password)) return
        viewModelScope.launch {
            _ui.value = AuthUiState(cargando = true)
            val id = repo.iniciarSesion(alias, password)
            if (id == null) {
                _ui.value = AuthUiState(error = "Usuario o contraseña incorrectos")
            } else {
                sesion.iniciar(id)
                _ui.value = AuthUiState()
            }
        }
    }

    // paso 1 del registro: avisa si el alias está libre antes de pasar a elegir asignaturas
    fun comprobarAlias(alias: String, resultado: (Boolean) -> Unit) {
        viewModelScope.launch { resultado(repo.aliasDisponible(alias)) }
    }

    // paso 2 del registro: crea la cuenta con su rol y asignaturas, e inicia sesión
    fun registrar(
        alias: String,
        password: String,
        repetir: String,
        rol: Rol,
        asignaturaIds: List<Int>
    ) {
        if (!validarRegistro(alias, password, repetir) || asignaturaIds.isEmpty()) {
            _ui.value = AuthUiState(error = "Revisa los datos ingresados")
            return
        }
        if (!validarAliasPorRol(alias, rol)) {
            _ui.value = AuthUiState(
                error = if (rol == Rol.DOCENTE) "El alias de docente debe ser Docente_[nombre]"
                else "El prefijo Docente_ es solo para docentes"
            )
            return
        }
        viewModelScope.launch {
            _ui.value = AuthUiState(cargando = true)
            val id = repo.registrar(alias, password, rol, asignaturaIds)
            if (id == null) {
                _ui.value = AuthUiState(error = "Ese alias ya está en uso")
            } else {
                sesion.iniciar(id)   // el registro inicia sesión automáticamente
                _ui.value = AuthUiState()
            }
        }
    }

    fun cambiarFoto(uri: Uri) {
        val id = (estadoSesion.value as? EstadoSesion.ConSesion)?.usuarioId ?: return
        viewModelScope.launch { fotoRepo.guardar(id, uri) }
    }

    fun quitarFoto() {
        val id = (estadoSesion.value as? EstadoSesion.ConSesion)?.usuarioId ?: return
        viewModelScope.launch { fotoRepo.quitar(id) }
    }

    fun cerrarSesion() {
        viewModelScope.launch { sesion.cerrar() }
    }

    fun limpiarError() {
        _ui.value = _ui.value.copy(error = null)
    }
}