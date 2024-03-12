package com.appsmoviles.magneticfield

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var magneticSensor: Sensor? = null
    private lateinit var magneticFieldTextView: TextView
    private lateinit var btnGuardar: Button
    private lateinit var btnVerDatosGuardados: Button
    private lateinit var btnEliminarDatos: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        magneticFieldTextView = findViewById(R.id.magneticFieldTextView)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnVerDatosGuardados = findViewById(R.id.btnVerDatosGuardados)
        btnEliminarDatos = findViewById(R.id.btnEliminarDatos)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (magneticSensor == null) {
            magneticFieldTextView.text = "Este dispositivo no tiene un sensor magnético."
        }

        btnGuardar.setOnClickListener {
            mostrarDialogoGuardar()
        }

        btnVerDatosGuardados.setOnClickListener {
            mostrarDialogoVerDatosGuardados()
        }

        btnEliminarDatos.setOnClickListener {
            mostrarDialogoEliminarDatos()
        }
    }

    private fun mostrarDialogoGuardar() {
        val currentMagneticData = magneticFieldTextView.text.toString()

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Guardar Datos")
        alertDialogBuilder.setMessage("¿Desea guardar los siguientes datos?\n\n$currentMagneticData")
        alertDialogBuilder.setPositiveButton("Guardar") { dialog, _ ->
            // Guardar datos
            agregarDatosAlmacenamientoInterno(currentMagneticData)
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun agregarDatosAlmacenamientoInterno(data: String) {
        try {
            val fileName = "magnetismo_data.txt"
            val oldData = leerDesdeAlmacenamientoInterno()
            val newData = "$data\n$oldData"

            // Dividir los datos en conjuntos de 4 líneas
            val lines = newData.lines().chunked(4)

            // Limitar a 10 conjuntos
            val limitedLines = lines.take(10).flatten()

            val outputStream: FileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
            for (line in limitedLines) {
                outputStream.write("$line\n".toByteArray())
            }
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun mostrarDialogoVerDatosGuardados() {
        val savedData = leerDesdeAlmacenamientoInterno()
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Datos Guardados")
        alertDialogBuilder.setMessage("Datos almacenados:\n\n$savedData")
        alertDialogBuilder.setPositiveButton("Aceptar") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun leerDesdeAlmacenamientoInterno(): String {
        return try {
            val fileName = "magnetismo_data.txt"
            val inputStream = openFileInput(fileName)
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            String(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }

    private fun mostrarDialogoEliminarDatos() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Eliminar Datos")
        alertDialogBuilder.setMessage("¿Está seguro de que desea eliminar todos los datos guardados?")
        alertDialogBuilder.setPositiveButton("Sí") { dialog, _ ->
            eliminarDatosAlmacenados()
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun eliminarDatosAlmacenados() {
        try {
            val nombreArchivo = "magnetismo_data.txt"
            val salida: FileOutputStream = openFileOutput(nombreArchivo, Context.MODE_PRIVATE)
            salida.close()
            Toast.makeText(this, "Datos eliminados correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al eliminar datos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        magneticSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            val magneticX = event.values[0]
            val magneticY = event.values[1]
            val magneticZ = event.values[2]

            magneticFieldTextView.text = "Fuerza del campo magnético:\nX: $magneticX\nY: $magneticY\nZ: $magneticZ"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
