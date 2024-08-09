package com.example.agendafirebase

import android.app.Activity
import android.app.ListActivity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*
import java.util.*

class ListaActivity : ListActivity() {
    private lateinit var basedatabase: FirebaseDatabase
    private lateinit var referencia: DatabaseReference
    private lateinit var btnNuevo: Button
    private val context: Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista)
        basedatabase = FirebaseDatabase.getInstance()
        referencia = basedatabase.getReferenceFromUrl(
            ReferenciasFirebase().URL_DATABASE + ReferenciasFirebase().DATABASE_NAME + "/" +
                    ReferenciasFirebase().TABLE_NAME
        )
        btnNuevo = findViewById(R.id.btnNuevo)
        obtenerContactos()
        btnNuevo.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun obtenerContactos() {
        val contactos = ArrayList<Contactos>()
        val listener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val contacto = dataSnapshot.getValue(Contactos::class.java)
                contacto?.let {
                    contactos.add(it)
                    val adapter = MyArrayAdapter(context, R.layout.layout_contacto, contactos)
                    setListAdapter(adapter)
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        referencia.addChildEventListener(listener)
    }

    inner class MyArrayAdapter(
        context: Context,
        private val textViewRecursoId: Int,
        private val objects: ArrayList<Contactos>
    ) : ArrayAdapter<Contactos>(context, textViewRecursoId, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(textViewRecursoId, null)
            val lblNombre = view.findViewById<TextView>(R.id.lblNombreContacto)
            val lblTelefono = view.findViewById<TextView>(R.id.lblTelefonoContacto)
            val btnModificar = view.findViewById<Button>(R.id.btnModificar)
            val btnBorrar = view.findViewById<Button>(R.id.btnBorrar)

            if (objects[position].favorite > 0) {
                lblNombre.setTextColor(Color.BLUE)
                lblTelefono.setTextColor(Color.BLUE)
            } else {
                lblNombre.setTextColor(Color.BLACK)
                lblTelefono.setTextColor(Color.BLACK)
            }

            lblNombre.text = objects[position].nombre
            lblTelefono.text = objects[position].telefono1

            btnBorrar.setOnClickListener {
                objects[position]._ID?.let { it1 -> borrarContacto(it1) }
                objects.removeAt(position)
                notifyDataSetChanged()
                Toast.makeText(applicationContext, "Contacto eliminado con éxito", Toast.LENGTH_SHORT).show()
            }

            btnModificar.setOnClickListener {
                val oBundle = Bundle()
                oBundle.putSerializable("contacto", objects[position])
                val i = Intent()
                i.putExtras(oBundle)
                setResult(Activity.RESULT_OK, i)
                finish()
            }
            return view
        }
    }

    private fun borrarContacto(childIndex: String) {
        referencia.child(childIndex).removeValue()
    }
}