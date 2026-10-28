package com.example.caso_semestral.data

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object Seguridad {

    private const val ITERACIONES = 120_000
    private const val LARGO_CLAVE = 256

    fun nuevoSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.toHex()
    }

    fun hash(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), salt.hexToBytes(), ITERACIONES, LARGO_CLAVE)
        val clave = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec)
        return clave.encoded.toHex()
    }

    // comparación en tiempo constante (evita ataques por tiempo de respuesta)
    fun coincide(password: String, salt: String, hashGuardado: String): Boolean {
        val calculado = hash(password, salt).toByteArray()
        return MessageDigest.isEqual(calculado, hashGuardado.toByteArray())
    }

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
    private fun String.hexToBytes() = chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}