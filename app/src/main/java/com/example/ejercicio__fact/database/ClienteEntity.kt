package com.example.ejercicio__fact.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class ClienteEntity(

    @PrimaryKey
    val rncCedula: String,

    val nombre: String
)