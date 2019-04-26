package com.garcia76.clientsdk

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_settings.*

class Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        CargarAjustes()
        guardar_ajustes_btn.setOnClickListener {
            GuardarAjustes()
        }

        limpiar_ajustes_btn.setOnClickListener {

        }

    }


    fun CargarAjustes(){
        val myPreferences = "myPrefs"
        val sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        val sipcontroller = sharedPreferences.getString("sipcontroller", "sip-na1.avaya.com")
        val sipdomain = sharedPreferences.getString("sipdomain", "net.avaya.com")
        val sipport = sharedPreferences.getInt("sipport",5061)
        sip_controller_txt.setText(sipcontroller)
        sip_domain_txt.setText(sipdomain)
        sip_port_txt.setText(sipport.toString())
    }

    fun GuardarAjustes(){
        val myPreferences = "myPrefs"
        val sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("sipcontroller", sip_controller_txt.text.toString())
        editor.putString("sipdomain", sip_domain_txt.text.toString())
        editor.putInt("sipport", sip_port_txt.text.toString().toInt())
        editor.apply()
        // build alert dialog
        val dialogBuilder = AlertDialog.Builder(this)
        // set message of alert dialog
        dialogBuilder.setMessage("Tus ajustes se han guardado")
                // if the dialog is cancelable

                // positive button text and action
                .setPositiveButton("Aceptar", DialogInterface.OnClickListener {
                    dialog, id -> finish()
                })


        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle("Confirmación")
        // show alert dialog
        alert.show()
    }


}
