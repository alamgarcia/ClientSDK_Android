package com.garcia76.clientsdk.Clases

import android.app.Activity
import android.util.Log
import android.view.View
import com.avaya.clientservices.call.Call
import com.avaya.clientservices.call.CallService
import com.avaya.clientservices.call.CallServiceListener
import com.garcia76.clientsdk.R
import com.tapadoo.alerter.Alerter
import kotlinx.android.synthetic.main.activity_main_frame.*

abstract class AppCallServiceHandler: CallServiceListener {
    companion object : CallServiceListener {
        private lateinit var context: Activity
        fun setContext(con: Activity) {
            context = con
        }

        override fun onActiveCallChanged(p0: CallService?, p1: Call?) {
            Log.d("CSDK-CSListen", "Llamada activa ha cambiado")
            context.status_txt.text = "Llamada Iniciada"
            context.deleteButton.setOnClickListener {
                p1?.end()
            }
        }

        override fun onCallServiceCapabilityChanged(p0: CallService?) {
            Log.d("CSDK-CSListen", "Capacidad en el servicio de llamada ha cambiado")
            Log.d("CSDK-CSListen", p0?.voIPCallingCapability.toString())

        }

        override fun onCallCreated(p0: CallService?, p1: Call?) {
            Log.d("CSDK-CSListen", "Llamada Creada")

        }

        override fun onIncomingCallUndelivered(p0: CallService?, p1: Call?) {
            Log.d("CSDK-CSListen", "Llamada entrante sin entregar")

        }

        override fun onIncomingCallReceived(p0: CallService?, p1: Call?) {
            Log.d("CSDK-CSListen", "Llamada entrante")
            Log.d("CSDK-CSListen", p1?.remoteNumber.toString())
            Alerter.create(context)
                .setTitle("Llamada entrante: ${p1?.remoteNumber} ")
                .setText("Asunto: ${p1?.subject}")
                .setDuration(10000)
                .addButton("Audio", R.style.AlertButton, View.OnClickListener {
                    p1?.accept()
                    Alerter.hide()

                })
                .addButton("Video", R.style.AlertButton, View.OnClickListener {
                })
                .addButton("Colgar", R.style.AlertButton, View.OnClickListener {
                    p1?.ignore()
                    Alerter.hide()
                })
                .show()
        }

        override fun onCallRemoved(p0: CallService?, p1: Call?) {
            Log.d("CSDK-CSListen", "Se ha removido la llamada")
            context.status_txt.text = "Llamada Terminada"
        }
    }
}
