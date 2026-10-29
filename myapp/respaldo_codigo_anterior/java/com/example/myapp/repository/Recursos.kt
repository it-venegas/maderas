package com.example.myapp.repository


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.LocationManager
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

data class Ubicacion(val lat: Double, val lng: Double)

/** Acceso a recursos del dispositivo (rúbrica IE 2.4.1): ubicación y almacenamiento de la foto de la cámara. */
object Recursos {
    val PERMISOS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private fun concedido(c: Context, p: String) =
        ContextCompat.checkSelfPermission(c, p) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun ubicacion(context: Context): Ubicacion? {
        if (!concedido(context, Manifest.permission.ACCESS_FINE_LOCATION) &&
            !concedido(context, Manifest.permission.ACCESS_COARSE_LOCATION)) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val mejor = lm.getProviders(true).mapNotNull {
            try { lm.getLastKnownLocation(it) } catch (e: SecurityException) { null }
        }.maxByOrNull { it.time }
        return mejor?.let { Ubicacion(it.latitude, it.longitude) }
    }

    fun guardarFoto(context: Context, bitmap: Bitmap): String {
        val dir = File(context.filesDir, "fotos").apply { mkdirs() }
        val archivo = File(dir, "reg_${System.currentTimeMillis()}.jpg")
        FileOutputStream(archivo).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        return archivo.absolutePath
    }
}
