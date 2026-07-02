package com.lembra.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FichaAlerta::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fichaAlertaDao(): FichaAlertaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE fichas_alerta ADD COLUMN sincronizarCalendario INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE fichas_alerta ADD COLUMN calendarioId INTEGER NOT NULL DEFAULT -1"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE fichas_alerta ADD COLUMN ubicacion TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE fichas_alerta ADD COLUMN horaMinutos INTEGER NOT NULL DEFAULT -1"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lembra.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { INSTANCE = it }
            }
    }
}
