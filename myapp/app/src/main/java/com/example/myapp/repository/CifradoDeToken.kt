package com.example.myapp.repository

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val ALMACEN_DE_CLAVES = "AndroidKeyStore"
private const val ALIAS_DE_LA_CLAVE = "cenamad_token"
private const val TRANSFORMACION = "AES/GCM/NoPadding"
private const val BYTES_DEL_VECTOR = 12
private const val BITS_DE_LA_ETIQUETA = 128

object CifradoDeToken {

    // ---------- Cifrar y descifrar ----------

    fun cifrar(texto: String): String {
        val cifrador = Cipher.getInstance(TRANSFORMACION)
        cifrador.init(Cipher.ENCRYPT_MODE, obtenerClave())
        val cifrado = cifrador.doFinal(texto.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cifrador.iv + cifrado, Base64.NO_WRAP)
    }

    fun descifrar(guardado: String): String {
        val bytes = Base64.decode(guardado, Base64.NO_WRAP)
        val cifrador = Cipher.getInstance(TRANSFORMACION)
        cifrador.init(
            Cipher.DECRYPT_MODE,
            obtenerClave(),
            GCMParameterSpec(BITS_DE_LA_ETIQUETA, bytes, 0, BYTES_DEL_VECTOR)
        )
        val original = cifrador.doFinal(bytes, BYTES_DEL_VECTOR, bytes.size - BYTES_DEL_VECTOR)
        return String(original, Charsets.UTF_8)
    }

    // ---------- Clave guardada en el sistema ----------

    private fun obtenerClave(): SecretKey {
        val almacen = KeyStore.getInstance(ALMACEN_DE_CLAVES)
        almacen.load(null)
        val existente = almacen.getKey(ALIAS_DE_LA_CLAVE, null) as? SecretKey
        return existente ?: crearClave()
    }

    private fun crearClave(): SecretKey {
        val generador = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ALMACEN_DE_CLAVES)
        val especificacion = KeyGenParameterSpec.Builder(
            ALIAS_DE_LA_CLAVE,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        generador.init(especificacion)
        return generador.generateKey()
    }
}
