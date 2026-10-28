package com.example.caso_semestral.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// v1 -> v2: agrega el rol al usuario, la tabla de asignaturas por usuario
// y la asignatura en cada documento. No borra los datos que ya existían.
private val MIGRACION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE usuarios ADD COLUMN rol TEXT NOT NULL DEFAULT 'ESTUDIANTE'")
        db.execSQL("ALTER TABLE documentos ADD COLUMN asignaturaId INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `usuario_asignaturas` (" +
                    "`usuarioId` INTEGER NOT NULL, `asignaturaId` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`usuarioId`, `asignaturaId`), " +
                    "FOREIGN KEY(`usuarioId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
    }
}

// v2 -> v3: agrega la foto de perfil al usuario (null = sin foto). No borra datos.
private val MIGRACION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE usuarios ADD COLUMN fotoRuta TEXT")
    }
}

@Database(
    entities = [UsuarioEntity::class, DocumentoEntity::class, UsuarioAsignaturaEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun documentoDao(): DocumentoDao
    abstract fun usuarioAsignaturaDao(): UsuarioAsignaturaDao

    companion object {
        @Volatile private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "esfocar.db"
                ).addMigrations(MIGRACION_1_2, MIGRACION_2_3).build().also { instancia = it }
            }
    }
}