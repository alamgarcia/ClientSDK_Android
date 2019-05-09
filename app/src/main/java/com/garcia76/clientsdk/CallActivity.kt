package com.garcia76.clientsdk

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.garcia76.clientsdk.Clases.AppCallServiceHandler

import kotlinx.android.synthetic.main.activity_call.*

class CallActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    override fun onStart() {
        super.onStart()
        var callServiceHandler = AppCallServiceHandler
        callService = mUser?.callService
        Log.d("CSDK", "Creado Servicio de llamada")
        callService?.addListener(callServiceHandler)
        var call = callService?.createCall()
        call?.remoteAddress = "2316"
        call?.start()
    }

}
