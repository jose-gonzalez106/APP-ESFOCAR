package com.example.caso_semestral.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.caso_semestral.data.DocumentoRepository
import com.example.caso_semestral.data.local.AppDatabase
import com.example.caso_semestral.data.local.DocumentoEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentosViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = DocumentoRepository(app, AppDatabase.obtener(app).documentoDao())

    // se asigna al iniciar sesión; la lista se recalcula sola si cambia
    private val usuarioId = MutableStateFlow<Int?>(null)

    val documentos: StateFlow<List<DocumentoEntity>> = usuarioId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repo.listar(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _seleccionado = MutableStateFlow<DocumentoEntity?>(null)
    val seleccionado: StateFlow<DocumentoEntity?> = _seleccionado.asStateFlow()

    // mensajes para mostrar en un Snackbar (null = nada que mostrar)
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _ocupado = MutableStateFlow(false)
    val ocupado: StateFlow<Boolean> = _ocupado.asStateFlow()

    fun establecerUsuario(id: Int?) {
        usuarioId.value = id
        if (id == null) _seleccionado.value = null   // al cerrar sesión se limpia todo
    }

    fun cargar(uri: Uri, asignaturaId: Int = 0) {
        val id = usuarioId.value ?: return
        viewModelScope.launch {
            _ocupado.value = true
            val resultado = repo.cargar(id, uri, asignaturaId)
            _mensaje.value = if (resultado != null) {
                "Documento cargado"
            } else {
                "No se pudo cargar (máximo 20 MB)"
            }
            _ocupado.value = false
        }
    }

    fun eliminar(documento: DocumentoEntity) {
        viewModelScope.launch {
            repo.eliminar(documento)
            if (_seleccionado.value?.id == documento.id) _seleccionado.value = null
            _mensaje.value = "Documento eliminado"
        }
    }

    fun seleccionar(documentoId: Int) {
        val id = usuarioId.value ?: return
        viewModelScope.launch { _seleccionado.value = repo.obtener(documentoId, id) }
    }

    fun mensajeMostrado() {
        _mensaje.value = null
    }
}