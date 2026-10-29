package com.example.myapp.repository

import com.example.myapp.model.Asistencia
import com.example.myapp.model.Rol
import com.example.myapp.model.Usuario

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

/** Única puerta de acceso a la BD: la UI y los ViewModels nunca escriben SQL. */
class AsistenciaRepository private constructor(private val helper: AsistenciaDb) {

    companion object {
        @Volatile private var instancia: AsistenciaRepository? = null
        fun get(context: Context): AsistenciaRepository = instancia ?: synchronized(this) {
            instancia ?: AsistenciaRepository(AsistenciaDb(context)).also { instancia = it }
        }
    }

    private val select = """SELECT a.id, a.run, u.nombre, a.fecha, a.entrada, a.salida, a.lat, a.lng, a.foto, a.manual
        FROM asistencias a JOIN usuarios u ON u.run = a.run"""

    private fun Cursor.aAsistencia() = Asistencia(
        id = getLong(0), run = getString(1), nombre = getString(2), fecha = getString(3),
        entrada = if (isNull(4)) null else getString(4),
        salida = if (isNull(5)) null else getString(5),
        lat = if (isNull(6)) null else getDouble(6),
        lng = if (isNull(7)) null else getDouble(7),
        foto = if (isNull(8)) null else getString(8),
        manual = getInt(9) == 1
    )

    private fun consultar(where: String, args: Array<String>): List<Asistencia> =
        helper.readableDatabase.rawQuery("$select $where ORDER BY a.fecha DESC, a.entrada DESC", args).use { c ->
            buildList { while (c.moveToNext()) add(c.aAsistencia()) }
        }

    private fun Cursor.aUsuario() = Usuario(getString(0), getString(1), getString(2), Rol.valueOf(getString(3)))

    fun autenticar(run: String, password: String): Usuario? =
        helper.readableDatabase.rawQuery(
            "SELECT run, nombre, cargo, rol FROM usuarios WHERE run = ? AND password_hash = ?",
            arrayOf(run, sha256(password))
        ).use { c -> if (c.moveToFirst()) c.aUsuario() else null }

    fun trabajadores(): List<Usuario> =
        helper.readableDatabase.rawQuery(
            "SELECT run, nombre, cargo, rol FROM usuarios WHERE rol = ? ORDER BY nombre", arrayOf(Rol.TRABAJADOR.name)
        ).use { c -> buildList { while (c.moveToNext()) add(c.aUsuario()) } }

    fun historial(run: String? = null): List<Asistencia> =
        if (run == null) consultar("", emptyArray()) else consultar("WHERE a.run = ?", arrayOf(run))

    fun asistenciaDe(run: String, fecha: String): Asistencia? =
        consultar("WHERE a.run = ? AND a.fecha = ?", arrayOf(run, fecha)).firstOrNull()

    fun registrarEntrada(run: String, fecha: String, hora: String, lat: Double?, lng: Double?, foto: String?) {
        helper.writableDatabase.insertOrThrow("asistencias", null, ContentValues().apply {
            put("run", run); put("fecha", fecha); put("entrada", hora); put("manual", 0)
            if (lat != null && lng != null) { put("lat", lat); put("lng", lng) }
            if (foto != null) put("foto", foto)
        })
    }

    fun registrarSalida(id: Long, hora: String, foto: String?) {
        helper.writableDatabase.update("asistencias", ContentValues().apply {
            put("salida", hora); if (foto != null) put("foto", foto)
        }, "id = ?", arrayOf(id.toString()))
    }

    /** Registro del administrador: crea o reemplaza la asistencia de ese trabajador en esa fecha. */
    fun registrarManual(run: String, fecha: String, entrada: String, salida: String?) {
        helper.writableDatabase.insertWithOnConflict("asistencias", null, ContentValues().apply {
            put("run", run); put("fecha", fecha); put("entrada", entrada); put("manual", 1)
            if (salida.isNullOrBlank()) putNull("salida") else put("salida", salida)
        }, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun presentes(fecha: String): Int =
        helper.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM asistencias WHERE fecha = ? AND entrada IS NOT NULL AND salida IS NULL", arrayOf(fecha)
        ).use { it.moveToFirst(); it.getInt(0) }

    fun totalTrabajadores(): Int =
        helper.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM usuarios WHERE rol = ?", arrayOf(Rol.TRABAJADOR.name)
        ).use { it.moveToFirst(); it.getInt(0) }
}
