
package com.example.ejercicio__fact

import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ejercicio__fact.database.ClienteEntity
import com.example.ejercicio__fact.database.FacturaEntity
import com.example.ejercicio__fact.database.ProductoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var edtRncCedula: EditText
    private lateinit var edtNombreCliente: EditText
    private lateinit var edtProductoId: EditText
    private lateinit var edtCantidad: EditText

    private lateinit var btnRegistrarCliente: Button
    private lateinit var btnBuscarProducto: Button
    private lateinit var btnRegistrarFactura: Button
    private lateinit var btnGenerarPdf: Button

    private lateinit var txtProducto: TextView
    private lateinit var txtPrecio: TextView
    private lateinit var txtStock: TextView
    private lateinit var txtTotal: TextView
    private lateinit var txtResultado: TextView

    private lateinit var dao: com.example.ejercicio__fact.database.FacturacionDao

    private var productoActual: ProductoEntity? = null

    // Datos de la última factura
    private var ultimaFacturaId: Long = 0
    private var ultimaFacturaFecha: String = ""
    private var ultimoCliente: String = ""
    private var ultimoRncCedula: String = ""
    private var ultimoProducto: String = ""
    private var ultimaCantidad: Int = 0
    private var ultimoPrecio: Double = 0.0
    private var ultimoTotal: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->

            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        inicializarVistas()

        val app = application as FacturacionApp
        dao = app.facturacionDao

        configurarBotones()
    }

    private fun inicializarVistas() {

        edtRncCedula = findViewById(R.id.edtRncCedula)
        edtNombreCliente = findViewById(R.id.edtNombreCliente)
        edtProductoId = findViewById(R.id.edtProductoId)
        edtCantidad = findViewById(R.id.edtCantidad)

        btnRegistrarCliente = findViewById(R.id.btnRegistrarCliente)
        btnBuscarProducto = findViewById(R.id.btnBuscarProducto)
        btnRegistrarFactura = findViewById(R.id.btnRegistrarFactura)
        btnGenerarPdf = findViewById(R.id.btnGenerarPdf)

        txtProducto = findViewById(R.id.txtProducto)
        txtPrecio = findViewById(R.id.txtPrecio)
        txtStock = findViewById(R.id.txtStock)
        txtTotal = findViewById(R.id.txtTotal)
        txtResultado = findViewById(R.id.txtResultado)
    }

    private fun configurarBotones() {

        btnRegistrarCliente.setOnClickListener {
            registrarCliente()
        }

        btnBuscarProducto.setOnClickListener {
            buscarProducto()
        }

        btnRegistrarFactura.setOnClickListener {
            registrarFactura()
        }

        btnGenerarPdf.setOnClickListener {
            generarPDF()
        }
    }

    private fun registrarCliente() {

        val rncCedula = edtRncCedula.text.toString().trim()
        val nombre = edtNombreCliente.text.toString().trim()

        if (rncCedula.isEmpty() || nombre.isEmpty()) {

            Toast.makeText(
                this,
                "Complete los datos del cliente",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        CoroutineScope(Dispatchers.IO).launch {

            val clienteExistente = dao.buscarCliente(rncCedula)

            if (clienteExistente == null) {

                dao.insertarCliente(
                    ClienteEntity(
                        rncCedula = rncCedula,
                        nombre = nombre
                    )
                )

                withContext(Dispatchers.Main) {

                    Toast.makeText(
                        this@MainActivity,
                        "Cliente registrado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } else {

                withContext(Dispatchers.Main) {

                    Toast.makeText(
                        this@MainActivity,
                        "El cliente ya existe",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun buscarProducto() {

        val idTexto = edtProductoId.text.toString().trim()

        if (idTexto.isEmpty()) {

            Toast.makeText(
                this,
                "Ingrese el ID del producto",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val id = idTexto.toIntOrNull()

        if (id == null) {

            Toast.makeText(
                this,
                "El ID debe ser numérico",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        CoroutineScope(Dispatchers.IO).launch {

            val producto = dao.buscarProducto(id)

            withContext(Dispatchers.Main) {

                if (producto != null) {

                    productoActual = producto

                    txtProducto.text =
                        "Producto: ${producto.nombre}"

                    txtPrecio.text =
                        "Precio: RD$ ${String.format("%.2f", producto.precio)}"

                    txtStock.text =
                        "Stock disponible: ${producto.stock}"

                    calcularTotal()

                } else {

                    productoActual = null

                    txtProducto.text = "Producto: No encontrado"
                    txtPrecio.text = "Precio: "
                    txtStock.text = "Stock disponible: "
                    txtTotal.text = "Total: RD$ 0.00"

                    Toast.makeText(
                        this@MainActivity,
                        "Producto no encontrado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun calcularTotal() {

        val producto = productoActual ?: return

        val cantidadTexto =
            edtCantidad.text.toString().trim()

        if (cantidadTexto.isEmpty()) {
            txtTotal.text = "Total: RD$ 0.00"
            return
        }

        val cantidad = cantidadTexto.toIntOrNull()

        if (cantidad == null || cantidad <= 0) {
            txtTotal.text = "Total: RD$ 0.00"
            return
        }

        val total = producto.precio * cantidad

        txtTotal.text =
            "Total: RD$ ${String.format("%.2f", total)}"
    }

    private fun registrarFactura() {

        val rncCedula = edtRncCedula.text.toString().trim()
        val cantidadTexto = edtCantidad.text.toString().trim()

        if (rncCedula.isEmpty()) {

            Toast.makeText(
                this,
                "Ingrese el RNC o Cédula del cliente",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val producto = productoActual

        if (producto == null) {

            Toast.makeText(
                this,
                "Primero busque un producto",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val cantidad = cantidadTexto.toIntOrNull()

        if (cantidad == null || cantidad <= 0) {

            Toast.makeText(
                this,
                "Ingrese una cantidad válida",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (cantidad > producto.stock) {

            Toast.makeText(
                this,
                "Stock insuficiente",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val total = producto.precio * cantidad

        val fechaActual = System.currentTimeMillis()

        val formatoFecha =
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val fechaTexto =
            formatoFecha.format(Date(fechaActual))

        CoroutineScope(Dispatchers.IO).launch {

            val cliente = dao.buscarCliente(rncCedula)

            if (cliente == null) {

                withContext(Dispatchers.Main) {

                    Toast.makeText(
                        this@MainActivity,
                        "El cliente no está registrado",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                return@launch
            }

            val factura = FacturaEntity(
                rncCedula = rncCedula,
                productoId = producto.id,
                cantidad = cantidad,
                precio = producto.precio,
                total = total,
                fecha = fechaActual
            )

            val numeroFactura =
                dao.insertarFactura(factura)

            // Guardar información de la última factura
            ultimaFacturaId = numeroFactura
            ultimaFacturaFecha = fechaTexto
            ultimoCliente = cliente.nombre
            ultimoRncCedula = rncCedula
            ultimoProducto = producto.nombre
            ultimaCantidad = cantidad
            ultimoPrecio = producto.precio
            ultimoTotal = total

            // Descontar el producto del inventario
            dao.actualizarStock(
                productoId = producto.id,
                cantidad = cantidad
            )

            withContext(Dispatchers.Main) {

                txtResultado.text =
                    "FACTURA #$numeroFactura\n\n" +
                            "Fecha: $fechaTexto\n\n" +
                            "Cliente: ${cliente.nombre}\n" +
                            "RNC/Cédula: $rncCedula\n\n" +
                            "Producto: ${producto.nombre}\n" +
                            "Cantidad: $cantidad\n" +
                            "Precio: RD$ ${String.format("%.2f", producto.precio)}\n\n" +
                            "TOTAL: RD$ ${String.format("%.2f", total)}"

                txtStock.text =
                    "Stock disponible: ${producto.stock - cantidad}"

                Toast.makeText(
                    this@MainActivity,
                    "Factura registrada correctamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun generarPDF() {

        if (ultimaFacturaId == 0L) {

            Toast.makeText(
                this,
                "Primero registra una factura",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        try {

            val documento = PdfDocument()

            val paginaInfo = PdfDocument.PageInfo.Builder(
                595,
                842,
                1
            ).create()

            val pagina = documento.startPage(paginaInfo)

            val canvas = pagina.canvas
            val paint = Paint()

            // Título
            paint.textSize = 24f
            paint.isFakeBoldText = true

            canvas.drawText(
                "SISTEMA DE FACTURACIÓN",
                130f,
                60f,
                paint
            )

            // Número de factura
            paint.textSize = 18f

            canvas.drawText(
                "FACTURA #$ultimaFacturaId",
                50f,
                110f,
                paint
            )

            // Fecha
            paint.textSize = 14f
            paint.isFakeBoldText = false

            canvas.drawText(
                "Fecha: $ultimaFacturaFecha",
                50f,
                140f,
                paint
            )

            // Cliente
            canvas.drawText(
                "Cliente: $ultimoCliente",
                50f,
                180f,
                paint
            )

            canvas.drawText(
                "RNC/Cédula: $ultimoRncCedula",
                50f,
                205f,
                paint
            )

            // Producto
            canvas.drawText(
                "Producto: $ultimoProducto",
                50f,
                250f,
                paint
            )

            canvas.drawText(
                "Cantidad: $ultimaCantidad",
                50f,
                275f,
                paint
            )

            canvas.drawText(
                "Precio: RD$ ${String.format("%.2f", ultimoPrecio)}",
                50f,
                300f,
                paint
            )

            // Total
            paint.textSize = 20f
            paint.isFakeBoldText = true

            canvas.drawText(
                "TOTAL: RD$ ${String.format("%.2f", ultimoTotal)}",
                50f,
                350f,
                paint
            )

            // Mensaje final
            paint.textSize = 14f
            paint.isFakeBoldText = false

            canvas.drawText(
                "Gracias por su compra",
                200f,
                400f,
                paint
            )

            documento.finishPage(pagina)

            val carpeta = getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS
            )

            val archivo = File(
                carpeta,
                "Factura_$ultimaFacturaId.pdf"
            )

            val outputStream =
                FileOutputStream(archivo)

            documento.writeTo(outputStream)

            outputStream.close()

            documento.close()

            // Abrir el PDF automáticamente
            abrirPDF(archivo)

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Error al generar PDF: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun abrirPDF(archivo: File) {

        try {

            val uri: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                archivo
            )

            val intent = Intent(Intent.ACTION_VIEW)

            intent.setDataAndType(
                uri,
                "application/pdf"
            )

            intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            startActivity(intent)

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "No hay una aplicación disponible para abrir PDF",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
