package com.example.myapp.ui

import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

fun pedirHuella(context: Context, onExito: () -> Unit, onError: (String) -> Unit) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        onError("La huella necesita Android 9 o superior")
        return
    }
    mostrarDialogoHuella(context, onExito, onError)
}

@RequiresApi(Build.VERSION_CODES.P)
private fun mostrarDialogoHuella(context: Context, onExito: () -> Unit, onError: (String) -> Unit) {
    val ejecutor = ContextCompat.getMainExecutor(context)

    val dialogo = BiometricPrompt.Builder(context)
        .setTitle("Ingresar con huella")
        .setSubtitle("Apoya tu dedo en el sensor")
        .setNegativeButton("Cancelar", ejecutor) { _, _ -> }
        .build()

    val respuesta = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
            onExito()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
            if (errorCode != 5 && errorCode != 10) {
                onError(errString?.toString() ?: "No se pudo leer la huella")
            }
        }
    }

    dialogo.authenticate(CancellationSignal(), ejecutor, respuesta)
}
