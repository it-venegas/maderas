package com.example.myapp.repository

import com.example.myapp.model.Fechas
import com.example.myapp.model.Rol

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

internal fun sha256(s: String): String =
    MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }

class AsistenciaDb(context: Context) : SQLiteOpenHelper(context.applicationContext, "cenamad.db", null, 1) {

    override fun onConfigure(db: SQLiteDatabase) { db.setForeignKeyConstraintsEnabled(true) }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE usuarios (
                run TEXT PRIMARY KEY, nombre TEXT NOT NULL, cargo TEXT NOT NULL,
                rol TEXT NOT NULL, password_hash TEXT NOT NULL)"""
        )
        db.execSQL(
            """CREATE TABLE asistencias (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                run TEXT NOT NULL REFERENCES usuarios(run),
                fecha TEXT NOT NULL, entrada TEXT, salida TEXT,
                lat REAL, lng REAL, foto TEXT,
                manual INTEGER NOT NULL DEFAULT 0,
                UNIQUE(run, fecha))"""
        )
        sembrar(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    private class UsuarioSemilla(val run: String, val nombre: String, val cargo: String, val rol: Rol, val pass: String)

    private fun sembrar(db: SQLiteDatabase) {
        val usuarios = listOf(
            UsuarioSemilla("11222333-9", "Valentina Rojas", "Jefa de operaciones", Rol.ADMINISTRADOR, "admin123"),
            UsuarioSemilla("18452763-4", "Camila Muñoz", "Operaria de aserradero", Rol.TRABAJADOR, "maderas123"),
            UsuarioSemilla("17654321-3", "Diego Soto", "Operador de maquinaria", Rol.TRABAJADOR, "maderas123"),
            UsuarioSemilla("19876543-0", "Matías Pérez", "Control de calidad", Rol.TRABAJADOR, "maderas123"),
            UsuarioSemilla("20123456-5", "Javiera Díaz", "Logística", Rol.TRABAJADOR, "maderas123")
        )
        usuarios.forEach { u ->
            db.insert("usuarios", null, ContentValues().apply {
                put("run", u.run); put("nombre", u.nombre); put("cargo", u.cargo)
                put("rol", u.rol.name); put("password_hash", sha256(u.pass))
            })
        }
        val trabajadores = usuarios.filter { it.rol == Rol.TRABAJADOR }
        for (d in 1..8) {
            val f = Fechas.haceDias(d)
            if (Fechas.esFinDeSemana(f)) continue
            trabajadores.forEachIndexed { i, u ->
                if ((i + d) % 6 == 0) return@forEachIndexed
                db.insert("asistencias", null, ContentValues().apply {
                    put("run", u.run); put("fecha", f)
                    put("entrada", "08:%02d".format(15 + (i * 7 + d * 3) % 30))
                    put("salida", "17:%02d".format(10 + (i * 5 + d * 4) % 40))
                    put("manual", 0)
                })
            }
        }
    }
}
