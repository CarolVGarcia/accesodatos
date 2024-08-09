package com.example.agendafirebase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btnGuardar: Button
    private lateinit var btnListar: Button
    private lateinit var btnLimpiar: Button
    private lateinit var txtNombre: TextView
    private lateinit var txtDireccion: TextView
    private lateinit var txtTelefono1: TextView
    private lateinit var txtTelefono2: TextView
    private lateinit var txtNotas: TextView
    private lateinit var cbkFavorite: CheckBox
    private lateinit var basedatabase: FirebaseDatabase
    private lateinit var referencia: DatabaseReference
    private var savedContacto: Contactos? = null
    private var id: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initComponents()
        setEvents()
    }

    private fun initComponents() {
        // Se obtiene una instancia de la base de datos y se obtiene la referencia que apunta a la tabla contactos
        basedatabase = FirebaseDatabase.getInstance()
        referencia = basedatabase.getReferenceFromUrl(
            "${ReferenciasFirebase().URL_DATABASE}${ReferenciasFirebase().DATABASE_NAME}/${ReferenciasFirebase().TABLE_NAME}"
        )
        //-----------------------------------------------------------------------
        txtNombre = findViewById(R.id.txtNombre)
        txtTelefono1 = findViewById(R.id.txtTelefono1)
        txtTelefono2 = findViewById(R.id.txtTelefono2)
        txtDireccion = findViewById(R.id.txtDireccion)
        txtNotas = findViewById(R.id.txtNotas)
        cbkFavorite = findViewById(R.id.cbxFavorito)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnListar = findViewById(R.id.btnListar)
        btnLimpiar = findViewById(R.id.btnLimpiar)
        savedContacto = null
    }

    private fun setEvents() {
        btnGuardar.setOnClickListener(this)
        btnListar.setOnClickListener(this)
        btnLimpiar.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (isNetworkAvailable()) {
            when (view.id) {
                R.id.btnGuardar -> {
                    var completo = true
                    if (txtNombre.text.toString().isEmpty()) {
                        txtNombre.error = "Introduce el Nombre"
                        completo = false
                    }
                    if (txtTelefono1.text.toString().isEmpty()) {
                        txtTelefono1.error = "Introduce el Telefono Principal"
                        completo = false
                    }
                    if (txtDireccion.text.toString().isEmpty()) {
                        txtDireccion.error = "Introduce la Direccion"
                        completo = false
                    }
                    if (completo) {
                        val nContacto = Contactos().apply {
                            nombre = txtNombre.text.toString()
                            telefono1 = txtTelefono1.text.toString()
                            telefono2 = txtTelefono2.text.toString()
                            direccion = txtDireccion.text.toString()
                            notas = txtNotas.text.toString()
                            favorite = if (cbkFavorite.isChecked) 1 else 0
                        }
                        if (savedContacto == null) {
                            agregarContacto(nContacto)
                            Toast.makeText(applicationContext, "Contacto guardado con exito", Toast.LENGTH_SHORT).show()
                            limpiar()
                        } else {
                            actualizarContacto(id, nContacto)
                            Toast.makeText(applicationContext, "Contacto actualizado con exito", Toast.LENGTH_SHORT).show()
                            limpiar()
                        }
                    }
                }
                R.id.btnLimpiar -> limpiar()
                R.id.btnListar -> {
                    val i = Intent(this, ListaActivity::class.java)
                    limpiar()
                    startActivityForResult(i, 0)
                }
            }
        } else {
            Toast.makeText(applicationContext, "Se necesita tener conexion a internet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun agregarContacto(c: Contactos) {
        val newContactoReference = referencia.push()
        // Obtener el id del registro y setearlo
        val id = newContactoReference.key
        c._ID = id
        newContactoReference.setValue(c)
    }

    private fun actualizarContacto(id: String, p: Contactos) {
        // Actualizar un objeto al nodo referencia
        p._ID = id
        referencia.child(id).setValue(p)
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        return ni != null && ni.isConnected
    }

    private fun limpiar() {
        savedContacto = null
        txtNombre.setText("")
        txtTelefono1.setText("")
        txtTelefono2.setText("")
        txtNotas.setText("")
        txtDireccion.setText("")
        cbkFavorite.isChecked = false
        id = ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        intent?.extras?.let { bundle ->
            if (resultCode == Activity.RESULT_OK) {
                val contacto = bundle.getSerializable("contacto") as Contactos
                savedContacto = contacto
                id = contacto._ID ?: ""
                txtNombre.setText(contacto.nombre)
                txtTelefono1.setText(contacto.telefono1)
                txtTelefono2.setText(contacto.telefono2)
                txtDireccion.setText(contacto.direccion)
                txtNotas.setText(contacto.notas)
                cbkFavorite.isChecked = contacto.favorite > 0
            } else {
                limpiar()
            }
        }
    }
}