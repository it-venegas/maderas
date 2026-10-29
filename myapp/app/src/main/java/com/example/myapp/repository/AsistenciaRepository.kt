package com.example.myapp.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.myapp.model.Asistencia
import com.example.myapp.model.Rol
import com.example.myapp.model.Usuario

class AsistenciaRepository private constructor(context: Context) {

    private val helper = AsistenciaDb(context)
    private val prefs = context.getSharedPreferences("cenamad", Context.MODE_PRIVATE)

    companion object {
        private var instancia: AsistenciaRepository? = null

        fun get(context: Context): AsistenciaRepository {
            val actual = instancia ?: AsistenciaRepository(context.applicationContext)
            instancia = actual
            return actual
        }
    }

    private val consulta = "SELECT a.id, a.run, u.nombre, a.fecha, a.entrada, a.salida, " +
            "a.lat, a.lng, a.foto, a.manual FROM asistencias a JOIN usuarios u ON u.run = a.run"

    private fun leerAsistencia(c: Cursor) = Asistencia(
        id = c.getLong(0),
        run = c.getString(1),
        nombre = c.getString(2),
        fecha = c.getString(3),
        entrada = c.getString(4),
        salida = c.getString(5),
        lat = if (c.isNull(6)) null else c.getDouble(6),
        lng = if (c.isNull(7)) null else c.getDouble(7),
        foto = c.getString(8),
        manual = c.getInt(9) == 1
    )

    private fun leerUsuario(c: Cursor) =
        Usuario(c.getString(0), c.getString(1), c.getString(2), Rol.valueOf(c.getString(3)))

    private fun buscar(where: String, args: Array<String>): List<Asistencia> {
        val lista = mutableListOf<Asistencia>()
        val c = helper.readableDatabase.rawQuery(
            "$consulta $where ORDER BY a.fecha DESC, a.entrada DESC", args
        )
        while (c.moveToNext()) lista.add(leerAsistencia(c))
        c.close()
        return lista
    }

    private fun contar(sql: String, args: Array<String>): Int {
        val c = helper.readableDatabase.rawQuery(sql, args)
        c.moveToFirst()
        val n = c.getInt(0)
        c.close()
        return n
    }

    fun autenticar(run: String, password: String): Usuario? {
        val c = helper.readableDatabase.rawQuery(
            "SELECT run, nombre, cargo, rol FROM usuarios WHERE run = ? AND password_hash = ?",
            arrayOf(run, sha256(password))
        )
        val usuario = if (c.moveToFirst()) leerUsuario(c) else null
        c.close()
        return usuario
    }

    fun trabajadores(): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        val c = helper.readableDatabase.rawQuery(
            "SELECT run, nombre, cargo, rol FROM usuarios WHERE rol = ? ORDER BY nombre",
            arrayOf(Rol.TRABAJADOR.name)
        )
        while (c.moveToNext()) lista.add(leerUsuario(c))
        c.close()
        return lista
    }

    fun totalTrabajadores(): Int =
        contar("SELECT COUNT(*) FROM usuarios WHERE rol = ?", arrayOf(Rol.TRABAJADOR.name))

    fun guardarUltimoRun(run: String) {
        prefs.edit().putString("ultimo_run", run).apply()
    }

    fun ultimoUsuario(): Usuario? {
        val run = prefs.getString("ultimo_run", null) ?: return null
        val c = helper.readableDatabase.rawQuery(
            "SELECT run, nombre, cargo, rol FROM usuarios WHERE run = ?", arrayOf(run)
        )
        val usuario = if (c.moveToFirst()) leerUsuario(c) else null
        c.close()
        return usuario
    }

    fun historial(run: String? = null): List<Asistencia> {
        if (run == null) return buscar("", emptyArray<String>())
        return buscar("WHERE a.run = ?", arrayOf(run))
    }

    fun asistenciaDe(run: String, fecha: String): Asistencia? =
        buscar("WHERE a.run = ? AND a.fecha = ?", arrayOf(run, fecha)).firstOrNull()

    fun registrarEntrada(run: String, fecha: String, hora: String, lat: Double?, lng: Double?, foto: String?) {
        val datos = ContentValues()
        datos.put("run", run)
        datos.put("fecha", fecha)
        datos.put("entrada", hora)
        datos.put("manual", 0)
        if (lat != null && lng != null) {
            datos.put("lat", lat)
            datos.put("lng", lng)
        }
        if (foto != null) datos.put("foto", foto)
        helper.writableDatabase.insertOrThrow("asistencias", null, datos)
    }

    fun registrarSalida(id: Long, hora: String, foto: String?) {
        val datos = ContentValues()
        datos.put("salida", hora)
        if (foto != null) datos.put("foto", foto)
        helper.writableDatabase.update("asistencias", datos, "id = ?", arrayOf(id.toString()))
    }

    fun registrarManual(run: String, fecha: String, entrada: String, salida: String?) {
        val datos = ContentValues()
        datos.put("run", run)
        datos.put("fecha", fecha)
        datos.put("entrada", entrada)
        datos.put("manual", 1)
        if (salida.isNullOrBlank()) datos.putNull("salida") else datos.put("salida", salida)
        helper.writableDatabase.insertWithOnConflict("asistencias", null, datos, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun presentes(fecha: String): Int =
        contar(
            "SELECT COUNT(*) FROM asistencias WHERE fecha = ? AND entrada IS NOT NULL AND salida IS NULL",
            arrayOf(fecha)
        )
}
