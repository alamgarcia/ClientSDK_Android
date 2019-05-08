package com.garcia76.clientsdk

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextSwitcher
import androidx.appcompat.app.AppCompatActivity
import com.avaya.clientservices.call.CallService
import com.avaya.clientservices.client.Client
import com.avaya.clientservices.client.ClientConfiguration
import com.avaya.clientservices.client.CreateUserCompletionHandler
import com.avaya.clientservices.client.UserCreatedException
import com.avaya.clientservices.common.ConnectionPolicy
import com.avaya.clientservices.common.SignalingServer
import com.avaya.clientservices.user.User
import com.avaya.clientservices.user.UserConfiguration
import com.garcia76.clientsdk.Clases.AppCallHandler
import com.garcia76.clientsdk.Clases.AppCallServiceHandler
import com.garcia76.clientsdk.Clases.SIPCredentialProvider
import kotlinx.android.synthetic.main.activity_main_frame.*
import java.util.*

private const val TAG = "Speech"
private var mThread: Thread? = null
var mUser:User ? = null
var callService: CallService ? = null



class MainFrame : AppCompatActivity() {
    var clientConfiguration: ClientConfiguration? = null
    var useruuid: String? = null
    val myPreferences = "myPrefs"
    private lateinit var mTextView: TextSwitcher

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_frame)

        SIPCredentialProvider.setContext(this)
        AppCallHandler.setContext(this)
        AppCallServiceHandler.setContext(this)

        val sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        clientConfiguration = ClientConfiguration(
            "/storage/sdcard0/avayaclientservices",
            "Avaya Vantage Basic", "Avaya",
            Build.MODEL, Build.VERSION.RELEASE, "2.0.0", "Avaya"
        )
        useruuid = sharedPreferences.getString("uuid_agent", "")

        if (Funciones().isNullOrEmpty(useruuid)) {
            Log.d("CSDK", "No se ha encontrado UUID Generando")
            useruuid = UUID.randomUUID().toString()
            Log.d("CSDK", "UUID Generado $useruuid")
            val editor = sharedPreferences.edit()
            editor.putString("uuid_agent", useruuid)
            editor.apply()
            Log.d("CSDK", "UUID Guardado")

        } else {
            Log.d("CSDK", "UUID encontrado: $useruuid")

        }

        val username = sharedPreferences.getString("username", "")
        val sipcontroller = sharedPreferences.getString("sipcontroller", "sip-na1.avaya.com")
        val sipport = sharedPreferences.getInt("sipport", 5061)
        val sipdomain = sharedPreferences.getString("sipdomain", "net.avaya.com")

        Log.d("CSDK", username)
        Log.d("CSDK", sipcontroller)
        Log.d("CSDK", sipport.toString())
        Log.d("CSDK", sipdomain)

        var client = Client(clientConfiguration, this.application, ApplicationClientListener())
        var userConfiguration = UserConfiguration()
        var sipConfiguration = userConfiguration.sipUserConfiguration
        sipConfiguration.isEnabled = true
        sipConfiguration.userId = username
        sipConfiguration.domain = sipdomain


        val signalingServer = SignalingServer(
            SignalingServer.TransportType.TLS,
            sipcontroller, // Provided by your administrator
            sipport, // Provided by your administrator
            SignalingServer.FailbackPolicy.AUTOMATIC
        )                // Allow the Client SDK to manage failback
        sipConfiguration.connectionPolicy = ConnectionPolicy(signalingServer)
        sipConfiguration.credentialProvider = SIPCredentialProvider
        userConfiguration.sipUserConfiguration = sipConfiguration

        client.createUser(userConfiguration, object : CreateUserCompletionHandler {
            override fun onSuccess(user: User) {
                mUser = user
                mUser?.start()



                dialButton.setOnClickListener {
                    val intent = Intent(this@MainFrame.applicationContext, CallActivity::class.java)
                    // Pasar Valores entre Actividades
                    intent.putExtra("numero", phone_number.text.toString())
                    startActivity(intent)



                }
            }

            override fun onError(error: UserCreatedException) {
                Log.d("CSDK", "Failed to create user", error)
                val dialogBuilder = AlertDialog.Builder(this@MainFrame)
                dialogBuilder.setMessage("Ha ocurrido un eror ${error.failureReason}")
                    .setPositiveButton("Aceptar") { dialog, id ->
                        finish()
                    }
                val alert = dialogBuilder.create()
                alert.setTitle("Ha ocurrido un error")
                alert.show()
            }
        })

    }
}

