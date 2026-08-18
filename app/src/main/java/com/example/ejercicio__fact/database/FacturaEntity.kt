package com.example.ejercicio__fact.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "facturas")
data class FacturaEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val rncCedula: String,

    val productoId: Int,

    val cantidad: Int,

    val precio: Double,

    val total: Double,

    val fecha: Long = System.currentTimeMillis()
)