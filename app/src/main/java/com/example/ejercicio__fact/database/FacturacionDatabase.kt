package com.example.ejercicio__fact.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
@Database(
    entities = [
        ClienteEntity::class,
        ProductoEntity::class,
        FacturaEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FacturacionDatabase : RoomDatabase() {

    abstract fun facturacionDao(): FacturacionDao

    companion object {

        @Volatile
        private var INSTANCE: FacturacionDatabase? = null

        fun getDatabase(context: Context): FacturacionDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FacturacionDatabase::class.java,
                    "facturacion_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                // Crear producto de prueba
                CoroutineScope(Dispatchers.IO).launch {

                    instance.facturacionDao().insertarProducto(
                        ProductoEntity(
                            id = 1,
                            nombre = "Producto de prueba",
                            precio = 500.00,
                            stock = 10
                        )
                    )
                }

                instance
            }
        }
    }
}