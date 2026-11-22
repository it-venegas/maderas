package com.example.myapp.repository

import com.example.myapp.model.Fechas
import com.example.myapp.model.Rol

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

private const val NOMBRE_BASE_DE_DATOS = "cenamad.db"
private const val VERSION_BASE_DE_DATOS = 1
private const val DIAS_DE_HISTORIAL_DE_EJEMPLO = 8

internal fun sha256(texto: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(texto.toByteArray())
        .joinToString("") { "%02x".format(it) }

class AsistenciaDb(context: Context) :
    SQLiteOpenHelper(context.applicationContext, NOMBRE_BASE_DE_DATOS, null, VERSION_BASE_DE_DATOS) {

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        crearTablaUsuarios(db)
        crearTablaAsistencias(db)
        insertarDatosDeEjemplo(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS asistencias")
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS asistencias")
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        onCreate(db)
    }

    // ---------- Tablas ----------

    private fun crearTablaUsuarios(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE usuarios (
                run TEXT PRIMARY KEY, nombre TEXT NOT NULL, cargo TEXT NOT NULL,
                rol TEXT NOT NULL, password_hash TEXT NOT NULL)"""
        )
    }

    private fun crearTablaAsistencias(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE asistencias (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                run TEXT NOT NULL REFERENCES usuarios(run),
                fecha TEXT NOT NULL, entrada TEXT, salida TEXT,
                lat REAL, lng REAL, foto TEXT,
                manual INTEGER NOT NULL DEFAULT 0,
                UNIQUE(run, fecha))"""
        )
    }

    // ---------- Datos de ejemplo ----------

    private class UsuarioDeEjemplo(
        val run: String,
        val nombre: String,
        val cargo: String,
        val rol: Rol,
        val password: String
    )

    private val usuariosDeEjemplo = listOf(
        UsuarioDeEjemplo("11222333-9", "Valentina Rojas", "Jefa de operaciones", Rol.ADMINISTRADOR, "admin123"),
        UsuarioDeEjemplo("18452763-4", "Camila Muñoz", "Operaria de aserradero", Rol.TRABAJADOR, "maderas123"),
        UsuarioDeEjemplo("17654321-3", "Diego Soto", "Operador de maquinaria", Rol.TRABAJADOR, "maderas123"),
        UsuarioDeEjemplo("19876543-0", "Matías Pérez", "Control de calidad", Rol.TRABAJADOR, "maderas123"),
        UsuarioDeEjemplo("20123456-5", "Javiera Díaz", "Logística", Rol.TRABAJADOR, "maderas123")
    )

    private fun insertarDatosDeEjemplo(db: SQLiteDatabase) {
        usuariosDeEjemplo.forEach { insertarUsuario(db, it) }
        insertarAsistenciasDeEjemplo(db, usuariosDeEjemplo.filter { it.rol == Rol.TRABAJADOR })
    }

    private fun insertarUsuario(db: SQLiteDatabase, usuario: UsuarioDeEjemplo) {
        val valores = ContentValues().apply {
            put("run", usuario.run)
            put("nombre", usuario.nombre)
            put("cargo", usuario.cargo)
            put("rol", usuario.rol.name)
            put("password_hash", sha256(usuario.password))
        }
        db.insert("usuarios", null, valores)
    }

    private fun insertarAsistenciasDeEjemplo(db: SQLiteDatabase, trabajadores: List<UsuarioDeEjemplo>) {
        for (diasAtras in 1..DIAS_DE_HISTORIAL_DE_EJEMPLO) {
            val fecha = Fechas.haceDias(diasAtras)
            if (Fechas.esFinDeSemana(fecha)) continue

            trabajadores.forEachIndexed { posicion, trabajador ->
                if (faltaEseDia(posicion, diasAtras)) return@forEachIndexed
                insertarAsistencia(db, trabajador, fecha, posicion, diasAtras)
            }
        }
    }

    // Para que el historial de ejemplo no sea perfecto, cada tanto un trabajador falta un día.
    private fun faltaEseDia(posicionDelTrabajador: Int, diasAtras: Int): Boolean =
        (posicionDelTrabajador + diasAtras) % 6 == 0

    private fun insertarAsistencia(
        db: SQLiteDatabase,
        trabajador: UsuarioDeEjemplo,
        fecha: String,
        posicionDelTrabajador: Int,
        diasAtras: Int
    ) {
        val minutoDeEntrada = 15 + (posicionDelTrabajador * 7 + diasAtras * 3) % 30
        val minutoDeSalida = 10 + (posicionDelTrabajador * 5 + diasAtras * 4) % 40

        val valores = ContentValues().apply {
            put("run", trabajador.run)
            put("fecha", fecha)
            put("entrada", "08:%02d".format(minutoDeEntrada))
            put("salida", "17:%02d".format(minutoDeSalida))
            put("manual", 0)
        }
        db.insert("asistencias", null, valores)
    }
}
