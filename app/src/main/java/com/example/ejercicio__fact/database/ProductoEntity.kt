package com.example.ejercicio__fact.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class ProductoEntity(

    @PrimaryKey
    val id: Int,

    val nombre: String,

    val precio: Double,

    val stock: Int
)