package com.example.myapp.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.myapp.model.Asistencia
import com.example.myapp.model.Fechas
import com.example.myapp.model.Rol
import com.example.myapp.model.Usuario

private const val NOMBRE_PREFERENCIAS = "cenamad"
private const val CLAVE_ULTIMO_RUN = "ultimo_run"
private const val CLAVE_SESION_RECORDADA = "sesion_run"

private const val SELECCIONAR_ASISTENCIAS =
    "SELECT a.id AS id, a.run AS run, u.nombre AS nombre, a.fecha AS fecha, " +
        "a.entrada AS entrada, a.salida AS salida, a.lat AS lat, a.lng AS lng, " +
        "a.foto AS foto, a.manual AS manual " +
        "FROM asistencias a JOIN usuarios u ON u.run = a.run"
private const val ORDEN_MAS_RECIENTE_PRIMERO = "ORDER BY a.fecha DESC, a.entrada DESC"

private const val SELECCIONAR_USUARIOS = "SELECT run, nombre, cargo, rol FROM usuarios"

class AsistenciaRepositoryLocal(context: Context) : AsistenciaRepository {
    private val baseDeDatos = AsistenciaDb(context)
    private val preferencias = context.getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE)

    // ---------- Usuarios e inicio de sesión ----------

    override suspend fun autenticar(run: String, password: String): Usuario? =
        leerUsuarios(
            "$SELECCIONAR_USUARIOS WHERE run = ? AND password_hash = ?",
            arrayOf(run, sha256(password))
        ).firstOrNull()

    override suspend fun trabajadores(): List<Usuario> =
        leerUsuarios(
            "$SELECCIONAR_USUARIOS WHERE rol = ? ORDER BY nombre",
            arrayOf(Rol.TRABAJADOR.name)
        )

    private fun buscarUsuarioPorRun(run: String): Usuario? =
        leerUsuarios("$SELECCIONAR_USUARIOS WHERE run = ?", arrayOf(run)).firstOrNull()

    // ---------- Último usuario (botón de huella) ----------

    override fun guardarUltimoRun(run: String) {
        preferencias.edit().putString(CLAVE_ULTIMO_RUN, run).apply()
    }

    override suspend fun ultimoUsuario(): Usuario? {
        val run = preferencias.getString(CLAVE_ULTIMO_RUN, null) ?: return null
        return buscarUsuarioPorRun(run)
    }

    // ---------- Sesión recordada ("Recuérdame") ----------

    override fun recordarSesion(usuario: Usuario) {
        preferencias.edit().putString(CLAVE_SESION_RECORDADA, usuario.run).apply()
    }

    override fun olvidarSesion() {
        preferencias.edit().remove(CLAVE_SESION_RECORDADA).apply()
    }

    override suspend fun sesionRecordada(): Usuario? {
        val run = preferencias.getString(CLAVE_SESION_RECORDADA, null) ?: return null
        val usuario = buscarUsuarioPorRun(run)
        if (usuario == null) olvidarSesion()
        return usuario
    }

    // ---------- Consulta de asistencias ----------

    override suspend fun historial(run: String?): List<Asistencia> {
        if (run == null) return buscarAsistencias("", emptyArray())
        return buscarAsistencias("WHERE a.run = ?", arrayOf(run))
    }

    override suspend fun asistenciaDe(run: String, fecha: String): Asistencia? =
        buscarAsistencias("WHERE a.run = ? AND a.fecha = ?", arrayOf(run, fecha)).firstOrNull()

    // ---------- Registro de asistencias ----------

    override suspend fun registrarEntrada(run: String, foto: String?, ubicacion: Ubicacion?): Asistencia {
        val fecha = Fechas.hoy()
        val hora = Fechas.ahora()
        val valores = ContentValues()
        valores.put("run", run)
        valores.put("fecha", fecha)
        valores.put("entrada", hora)
        valores.put("manual", 0)
        if (ubicacion != null) {
            valores.put("lat", ubicacion.lat)
            valores.put("lng", ubicacion.lng)
        }
        if (foto != null) valores.put("foto", foto)
        val id = baseDeDatos.writableDatabase.insertOrThrow("asistencias", null, valores)
        return Asistencia(
            id = id,
            run = run,
            nombre = buscarUsuarioPorRun(run)?.nombre ?: "",
            fecha = fecha,
            entrada = hora,
            salida = null,
            lat = ubicacion?.lat,
            lng = ubicacion?.lng,
            foto = foto,
            manual = false
        )
    }

    override suspend fun registrarSalida(registro: Asistencia, foto: String?): Asistencia {
        val hora = Fechas.ahora()
        val valores = ContentValues()
        valores.put("salida", hora)
        if (foto != null) valores.put("foto", foto)
        baseDeDatos.writableDatabase.update("asistencias", valores, "id = ?", arrayOf(registro.id.toString()))
        return registro.copy(salida = hora, foto = foto ?: registro.foto)
    }

    override suspend fun registrarManual(run: String, fecha: String, entrada: String, salida: String?) {
        val valores = ContentValues()
        valores.put("run", run)
        valores.put("fecha", fecha)
        valores.put("entrada", entrada)
        valores.put("manual", 1)
        if (salida.isNullOrBlank()) valores.putNull("salida") else valores.put("salida", salida)
        baseDeDatos.writableDatabase.insertWithOnConflict(
            "asistencias", null, valores, SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    // ---------- Lectura de la base de datos ----------

    private fun buscarAsistencias(condicion: String, argumentos: Array<String>): List<Asistencia> =
        leerFilas(
            "$SELECCIONAR_ASISTENCIAS $condicion $ORDEN_MAS_RECIENTE_PRIMERO",
            argumentos
        ) { fila -> leerAsistencia(fila) }

    private fun leerUsuarios(consulta: String, argumentos: Array<String>): List<Usuario> =
        leerFilas(consulta, argumentos) { fila -> leerUsuario(fila) }

    private fun <T> leerFilas(consulta: String, argumentos: Array<String>, convertir: (Cursor) -> T): List<T> {
        val resultado = mutableListOf<T>()
        baseDeDatos.readableDatabase.rawQuery(consulta, argumentos).use { filas ->
            while (filas.moveToNext()) resultado.add(convertir(filas))
        }
        return resultado
    }

    private fun leerAsistencia(fila: Cursor) = Asistencia(
        id = fila.enteroLargo("id"),
        run = fila.texto("run"),
        nombre = fila.texto("nombre"),
        fecha = fila.texto("fecha"),
        entrada = fila.textoOpcional("entrada"),
        salida = fila.textoOpcional("salida"),
        lat = fila.decimalOpcional("lat"),
        lng = fila.decimalOpcional("lng"),
        foto = fila.textoOpcional("foto"),
        manual = fila.entero("manual") == 1
    )

    private fun leerUsuario(fila: Cursor) = Usuario(
        run = fila.texto("run"),
        nombre = fila.texto("nombre"),
        cargo = fila.texto("cargo"),
        rol = Rol.valueOf(fila.texto("rol"))
    )
}

// ---------- Lectura de columnas por nombre ----------

private fun Cursor.posicionDe(columna: String): Int = getColumnIndexOrThrow(columna)

private fun Cursor.texto(columna: String): String = getString(posicionDe(columna))

private fun Cursor.textoOpcional(columna: String): String? = getString(posicionDe(columna))

private fun Cursor.entero(columna: String): Int = getInt(posicionDe(columna))

private fun Cursor.enteroLargo(columna: String): Long = getLong(posicionDe(columna))

private fun Cursor.decimalOpcional(columna: String): Double? {
    val posicion = posicionDe(columna)
    return if (isNull(posicion)) null else getDouble(posicion)
}
