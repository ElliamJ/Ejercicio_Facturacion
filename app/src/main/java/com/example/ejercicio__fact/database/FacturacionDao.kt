package com.example.ejercicio__fact.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FacturacionDao {

    @Query("SELECT * FROM productos WHERE id = :id")
    fun buscarProducto(id: Int): ProductoEntity?

    @Query("SELECT * FROM clientes WHERE rncCedula = :rncCedula")
    fun buscarCliente(rncCedula: String): ClienteEntity?

    @Insert
    fun insertarCliente(cliente: ClienteEntity)

    @Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE)
    fun insertarProducto(producto: ProductoEntity)

    @Insert
    fun insertarFactura(factura: FacturaEntity): Long

    @Query("UPDATE productos SET stock = stock - :cantidad WHERE id = :productoId")
    fun actualizarStock(productoId: Int, cantidad: Int)
}