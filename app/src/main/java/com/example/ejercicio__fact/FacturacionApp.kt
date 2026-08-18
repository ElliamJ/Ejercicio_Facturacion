package com.example.ejercicio__fact

import android.app.Application
import com.example.ejercicio__fact.database.FacturacionDatabase

class FacturacionApp : Application() {

    val database by lazy {
        FacturacionDatabase.getDatabase(this)
    }

    val facturacionDao by lazy {
        database.facturacionDao()
    }
}