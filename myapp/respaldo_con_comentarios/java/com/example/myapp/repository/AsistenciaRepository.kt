package com.example.myapp.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.myapp.model.Asistencia
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

class AsistenciaRepository private constructor(context: Context) {

    private val baseDeDatos = AsistenciaDb(context)
    private val preferencias = context.getSharedPreferences(NOMBRE_PREFERENCIAS, Context.MODE_PRIVATE)

    companion object {
        private var instanciaUnica: AsistenciaRepository? = null

        fun get(context: Context): AsistenciaRepository {
            val instancia = instanciaUnica ?: AsistenciaRepository(context.applicationContext)
            instanciaUnica = instancia
            return instancia
        }
    }

    // ---------- Usuarios e inicio de sesión ----------

    fun autenticar(run: String, password: String): Usuario? =
        leerUsuarios(
            "$SELECCIONAR_USUARIOS WHERE run = ? AND password_hash = ?",
            arrayOf(run, sha256(password))
        ).firstOrNull()

    fun trabajadores(): List<Usuario> =
        leerUsuarios(
            "$SELECCIONAR_USUARIOS WHERE rol = ? ORDER BY nombre",
            arrayOf(Rol.TRABAJADOR.name)
        )

    fun totalTrabajadores(): Int =
        contar("SELECT COUNT(*) FROM usuarios WHERE rol = ?", arrayOf(Rol.TRABAJADOR.name))

    private fun buscarUsuarioPorRun(run: String): Usuario? =
        leerUsuarios("$SELECCIONAR_USUARIOS WHERE run = ?", arrayOf(run)).firstOrNull()

    // ---------- Último usuario (botón de huella) ----------

    fun guardarUltimoRun(run: String) {
        preferencias.edit().putString(CLAVE_ULTIMO_RUN, run).apply()
    }

    fun ultimoUsuario(): Usuario? {
        val run = preferencias.getString(CLAVE_ULTIMO_RUN, null) ?: return null
        return buscarUsuarioPorRun(run)
    }

    // ---------- Sesión recordada ("Recuérdame") ----------
    // Se guarda solo el RUN (nunca la contraseña). Al abrir la app se vuelve a leer
    // el usuario desde la base de datos con ese RUN.

    fun recordarSesion(run: String) {
        preferencias.edit().putString(CLAVE_SESION_RECORDADA, run).apply()
    }

    fun olvidarSesion() {
        preferencias.edit().remove(CLAVE_SESION_RECORDADA).apply()
    }

    fun sesionRecordada(): Usuario? {
        val run = preferencias.getString(CLAVE_SESION_RECORDADA, null) ?: return null
        val usuario = buscarUsuarioPorRun(run)
        if (usuario == null) olvidarSesion()
        return usuario
    }

    // ---------- Consulta de asistencias ----------

    fun historial(run: String? = null): List<Asistencia> {
        if (run == null) return buscarAsistencias("", emptyArray())
        return buscarAsistencias("WHERE a.run = ?", arrayOf(run))
    }

    fun asistenciaDe(run: String, fecha: String): Asistencia? =
        buscarAsistencias("WHERE a.run = ? AND a.fecha = ?", arrayOf(run, fecha)).firstOrNull()

    fun presentes(fecha: String): Int =
        contar(
            "SELECT COUNT(*) FROM asistencias WHERE fecha = ? AND entrada IS NOT NULL AND salida IS NULL",
            arrayOf(fecha)
        )

    // ---------- Registro de asistencias ----------

    fun registrarEntrada(run: String, fecha: String, hora: String, lat: Double?, lng: Double?, foto: String?) {
        val valores = ContentValues()
        valores.put("run", run)
        valores.put("fecha", fecha)
        valores.put("entrada", hora)
        valores.put("manual", 0)
        if (lat != null && lng != null) {
            valores.put("lat", lat)
            valores.put("lng", lng)
        }
        if (foto != null) valores.put("foto", foto)
        baseDeDatos.writableDatabase.insertOrThrow("asistencias", null, valores)
    }

    fun registrarSalida(id: Long, hora: String, foto: String?) {
        val valores = ContentValues()
        valores.put("salida", hora)
        if (foto != null) valores.put("foto", foto)
        baseDeDatos.writableDatabase.update("asistencias", valores, "id = ?", arrayOf(id.toString()))
    }

    fun registrarManual(run: String, fecha: String, entrada: String, salida: String?) {
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

    private fun contar(consulta: String, argumentos: Array<String>): Int =
        leerFilas(consulta, argumentos) { fila -> fila.getInt(0) }.first()

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
