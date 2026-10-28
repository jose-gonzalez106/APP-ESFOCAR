package com.example.caso_semestral.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sesion")

class SesionManager(context: Context) {

    private val store = context.applicationContext.dataStore
    private val claveUsuario = intPreferencesKey("usuario_id")

    // null = no hay sesión iniciada
    val usuarioId: Flow<Int?> = store.data.map { it[claveUsuario] }

    suspend fun iniciar(usuarioId: Int) {
        store.edit { it[claveUsuario] = usuarioId }
    }

    suspend fun cerrar() {
        store.edit { it.remove(claveUsuario) }
    }
}